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
package org.apache.wicket.core.util.file;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.file.Path;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.util.string.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Maintain a list of paths which might either be ordinary folders of the filesystem or relative
 * paths to the web application's servlet context.
 *
 * @author Johan Compagner
 */
public final class WebApplicationPath extends Path
{
	private final static Logger log = LoggerFactory.getLogger(WebApplicationPath.class);

	private static final String WEB_INF = "WEB-INF/";

	/** The list of urls in the path */
	private final List<String> webappPaths = new ArrayList<String>();

	/** The web apps servlet context */
	private final ServletContext servletContext;

	/**
	 * Constructor
	 *
	 * @param servletContext
	 *            The webapplication context where the resources must be loaded from
	 */
	public WebApplicationPath(final ServletContext servletContext)
	{
		this.servletContext = servletContext;

		// adding root so servlet context resources are always checked
		webappPaths.add("/");
	}

	/**
	 * @param path
	 *            add a path that is lookup through the servlet context
	 */
	@Override
	public void add(String path)
	{
		final Folder folder = new Folder(path);
		if (folder.exists())
		{
			log.debug("Added path '{}' as a folder.", path);
			super.add(folder);
		}
		else
		{
			if (!path.startsWith("/"))
			{
				path = "/" + path;
			}
			if (!path.endsWith("/"))
			{
				path += "/";
			}
			log.debug("Added path '{}' as a web path.", path);
			webappPaths.add(path);
		}
	}

	/**
	 *
	 * @see org.apache.wicket.util.file.IResourceFinder#find(Class, String)
	 */
	@Override
	public IResourceStream find(final Class<?> clazz, final String pathname)
	{
		if (pathname == null)
		{
			return null;
		}

		IResourceStream resourceStream = super.find(clazz, pathname);

		if (resourceStream == null && pathname.startsWith(WEB_INF) == false)
		{
			for (String path : webappPaths)
			{
				try
				{
					final URL url = servletContext.getResource(path + pathname);
					if (url != null)
					{
						resourceStream = new UrlResourceStream(url);
						break;
					}
				}
				catch (Exception ex)
				{
					// ignore, file couldn't be found
				}
			}
		}

		return resourceStream;
	}

	public List<String> getWebappPaths()
	{
		return webappPaths;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[folders = " + StringList.valueOf(getFolders()) + ", webapppaths: " +
			StringList.valueOf(webappPaths) + "]";
	}
}
