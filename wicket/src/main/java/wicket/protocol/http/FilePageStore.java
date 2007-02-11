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
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Page;
import wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore;
import wicket.util.concurrent.ConcurrentHashMap;
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

	private final ConcurrentHashMap storePageMap;

	private final PageSavingThread thread;

	private final String appName;

	volatile long totalSavingTime = 0;
	volatile long totalSerializationTime = 0;

	

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
		storePageMap = new ConcurrentHashMap();
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
	public Page getPage(String sessionId, int id, int versionNumber, int ajaxVersionNumber)
	{
		byte[] bytes = testMap(sessionId, id, versionNumber,ajaxVersionNumber);
		if(bytes != null)
		{
			Page page = (Page)Objects.byteArrayToObject(bytes);
			page = page.getVersion(versionNumber);
			return page; 
		}
		File sessionDir = new File(getWorkDir(), sessionId);
		if (sessionDir.exists())
		{
			File pageFile = getPageFile(id, versionNumber, ajaxVersionNumber, sessionDir);
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
		synchronized (storePageMap)
		{
			storePageMap.put(new SessionPageKey(sessionId, page,true), page);
			storePageMap.notifyAll();
		}
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
			byte[] bytes = serializePage(page);
			if (bytes != null)
			{
				storePageMap.put(new SessionPageKey(sessionId, page,false), bytes);
			}
			
		}
		else
		{
			synchronized (storePageMap)
			{
				storePageMap.put(new SessionPageKey(sessionId, page,false), page);
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
		testMap(sessionId, page.getNumericId(), page.getCurrentVersionNumber(), page.getAjaxVersionNumber());
	}


	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#destroy()
	 */
	public void destroy()
	{
		thread.stop();
	}

	private byte[] testMap(String sessionId, int id, int versionNumber, int ajaxVersionNumber)
	{
		SessionPageKey curentKey = new SessionPageKey(sessionId, id, versionNumber,ajaxVersionNumber,null);
		Object value = storePageMap.get(curentKey);
		if (value instanceof Page)
		{
			// if the page was still not saved before the next request. Then 
			// the request itself will serialize it
			if( storePageMap.put(curentKey, SERIALIZING) == value)
			{
				byte[] bytes = serializePage((Page)value);
				if ( bytes != null)
				{
					storePageMap.put(curentKey,bytes);
				}
				else
				{
					storePageMap.remove(curentKey);
				}
				return bytes;
			}
			else
			{
				value = SERIALIZING;
			}
		}
		while(value == SERIALIZING)
		{
			try
			{
				Thread.sleep(50);
			}
			catch (InterruptedException ex)
			{
				throw new RuntimeException(ex);
			}
			value = storePageMap.get(curentKey);
		}
		return value instanceof byte[]? (byte[])value:null;
	}


	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#unbind(java.lang.String)
	 */
	public void unbind(String sessionId)
	{
		synchronized (storePageMap)
		{
			SessionPageKey key = new SessionPageKey(sessionId, -1, -1, -1, true,null);
			storePageMap.put(key, key);
			storePageMap.notifyAll();
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
	 * @param id
	 * @param versionNumber
	 * @param ajaxVersionNumber 
	 * @param sessionDir
	 * @return The file pointing to the page
	 */
	private File getPageFile(int id, int versionNumber, int ajaxVersionNumber, File sessionDir)
	{
		return new File(sessionDir, appName + "-page-" + id + "-version-" + versionNumber + "-ajax-" + ajaxVersionNumber);
	}
	
	private byte[] serializePage(Page page)
	{
		long t1 = System.currentTimeMillis();
		byte[] bytes = Objects.objectToByteArray(page);
		totalSerializationTime += (System.currentTimeMillis()-t1);
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
		File pageFile = getPageFile(key.id, key.versionNumber,key.ajaxVersionNumber, sessionDir);

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
	}
	

	private class SessionPageKey
	{
		private final String sessionId;
		private final int id;
		private final int versionNumber;
		private final int ajaxVersionNumber;
		private final boolean remove;
		private final Class pageClass;

		SessionPageKey(String sessionId, Page page, boolean remove)
		{
			this(sessionId, page.getNumericId(), page.getCurrentVersionNumber(), 
					page.getAjaxVersionNumber(), remove, page.getClass());
		}

		SessionPageKey(String sessionId, int id, int versionNumber, int ajaxVersionNumber, Class pageClass)
		{
			this(sessionId, id, versionNumber, ajaxVersionNumber, false, pageClass);
		}

		SessionPageKey(String sessionId, int id, int versionNumber, int ajaxVersionNumber, boolean remove, Class pageClass)
		{
			this.sessionId = sessionId;
			this.id = id;
			this.versionNumber = versionNumber;
			this.ajaxVersionNumber = ajaxVersionNumber;
			this.remove = remove;
			this.pageClass = pageClass;
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
						&& sessionId.equals(key.sessionId) && remove == key.remove;
			}
			return false;
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
				Iterator iterator = null;
				try
				{
					iterator = storePageMap.keySet().iterator();
					if (!iterator.hasNext())
					{
						synchronized (storePageMap)
						{
							if (stop)
								return;
							storePageMap.wait();
						}
						continue;
					}
					while (iterator.hasNext())
					{
						SessionPageKey key = (SessionPageKey)iterator.next();
						Object value = storePageMap.get(key);
						// continue if the value is already null or currently SERIALIZING (by another thread)
						if (value == null || value == SERIALIZING)
							continue;
						if (key.remove)
						{
							if (key.id == -1)
							{
								removeSession(key.sessionId);
								// now remove any other pending save for that
								// page.
								removeSessionFromPendingMap(key.sessionId);
							}
							else
							{
								removePage(key.sessionId, key.id);
								// now remove any other pending save for that
								// page.
								removePageFromPendingMap(key.sessionId, key.id);
							}
							iterator.remove();
						}
						else 
						{
							byte[] pageBytes = null;
							if ( value instanceof Page)
							{
								if (storePageMap.put(key, SERIALIZING) == value)
								{
									pageBytes = serializePage((Page)value);
									if ( pageBytes != null)
									{
										storePageMap.put(key, pageBytes);
									}
									else
									{
										iterator.remove();
									}
								}
							}
							else
							{
								pageBytes = (byte[])value;
							}
							if ( pageBytes != null)
							{
								savePage(key, pageBytes);
								iterator.remove();
							}
						}
					}
				}
				catch (Exception e)
				{
					log.error("Error in page save thread", e);
					// removing the one that did fail...
					if (iterator != null)
						iterator.remove();
				}
			}
		}

		/**
		 * @param sessionId
		 * @param id
		 */
		private void removePageFromPendingMap(String sessionId, int id)
		{
			Iterator iterator = storePageMap.keySet().iterator();
			while (iterator.hasNext())
			{
				SessionPageKey key = (SessionPageKey)iterator.next();
				if (key.sessionId == sessionId && key.id == id)
				{
					iterator.remove();
				}
			}
		}

		private void removeSessionFromPendingMap(String sessionId)
		{
			Iterator iterator = storePageMap.keySet().iterator();
			while (iterator.hasNext())
			{
				SessionPageKey key = (SessionPageKey)iterator.next();
				if (key.sessionId == sessionId)
				{
					iterator.remove();
				}
			}
		}

		private void removePage(String sessionId, int id)
		{
			File sessionDir = new File(getWorkDir(), sessionId);
			if (sessionDir.exists())
			{
				final String filepart = appName + "-page-" + id;
				sessionDir.listFiles(new FilenameFilter()
				{
					public boolean accept(File dir, String name)
					{
						return name.startsWith(filepart);
					}
				});
			}

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

	}
}
