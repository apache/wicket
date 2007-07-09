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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.pagestore.PageWindowManager.PageWindow;
import org.apache.wicket.util.concurrent.ConcurrentHashMap;
import org.apache.wicket.util.lang.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matej Knopp
 */
public class DiskPageStore extends AbstractPageStore
{
	private static class PageMapEntry
	{
		private String pageMapName;
		private String fileName;
		private PageWindowManager manager;
	}

	private class SessionEntry
	{
		private String sessionId;
		private List pageMapEntryList = new ArrayList();

		/**
		 * @return
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

		private PageMapEntry getPageMapEntry(String pageMapName, boolean create)
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

		private void removePageMapEntry(PageMapEntry entry)
		{
			fileChannelPool.closeAndDeleteFileChannel(entry.fileName);
			pageMapEntryList.remove(entry);
		}

		/**
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
		 * @param page
		 */
		public synchronized void savePage(SerializedPage page)
		{
			PageMapEntry entry = getPageMapEntry(page.getPageMapName(), true);
			PageWindow window = entry.manager.savePage(page.getPageId(), page.getVersionNumber(),
					page.getAjaxVersionNumber(), page.getData().length);
			pageMapEntryList.remove(entry);
			pageMapEntryList.add(entry);

			while (getTotalSize() > getMaxSizePerSession() && pageMapEntryList.size() > 1)
			{
				removePageMapEntry((PageMapEntry)pageMapEntryList.get(0));
			}

			FileChannel channel = fileChannelPool.getFileChannel(entry.fileName, true);
			try
			{
				channel.write(ByteBuffer.wrap(page.getData()), window.getFilePartOffset());
			}
			catch (IOException e)
			{
				log.error("Error writing to a channel " + channel, e);
			}
			finally
			{
				fileChannelPool.returnFileChannel(channel);
			}
		}

		/**
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
		 * @param pageMapName
		 * @param id
		 * @param versionNumber
		 * @param ajaxVersionNumber
		 * @return
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
					FileChannel channel = fileChannelPool.getFileChannel(entry.fileName, false);
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
				}
			}
			return result;
		}

		/**
		 * 
		 */
		public synchronized void unbind()
		{
			while (pageMapEntryList.size() > 0)
			{
				removePageMapEntry((PageMapEntry)pageMapEntryList.get(pageMapEntryList.size() - 1));
			}
		}
	}

	private File getSessionFolder(String sessionId, boolean create)
	{
		File storeFolder = new File(fileStoreFolder, appName + "-filestore");
		File sessionFolder = new File(storeFolder, sessionId);
		if (create)
		{
			mkdirs(sessionFolder);
		}
		return sessionFolder;
	}

	// for some reason, simple file.mkdirs sometimes fails under heavy load
	private void mkdirs(File file)
	{
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

	private String getPageMapFileName(String sessionId, String pageMapName,
			boolean createSessionFolder)
	{
		File sessionFolder = getSessionFolder(sessionId, createSessionFolder);
		return new File(sessionFolder, "pm-" + pageMapName).getAbsolutePath();
	}

	private final int maxSizePerPageMap;

	protected int getMaxSizePerPageMap()
	{
		return maxSizePerPageMap;
	}

	private final int maxSizePerSession;

	protected int getMaxSizePerSession()
	{
		return maxSizePerSession;
	}

	private final FileChannelPool fileChannelPool;

	private final File fileStoreFolder;

	protected File getFileStoreFolder()
	{
		return fileStoreFolder;
	}

	private final String appName;

	/**
	 * Construct.
	 * 
	 * @param fileStoreFolder
	 * @param maxSizePerPagemap
	 * @param maxSizePerSession
	 * @param fileChannelPoolCapacity
	 */
	public DiskPageStore(File fileStoreFolder, int maxSizePerPagemap,
			int maxSizePerSession, int fileChannelPoolCapacity)
	{
		this.maxSizePerPageMap = maxSizePerPagemap;
		this.maxSizePerSession = maxSizePerSession;
		this.fileChannelPool = new FileChannelPool(fileChannelPoolCapacity);
		this.fileStoreFolder = fileStoreFolder;

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
	 * Construct.
	 * 
	 * @param maxSizePerPagemap
	 * @param maxSizePerSession
	 * @param fileChannelPoolCapacity
	 */
	public DiskPageStore(int maxSizePerPagemap, int maxSizePerSession,
			int fileChannelPoolCapacity)
	{
		this(getDefaultFileStoreFolder(), maxSizePerPagemap, maxSizePerSession,
				fileChannelPoolCapacity);
	}

	public void destroy()
	{
		fileChannelPool.destroy();
		if (pageSavingThread != null)
		{
			pageSavingThread.stop();
		}
	}

	private Map /* <String, SessionEntry> */sessionIdToEntryMap = new ConcurrentHashMap();
	
	private SessionEntry getSessionEntry(String sessionId, boolean createIfDoesNotExist)
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

	public void pageAccessed(String sessionId, Page page)
	{
	}

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
				List pages = getPagesToSaveList(sessionId);
				synchronized (pages)
				{
					flushPagesToSaveList(sessionId, pages);
					removePage(entry, pageMap, id);
				}
			}
		}
	}

	public void storePage(String sessionId, Page page)
	{
		SessionEntry entry = getSessionEntry(sessionId, true);

		List pages = serializePage(page);

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

	private static class SessionPageMapKey
	{
		private final String sessionId;
		private final String pageMapName;

		/**
		 * Construct.
		 * 
		 * @param sessionId
		 * @param pageMapName
		 */
		public SessionPageMapKey(String sessionId, String pageMapName)
		{
			this.sessionId = sessionId;
			this.pageMapName = pageMapName;
		}

		/**
		 * @return
		 */
		public String getSessionId()
		{
			return sessionId;
		}

		/**
		 * @return
		 */
		public String getPageMapName()
		{
			return pageMapName;
		}

		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}

			if (obj instanceof SessionPageMapKey == false)
			{
				return false;
			}

			SessionPageMapKey rhs = (SessionPageMapKey)obj;

			return Objects.equal(sessionId, rhs.sessionId) &&
					Objects.equal(pageMapName, rhs.pageMapName);
		}
		
		public int hashCode()
		{
			return Objects.hashCode(new Object[] { sessionId, pageMapName } );
		}
	}
	
	// map from session id to serializedpage list
	// this contains lists for all active sessions
	private Map /* <String, List<SerializedPage> */ pagesToSaveAll = new ConcurrentHashMap();
	
	// contains list of serialized pages to be saved - only non empty lists
	private Map /* <String, List<SerializedPage> */ pagesToSaveActive = new ConcurrentHashMap();
	
	private List getPagesToSaveList(String sessionId)
	{
		List list = (List) pagesToSaveAll.get(sessionId);
		if (list == null)
		{
			synchronized (pagesToSaveAll)
			{
				list = (List) pagesToSaveAll.get(sessionId);
				if (list == null)
				{
					list = new ArrayList();
					pagesToSaveAll.put(sessionId, list);
				}
			}
		}
		return list;
	}
	
	private void flushPagesToSaveList(String sessionId, List list)
	{
		if (list != null)
		{
			for (Iterator i = list.iterator(); i.hasNext();) 
			{
				SerializedPage page = (SerializedPage) i.next();
				getSessionEntry(sessionId, true).savePage(page);
			}
			list.clear();
		}
	}
	
	private void schedulePagesSave(String sessionId, List/* <SerializedPage */ pages)
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
	
	private class PageSavingThread implements Runnable
	{
		private volatile boolean stop = false;
		
		public void run()
		{
			while (stop == false)
			{
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
					
				for (Iterator i = pagesToSaveActive.entrySet().iterator(); i.hasNext(); )
				{
					Map.Entry entry = (Map.Entry) i.next();
					String sessionId = (String) entry.getKey();
					List pages = (List) entry.getValue();
					
					synchronized (pages)
					{
						flushPagesToSaveList(sessionId, pages);
						i.remove();
					}
				}
			}
		}
		
		/**
		 * 
		 */
		public void stop()
		{
			this.stop = true;
		}
	};
	
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
	
	private int getSavingThreadSleepTime() 
	{
		return 100;
	}
	
	private boolean isSynchronous()
	{
		return false;
	}

	private static final Logger log = LoggerFactory.getLogger(DiskPageStore.class);

}
