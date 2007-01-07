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
package wicket.protocol.http.portlet;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletContext;

import wicket.util.file.File;
import wicket.util.file.Folder;
import wicket.util.file.IResourcePath;
import wicket.util.resource.FileResourceStream;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.UrlResourceStream;
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
	private final List<String> webappPaths = new ArrayList<String>();

	/** The list of folders in the path */
	private final List<Folder> folders = new ArrayList<Folder>();

	/** Portlet Context */
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
	 * 
	 * @see wicket.util.file.IResourceFinder#find(Class, String)
	 */
	public IResourceStream find(final Class clazz, final String pathname)
	{
		for (Folder folder : folders)
		{
			File file = new File(folder, pathname);
			if (file.exists())
			{
				return new FileResourceStream(file);
			}
		}
		for (String path : webappPaths)
		{
			try
			{
				final URL url = portletContext.getResource(path + pathname);
				if (url != null)
				{
					return new UrlResourceStream(url);
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