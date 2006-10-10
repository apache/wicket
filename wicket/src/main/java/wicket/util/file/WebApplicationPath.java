/*
 * $Id$ $Revision$ $Date$
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
package wicket.util.file;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import wicket.util.string.StringList;

/**
 * Mantains a list of folders as a path.
 * 
 * @author Johan Compagner
 */
public final class WebApplicationPath implements IResourcePath
{
	/** The list of urls in the path */
	private final List<Comparable> webappPaths = new ArrayList<Comparable>();

	/** The list of folders in the path */
	private final List<Comparable> folders = new ArrayList<Comparable>();

	private final ServletContext servletContext;

	/**
	 * Constructor
	 * 
	 * @param servletContext
	 *            The webapplication context where the resources must be loaded
	 *            from
	 */
	public WebApplicationPath(ServletContext servletContext)
	{
		this.servletContext = servletContext;
	}

	/**
	 * @param folder
	 *            add a path that is lookup through the servlet context
	 */
	public void add(String folder)
	{
		final Folder f = new Folder(folder);
		if (f.exists())
		{
			folders.add(f);
		}
		else
		{
			if (!folder.startsWith("/"))
			{
				folder = "/" + folder;
			}
			if (!folder.endsWith("/"))
			{
				folder += "/";
			}
			webappPaths.add(folder);
		}
	}

	/**
	 * Looks for a given pathname along this path
	 * 
	 * @param pathname
	 *            The filename with possible path
	 * @return The file located on the path
	 */
	public URL find(final String pathname)
	{
		for (Comparable comparable : folders)
		{
			Folder folder = (Folder)comparable;
			File file = new File(folder, pathname);
			if (file.exists())
			{
				try
				{
					return file.toURI().toURL();
				}
				catch (MalformedURLException ex)
				{
					// ignore
				}
			}
		}
		for (Comparable comparable : webappPaths)
		{
			final String path = (String)comparable;
			try
			{
				final URL file = servletContext.getResource(path + pathname);
				if (file != null)
				{
					return file;
				}
			}
			catch (Exception ex)
			{
				// ignore, file couldn't be found
			}
		}

		return null;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[folders = " + StringList.valueOf(folders) + ", webapppaths: "
				+ StringList.valueOf(webappPaths) + "]";
	}
}
