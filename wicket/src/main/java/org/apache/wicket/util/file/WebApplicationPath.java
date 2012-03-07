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
package org.apache.wicket.util.file;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.UrlResourceStream;
import org.apache.wicket.util.string.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Maintain a list of paths which might either be ordinary folders of the filesystem or relative
 * paths to the web application's servlet context.
 * 
 * @author Johan Compagner
 */
public final class WebApplicationPath implements IResourcePath
{
	private final static Logger log = LoggerFactory.getLogger(WebApplicationPath.class);

	private static final String WEB_INF = "WEB-INF/";

	/** The list of urls in the path */
	private final List<String> webappPaths = new ArrayList<String>();

	/** The list of folders in the path */
	private final List<Folder> folders = new ArrayList<Folder>();

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
	}

	/**
	 * @param path
	 *            add a path that is lookup through the servlet context
	 */
	public void add(String path)
	{
		final Folder folder = new Folder(path);
		if (folder.exists())
		{
			folders.add(folder);
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
			webappPaths.add(path);
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.util.file.IResourceFinder#find(Class, String)
	 */
	public IResourceStream find(final Class<?> clazz, String pathname)
	{
		while (pathname.startsWith("/"))
		{
			pathname = pathname.substring(1);
		}
		Iterator<Folder> foldersIter = folders.iterator();
		while (foldersIter.hasNext())
		{
			Folder folder = foldersIter.next();
			final File file = new File(folder, pathname);
			if (file.exists())
			{
				return new FileResourceStream(file);
			}
		}

		if (pathname.startsWith(WEB_INF) == false)
		{
			Iterator<String> webappPathsIter = webappPaths.iterator();
			while (webappPathsIter.hasNext())
			{
				String path = webappPathsIter.next();
				try
				{
					final URL url = servletContext.getResource(path + pathname);
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
		}

		return null;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[folders = " + StringList.valueOf(folders) + ", webapppaths: " +
			StringList.valueOf(webappPaths) + "]";
	}

	/* package private for test in 1.4 only */
	final void addToWebPath(String path)
	{
		webappPaths.add(path);
	}
}
