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

	private File defaultWorkDir;

	/**
	 * Construct.
	 */
	public FilePageStore()
	{
		defaultWorkDir = (File)((WebApplication)Application.get()).getServletContext()
				.getAttribute("javax.servlet.context.tempdir");
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
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#getPage(java.lang.String,
	 *      int, int)
	 */
	public Page getPage(String sessionId, int id, int versionNumber)
	{
		File sessionDir = new File(getWorkDir(), sessionId);
		if (sessionDir.exists())
		{
			File pageFile = getPageFile(id, versionNumber, sessionDir);
			if (pageFile.exists())
			{
				// check higher numbers and delete (if they exist, this is a
				// rollback, and versions after this one should be removed to
				// make place for new version)
				int tmp = versionNumber;
				File f;
				while ((f = getPageFile(id, tmp++, sessionDir)).exists())
				{
					f.delete();
				}

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

					Page page = (Page)Objects.byteArrayToObject(pageData);
					return page.getVersion(versionNumber);
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
	 * @param id
	 * @param versionNumber
	 * @param sessionDir
	 * @return The file pointing to the page
	 */
	private File getPageFile(int id, int versionNumber, File sessionDir)
	{
		return new File(sessionDir, Application.get().getApplicationKey() + "-page-" + id
				+ "-version-" + versionNumber);
	}

	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#removePage(java.lang.String,
	 *      wicket.Page)
	 */
	public void removePage(String sessionId, Page page)
	{
		File sessionDir = new File(getWorkDir(), sessionId);
		if (sessionDir.exists())
		{
			File pageFile = getPageFile(page.getNumericId(), page.getCurrentVersionNumber(),
					sessionDir);
			if (pageFile.exists())
			{
				pageFile.delete();
			}
		}
	}

	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#storePage(java.lang.String,
	 *      wicket.Page)
	 */
	public void storePage(String sessionId, Page page)
	{
		File sessionDir = new File(getWorkDir(), sessionId);
		sessionDir.mkdirs();
		File pageFile = getPageFile(page.getNumericId(), page.getCurrentVersionNumber(), sessionDir);

		// only store when not yet stored
		if (!pageFile.exists())
		{
			page.internalDetach();
			FileOutputStream fos = null;
			try
			{
				long t1 = System.currentTimeMillis();
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
				fos = new FileOutputStream(pageFile);
				ByteBuffer bb = ByteBuffer.wrap(bytes);
				fos.getChannel().write(bb);
				if (log.isDebugEnabled())
				{
					long t2 = System.currentTimeMillis();
					log.info("storing page " + page.getNumericId() + ","
							+ page.getCurrentVersionNumber() + " for session " + sessionId
							+ " took " + (t2 - t1) + " miliseconds");
				}
			}
			catch (Exception e)
			{
				log.error("Error saving page " + page.getId() + ","
						+ page.getCurrentVersionNumber() + " for the sessionid " + sessionId, e);
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
		}
	}

	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#unbind(java.lang.String)
	 */
	public void unbind(String sessionId)
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
			sessionDir.delete();
		}
	}
}
