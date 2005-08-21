/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.markup.html;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import wicket.Application;
import wicket.RequestCycle;
import wicket.SharedResources;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebApplication;
import wicket.util.resource.IResourceStream;

/**
 * Represents a localizable static resource.
 * <p>
 * Use like eg:
 * <pre>
 * private static final StaticResource IMG_UNKNOWN =
 * 		StaticResource.get("path", "questionmark.gif");
 * </pre>
 * where the static resource references image 'questionmark.gif' from the
 * the package that EditPage is in. 
 * </p>
 * 
 * @author Jonathan Locke
 */
public class StaticFileResource extends WebResource
{
	/** Map from key to resource */
	private static Map resourceMap = new HashMap();

	/** The path to the resource */
	final String absolutePath;

	/** The resource's locale */
	final Locale locale;

	/** The resource's style */
	final String style;

	/** the application to use for getting the resource stream */
	private transient Application application;

	/**
	 * Gets a non-localized resource for a given set of criteria. Only one resource
	 * will be loaded for the same criteria.
	 * 
	 * @param path
	 *            The path to the resource
	 * @return The resource
	 */
	public static StaticFileResource get(final String path)
	{
		return get(path, null, null);
	}

	/**
	 * Gets the resource for a given set of criteria. Only one resource will be
	 * loaded for the same criteria.
	 * 
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource (see {@link wicket.Session})
	 * @return The resource
	 */
	public static StaticFileResource get(final String path,
			final Locale locale, final String style)
	{
		final String key = SharedResources.path(path, locale, style);
		synchronized (resourceMap)
		{
			StaticFileResource resource = (StaticFileResource)resourceMap.get(key);
			if (resource == null)
			{
				resource = new StaticFileResource(path, locale, style);
				resourceMap.put(key, resource);
			}
			return resource;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource
	 */
	private StaticFileResource(final String path, final Locale locale,
			final String style)
	{
		// Convert resource path to absolute path relative to base package
		this.absolutePath = SharedResources.path(path, locale, style);
		this.locale = locale;
		this.style = style;
		this.application = RequestCycle.get().getApplication();
	}

	/**
	 * @return Gets the resource for the component.
	 */
	public IResourceStream getResourceStream()
	{
		if (resourceStream == null)
		{
			// Locate resource
			this.resourceStream = application.getResourceStreamLocator().locate(
					getClass().getClassLoader(), absolutePath, style, locale, null);

			// Check that resource was found
			if (this.resourceStream == null)
			{
				throw new WicketRuntimeException("Unable to find static resource [path = "
						+ absolutePath + ", style = " + style + ", locale = " + locale + "]");
			}
		}
		return resourceStream;
	}

	/**
	 * set the application object on this resource.
	 * @param webApplication
	 */
	public void setApplication(WebApplication webApplication)
	{
		this.application = webApplication;
	}
}
