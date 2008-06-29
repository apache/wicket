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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.ISerializationAwarePageStore;
import org.apache.wicket.protocol.http.pagestore.PageWindowManager.PageWindow;
import org.apache.wicket.protocol.http.pagestore.SerializedPagesCache.SerializedPageWithSession;
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
	protected static class PageMapEntry implements Serializable
	{
		private static final long serialVersionUID = 1L;

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
	protected static class SessionEntry implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String sessionId;
		private final List<PageMapEntry> pageMapEntryList = new ArrayList<PageMapEntry>();
		private transient DiskPageStore diskPageStore;

		protected SessionEntry(DiskPageStore diskPageStore)
		{
			this.diskPageStore = diskPageStore;
		}

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
			for (Iterator<PageMapEntry> i = pageMapEntryList.iterator(); i.hasNext();)
			{
				PageMapEntry entry = i.next();
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
		public List /* <PageMapEntry> */<PageMapEntry> getPageMapEntryList()
		{
			return Collections.unmodifiableList(pageMapEntryList);
		}

		/**
		 * Returns a {@link PageMapEntry} for specified pagemap. If the create attribute is set and
		 * the pagemap does not exist, new {@link PageMapEntry} will be created.
		 * 
		 * @param pageMapName
		 * @param create
		 * @return page map entry
		 */
		public PageMapEntry getPageMapEntry(String pageMapName, boolean create)
		{
			PageMapEntry result = null;
			for (Iterator<PageMapEntry> i = pageMapEntryList.iterator(); i.hasNext();)
			{
				PageMapEntry entry = i.next();
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
				result.fileName = diskPageStore.getPageMapFileName(sessionId, pageMapName, true);
				result.manager = new PageWindowManager(diskPageStore.getMaxSizePerPageMap());
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
			diskPageStore.fileChannelPool.closeAndDeleteFileChannel(entry.fileName);
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
				PageWindow window = entry.manager.createPageWindow(page.getPageId(),
					page.getVersionNumber(), page.getAjaxVersionNumber(), page.getData().length);

				// remove the entry and add it to the end of entry list (to mark
				// it as last accessed(
				pageMapEntryList.remove(entry);
				pageMapEntryList.add(entry);

				// if we exceeded maximum session size, try to remove as many
				// pagemap as necessary and possible
				while (getTotalSize() > diskPageStore.getMaxSizePerSession() &&
					pageMapEntryList.size() > 1)
				{
					removePageMapEntry(pageMapEntryList.get(0));
				}

				// take the filechannel from the pool
				FileChannel channel = diskPageStore.fileChannelPool.getFileChannel(entry.fileName,
					true);
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
					diskPageStore.fileChannelPool.returnFileChannel(channel);
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
		 * @return serialized page data
		 */
		public byte[] loadPage(PageWindow window, String pageMapFileName)
		{
			byte[] result = null;
			FileChannel channel = diskPageStore.fileChannelPool.getFileChannel(pageMapFileName,
				false);
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
					diskPageStore.fileChannelPool.returnFileChannel(channel);
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
				removePageMapEntry(pageMapEntryList.get(pageMapEntryList.size() - 1));
			}
			File sessionFolder = diskPageStore.getSessionFolder(sessionId, false);
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
		 * @return true if page exists
		 */
		public synchronized boolean exists(String pageMapName, int pageId, int versionNumber)
		{
			PageMapEntry entry = getPageMapEntry(pageMapName, false);
			return entry != null &&
				entry.getManager().getPageWindow(pageId, versionNumber, -1) != null;
		}
	}

	private File getStoreFolder()
	{
		File storeFolder = new File(fileStoreFolder, appName + "-filestore");
		return storeFolder;
	}

	/**
	 * Returns the folder for the specified sessions. If the folder doesn't exist and the create
	 * flag is set, the folder will be created.
	 * 
	 * @param sessionId
	 * @param create
	 * @return folder used to store session data
	 */
	private File getSessionFolder(String sessionId, boolean create)
	{
		File storeFolder = getStoreFolder();

		sessionId = sessionId.replace('*', '_');
		sessionId = sessionId.replace('/', '_');

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
	 * file) does not exist and createSessionFolder is true, the folder will be created.
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
	 * @return max sie of page map
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
	 * @return max size of session
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
	 * @return file store folder
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

		loadIndex();

		initPageSavingThread();
	}

	@SuppressWarnings("unchecked")
	private void loadIndex()
	{
		File storeFolder = getStoreFolder();
		File index = new File(storeFolder, "DiskPageStoreIndex");
		if (index.exists() && index.length() > 0)
		{
			try
			{
				InputStream stream = new FileInputStream(index);
				ObjectInputStream ois = new ObjectInputStream(stream);
				Map<String, SessionEntry> map = (Map<String, SessionEntry>)ois.readObject();
				sessionIdToEntryMap = new ConcurrentHashMap<String, SessionEntry>(map);
				for (Iterator<Entry<String, SessionEntry>> entries = sessionIdToEntryMap.entrySet()
					.iterator(); entries.hasNext();)
				{
					// initialize the diskPageStore reference
					Entry<String, SessionEntry> entry = entries.next();
					SessionEntry sessionEntry = entry.getValue();
					sessionEntry.diskPageStore = this;
				}
				stream.close();
			}
			catch (Exception e)
			{
				log.error("Couldn't load DiskPageStore index from file " + index + ".", e);
			}
		}
		index.delete();
	}

	private void saveIndex()
	{
		File storeFolder = getStoreFolder();
		if (storeFolder.exists())
		{
			File index = new File(storeFolder, "DiskPageStoreIndex");
			index.delete();
			try
			{
				OutputStream stream = new FileOutputStream(index);
				ObjectOutputStream oos = new ObjectOutputStream(stream);
				oos.writeObject(sessionIdToEntryMap);
				stream.close();
			}
			catch (Exception e)
			{
				log.error("Couldn't write DiskPageStore index to file " + index + ".", e);
			}
		}
	}

	private static File getDefaultFileStoreFolder()
	{
		final File dir = (File)((WebApplication)Application.get()).getServletContext()
			.getAttribute("javax.servlet.context.tempdir");
		if (dir != null)
		{
			return dir;
		}
		else
		{
			try
			{
				return File.createTempFile("file-prefix", null).getParentFile();
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException(e);
			}
		}
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
		this((int)Bytes.megabytes(10).bytes(), (int)Bytes.megabytes(100).bytes(), 50);
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#destroy()
	 */
	public void destroy()
	{
		if (!isSynchronous())
		{
			// make sure that all pages are saved in asynchronous mode
			synchronized (pagesToSaveAll)
			{
				for (Entry<String, List<SerializedPage>> entry : pagesToSaveAll.entrySet())
				{
					String sessionId = entry.getKey();
					List<SerializedPage> pages = entry.getValue();
					synchronized (pages)
					{
						flushPagesToSaveList(sessionId, pages);
					}
				}
			}
		}

		saveIndex();

		fileChannelPool.destroy();
		if (pageSavingThread != null)
		{
			pageSavingThread.stop();
		}
	}

	private Map<String, SessionEntry> sessionIdToEntryMap = new ConcurrentHashMap<String, SessionEntry>();

	/**
	 * Returns the SessionEntry for session with given id. If the entry does not yet exist and the
	 * createIfDoesNotExist attribute is set, new SessionEntry will be created.
	 * 
	 * @param sessionId
	 * @param createIfDoesNotExist
	 * @return session entry
	 */
	protected SessionEntry getSessionEntry(String sessionId, boolean createIfDoesNotExist)
	{
		SessionEntry entry = sessionIdToEntryMap.get(sessionId);
		if (entry == null && createIfDoesNotExist)
		{
			synchronized (sessionIdToEntryMap)
			{
				entry = sessionIdToEntryMap.get(sessionId);
				if (entry == null)
				{
					entry = new SessionEntry(this);
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

	public <T> Page getPage(String sessionId, String pagemap, int id, int versionNumber,
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
				List<SerializedPage> pages = getPagesToSaveList(sessionId);
				synchronized (pages)
				{
					flushPagesToSaveList(sessionId, pages);
					data = entry.loadPage(pagemap, id, versionNumber, ajaxVersionNumber);
				}
			}

			if (data != null)
			{
				@SuppressWarnings("unchecked")
				final Page ret = deserializePage(data, versionNumber);
				return ret;
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
				List<SerializedPage> pages = getPagesToSaveList(sessionId);
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
	protected void storeSerializedPages(String sessionId, List<SerializedPage> pages)
	{
		SessionEntry entry = getSessionEntry(sessionId, true);

		if (isSynchronous())
		{
			for (Iterator<SerializedPage> i = pages.iterator(); i.hasNext();)
			{
				SerializedPage serializedPage = i.next();
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
	protected void onPagesSerialized(String sessionId, List<SerializedPage> pages)
	{

	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#storePage(java.lang.String,
	 *      org.apache.wicket.Page)
	 */
	public void storePage(String sessionId, Page page)
	{
		List<SerializedPage> pages = serializePage(page);

		serializedPagesCache.storePage(sessionId, page, pages);

		onPagesSerialized(sessionId, pages);

		storeSerializedPages(sessionId, pages);
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#unbind(java.lang.String)
	 */
	public void unbind(String sessionId)
	{
		SessionEntry entry = sessionIdToEntryMap.remove(sessionId);
		if (entry != null)
		{
			if (isSynchronous())
			{
				entry.unbind();
			}
			else
			{
				List<SerializedPage> pages = getPagesToSaveList(sessionId);
				synchronized (pages)
				{
					flushPagesToSaveList(sessionId, pages);
					entry.unbind();
				}
				pagesToSaveAll.remove(sessionId);
			}
		}
	}

	// map from session id to serialized page list
	// this contains lists for all active sessions
	private final Map<String, List<SerializedPage>> pagesToSaveAll = new ConcurrentHashMap<String, List<SerializedPage>>();

	// contains list of serialized pages to be saved - only non empty lists
	private final Map<String, List<SerializedPage>> pagesToSaveActive = new ConcurrentHashMap<String, List<SerializedPage>>();

	/**
	 * Returns the list of pages to be saved for the specified session id. If the list is not found,
	 * new list is created.
	 * 
	 * @param sessionId
	 * @return pages to save
	 */
	protected List<SerializedPage> getPagesToSaveList(String sessionId)
	{
		List<SerializedPage> list = pagesToSaveAll.get(sessionId);
		if (list == null)
		{
			synchronized (pagesToSaveAll)
			{
				list = pagesToSaveAll.get(sessionId);
				if (list == null)
				{
					list = new ArrayList<SerializedPage>();
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
	protected void flushPagesToSaveList(String sessionId, List<SerializedPage> list)
	{
		if (list != null)
		{
			for (Iterator<SerializedPage> i = list.iterator(); i.hasNext();)
			{
				try
				{
					SerializedPage page = i.next();
					getSessionEntry(sessionId, true).savePage(page);
				}
				catch (Exception e)
				{
					// We have to catch the exception here to process the other entries,
					// otherwise there would be a big memory leak
					log.error("Error flushing page", e);
				}
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
	private void schedulePagesSave(String sessionId, List<SerializedPage> pages)
	{
		List<SerializedPage> list = getPagesToSaveList(sessionId);
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
		private volatile Boolean stop = Boolean.FALSE;

		public void run()
		{
			while (stop == Boolean.FALSE)
			{
				// wait until we have something to save
				while (pagesToSaveActive.isEmpty() && stop == false)
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
				for (Iterator<Entry<String, List<SerializedPage>>> i = pagesToSaveActive.entrySet()
					.iterator(); i.hasNext();)
				{
					Entry<String, List<SerializedPage>> entry = i.next();
					String sessionId = entry.getKey();
					List<SerializedPage> pages = entry.getValue();

					synchronized (pages)
					{
						try
						{
							flushPagesToSaveList(sessionId, pages);
						}
						catch (Exception e)
						{
							log.error(
								"Error flushing serialized pages from worker thread for session " +
									sessionId, e);
						}
						i.remove();
					}
				}
			}

			stop = null;
		}

		/**
		 * Stops the worker thread.
		 */
		public void stop()
		{
			if (stop == null)
				return;

			stop = Boolean.TRUE;

			// Block the calling thread until this thread has really stopped running
			while (stop != null)
			{
				try
				{
					Thread.sleep(getSavingThreadSleepTime());
				}
				catch (InterruptedException ignore)
				{
				}
			}
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
	 * @return sleep time
	 */
	protected int getSavingThreadSleepTime()
	{
		return 100;
	}

	/**
	 * Returns whether the {@link DiskPageStore} should work in synchronous or asynchronous mode.
	 * Asynchronous mode uses a worker thread to save pages, which results in smoother performance.
	 * 
	 * @return <code>true</code> store is synchronous
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
	 * @return size
	 */
	protected int getLastRecentlySerializedPagesCacheSize()
	{
		return lastRecentlySerializedPagesCacheSize;
	}

	private final SerializedPagesCache serializedPagesCache = new SerializedPagesCache(
		getLastRecentlySerializedPagesCacheSize());

	/**
	 * Strips the actual serialized page data. This is used to store
	 * {@link SerializedPageWithSession} instance in http session to reduce the memory consumption.
	 * The data can be stripped because it's already stored on disk
	 * 
	 * @param page
	 * @return SerializedPageWithSession data
	 */
	private SerializedPageWithSession stripSerializedPage(SerializedPageWithSession page)
	{
		List<SerializedPage> pages = new ArrayList<SerializedPage>(page.pages.size());
		for (Iterator<SerializedPage> i = page.pages.iterator(); i.hasNext();)
		{
			SerializedPage sp = i.next();
			pages.add(new SerializedPage(sp.getPageId(), sp.getPageMapName(),
				sp.getVersionNumber(), sp.getAjaxVersionNumber(), null));
		}
		return new SerializedPageWithSession(page.sessionId, page.pageId, page.pageMapName,
			page.versionNumber, page.ajaxVersionNumber, pages);
	}

	private byte[] getPageData(String sessionId, int pageId, String pageMapName, int versionNumber,
		int ajaxVersionNumber)
	{
		SessionEntry entry = getSessionEntry(sessionId, false);
		if (entry != null)
		{
			byte[] data;

			if (isSynchronous())
			{
				data = entry.loadPage(pageMapName, pageId, versionNumber, ajaxVersionNumber);
			}
			else
			{
				// we need to make sure that the there are no pending pages to
				// be saved before loading a page
				List<SerializedPage> pages = getPagesToSaveList(sessionId);
				synchronized (pages)
				{
					flushPagesToSaveList(sessionId, pages);
					data = entry.loadPage(pageMapName, pageId, versionNumber, ajaxVersionNumber);
				}
			}
			return data;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Loads the data stripped by
	 * {@link #stripSerializedPage(org.apache.wicket.protocol.http.pagestore.DiskPageStore.SerializedPageWithSession)}.
	 * 
	 * @param page
	 * @return SerializedPageWithSession isntance
	 */
	private SerializedPageWithSession restoreStrippedSerializedPage(SerializedPageWithSession page)
	{
		List<SerializedPage> pages = new ArrayList<SerializedPage>(page.pages.size());
		for (Iterator<SerializedPage> i = page.pages.iterator(); i.hasNext();)
		{
			SerializedPage sp = i.next();
			byte data[] = getPageData(page.sessionId, sp.getPageId(), sp.getPageMapName(),
				sp.getVersionNumber(), sp.getAjaxVersionNumber());

			pages.add(new SerializedPage(sp.getPageId(), sp.getPageMapName(),
				sp.getVersionNumber(), sp.getAjaxVersionNumber(), data));
		}

		return new SerializedPageWithSession(page.sessionId, page.pageId, page.pageMapName,
			page.versionNumber, page.ajaxVersionNumber, pages);
	}


	/**
	 * {@inheritDoc}
	 */
	public Serializable prepareForSerialization(String sessionId, Object page)
	{
		SerializedPageWithSession result = null;
		if (page instanceof Page)
		{
			result = serializedPagesCache.getPage((Page)page);
			if (result == null)
			{
				List<SerializedPage> serialized = serializePage((Page)page);
				result = serializedPagesCache.storePage(sessionId, (Page)page, serialized);
			}
		}
		else if (page instanceof SerializedPageWithSession)
		{
			SerializedPageWithSession serialized = (SerializedPageWithSession)page;
			if (serialized.page.get() == SerializedPageWithSession.NO_PAGE)
			{
				// stripped page, need to restore it first
				result = restoreStrippedSerializedPage(serialized);
			}
			else
			{
				result = serialized;
			}
		}

		if (result != null)
		{
			return result;
		}
		else
		{
			return (Serializable)page;
		}
	}

	protected boolean storeAfterSessionReplication()
	{
		return true;
	}

	/**
	 * @see org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.ISerializationAwarePageStore#restoreAfterSerialization(java.io.Serializable)
	 */
	public Object restoreAfterSerialization(Serializable serializable)
	{
		if (!storeAfterSessionReplication() || serializable instanceof Page)
		{
			return serializable;
		}
		else if (serializable instanceof SerializedPageWithSession)
		{
			SerializedPageWithSession page = (SerializedPageWithSession)serializable;
			if (page.page == null || page.page.get() != SerializedPageWithSession.NO_PAGE)
			{
				storeSerializedPages(page.sessionId, page.pages);
				return stripSerializedPage(page);
			}
			else
			{
				return page;
			}
		}
		else
		{
			String type = serializable != null ? serializable.getClass().getName() : null;
			throw new IllegalArgumentException("Unknown object type " + type);
		}
	}

	public Page convertToPage(Object page)
	{
		if (page instanceof Page)
		{
			return (Page)page;
		}
		else if (page instanceof SerializedPageWithSession)
		{
			SerializedPageWithSession serialized = (SerializedPageWithSession)page;

			if (serialized.page == null ||
				serialized.page.get() != SerializedPageWithSession.NO_PAGE)
			{
				storeSerializedPages(serialized.sessionId, serialized.pages);
			}

			return getPage(serialized.sessionId, serialized.pageMapName, serialized.pageId,
				serialized.versionNumber, serialized.ajaxVersionNumber);
		}
		else
		{
			String type = page != null ? page.getClass().getName() : null;
			throw new IllegalArgumentException("Unknown object type + type");
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
				List<SerializedPage> pages = getPagesToSaveList(sessionId);
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
