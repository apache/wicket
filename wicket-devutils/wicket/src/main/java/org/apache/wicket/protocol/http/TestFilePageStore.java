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
package org.apache.wicket.protocol.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore;
import org.apache.wicket.util.lang.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Stores pages on disk.
 * <p>
 * Override {@link #getWorkDir()} to change the default directory for pages, which is configured
 * from the javax.servlet.context.tempdir attribute in the servlet context.
 * 
 * @author jcompagner
 */
public class TestFilePageStore implements IPageStore
{
	/** log. */
	protected static Logger log = LoggerFactory.getLogger(TestFilePageStore.class);

	public void destroy()
	{
	}

	private final File defaultWorkDir;
	private final String appName;

	volatile long totalSavingTime = 0;
	volatile long totalSerializationTime = 0;

	private volatile int saved;

	private volatile int bytesSaved;

	/**
	 * Construct.
	 */
	public TestFilePageStore()
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
	public TestFilePageStore(File dir)
	{
		defaultWorkDir = dir;
		appName = Application.get().getApplicationKey();
	}

	private class SessionPageKey
	{
		private final String sessionId;
		private final int id;
		private final int versionNumber;
		private final int ajaxVersionNumber;
		private final String pageMap;
		private final String pageClassName;

		SessionPageKey(String sessionId, Page page)
		{
			this(sessionId, page.getNumericId(), page.getCurrentVersionNumber(),
				page.getAjaxVersionNumber(), page.getPageMapName(), page.getClass());
		}

		<T extends Page> SessionPageKey(String sessionId, int id, int versionNumber,
			int ajaxVersionNumber, String pagemap, Class<T> pageClass)
		{
			this.sessionId = sessionId;
			this.id = id;
			this.versionNumber = versionNumber;
			this.ajaxVersionNumber = ajaxVersionNumber;
			pageClassName = pageClass.getName();
			pageMap = pagemap;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return sessionId.hashCode() + id + versionNumber;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof SessionPageKey)
			{
				SessionPageKey key = (SessionPageKey)obj;
				return id == key.id &&
					versionNumber == key.versionNumber &&
					ajaxVersionNumber == key.ajaxVersionNumber &&
					((pageMap != null && pageMap.equals(key.pageMap)) || (pageMap == null && key.pageMap == null)) &&
					sessionId.equals(key.sessionId);
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "SessionPageKey[" + sessionId + "," + id + "," + versionNumber + "," +
				ajaxVersionNumber + ", " + pageMap + "]";
		}
	}


	public <T> Page getPage(String sessionId, String pagemap, int id, int versionNumber,
		int ajaxVersionNumber)
	{
		SessionPageKey currentKey = new SessionPageKey(sessionId, id, versionNumber,
			ajaxVersionNumber, pagemap, null);
		File sessionDir = new File(getWorkDir(), sessionId);
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
					log.debug("restoring page " + page.getClass() + "[" + page.getNumericId() +
						"," + page.getCurrentVersionNumber() + "] size: " + pageData.length +
						" for session " + sessionId + " took " + (t2 - t1) +
						" miliseconds to read in and " + (t3 - t2) + " miliseconds to deserialize");
				}
				final Page ret = page;
				return ret;
			}
			catch (Exception e)
			{
				log.error("Error", e);
			}
		}
		return null;
	}

	public void pageAccessed(String sessionId, Page page)
	{

	}

	public void removePage(String sessionId, String pageMapName, int pageId)
	{
	}

	public void storePage(String sessionId, Page page)
	{
		SessionPageKey key = new SessionPageKey(sessionId, page);
		byte[] serialized = Objects.objectToByteArray(page);
		// map.put(key, serialized);
		savePage(key, serialized);
	}

	public void unbind(String sessionId)
	{
	}

	private File getPageFile(SessionPageKey key, File sessionDir)
	{
		return new File(sessionDir, appName + "-pm-" + key.pageMap + "-p-" + key.id + "-v-" +
			key.versionNumber + "-a-" + key.ajaxVersionNumber);
	}

	private File getWorkDir()
	{
		return defaultWorkDir;
	}

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
			log.error("Error saving page " + key.pageClassName + " [" + key.id + "," +
				key.versionNumber + "] for the sessionid " + key.sessionId);
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
			log.debug("storing page " + key.pageClassName + "[" + key.id + "," + key.versionNumber +
				"] size: " + length + " for session " + key.sessionId + " took " + (t3 - t1) +
				" miliseconds to save");
		}
		totalSavingTime += (t3 - t1);
		saved++;
		bytesSaved += length;
	}

	public boolean containsPage(String sessionId, String pageMapName, int pageId, int pageVersion)
	{
		return false;
	}
}
