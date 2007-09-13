/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.protocol.http.pagestore;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.FilePageStore;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.ISerializationAwarePageStore;
import org.apache.wicket.protocol.http.pagestore.PageWindowManager.PageWindow;
import org.apache.wicket.util.concurrent.ConcurrentHashMap;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IPageStore} implementation that stores the serialized pages grouped in a single file per
 * pagemap.
 * <p>
 * This store was designed to overcome the problems of {@link FilePageStore} which stores the pages
 * in separate files per page.
 * <p>
 * {@link DiskPageStore} allows to set maximum size for pagemap file and maximum size for session.
 * If the maximum size for session is exceeded, the last recently used pagemap file is removed.
 * 
 * @author Matej Knopp
 */
public class DiskPageStore extends AbstractPageStore implements ISerializationAwarePageStore
{
	/**
	 * Each PageMap is represented by this class.
	 * 
	 * @author Matej Knopp
	 */
	protected static class PageMapEntry
	{
		private String pageMapName;
		private String fileName;
		private PageWindowManager manager;

		/**
		 * @return the name of pagemap
		 */
		public String getPageMapName()
		{
			return pageMapName;
		}

		/**
		 * @return path of file that contains serialized pages in this pagemap
		 */
		public String getFileName()
		{
			return fileName;
		}

		/**
		 * @return manager that maintains information about pages inside the file
		 */
		public PageWindowManager getManager()
		{
			return manager;
		}
	}

	/**
	 * Represents a session,
	 * 
	 * @author Matej Knopp
	 */
	protected class SessionEntry
	{
		private String sessionId;
		private final List pageMapEntryList = new ArrayList();

		/**
		 * @return session id
		 */
		public String getSessionId()
		{
			return sessionId;
		}

		/**
		 * @return summed size of all pagemap files
		 */
		public int getTotalSize()
		{
			int result = 0;
			for (Iterator i = pageMapEntryList.iterator(); i.hasNext();)
			{
				PageMapEntry entry = (PageMapEntry)i.next();
				if (entry.manager != null)
				{
					result += entry.manager.getTotalSize();
				}
			}
			return result;
		}

		/**
		 * @return list of {@link PageMapEntry} for this session
		 */
		public List /* <PageMapEntry> */getPageMapEntryList()
		{
			return Collections.unmodifiableList(pageMapEntryList);
		}

		/**
		 * Returns a {@link PageMapEntry} for specified pagemap. If the create attribute is set and
		 * the pagemap does not exist, new {@link PageMapEntry} will be created.
		 * 
		 * @param pageMapName
		 * @param create
		 * @return
		 */
		public PageMapEntry getPageMapEntry(String pageMapName, boolean create)
		{
			PageMapEntry result = null;
			for (Iterator i = pageMapEntryList.iterator(); i.hasNext();)
			{
				PageMapEntry entry = (PageMapEntry)i.next();
				if (entry.pageMapName == pageMapName ||
						(entry.pageMapName != null && entry.pageMapName.equals(pageMapName)))
				{
					result = entry;
				}

			}
			if (result == null && create)
			{
				result = new PageMapEntry();
				result.pageMapName = pageMapName;
				result.fileName = getPageMapFileName(sessionId, pageMapName, true);
				result.manager = new PageWindowManager(getMaxSizePerPageMap());
				pageMapEntryList.add(result);
			}
			return result;
		}

		/**
		 * Removes the pagemap entry and deletes the file.
		 * 
		 * @param entry
		 */
		private void removePageMapEntry(PageMapEntry entry)
		{
			fileChannelPool.closeAndDeleteFileChannel(entry.fileName);
			pageMapEntryList.remove(entry);
		}

		/**
		 * Removes the specified pagemap and deletes the file.
		 * 
		 * @param pageMapName
		 */
		public synchronized void removePageMap(String pageMapName)
		{
			PageMapEntry entry = getPageMapEntry(pageMapName, false);
			if (entry != null)
			{
				removePageMapEntry(entry);
			}
		}

		/**
		 * Saves the serialized page to appropriate pagemap file.
		 * 
		 * @param page
		 */
		public synchronized void savePage(SerializedPage page)
		{
			// only save page that has some data
			if (page.getData() != null)
			{
				PageMapEntry entry = getPageMapEntry(page.getPageMapName(), true);

				// allocate window for page
				PageWindow window = entry.manager.createPageWindow(page.getPageId(), page
						.getVersionNumber(), page.getAjaxVersionNumber(), page.getData().length);

				// remove the entry and add it to the end of entry list (to mark
				// it as last accessed(
				pageMapEntryList.remove(entry);
				pageMapEntryList.add(entry);

				// if we exceeded maximum session size, try to remove as many
				// pagemap as necessary and possible
				while (getTotalSize() > getMaxSizePerSession() && pageMapEntryList.size() > 1)
				{
					removePageMapEntry((PageMapEntry)pageMapEntryList.get(0));
				}

				// take the filechannel from the pool
				FileChannel channel = fileChannelPool.getFileChannel(entry.fileName, true);
				try
				{
					// write the content
					channel.write(ByteBuffer.wrap(page.getData()), window.getFilePartOffset());
				}
				catch (IOException e)
				{
					log.error("Error writing to a channel " + channel, e);
				}
				finally
				{
					// return the "borrowed" file channel
					fileChannelPool.returnFileChannel(channel);
				}
			}
		}

		/**
		 * Removes the page from pagemap file.
		 * 
		 * @param pageMapName
		 * @param pageId
		 */
		public synchronized void removePage(String pageMapName, int pageId)
		{
			PageMapEntry entry = getPageMapEntry(pageMapName, false);
			if (entry != null)
			{
				entry.manager.removePage(pageId);
			}
		}

		/**
		 * Loads the part of pagemap file specified by the given PageWindow.
		 * 
		 * @param window
		 * @param pageMapFileName
		 * @return
		 */
		public byte[] loadPage(PageWindow window, String pageMapFileName)
		{
			byte[] result = null;
			FileChannel channel = fileChannelPool.getFileChannel(pageMapFileName, false);
			if (channel != null)
			{
				ByteBuffer buffer = ByteBuffer.allocate(window.getFilePartSize());
				try
				{
					channel.read(buffer, window.getFilePartOffset());
					if (buffer.hasArray())
					{
						result = buffer.array();
					}
				}
				catch (IOException e)
				{
					log.error("Error reading from file channel " + channel, e);
				}
				finally
				{
					fileChannelPool.returnFileChannel(channel);
				}
			}
			return result;
		}

		/**
		 * Loads the specified page data.
		 * 
		 * @param pageMapName
		 * @param id
		 * @param versionNumber
		 * @param ajaxVersionNumber
		 * @return page data or null if the page is no longer in pagemap file
		 */
		public synchronized byte[] loadPage(String pageMapName, int id, int versionNumber,
				int ajaxVersionNumber)
		{
			byte[] result = null;
			PageMapEntry entry = getPageMapEntry(pageMapName, false);
			if (entry != null)
			{
				PageWindow window = entry.manager.getPageWindow(id, versionNumber,
						ajaxVersionNumber);
				if (window != null)
				{
					result = loadPage(window, entry.fileName);
				}
			}
			return result;
		}

		/**
		 * Deletes all files for this session.
		 */
		public synchronized void unbind()
		{
			while (pageMapEntryList.size() > 0)
			{
				removePageMapEntry((PageMapEntry)pageMapEntryList.get(pageMapEntryList.size() - 1));
			}
			File sessionFolder = getSessionFolder(sessionId, false);
			if (sessionFolder.exists())
			{
				sessionFolder.delete();
			}
		}

		/**
		 * Returns true if the given page exists for specified pageMap
		 * 
		 * @param pageMapName
		 * @param pageId
		 * @param versionNumber
		 * @return
		 */
		public synchronized boolean exists(String pageMapName, int pageId, int versionNumber)
		{
			PageMapEntry entry = getPageMapEntry(pageMapName, false);
			return entry != null &&
					entry.getManager().getPageWindow(pageId, versionNumber, -1) != null;
		}
	}

	/**
	 * Returns the folder for the specified sessions. If the folder doesn't exist and the create
	 * flag is set, the folder will be created.
	 * 
	 * @param sessionId
	 * @param create
	 * @return
	 */
	private File getSessionFolder(String sessionId, boolean create)
	{
		File storeFolder = new File(fileStoreFolder, appName + "-filestore");
		File sessionFolder = new File(storeFolder, sessionId);
		if (create && sessionFolder.exists() == false)
		{
			mkdirs(sessionFolder);
		}
		return sessionFolder;
	}


	/**
	 * Utility method for creating a directory
	 * 
	 * @param file
	 */
	private void mkdirs(File file)
	{
		// for some reason, simple file.mkdirs sometimes fails under heavy load
		for (int j = 0; j < 5; ++j)
		{
			for (int i = 0; i < 10; ++i)
			{
				if (file.mkdirs())
				{
					return;
				}
			}
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException ignore)
			{
			}
		}
		log.error("Failed to make directory " + file);
	}

	/**
	 * Returns the file name for specified pagemap. If the session folder (folder that contains the
	 * file) does not exist and createSessionFolder is true, the folder will becreated.
	 * 
	 * @param sessionId
	 * @param pageMapName
	 * @param createSessionFolder
	 * @return file name for pagemap
	 */
	private String getPageMapFileName(String sessionId, String pageMapName,
			boolean createSessionFolder)
	{
		File sessionFolder = getSessionFolder(sessionId, createSessionFolder);
		return new File(sessionFolder, "pm-" + pageMapName).getAbsolutePath();
	}

	private final int maxSizePerPageMap;

	/**
	 * Return maximum pagemap file size (in bytes).
	 * 
	 * @return
	 */
	protected int getMaxSizePerPageMap()
	{
		return maxSizePerPageMap;
	}

	private final int maxSizePerSession;

	/**
	 * Returns maximum size per session (in bytes). After the session exceeds this size, appropriate
	 * number of last recently used pagemap files will be removed.
	 * 
	 * @return
	 */
	protected int getMaxSizePerSession()
	{
		return maxSizePerSession;
	}

	private final FileChannelPool fileChannelPool;

	private final File fileStoreFolder;

	/**
	 * Returns the "root" file store folder.
	 * 
	 * @return
	 */
	protected File getFileStoreFolder()
	{
		return fileStoreFolder;
	}

	private final String appName;

	/**
	 * Creates a new {@link DiskPageStore} instance.
	 * 
	 * @param fileStoreFolder
	 *            folder in which the session folders containing pagemap files will be stored
	 * @param maxSizePerPagemap
	 *            the maximum size of pagemap file (in bytes)
	 * @param maxSizePerSession
	 *            the maximum size of session (in bytes)
	 * @param fileChannelPoolCapacity
	 *            the maximum number of concurrently opened files (higher number improves
	 *            performance under heavy load).
	 */
	public DiskPageStore(File fileStoreFolder, int maxSizePerPagemap, int maxSizePerSession,
			int fileChannelPoolCapacity)
	{
		maxSizePerPageMap = maxSizePerPagemap;
		this.maxSizePerSession = maxSizePerSession;
		fileChannelPool = new FileChannelPool(fileChannelPoolCapacity);
		this.fileStoreFolder = fileStoreFolder;

		if (maxSizePerSession < maxSizePerPageMap)
		{
			throw new IllegalArgumentException(
					"Provided maximum session size must be bigger than maximum pagemap size");
		}

		this.fileStoreFolder.mkdirs();
		appName = Application.get().getApplicationKey();

		initPageSavingThread();
	}

	private static File getDefaultFileStoreFolder()
	{
		return (File)((WebApplication)Application.get()).getServletContext().getAttribute(
				"javax.servlet.context.tempdir");
	}

	/**
	 * Creates a new {@link DiskPageStore} instance.
	 * 
	 * @param maxSizePerPagemap
	 *            the maximum size of pagemap file (in bytes)
	 * @param maxSizePerSession
	 *            the maximum size of session (in bytes)
	 * @param fileChannelPoolCapacity
	 *            the maximum number of concurrently opened files (higher number improves
	 *            performance under heavy load).
	 * 
	 */
	public DiskPageStore(int maxSizePerPagemap, int maxSizePerSession, int fileChannelPoolCapacity)
	{
		this(getDefaultFileStoreFolder(), maxSizePerPagemap, maxSizePerSession,
				fileChannelPoolCapacity);
	}

	/**
	 * Creates a new {@link DiskPageStore} instance.
	 */
	public DiskPageStore()
	{
		this((int)Bytes.megabytes(10).bytes(), (int)Bytes.megabytes(100).bytes(), 25);
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#destroy()
	 */
	public void destroy()
	{
		fileChannelPool.destroy();
		if (pageSavingThread != null)
		{
			pageSavingThread.stop();
		}
	}

	private final Map /* <String, SessionEntry> */sessionIdToEntryMap = new ConcurrentHashMap();

	/**
	 * Returns the SessionEntry for session with given id. If the entry does not yet exist and the
	 * createIfDoesNotExist attribute is set, new SessionEntry will be created.
	 * 
	 * @param sessionId
	 * @param createIfDoesNotExist
	 * @return
	 */
	protected SessionEntry getSessionEntry(String sessionId, boolean createIfDoesNotExist)
	{
		SessionEntry entry = (SessionEntry)sessionIdToEntryMap.get(sessionId);
		if (entry == null && createIfDoesNotExist)
		{
			synchronized (sessionIdToEntryMap)
			{
				entry = (SessionEntry)sessionIdToEntryMap.get(sessionId);
				if (entry == null)
				{
					entry = new SessionEntry();
					entry.sessionId = sessionId;
					sessionIdToEntryMap.put(sessionId, entry);
				}
			}
		}
		return entry;
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#getPage(java.lang.String,
	 *      java.lang.String, int, int, int)
	 */
	public Page getPage(String sessionId, String pagemap, int id, int versionNumber,
			int ajaxVersionNumber)
	{
		SessionEntry entry = getSessionEntry(sessionId, false);
		if (entry != null)
		{
			byte[] data;

			if (isSynchronous())
			{
				data = entry.loadPage(pagemap, id, versionNumber, ajaxVersionNumber);
			}
			else
			{
				// we need to make sure that the there are no pending pages to
				// be saved before loading a page
				List pages = getPagesToSaveList(sessionId);
				synchronized (pages)
				{
					flushPagesToSaveList(sessionId, pages);
					data = entry.loadPage(pagemap, id, versionNumber, ajaxVersionNumber);
				}
			}

			if (data != null)
			{
				return deserializePage(data, versionNumber);
			}
		}

		return null;
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#pageAccessed(java.lang.String,
	 *      org.apache.wicket.Page)
	 */
	public void pageAccessed(String sessionId, Page page)
	{
	}

	/**
	 * Removes the page (or entire pagemap) from specified session.
	 * 
	 * @param entry
	 * @param pageMap
	 * @param id
	 *            page id to remove or -1 if the whole pagemap should be removed
	 */
	private void removePage(SessionEntry entry, String pageMap, int id)
	{
		if (id != -1)
		{
			entry.removePage(pageMap, id);
		}
		else
		{
			entry.removePageMap(pageMap);
		}
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#removePage(java.lang.String,
	 *      java.lang.String, int)
	 */
	public void removePage(String sessionId, String pageMap, int id)
	{
		SessionEntry entry = getSessionEntry(sessionId, false);
		if (entry != null)
		{
			if (isSynchronous())
			{
				removePage(entry, pageMap, id);
			}
			else
			{
				// we need to make sure that the there are no pending pages to
				// be saved before removing the page (or pagemap)
				List pages = getPagesToSaveList(sessionId);
				synchronized (pages)
				{
					flushPagesToSaveList(sessionId, pages);
					removePage(entry, pageMap, id);
				}
			}
		}
	}

	/**
	 * Stores the serialized pages. The storing is done either immediately (in synchronous mode) or
	 * it's scheduled to be stored by the worker thread.
	 * 
	 * @param sessionId
	 * @param pages
	 */
	protected void storeSerializedPages(String sessionId, List /* <SerializedPage> */pages)
	{
		SessionEntry entry = getSessionEntry(sessionId, true);

		if (isSynchronous())
		{
			for (Iterator i = pages.iterator(); i.hasNext();)
			{
				SerializedPage serializedPage = (SerializedPage)i.next();
				entry.savePage(serializedPage);
			}
		}
		else
		{
			schedulePagesSave(sessionId, pages);
		}
	}

	/**
	 * Hook for processing serialized pages (e.g. sending those across cluster)
	 * 
	 * @param sessionId
	 * @param pages
	 */
	protected void onPagesSerialized(String sessionId, List /* <SerializedPage */pages)
	{

	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#storePage(java.lang.String,
	 *      org.apache.wicket.Page)
	 */
	public void storePage(String sessionId, Page page)
	{
		List pages = serializePage(page);

		cacheSerializedPage(sessionId, page, pages);

		onPagesSerialized(sessionId, pages);

		storeSerializedPages(sessionId, pages);
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#unbind(java.lang.String)
	 */
	public void unbind(String sessionId)
	{
		SessionEntry entry = (SessionEntry)sessionIdToEntryMap.get(sessionId);
		if (entry != null)
		{
			if (isSynchronous())
			{
				entry.unbind();
			}
			else
			{
				List pages = getPagesToSaveList(sessionId);
				synchronized (pages)
				{
					flushPagesToSaveList(sessionId, pages);
					entry.unbind();
				}
				pagesToSaveAll.remove(sessionId);
			}
		}
	}

	// map from session id to serializedpage list
	// this contains lists for all active sessions
	private final Map /* <String, List<SerializedPage>> */pagesToSaveAll = new ConcurrentHashMap();

	// contains list of serialized pages to be saved - only non empty lists
	private final Map /* <String, List<SerializedPage>> */pagesToSaveActive = new ConcurrentHashMap();

	/**
	 * Returns the list of pages to be saved for the specified session id. If the list is not found,
	 * new list is created.
	 * 
	 * @param sessionId
	 * @return
	 */
	protected List getPagesToSaveList(String sessionId)
	{
		List list = (List)pagesToSaveAll.get(sessionId);
		if (list == null)
		{
			synchronized (pagesToSaveAll)
			{
				list = (List)pagesToSaveAll.get(sessionId);
				if (list == null)
				{
					list = new ArrayList();
					pagesToSaveAll.put(sessionId, list);
				}
			}
		}
		return list;
	}

	/**
	 * Saves all entries from the specified list.
	 * 
	 * @param sessionId
	 * @param list
	 */
	protected void flushPagesToSaveList(String sessionId, List /* <SerializedPage> */list)
	{
		if (list != null)
		{
			for (Iterator i = list.iterator(); i.hasNext();)
			{
				SerializedPage page = (SerializedPage)i.next();
				getSessionEntry(sessionId, true).savePage(page);
			}
			list.clear();
		}
	}

	/**
	 * Schedules the pages to be saved by the worker thread.
	 * 
	 * @param sessionId
	 * @param pages
	 */
	private void schedulePagesSave(String sessionId, List/* <SerializedPage> */pages)
	{
		List list = getPagesToSaveList(sessionId);
		synchronized (list)
		{
			list.addAll(pages);

			if (list.size() > 0 && pagesToSaveActive.containsKey(sessionId) == false)
			{
				pagesToSaveActive.put(sessionId, list);
			}
		}
	}

	/**
	 * Worker thread that saves the serialized pages. Saving pages in the separate thread results in
	 * smoother performance under load.
	 * 
	 * @author Matej Knopp
	 */
	private class PageSavingThread implements Runnable
	{
		private volatile boolean stop = false;

		public void run()
		{
			while (stop == false)
			{
				// wait until we have something to save
				while (pagesToSaveActive.isEmpty())
				{
					try
					{
						Thread.sleep(getSavingThreadSleepTime());
					}
					catch (InterruptedException ignore)
					{
					}
				}

				// iterate through lists of pages to be saved
				for (Iterator i = pagesToSaveActive.entrySet().iterator(); i.hasNext();)
				{
					Map.Entry entry = (Map.Entry)i.next();
					String sessionId = (String)entry.getKey();
					List pages = (List)entry.getValue();

					synchronized (pages)
					{
						try
						{
							flushPagesToSaveList(sessionId, pages);
						}
						catch (Exception e)
						{
							log
									.error(
											"Error flushing serialized pages from worker thread for session " +
													sessionId, e);
						}
						i.remove();
					}
				}
			}
		}

		/**
		 * Stops the worker thread.
		 */
		public void stop()
		{
			stop = true;
		}
	};

	/**
	 * Initializes the worker thread.
	 */
	private void initPageSavingThread()
	{
		if (isSynchronous() == false)
		{
			pageSavingThread = new PageSavingThread();
			Thread t = new Thread(pageSavingThread, "PageSavingThread-" + appName);
			t.setDaemon(true);
			t.setPriority(Thread.MAX_PRIORITY);
			t.start();
		}
	}

	private PageSavingThread pageSavingThread = null;

	/**
	 * Returns the amount time in milliseconds for the saving thread to sleep between checking
	 * whether there are pending serialized pages to be saved.
	 * 
	 * @return
	 */
	protected int getSavingThreadSleepTime()
	{
		return 100;
	}

	/**
	 * Returns whether the {@link DiskPageStore} should work in synchronous or asynchronous mode.
	 * Asynchronous mode uses a worker thread to save pages, which results in smoother performance.
	 * 
	 * @return
	 */
	protected boolean isSynchronous()
	{
		return false;
	}

	private int lastRecentlySerializedPagesCacheSize = 50;

	/**
	 * Sets the number of last recently serialized pages kept in cache. The cache is used to aid
	 * performance on session replication.
	 * 
	 * @param lastRecentlySerializedPagesCacheSize
	 */
	public void setLastRecentlySerializedPagesCacheSize(int lastRecentlySerializedPagesCacheSize)
	{
		this.lastRecentlySerializedPagesCacheSize = lastRecentlySerializedPagesCacheSize;
	}

	/**
	 * @return
	 */
	public int getLastRecentlySerializedPagesCacheSize()
	{
		return lastRecentlySerializedPagesCacheSize;
	}

	private final List /* SerializedPageWithSession */lastRecentlySerializedPagesCache = new ArrayList(
			lastRecentlySerializedPagesCacheSize);

	private SerializedPageWithSession removePageFromLastRecentlySerializedPagesCache(Page page)
	{
		for (Iterator i = lastRecentlySerializedPagesCache.iterator(); i.hasNext();)
		{
			SerializedPageWithSession entry = (SerializedPageWithSession)i.next();
			if (entry != null && entry.page.get() == page)
			{
				i.remove();
				return entry;
			}
		}
		return null;
	}

	/**
	 * Store the serialized page in the request metadata,.
	 * 
	 * @param sessionId
	 * @param page
	 * @param pagesList
	 */
	private void cacheSerializedPage(String sessionId, Page page,
			List /* <SerializedPage> */pagesList)
	{
		if (getLastRecentlySerializedPagesCacheSize() > 0)
		{
			SerializedPageWithSession entry = new SerializedPageWithSession(sessionId, page,
					pagesList);

			synchronized (lastRecentlySerializedPagesCache)
			{
				removePageFromLastRecentlySerializedPagesCache(page);
				lastRecentlySerializedPagesCache.add(entry);
				if (lastRecentlySerializedPagesCache.size() > getLastRecentlySerializedPagesCacheSize())
				{
					lastRecentlySerializedPagesCache.remove(0);
				}
			}
		}
	}

	/**
	 * 
	 * @author Matej Knopp
	 */
	private static class SerializedPageWithSession implements Serializable
	{
		private static final long serialVersionUID = 1L;

		// this is used for lookup on pagemap serialization. We don't have the
		// session id at that point, because it can happen outside the request
		// thread. We only have the page instance and we need to use it as a key
		private final transient WeakReference /* <Page> */page;

		// list of serialized pages
		private final List pages;

		private final String sessionId;

		// after deserialization, we need to be able to know which page to load
		private final int pageId;
		private final String pageMapName;
		private final int versionNumber;
		private final int ajaxVersionNumber;

		private SerializedPageWithSession(String sessionId, Page page,
				List /* <SerializablePage> */pages)
		{
			this.sessionId = sessionId;
			pageId = page.getNumericId();
			pageMapName = page.getPageMapName();
			versionNumber = page.getCurrentVersionNumber();
			ajaxVersionNumber = page.getAjaxVersionNumber();
			this.pages = new ArrayList(pages);
			this.page = new WeakReference(page);
		}

		public String toString()
		{
			return getClass().getSimpleName() + " [ pageId:" + pageId + ", pageMapName: " +
					pageMapName + ", session: " + sessionId + "]";
		}
	};

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.ISerializationAwarePageStore#prepareForSerialization(org.apache.wicket.Page)
	 */
	public Serializable prepareForSerialization(Page page)
	{
		Serializable result = page;

		if (getLastRecentlySerializedPagesCacheSize() > 0)
		{
			SerializedPageWithSession entry;
			synchronized (lastRecentlySerializedPagesCache)
			{
				entry = removePageFromLastRecentlySerializedPagesCache(page);
			}

			if (entry != null)
			{
				result = entry;
			}
		}

		return result;
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.ISerializationAwarePageStore#restoreAfterSerialization(java.io.Serializable)
	 */
	public Page restoreAfterSerialization(Serializable serializable)
	{
		if (serializable instanceof Page)
		{
			return (Page)serializable;
		}
		else if (serializable instanceof SerializedPageWithSession)
		{
			SerializedPageWithSession page = (SerializedPageWithSession)serializable;
			storeSerializedPages(page.sessionId, page.pages);
			return getPage(page.sessionId, page.pageMapName, page.pageId, page.versionNumber,
					page.ajaxVersionNumber);
		}
		else
		{
			throw new IllegalArgumentException("Unknown object type");
		}
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#containsPage(java.lang.String,
	 *      java.lang.String, int, int)
	 */
	public boolean containsPage(String sessionId, String pageMapName, int pageId, int pageVersion)
	{
		SessionEntry entry = getSessionEntry(sessionId, false);
		if (entry != null)
		{
			byte[] data;

			if (isSynchronous())
			{
				return entry.exists(pageMapName, pageId, pageVersion);
			}
			else
			{
				// we need to make sure that the there are no pending pages to
				// be saved before loading a page
				List pages = getPagesToSaveList(sessionId);
				synchronized (pages)
				{
					flushPagesToSaveList(sessionId, pages);
					return entry.exists(pageMapName, pageId, pageVersion);
				}
			}
		}
		else
		{
			return false;
		}
	}


	private static final Logger log = LoggerFactory.getLogger(DiskPageStore.class);

}
