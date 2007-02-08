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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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

	private final File defaultWorkDir;

	private final ConcurrentHashMap storePageMap;

	private final PageSavingThread thread;

	private final String appName;

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
	public Page getPage(String sessionId, int id, int versionNumber)
	{
		testMap(sessionId, id, versionNumber);
		File sessionDir = new File(getWorkDir(), sessionId);
		if (sessionDir.exists())
		{
			File pageFile = getPageFile(id, versionNumber, sessionDir);
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
			storePageMap.put(new SessionPageKey(sessionId, page.getNumericId(), page
					.getCurrentVersionNumber(), true), page);
			storePageMap.notifyAll();
		}
	}

	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#storePage(java.lang.String,
	 *      wicket.Page)
	 */
	public void storePage(String sessionId, Page page)
	{
		synchronized (storePageMap)
		{
			storePageMap.put(new SessionPageKey(sessionId, page.getNumericId(), page
					.getCurrentVersionNumber()), page);
			storePageMap.notifyAll();
		}
	}

	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#pageAccessed(java.lang.String,
	 *      wicket.Page)
	 */
	public void pageAccessed(String sessionId, Page page)
	{
		testMap(sessionId, page.getNumericId(), page.getCurrentVersionNumber());
	}


	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#destroy()
	 */
	public void destroy()
	{
		thread.stop();
	}

	private void testMap(String sessionId, int id, int versionNumber)
	{
		SessionPageKey curentKey = new SessionPageKey(sessionId, id, versionNumber);
		Object key = storePageMap.get(curentKey);
		while (key != null)
		{
			if (log.isDebugEnabled())
			{
				log.debug("The page " + id + ":" + versionNumber + " for session " + sessionId
						+ " wasn't saved yet. blocking for 200ms");
			}
			synchronized (key)
			{
				try
				{
					// for now i just wait 200ms and then try again.
					// i could use synchronized() and then notifyAll in the
					// saving thread.
					// But maybe it is really busy and we should just block for
					// 200ms
					key.wait(200);
				}
				catch (InterruptedException ex)
				{
					log.error(ex);
				}
			}
			key = storePageMap.get(curentKey);
		}
	}


	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#unbind(java.lang.String)
	 */
	public void unbind(String sessionId)
	{
		synchronized (storePageMap)
		{
			SessionPageKey key = new SessionPageKey(sessionId, -1, -1, true);
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
	 * @param sessionDir
	 * @return The file pointing to the page
	 */
	private File getPageFile(int id, int versionNumber, File sessionDir)
	{
		return new File(sessionDir, appName + "-page-" + id + "-version-" + versionNumber);
	}

	private class SessionPageKey
	{
		private final String sessionId;
		private final int id;
		private final int versionNumber;
		private final boolean remove;

		SessionPageKey(String sessionId, int id, int versionNumber)
		{
			this(sessionId, id, versionNumber, false);
		}

		SessionPageKey(String sessionId, int id, int versionNumber, boolean remove)
		{
			this.sessionId = sessionId;
			this.id = id;
			this.versionNumber = versionNumber;
			this.remove = remove;
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
						if (value == null)
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
								removePage(key.sessionId, key.id, key.versionNumber);
								// now remove any other pending save for that
								// page.
								removePageFromPendingMap(key.sessionId, key.id);
							}
						}
						else
						{
							savePage(key.sessionId, (Page)value);
						}
						iterator.remove();
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

		private void removePage(String sessionId, int id, int currentVersionNumber)
		{
			File sessionDir = new File(getWorkDir(), sessionId);
			if (sessionDir.exists())
			{
				while (currentVersionNumber >= 0)
				{
					File pageFile = getPageFile(id, currentVersionNumber, sessionDir);
					if (pageFile.exists())
					{
						pageFile.delete();
					}
					currentVersionNumber--;
				}
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

		/**
		 * @param sessionId
		 * @param page
		 */
		private void savePage(String sessionId, Page page)
		{
			File sessionDir = new File(getWorkDir(), sessionId);
			sessionDir.mkdirs();
			File pageFile = getPageFile(page.getNumericId(), page.getCurrentVersionNumber(),
					sessionDir);

			FileOutputStream fos = null;
			long t1 = System.currentTimeMillis();
			long t2 = 0;
			int length = 0;
			try
			{
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				try
				{
					new ObjectOutputStream(out).writeObject(page);
				}
				finally
				{
					out.close();
				}
				byte[] bytes = out.toByteArray();
				t2 = System.currentTimeMillis();
				fos = new FileOutputStream(pageFile);
				ByteBuffer bb = ByteBuffer.wrap(bytes);
				fos.getChannel().write(bb);
				length = bytes.length;
			}
			catch (Exception e)
			{
				// trigger serialization again, but this time gather some more
				// info
				try
				{
					Objects.checkSerializable(page);
				}
				catch (Exception e1)
				{
					log.error("Error saving page " + page.getClass() + "[" + page.getId() + ","
							+ page.getCurrentVersionNumber() + "] for the sessionid " + sessionId
							+ ": " + e1.getMessage(), e1);
				}
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
			if (log.isDebugEnabled())
			{
				long t3 = System.currentTimeMillis();
				log.debug("storing page " + page.getClass() + "[" + page.getNumericId() + ","
						+ page.getCurrentVersionNumber() + "] size: " + length + " for session "
						+ sessionId + " took " + (t2 - t1) + " miliseconds to serialize and "
						+ (t3 - t2) + " miliseconds to save");
			}
		}
	}
}
