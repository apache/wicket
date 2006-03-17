/*
 * $Id: HelloWorldApplication.java 3646 2006-01-04 13:32:14 -0800 (Wed, 04 Jan
 * 2006) ivaynberg $ $Revision: 3646 $ $Date: 2006-01-04 13:32:14 -0800 (Wed, 04
 * Jan 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.customresourceloading;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.WicketRuntimeException;
import wicket.examples.WicketExampleApplication;
import wicket.protocol.http.WebApplication;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.UrlResourceStream;
import wicket.util.resource.locator.ClassLoaderResourceStreamLocator;
import wicket.util.resource.locator.IResourceStreamLocator;
import wicket.util.string.Strings;

/**
 * Application class for the custom resource loading example.
 * 
 * @author Eelco Hillenius
 */
public class CustomResourceLoadingApplication extends WicketExampleApplication
{
	/** log. */
	private static Log log = LogFactory.getLog(CustomResourceLoadingApplication.class);

	/**
	 * Custom implementation of {@link IResourceStreamLocator}.
	 */
	private final class CustomResourceStreamLocator implements IResourceStreamLocator
	{
		/**
		 * Locator to fallback on. Always a good idea to do this, because
		 * packaged custom components etc <strong>will</strong> depend in this
		 * loading.
		 */
		private ClassLoaderResourceStreamLocator classLoaderLocator = new ClassLoaderResourceStreamLocator();

		/**
		 * @see wicket.util.resource.locator.IResourceStreamLocator#locate(java.lang.Class,
		 *      java.lang.String, java.lang.String, java.util.Locale,
		 *      java.lang.String)
		 */
		public IResourceStream locate(Class clazz, String path, String style, Locale locale,
				String extension)
		{
			// Log attempt
			if (log.isDebugEnabled())
			{
				log.debug("Attempting to locate resource '" + path
						+ "' using classloader the servlet context");
			}

			String location;
			if (clazz == AlternativePageFromWebContext.class)
			{
				// this custom case disregards the path and tries it's own
				// scheme
				location = "/WEB-INF/templates/"
						+ Strings.lastPathComponent(AlternativePageFromWebContext.class.getName(),
								'.') + "." + extension;
			}
			else
			{
				// use the normal package to path conversion of the passed in
				// path variable
				location = "/WEB-INF/templates/" + path + "." + extension;
			}
			URL url;
			try
			{
				// try to load the resource from the web context
				url = getWicketServlet().getServletContext().getResource(location);
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
			return classLoaderLocator.locate(clazz, path, style, locale, extension);
		}

	}

	/**
	 * Constructor.
	 */
	public CustomResourceLoadingApplication()
	{
	}

	/**
	 * @see wicket.Application#getHomePage()
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
