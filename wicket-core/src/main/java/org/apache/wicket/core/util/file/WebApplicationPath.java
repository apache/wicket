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

import javax.servlet.ServletContext;

import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.resource.IResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An {@link IResourceFinder} that looks in a folder in the webapp context path. It will
 * <em>not</em> load files inside WEB-INF.
 * 
 * @author Johan Compagner
 * @author Carl-Eric Menzel
 */
public final class WebApplicationPath implements IResourceFinder
{
	private final static Logger log = LoggerFactory.getLogger(WebApplicationPath.class);

	private static final String WEB_INF = "WEB-INF/";

	/** The web apps servlet context */
	private final ServletContext servletContext;

	private final String path;

	/**
	 * Constructor
	 * 
	 * @param servletContext
	 *            The webapplication context where the resources must be loaded from
	 * @param path
	 *            The path inside the app context where to look.
	 */
	public WebApplicationPath(final ServletContext servletContext, String path)
	{
		this.servletContext = servletContext;
		if (!path.startsWith("/"))
		{
			path = "/" + path;
		}
		if (!path.endsWith("/"))
		{
			path += "/";
		}
		this.path = path;
	}


	/**
	 * 
	 * @see org.apache.wicket.util.file.IResourceFinder#find(Class, String)
	 */
	@Override
	public IResourceStream find(final Class<?> clazz, final String pathname)
	{
		IResourceStream resourceStream = null;
		if (pathname.startsWith(WEB_INF) == false)
		{
			try
			{
				final URL url = servletContext.getResource(path + pathname);
				if (url != null)
				{
					resourceStream = new UrlResourceStream(url);
				}
			}
			catch (Exception ex)
			{
				// ignore, file couldn't be found
			}
		}

		return resourceStream;
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[webapppath: " + path + "]";
	}
}
