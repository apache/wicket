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
package wicket.markup.html;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.AbortException;
import wicket.Application;
import wicket.RequestCycle;
import wicket.SharedResources;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.servlet.AbortWithWebErrorCodeException;
import wicket.util.lang.Packages;
import wicket.util.resource.IResourceStream;

/**
 * Represents a localizable static resource.
 * <p>
 * Use like eg:
 * 
 * <pre>
 * PackageResource IMG_UNKNOWN = PackageResource.get(EditPage.class, &quot;questionmark.gif&quot;);
 * </pre>
 * 
 * where the static resource references image 'questionmark.gif' from the the
 * package that EditPage is in to get a package resource.
 * </p>
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public class PackageResource extends WebResource
{
	/**
	 * Exception thrown when the creation of a package resource is not allowed.
	 */
	public static final class PackageResourceBlockedException extends WicketRuntimeException
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param message
		 */
		public PackageResourceBlockedException(String message)
		{
			super(message);
		}
	}

	/** log. */
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(PackageResource.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Binds a resource to the given application object. Will create the
	 * resource if not already in the shared resources of the application
	 * object.
	 * 
	 * @param application
	 *            The application to bind to.
	 * @param scope
	 *            The scope of the resource.
	 * @param name
	 *            The name of the resource (like &quot;myfile.js&quot;)
	 * @throw IllegalArgumentException when the requested package resource was
	 *        not found
	 */
	public static void bind(Application application, Class scope, String name)
	{
		bind(application, scope, name, null, null);
	}

	/**
	 * Binds a resource to the given application object. Will create the
	 * resource if not already in the shared resources of the application
	 * object.
	 * 
	 * @param application
	 *            The application to bind to.
	 * @param scope
	 *            The scope of the resource.
	 * @param name
	 *            The name of the resource (like &quot;myfile.js&quot;)
	 * @param locale
	 *            The locale of the resource.
	 * @throw IllegalArgumentException when the requested package resource was
	 *        not found
	 */
	public static void bind(Application application, Class scope, String name, Locale locale)
	{
		bind(application, scope, name, locale, null);
	}

	/**
	 * Binds a resource to the given application object. Will create the
	 * resource if not already in the shared resources of the application
	 * object.
	 * 
	 * @param application
	 *            The application to bind to.
	 * @param scope
	 *            The scope of the resource.
	 * @param name
	 *            The name of the resource (like &quot;myfile.js&quot;)
	 * @param locale
	 *            The locale of the resource.
	 * @param style
	 *            The style of the resource.
	 * @throw IllegalArgumentException when the requested package resource was
	 *        not found
	 */
	public static void bind(Application application, Class scope, String name, Locale locale,
			String style)
	{
		if (name == null)
		{
			throw new IllegalArgumentException("argument name may not be null");
		}

		// first check on a direct hit for efficiency
		if (exists(scope, name, locale, style))
		{
			// we have got a hit, so we may safely assume the name
			// argument is not a regular expression, and can thus
			// just add the resource and return
			get(scope, name, locale, style);
		}
		else
		{
			throw new IllegalArgumentException("no package resource was found for scope " + scope
					+ ", name " + name + ", locale " + locale + ", style " + style);
		}
	}

	/**
	 * Gets whether a resource for a given set of criteria exists.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading
	 *            the package resource, and to determine what package it is in.
	 *            Typically this is the class in which you call this method
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource (see {@link wicket.Session})
	 * @return true if a resource could be loaded, false otherwise
	 */
	public static boolean exists(final Class scope, final String path, final Locale locale,
			final String style)
	{
		String absolutePath = Packages.absolutePath(scope, path);
		return Application.get().getResourceSettings().getResourceStreamLocator().locate(scope,
				absolutePath, style, locale, null) != null;
	}

	/**
	 * Gets a non-localized resource for a given set of criteria. Only one
	 * resource will be loaded for the same criteria.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading
	 *            the package resource, and to determine what package it is in.
	 *            Typically this is the calling class/ the class in which you
	 *            call this method
	 * @param path
	 *            The path to the resource
	 * @return The resource
	 * @throws PackageResourceBlockedException
	 *             when the target resource is not accepted by
	 *             {@link IPackageResourceGuard the package resource guard}.
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
	 *            This argument will be used to get the class loader for loading
	 *            the package resource, and to determine what package it is in.
	 *            Typically this is the class in which you call this method
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource (see {@link wicket.Session})
	 * @return The resource
	 * @throws PackageResourceBlockedException
	 *             when the target resource is not accepted by
	 *             {@link IPackageResourceGuard the package resource guard}.
	 */
	public static PackageResource get(final Class scope, final String path, final Locale locale,
			final String style)
	{
		final SharedResources sharedResources = Application.get().getSharedResources();
		PackageResource resource = (PackageResource)sharedResources.get(scope, path, locale, style,
				true);
		if (resource == null)
		{
			resource = new PackageResource(scope, path, locale, style);
			sharedResources.add(scope, path, locale, style, resource);
		}
		return resource;
	}

	/** The path to the resource */
	private final String absolutePath;

	/** The resource's locale */
	private Locale locale;

	/** The path this resource was created with. */
	private final String path;

	/** The scoping class, used for class loading and to determine the package. */
	private final Class scope;

	/** The resource's style */
	private final String style;

	/**
	 * Hidden constructor.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading
	 *            the package resource, and to determine what package it is in
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource
	 * @throws PackageResourceBlockedException
	 *             when the target resource is not accepted by
	 *             {@link IPackageResourceGuard the package resource guard}.
	 */
	protected PackageResource(final Class scope, final String path, final Locale locale,
			final String style)
	{
		// Convert resource path to absolute path relative to base package
		this.absolutePath = Packages.absolutePath(scope, path);

		IPackageResourceGuard guard = Application.get().getResourceSettings()
				.getPackageResourceGuard();
		if (!guard.accept(scope, path))
		{
			throw new PackageResourceBlockedException("package resource " + absolutePath
					+ " may not be accessed");
		}

		this.scope = scope;
		this.path = path;
		this.locale = locale;
		this.style = style;

		if (locale != null)
		{
			// Get the resource stream so that the real locale that could be
			// resolved is set.
			getResourceStream();

			// Invalidate it again so that it won't hold up resources
			invalidate();
		}
	}

	/**
	 * Gets the absolute path of the resource.
	 * 
	 * @return the absolute resource path
	 */
	public final String getAbsolutePath()
	{
		return absolutePath;
	}

	/**
	 * Gets the locale.
	 * 
	 * @return The Locale of this package resource
	 */
	public final Locale getLocale()
	{
		return locale;
	}

	/**
	 * Gets the path this resource was created with.
	 * 
	 * @return the path
	 */
	public final String getPath()
	{
		return path;
	}

	/**
	 * @return Gets the resource for the component.
	 */
	@Override
	public IResourceStream getResourceStream()
	{
		// Locate resource
		IResourceStream resourceStream = Application.get().getResourceSettings()
				.getResourceStreamLocator().locate(scope, absolutePath, style, locale, null);

		// Check that resource was found
		if (resourceStream == null)
		{
			String msg = "Unable to find package resource [path = " + absolutePath + ", style = "
					+ style + ", locale = " + locale + "]";
			log.warn(msg);
			RequestCycle requestCycle = RequestCycle.get();
			if (requestCycle instanceof WebRequestCycle)
			{
				throw new AbortWithWebErrorCodeException(HttpServletResponse.SC_NOT_FOUND, msg);
			}
			else
			{
				throw new AbortException();
			}
		}
		this.locale = resourceStream.getLocale();
		return resourceStream;
	}

	/**
	 * Gets the scoping class, used for class loading and to determine the
	 * package.
	 * 
	 * @return the scoping class
	 */
	public final Class getScope()
	{
		return scope;
	}

	/**
	 * Gets the style.
	 * 
	 * @return the style
	 */
	public final String getStyle()
	{
		return style;
	}
}
