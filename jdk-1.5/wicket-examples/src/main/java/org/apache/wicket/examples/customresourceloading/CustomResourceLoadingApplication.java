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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.UrlResourceStream;
import org.apache.wicket.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.util.resource.locator.ResourceStreamLocator;
import org.apache.wicket.util.string.Strings;


/**
 * Application class for the custom resource loading example.
 * 
 * @author Eelco Hillenius
 */
public class CustomResourceLoadingApplication extends WicketExampleApplication
{
	/** log. */
	private static final Log log = LogFactory.getLog(CustomResourceLoadingApplication.class);

	/**
	 * Custom implementation of {@link IResourceStreamLocator}.
	 */
	private final class CustomResourceStreamLocator extends ResourceStreamLocator
	{
		/**
		 * @see org.apache.wicket.util.resource.locator.ResourceStreamLocator#locate(java.lang.Class,
		 *      java.lang.String)
		 */
		public IResourceStream locate(Class clazz, String path)
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
	public Class getHomePage()
	{
		return Index.class;
	}

	/**
	 * @see WebApplication#init()
	 */
	protected void init()
	{
		// instruct the application to use our custom resource stream locator
		getResourceSettings().setResourceStreamLocator(new CustomResourceStreamLocator());
	}
}
