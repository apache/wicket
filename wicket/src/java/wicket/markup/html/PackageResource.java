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
import wicket.Resource;
import wicket.SharedResources;
import wicket.WicketRuntimeException;
import wicket.util.lang.Packages;
import wicket.util.resource.IResourceStream;

/**
 * Represents a localizable static resource.
 * <p>
 * Use like eg:
 * <pre>
 * private static final PackageResource IMG_UNKNOWN =
 * 		PackageResource.get(EditPage.class, "questionmark.gif");
 * </pre>
 * where the static resource references image 'questionmark.gif' from the
 * the package that EditPage is in. 
 * </p>
 * 
 * @author Jonathan Locke
 */
public class PackageResource extends WebResource
{
	/** Map from key to resource */
	private static Map resourceMap = new HashMap();

	/** The path to the resource */
	final String absolutePath;

	/** The resource's locale */
	private Locale locale;

	/** The resource's style */
	final String style;

	/** The scoping class, used for class loading and to determine the package. */
	final Class scope;

	/**
	 * Binds a the resource to the given application object
	 * Will create the resource if not already in the shared resources of the application object.
	 * 
	 * @param application
	 * 			The application to bind to.
	 * @param scope
	 * 			The scope of the resource.
	 * @param name
	 * 			The name of the resource.
	 * @param locale
	 * 			The locale of the resource.
	 * @param style
	 * 			The style of the resource.
	 */
	public static void bind(Application application, Class scope, String name, Locale locale, String style)
	{
		Resource resource = application.getSharedResources().get(scope, name, locale, style, true);
		// Not available yet?
		if (resource == null)
		{
			// Share through application
			resource = get(scope, name, locale, style);
			application.getSharedResources().add(scope, name, ((PackageResource)resource).locale, style, resource);
		}
	}

	/**
	 * Binds a the resource to the given application object
	 * Will create the resource if not already in the shared resources of the application object.
	 * 
	 * @param application
	 * 			The application to bind to.
	 * @param scope
	 * 			The scope of the resource.
	 * @param name
	 * 			The name of the resource.
	 */
	public static void bind(Application application, Class scope, String name)
	{
		bind(application, scope, name, null, null);
	}
	
	/**
	 * Gets a non-localized resource for a given set of criteria. Only one resource
	 * will be loaded for the same criteria.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading the
	 *            package resource, and to determine what package it is in. Typically
	 *            this is the calling class/ the class in which you call this method
	 * @param path
	 *            The path to the resource
	 * @return The resource
	 */
	public static PackageResource get(final Class scope, final String path)
	{
		return get(scope, path, null, null);
	}

	/**
	 * Gets the resource for a given set of criteria. Only one resource will be
	 * loaded for the same criteria.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading the
	 *            package resource, and to determine what package it is in. Typically
	 *            this is the calling class/ the class in which you call this method
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource (see {@link wicket.Session})
	 * @return The resource
	 */
	public static PackageResource get(final Class scope, final String path,
			final Locale locale, final String style)
	{
		final String key = scope.getPackage().getName() + '/' + SharedResources.path(path, locale, style);
		synchronized (resourceMap)
		{
			PackageResource resource = (PackageResource)resourceMap.get(key);
			if (resource == null)
			{
				resource = new PackageResource(scope, path, locale, style);
				resourceMap.put(key, resource);
			}
			return resource;
		}
	}

	/**
	 * Hidden constructor.
	 *
	 * @param scope
	 *            This argument will be used to get the class loader for loading the
	 *            package resource, and to determine what package it is in
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource
	 */
	private PackageResource(final Class scope, final String path, final Locale locale,
			final String style)
	{
		this.scope = scope;
		// Convert resource path to absolute path relative to base package
		this.absolutePath = Packages.absolutePath(scope.getPackage(), path);
		this.locale = locale;
		this.style = style;
		
		if(locale != null)
		{
			// get the resource stream so that the real locale that could be resolved is set.
			getResourceStream();
			// invalidate it again so that it won't hold up resources
			invalidate();
		}
	}

	/**
	 * @return Gets the resource for the component.
	 */
	public IResourceStream getResourceStream()
	{
		// Locate resource
		IResourceStream resourceStream = Application.get().getResourceStreamLocator().locate(
				scope.getClassLoader(), absolutePath, style, locale, null);

		// Check that resource was found
		if (resourceStream == null)
		{
			throw new WicketRuntimeException("Unable to find package resource [path = "
					+ absolutePath + ", style = " + style + ", locale = " + locale + "]");
		}
		this.locale = resourceStream.getLocale();
		return resourceStream;
	}

	/**
	 * @return The Locale of this package resource 
	 */
	public Locale getLocale()
	{
		return locale;
	}
}
