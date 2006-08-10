/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.protocol.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Page;
import wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore;
import wicket.util.lang.Objects;

/**
 * @author jcompagner
 */
public class FilePageStore implements IPageStore
{
	/** log. */
	protected static Log log = LogFactory.getLog(FilePageStore.class);

	private File workDir;

	/**
	 * Construct.
	 */
	public FilePageStore()
	{
		workDir = (File)((WebApplication)Application.get()).getServletContext()
				.getAttribute("javax.servlet.context.tempdir");
	}

	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#getPage(java.lang.String,
	 *      int, int)
	 */
	public Page getPage(String sessionId, int id, int versionNumber)
	{
		File sessionDir = new File(workDir, sessionId);
		if (sessionDir.exists())
		{
			File pageFile = getPageFile(id, versionNumber, sessionDir);
			if (pageFile.exists())
			{
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
					log.debug("Error loading page " + id + " with version " + versionNumber
							+ " for the sessionid " + sessionId + " from disc", e);
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
		File sessionDir = new File(workDir, sessionId);
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
		File sessionDir = new File(workDir, sessionId);
		sessionDir.mkdirs();
		File pageFile = getPageFile(page.getNumericId(), page.getCurrentVersionNumber(), sessionDir);
		// TODO check can this be called everytime at this place? Putting should
		// be called after the rendering so it should be ok.
		page.internalDetach();
		byte[] bytes = Objects.objectToByteArray(page);
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(pageFile);
			ByteBuffer bb = ByteBuffer.wrap(bytes);
			fos.getChannel().write(bb);
		}
		catch (Exception e)
		{
			log.debug("Error saving page " + page.getId() + " with version "
					+ page.getCurrentVersionNumber() + " for the sessionid " + sessionId
					+ " from disc", e);
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

	/**
	 * @see wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore#unbind(java.lang.String)
	 */
	public void unbind(String sessionId)
	{
		File sessionDir = new File(workDir, sessionId);
		if (sessionDir.exists())
		{
			File[] files = sessionDir.listFiles();
			if (files != null)
			{
				for (File element : files)
				{
					element.delete();
				}
			}
			sessionDir.delete();
		}
	}
}
