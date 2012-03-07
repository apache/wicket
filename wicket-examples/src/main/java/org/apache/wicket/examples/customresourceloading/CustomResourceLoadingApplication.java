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
package org.apache.wicket.examples.customresourceloading;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.core.util.resource.locator.ResourceStreamLocator;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application class for the custom resource loading example.
 * 
 * @author Eelco Hillenius
 */
public class CustomResourceLoadingApplication extends WicketExampleApplication
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(CustomResourceLoadingApplication.class);

	/**
	 * Custom implementation of {@link IResourceStreamLocator}.
	 */
	private final class CustomResourceStreamLocator extends ResourceStreamLocator
	{
		/**
		 * @see org.apache.wicket.core.util.resource.locator.ResourceStreamLocator#locate(java.lang.Class,
		 *      java.lang.String)
		 */
		@Override
		public IResourceStream locate(Class<?> clazz, String path)
		{
			// Log attempt
			if (log.isDebugEnabled())
			{
				log.debug("Attempting to locate resource '" + path +
					"' using classloader the servlet context");
			}

			String location;
			if (clazz == AlternativePageFromWebContext.class)
			{
				// this custom case disregards the path and tries it's own
				// scheme
				String extension = path.substring(path.lastIndexOf('.') + 1);
				String simpleFileName = Strings.lastPathComponent(clazz.getName(), '.');
				location = "/WEB-INF/templates/" + simpleFileName + "." + extension;
			}
			else
			{
				// use the normal package to path conversion of the passed in
				// path variable
				location = "/WEB-INF/templates/" + path;
			}
			URL url;
			try
			{
				// try to load the resource from the web context
				url = getServletContext().getResource(location);
				if (url != null)
				{
					return new UrlResourceStream(url);
				}
			}
			catch (MalformedURLException e)
			{
				throw new WicketRuntimeException(e);
			}

			// resource not found; fall back on class loading
			return super.locate(clazz, path);
		}

	}

	/**
	 * Constructor.
	 */
	public CustomResourceLoadingApplication()
	{
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return Index.class;
	}

	/**
	 * @see WebApplication#init()
	 */
	@Override
	protected void init()
	{
		super.init();

		getResourceSettings().setResourceStreamLocator(new CustomResourceStreamLocator());
	}
}
