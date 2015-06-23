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
package org.apache.wicket.arquillian.testing.util;

import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.resource.IResourceStream;


/**
 * Maintain a list of paths which might either be ordinary folders of the filesystem or relative
 * paths to the web application's servlet context.
 * 
 * @author Johan Compagner
 * 
 * @author felipecalmeida
 * 		Modified to look inside servletContext and same package as Application.
 */
public final class ResourceWebApplicationPath implements IResourceFinder
{
	private static final String WEB_INF = "WEB-INF/";

	/** The web apps servlet context */
	private final ServletContext servletContext;
	
	/** basePath of the project org/apache/wicket/arquillian/testing **/
	private String basePath;

	/**
	 * Constructor
	 * 
	 * @param servletContext
	 *            The webapplication context where the resources must be loaded from
	 */
	public ResourceWebApplicationPath(String basePath,final ServletContext servletContext)
	{
		this.basePath = basePath.replaceAll("\\.", "\\/");
		this.servletContext = servletContext;
	}

	/**
	 * 
	 * @see org.apache.wicket.util.file.IResourceFinder#find(Class, String)
	 */
	public IResourceStream find(final Class<?> clazz, final String pathname)
	{

		if (pathname.startsWith(WEB_INF) == false)
		{
			try
			{
				final URL url = servletContext.getResource(pathname.replaceFirst(basePath, ""));
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
		return "[webapppath: " + basePath + "]";
	}
}
