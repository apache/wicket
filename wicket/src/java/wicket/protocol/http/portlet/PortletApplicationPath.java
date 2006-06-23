/*
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
package wicket.protocol.http.portlet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.portlet.PortletContext;

import wicket.util.file.Folder;
import wicket.util.file.IResourcePath;
import wicket.util.string.StringList;


/**
 * Loads resources from the portlet context
 * 
 * @author Johan Compagner
 * @author Janne Hietam&auml;ki
 */
public class PortletApplicationPath implements IResourcePath
{
	/** The list of urls in the path */
	private final List webappPaths = new ArrayList();

	/** The list of folders in the path */
	private final List folders = new ArrayList();

	private final PortletContext portletContext;

	/**
	 * Constructor
	 * 
	 * @param portletContext
	 *            The webapplication context where the resources must be loaded
	 *            from
	 */
	public PortletApplicationPath(PortletContext portletContext)
	{
		this.portletContext = portletContext;
	}

	/**
	 * @param folder
	 *            add a path that is lookup through the portlet context
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
		for (final Iterator iterator = folders.iterator(); iterator.hasNext();)
		{
			Folder folder = (Folder)iterator.next();
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
		for (final Iterator iterator = webappPaths.iterator(); iterator.hasNext();)
		{
			final String path = (String)iterator.next();
			try
			{
				final URL file = portletContext.getResource(path + pathname);
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
	public String toString()
	{
		return "[folders = " + StringList.valueOf(folders) + ", webapppaths: "
		+ StringList.valueOf(webappPaths) + "]";
	}

}
