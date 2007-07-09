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
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Very simple page store that uses separate file for each serialized
 * page instance. Also this store doesn't use any worker threads.
 * <p>
 * This store is for demonstration purposes only and will perfom badly
 * in production.
 * @author Matej Knopp
 */
public class SimpleSynchronousFilePageStore extends AbstractPageStore
{
	private final File defaultWorkDir;
	private final String appName;

	/**
	 * Construct.
	 * 
	 * @param workDir
	 */
	public SimpleSynchronousFilePageStore(File workDir)
	{
		this.defaultWorkDir = workDir;
		defaultWorkDir.mkdirs();

		appName = Application.get().getApplicationKey();
	}

	/**
	 * Construct.
	 */
	public SimpleSynchronousFilePageStore()
	{
		this((File)((WebApplication)Application.get()).getServletContext().getAttribute(
				"javax.servlet.context.tempdir"));
	}


	private File getPageFile(File sessionDir, String pageMapName, int pageId, int versionNumber,
			int ajaxVersionNumber)
	{
		return new File(sessionDir, appName + "-pm-" + pageMapName + "-p-" + pageId + "-v-" +
				versionNumber + "-a-" + ajaxVersionNumber);
	}


	public void destroy()
	{
	}

	protected byte[] loadPageData(File workDir, String sessionId, String pageMapName, int pageId,
			int versionNumber, int ajaxVersionNumber)
	{
		File sessionDir = new File(workDir, sessionId);
		byte[] pageData = null;

		if (sessionDir.exists())
		{
			File pageFile = getPageFile(sessionDir, pageMapName, pageId, versionNumber,
					ajaxVersionNumber);
			if (pageFile.exists())
			{
				long t1 = System.currentTimeMillis();
				FileInputStream fis = null;
				try
				{
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
				}
				catch (Exception e)
				{
					log.debug("Error loading page " + pageId + "," + versionNumber +
							" for the sessionid " + sessionId + " from disk", e);
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
		return pageData;
	}

	public Page getPage(String sessionId, String pageMapName, int pageId, int versionNumber,
			int ajaxVersionNumber)
	{
		byte data[] = loadPageData(defaultWorkDir, sessionId, pageMapName, pageId, versionNumber,
				ajaxVersionNumber);
		if (data != null)
		{
			return deserializePage(data, versionNumber);
		}
		else
		{
			return null;
		}
	}

	public void pageAccessed(String sessionId, Page page)
	{
	}

	private void removeFiles(String sessionId, String pageMap, int id)
	{
		File sessionDir = new File(defaultWorkDir, sessionId);
		if (sessionDir.exists())
		{
			final String filepart;
			if (id != -1)
			{
				filepart = appName + "-pm-" + pageMap + "-p-" + id;
			}
			else
			{
				filepart = appName + "-pm-" + pageMap;
			}
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

	protected long savePageData(String sessionId, SerializedPage page)
	{
		File sessionDir = new File(defaultWorkDir, sessionId);
		sessionDir.mkdirs();

		File pageFile = getPageFile(sessionDir, page.getPageMapName(), page.getPageId(), page
				.getVersionNumber(), page.getAjaxVersionNumber());
		FileOutputStream fos = null;
		int length = 0;
		try
		{
			fos = new FileOutputStream(pageFile);
			ByteBuffer bb = ByteBuffer.wrap(page.getData());
			fos.getChannel().write(bb);
			length = page.getData().length;
		}
		catch (Exception e)
		{
			log.error("Error saving page " + pageFile.getAbsolutePath());
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
		return length;
	}

	public void removePage(String sessionId, String pageMapName, int pageId)
	{
		removeFiles(sessionId, pageMapName, pageId);
	}

	public void storePage(String sessionId, Page page)
	{
		List/* SerializedPage */serialized = serializePage(page);

		for (Iterator i = serialized.iterator(); i.hasNext();)
		{
			SerializedPage serializedPage = (SerializedPage)i.next();
			savePageData(sessionId, serializedPage);
		}
	}

	private void removeSession(String sessionId)
	{
		File sessionDir = new File(defaultWorkDir, sessionId);
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

	public void unbind(String sessionId)
	{
		removeSession(sessionId);
	}

	private static final Logger log = LoggerFactory.getLogger(SimpleSynchronousFilePageStore.class);
}
