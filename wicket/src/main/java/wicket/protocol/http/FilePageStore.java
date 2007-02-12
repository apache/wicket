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
package wicket.protocol.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Page;
import wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore;
import wicket.util.lang.Objects;

/**
 * Stores pages on disk.
 * <p>
 * Override {@link #getWorkDir()} to change the default directory for pages,
 * which is configured from the javax.servlet.context.tempdir attribute in the
 * servlet context.
 * 
 * @author jcompagner
 */
public class FilePageStore implements IPageStore
{
	/** log. */
	protected static Log log = LogFactory.getLog(FilePageStore.class);
	
	private static final Object SERIALIZING = new Object();

	private final File defaultWorkDir;

	private final List storePageMap;

	private final PageSavingThread thread;

	private final String appName;

	volatile long totalSavingTime = 0;
	volatile long totalSerializationTime = 0;

	private volatile int saved;

	private volatile int bytesSaved;

	

	/**
	 * Construct.
	 */
	public FilePageStore()
	{
		this((File)((WebApplication)Application.get()).getServletContext().getAttribute(
				"javax.servlet.context.tempdir"));
	}

	/**
	 * Construct.
	 * 
	 * @param dir
	 *            The directory to save to.
	 */
	public FilePageStore(File dir)
	{
		defaultWorkDir = dir;
		storePageMap = Collections.synchronizedList(new LinkedList());
		thread = new PageSavingThread();
		appName = Application.get().getApplicationKey();
		Thread t = new Thread(thread, "FilePageStoreThread-" + appName);
		t.setDaemon(true);
		t.start();

	}

	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#getPage(java.lang.String,
	 *      int, int)
	 */
	public Page getPage(String sessionId, String pagemapName, int id, int versionNumber, int ajaxVersionNumber)
	{
		SessionPageKey currentKey = new SessionPageKey(sessionId, id, versionNumber,ajaxVersionNumber,pagemapName ,null);
		byte[] bytes = testMap(currentKey);
		if(bytes != null)
		{
			Page page = (Page)Objects.byteArrayToObject(bytes);
			page = page.getVersion(versionNumber);
			return page; 
		}
		File sessionDir = new File(getWorkDir(), sessionId);
		if (sessionDir.exists())
		{
			File pageFile = getPageFile(currentKey, sessionDir);
			if (pageFile.exists())
			{
				long t1 = System.currentTimeMillis();
				FileInputStream fis = null;
				try
				{
					byte[] pageData = null;
					fis = new FileInputStream(pageFile);
					int length = (int)pageFile.length();
					ByteBuffer bb = ByteBuffer.allocate(length);
					fis.getChannel().read(bb);
					if (bb.hasArray())
					{
						pageData = bb.array();
					}
					else
					{
						pageData = new byte[length];
						bb.get(pageData);
					}
					long t2 = System.currentTimeMillis();
					Page page = (Page)Objects.byteArrayToObject(pageData);
					page = page.getVersion(versionNumber);
					if (page != null && log.isDebugEnabled())
					{
						long t3 = System.currentTimeMillis();
						log.debug("restoring page " + page.getClass() + "[" + page.getNumericId()
								+ "," + page.getCurrentVersionNumber() + "] size: "
								+ pageData.length + " for session " + sessionId + " took "
								+ (t2 - t1) + " miliseconds to read in and " + (t3 - t2)
								+ " miliseconds to deserialize");
					}
					return page;
				}
				catch (Exception e)
				{
					log.debug("Error loading page " + id + "," + versionNumber
							+ " for the sessionid " + sessionId + " from disk", e);
				}
				finally
				{
					try
					{
						if (fis != null)
						{
							fis.close();
						}
					}
					catch (IOException ex)
					{
						// ignore
					}
				}
			}
		}
		return null;
	}

	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#removePage(java.lang.String,
	 *      wicket.Page)
	 */
	public void removePage(String sessionId, Page page)
	{
		removePageFromPendingMap(sessionId,page.getNumericId());
	}

	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#storePage(java.lang.String,
	 *      wicket.Page)
	 */
	public void storePage(String sessionId, Page page)
	{
		// if the pagemap falls behind, directly serialize it here.
		if(storePageMap.size() > 25)
		{
			SessionPageKey key = new SessionPageKey(sessionId, page);
			byte[] bytes = serializePage(key,page);
			if (bytes != null)
			{
				key.setObject(bytes);
				storePageMap.add(key);
			}
			
		}
		else
		{
			synchronized (storePageMap)
			{
				storePageMap.add(new SessionPageKey(sessionId, page));
				storePageMap.notifyAll();
			}
		}
	}

	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#pageAccessed(java.lang.String,
	 *      wicket.Page)
	 */
	public void pageAccessed(String sessionId, Page page)
	{
		SessionPageKey currentKey = new SessionPageKey(sessionId, page);
		testMap(currentKey);
	}


	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#destroy()
	 */
	public void destroy()
	{
		thread.stop();
	}

	private byte[] testMap(SessionPageKey currentKey )
	{
		synchronized (storePageMap)
		{
			int index = storePageMap.indexOf(currentKey);
			if (index != -1)
			{
				currentKey = (SessionPageKey)storePageMap.get(index);
				Object object = currentKey.data;
				if ( object instanceof Page)
				{
					storePageMap.remove(index);
				}
				else if ( object instanceof byte[])
				{
					return (byte[])object;
				}
				else if (object != SERIALIZING)
				{
					currentKey = null;
				}
			}
			else
			{
				currentKey = null;
			}
		}
		
		
		if (currentKey != null)
		{
			if ( currentKey.data == SERIALIZING)
			{
				synchronized (currentKey)
				{
					try
					{
						currentKey.wait();
					}
					catch (InterruptedException ex)
					{
						throw new RuntimeException(ex);
					}
				}
				Object data = currentKey.data;
				if(data instanceof byte[])
				{
					return (byte[])data;
				}
			}
			else
			{
				byte[] bytes = serializePage(currentKey, (Page)currentKey.data);
				if ( bytes != null)
				{
					currentKey.setObject(bytes);
					storePageMap.add(currentKey);
				}
				return bytes;
			}
		}
		return null;
	}


	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#unbind(java.lang.String)
	 */
	public void unbind(String sessionId)
	{
		removeSessionFromPendingMap(sessionId);
	}

	private void removeSessionFromPendingMap(String sessionId)
	{
		synchronized (storePageMap)
		{
			Iterator iterator = storePageMap.iterator();
			while (iterator.hasNext())
			{
				SessionPageKey key = (SessionPageKey)iterator.next();
				if (key.sessionId == sessionId)
				{
					iterator.remove();
				}
			}
		}
		
		removeSession(sessionId);
		
	}

	private void removeSession(String sessionId)
	{
		File sessionDir = new File(getWorkDir(), sessionId);
		if (sessionDir.exists())
		{
			File[] files = sessionDir.listFiles();
			if (files != null)
			{
				for (int i = 0; i < files.length; i++)
				{
					files[i].delete();
				}
			}
			if (!sessionDir.delete())
			{
				sessionDir.deleteOnExit();
			}
		}
	}

	/**
	 * @param sessionId
	 * @param id
	 */
	private void removePageFromPendingMap(String sessionId, int id)
	{
		synchronized (storePageMap)
		{
			Iterator iterator = storePageMap.iterator();
			while (iterator.hasNext())
			{
				SessionPageKey key = (SessionPageKey)iterator.next();
				if (key.sessionId == sessionId && key.id == id)
				{
					iterator.remove();
				}
			}
		}
		removePage(sessionId, id);
	}


	private void removePage(String sessionId, int id)
	{
		File sessionDir = new File(getWorkDir(), sessionId);
		if (sessionDir.exists())
		{
			final String filepart = appName + "-page-" + id;
			File[] listFiles = sessionDir.listFiles(new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					return name.startsWith(filepart);
				}
			});
			for (int i = 0; i < listFiles.length; i++)
			{
				listFiles[i].delete();
			}
		}

	}

	/**
	 * Returns the working directory for this disk-based PageStore. Override
	 * this to configure a different location. The default is
	 * javax.servlet.context.tempdir from the servlet context.
	 * 
	 * @return Working directory
	 */
	protected File getWorkDir()
	{
		return defaultWorkDir;
	}

	/**
	 * @param key 
	 * @param sessionDir
	 * @return The file pointing to the page
	 */
	private File getPageFile(SessionPageKey key, File sessionDir)
	{
		return new File(sessionDir, appName + "-pm-" + key.pageMap+ "-p-" + key.id + "-v-" + key.versionNumber + "-a-" + key.ajaxVersionNumber);
	}
	
	private byte[] serializePage(SessionPageKey key, Page page)
	{
		if (page.getCurrentVersionNumber() != key.versionNumber)
		{
			System.err.println("ERROR versionnumber dont match1!");
		}
		long t1 = System.currentTimeMillis();
		byte[] bytes = Objects.objectToByteArray(page);
		totalSerializationTime += (System.currentTimeMillis()-t1);
		if (page.getCurrentVersionNumber() != key.versionNumber)
		{
			System.err.println("ERROR versionnumber dont match2!");
		}
		return bytes;
	}
	
	/**
	 * @param sessionId
	 * @param key 
	 * @param bytes 
	 */
	private void savePage(SessionPageKey key, byte[] bytes)
	{
		File sessionDir = new File(getWorkDir(), key.sessionId);
		sessionDir.mkdirs();
		File pageFile = getPageFile(key, sessionDir);

		FileOutputStream fos = null;
		long t1 = System.currentTimeMillis();
		int length = 0;
		try
		{
			fos = new FileOutputStream(pageFile);
			ByteBuffer bb = ByteBuffer.wrap(bytes);
			fos.getChannel().write(bb);
			length = bytes.length;
		}
		catch (Exception e)
		{
			log.error("Error saving page " + key.pageClass +" [" + key.id + ","
					+ key.versionNumber + "] for the sessionid " + key.sessionId);
		}
		finally
		{
			try
			{
				if (fos != null)
				{
					fos.close();
				}
			}
			catch (IOException ex)
			{
				// ignore
			}
		}
		long t3 = System.currentTimeMillis();
		if (log.isDebugEnabled())
		{
			log.debug("storing page " + key.pageClass + "[" + key.id + ","
					+ key.versionNumber + "] size: " + length + " for session "
					+ key.sessionId + " took " + (t3 - t1) + " miliseconds to save");
		}
		totalSavingTime += (t3-t1);
		saved++;
		bytesSaved += length;
	}
	

	private class SessionPageKey
	{
		private final String sessionId;
		private final int id;
		private final int versionNumber;
		private final int ajaxVersionNumber;
		private final String pageMap;
		private final Class pageClass;
		
		private Object data;

		SessionPageKey(String sessionId, Page page)
		{
			this(sessionId, page.getNumericId(), page.getCurrentVersionNumber(), 
					page.getAjaxVersionNumber(), page.getPageMap().getName(), page.getClass(),page);
		}

		SessionPageKey(String sessionId, int id, int versionNumber, int ajaxVersionNumber, String pagemap, Class pageClass)
		{
			this(sessionId, id, versionNumber, ajaxVersionNumber, pagemap, pageClass, null);
		}
		
		SessionPageKey(String sessionId, int id, int versionNumber, int ajaxVersionNumber, String pagemap, Class pageClass, Page page)
		{
			this.sessionId = sessionId;
			this.id = id;
			this.versionNumber = versionNumber;
			this.ajaxVersionNumber = ajaxVersionNumber;
			this.pageClass = pageClass;
			this.pageMap = pagemap;
			this.data = page;
		}
		
		public Object getObject()
		{
			return data;
		}
		
		public void setObject(Object  o)
		{
			data = o;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			return sessionId.hashCode() + id + versionNumber;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj)
		{
			if (obj instanceof SessionPageKey)
			{
				SessionPageKey key = (SessionPageKey)obj;
				return id == key.id && versionNumber == key.versionNumber
						&& ajaxVersionNumber == key.ajaxVersionNumber
						&& ((pageMap != null && pageMap.equals(key.pageMap)) || (pageMap == null && key.pageMap == null)) 
						&& sessionId.equals(key.sessionId);
			}
			return false;
		}
		
		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return "SessionPageKey[" +sessionId + "," + id + "," + versionNumber + "," + ajaxVersionNumber + ", "+ pageMap+", " + data + "]";
		}
	}

	private class PageSavingThread implements Runnable
	{
		private volatile boolean stop = false;

		/**
		 * Stops this thread.
		 */
		public void stop()
		{
			System.err.println("Total time in saving: " + totalSavingTime);
			System.err.println("Total time in serialization: " + totalSerializationTime);
			System.err.println("Bytes saved: " + bytesSaved);
			System.err.println("Pages saved: " + saved);
			synchronized (storePageMap)
			{
				stop = true;
				storePageMap.notifyAll();
			}
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			while (!stop)
			{
				SessionPageKey key = null;
				try
				{
					Object data = null;
					synchronized (storePageMap)
					{
						if (stop)
							return;
						while (storePageMap.size() == 0)
						{
							storePageMap.wait();
						}
						key = (SessionPageKey)storePageMap.get(0);
						data = key.getObject();
						if ( data instanceof Page)
						{
							key.setObject(SERIALIZING);
						}
					}
					byte[] pageBytes = null;
					if ( data instanceof Page)
					{
						pageBytes = serializePage(key,(Page)data);
						synchronized (key)
						{
							key.setObject(pageBytes);
							key.notifyAll();
						}
					}
					else if ( data instanceof byte[])
					{
						pageBytes = (byte[])data;
					}
					if ( pageBytes != null)
					{
						savePage(key, pageBytes);
					}
					if (storePageMap.remove(0) != key)
					{
						System.err.println("Remove 0 is not the same!!");
					}
				}
				catch (Exception e)
				{
					log.error("Error in page save thread", e);
					// removing the one that did fail...
					storePageMap.remove(key);
				}
			}
		}
	}
}
