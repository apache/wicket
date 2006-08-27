/*
 * $Id: PackageResource.java 4898 2006-03-13 13:44:52 -0800 (Mon, 13 Mar 2006)
 * joco01 $ $Revision$ $Date: 2006-03-13 13:44:52 -0800 (Mon, 13 Mar
 * 2006) $
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

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.SharedResources;
import wicket.WicketRuntimeException;
import wicket.util.lang.PackageName;
import wicket.util.lang.Packages;
import wicket.util.resource.IResourceStream;
import wicket.util.string.Strings;

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
	 * Gets non-localized resources for a given set of criteria. Multiple
	 * resource can be loaded for the same criteria if they match the pattern.
	 * If no resources were found, this method returns null.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading
	 *            the package resource, and to determine what package it is in.
	 *            Typically this is the calling class/ the class in which you
	 *            call this method
	 * @param pattern
	 *            Regexp pattern to match resources
	 * @return The resources, never null but may be empty
	 * @deprecated Will be removed in 2.0; contribute resources one by one
	 *             instead
	 */
	public static PackageResource[] get(Class scope, Pattern pattern)
	{
		return get(scope, pattern, false);
	}

	/**
	 * Gets non-localized resources for a given set of criteria. Multiple
	 * resource can be loaded for the same criteria if they match the pattern.
	 * If no resources were found, this method returns null.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading
	 *            the package resource, and to determine what package it is in.
	 *            Typically this is the calling class/ the class in which you
	 *            call this method
	 * @param pattern
	 *            Regexp pattern to match resources
	 * @param recurse
	 *            Whether this method should recurse into sub packages
	 * @return The resources, never null but may be empty
	 * @deprecated Will be removed in 2.0; contribute resources one by one
	 *             instead
	 */
	public static PackageResource[] get(Class scope, Pattern pattern, boolean recurse)
	{
		final List resources = new ArrayList();
		String packageRef = Strings.replaceAll(PackageName.forClass(scope).getName(), ".", "/")
				.toString();
		ClassLoader loader = scope.getClassLoader();
		try
		{
			// loop through the resources of the package
			Enumeration packageResources = loader.getResources(packageRef);
			while (packageResources.hasMoreElements())
			{
				URL resource = (URL)packageResources.nextElement();
				URLConnection connection = resource.openConnection();
				if (connection instanceof JarURLConnection)
				{
					JarFile jf = ((JarURLConnection)connection).getJarFile();
					scanJarFile(scope, pattern, recurse, resources, packageRef, jf);
				}
				else
				{
					String absolutePath = scope.getResource("").toExternalForm();
					File basedir;
					URI uri;
					try
					{
						uri = new URI(absolutePath);
					}
					catch (URISyntaxException e)
					{
						throw new RuntimeException(e);
					}
					try
					{
						basedir = new File(uri);
					}
					catch (IllegalArgumentException e)
					{
						log.debug("Can't construct the uri as a file: " + absolutePath);
						// if this is throwen then the path is not really a
						// file. but could be a zip.
						String jarZipPart = uri.getSchemeSpecificPart();
						// lowercased for testing if jar/zip, but leave the real
						// filespec unchanged
						String lowerJarZipPart = jarZipPart.toLowerCase();
						int index = lowerJarZipPart.indexOf(".zip");
						if (index == -1)
							index = lowerJarZipPart.indexOf(".jar");
						if (index == -1)
							throw e;

						String filename = jarZipPart.substring(0, index + 4); // 4 =
						// len
						// of
						// ".jar"
						// or
						// ".zip"
						log.debug("trying the filename: " + filename + " to load as a zip/jar.");
						JarFile jarFile = new JarFile(filename, false);
						scanJarFile(scope, pattern, recurse, resources, packageRef, jarFile);
						return (PackageResource[])resources.toArray(new PackageResource[resources
								.size()]);
					}
					if (!basedir.isDirectory())
					{
						throw new IllegalStateException("unable to read resources from directory "
								+ basedir);
					}
					// should not be necesarry anymore
					// addResources(scope, pattern, resources, new
					// StringBuffer(""), basedir, recurse);
				}
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}

		return (PackageResource[])resources.toArray(new PackageResource[resources.size()]);
	}

	/* removed in 2.0 */
	private static void scanJarFile(Class scope, Pattern pattern, boolean recurse,
			final List resources, String packageRef, JarFile jf)
	{
		Enumeration enumeration = jf.entries();
		while (enumeration.hasMoreElements())
		{
			JarEntry je = (JarEntry)enumeration.nextElement();
			String name = je.getName();
			if (name.startsWith(packageRef))
			{
				name = name.substring(packageRef.length() + 1);
				if (pattern.matcher(name).matches() && (recurse || (name.indexOf('/') == -1)))
				{
					// we add the entry as a package resource
					resources.add(get(scope, name, null, null));
				}
			}
		}
	}

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

	/**
	 * common extension pattern for css files; matches all files with extension
	 * 'css'.
	 * 
	 * @deprecated Will be removed in 2.0; contribute resources one by one
	 *             instead
	 */
	public static final Pattern EXTENSION_CSS = Pattern.compile(".*\\.css");

	/**
	 * common extension pattern for javascript files; matches all files with
	 * extension 'js'.
	 * 
	 * @deprecated Will be removed in 2.0; contribute resources one by one
	 *             instead
	 */
	public static final Pattern EXTENSION_JS = Pattern.compile(".*\\.js");

	/** log. */
	private static final Log log = LogFactory.getLog(PackageResource.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Binds the resources that match the provided pattern to the given
	 * application object. Will create any resources if not already in the
	 * shared resources of the application object.
	 * 
	 * @param application
	 *            The application to bind to.
	 * @param scope
	 *            The scope of the resource.
	 * @param pattern
	 *            A regular expression to match against the contents of the
	 *            package of the provided scope class (eg &quot;.*\\.js&quot;
	 *            will add all the files with extension &quot;js&quot; from that
	 *            package).
	 * 
	 * @deprecated Since Wicket 1.2.1 this method is effectively a no-op.
	 *             {@link PackageResource package resources} are automatically
	 *             tried and bound as shared resources so that they don't have
	 *             to be pre-registered anymore. Will be removed in 2.0
	 */
	public static void bind(Application application, Class scope, Pattern pattern)
	{
	}

	/**
	 * Binds the resources that match the provided pattern to the given
	 * application object. Will create any resources if not already in the
	 * shared resources of the application object and does that recursively when
	 * the recurse parameter is true, or just for the scoped package if that
	 * parameter is false
	 * 
	 * @param application
	 *            The application to bind to.
	 * @param scope
	 *            The scope of the resource.
	 * @param pattern
	 *            A regular expression to match against the contents of the
	 *            package of the provided scope class (eg &quot;.*\\.js&quot;
	 *            will add all the files with extension &quot;js&quot; from that
	 *            package).
	 * @param recurse
	 *            Whether this method should recurse into sub packages
	 * 
	 * @deprecated Since Wicket 1.2.1 this method is effectively a no-op.
	 *             {@link PackageResource package resources} are automatically
	 *             tried and bound as shared resources so that they don't have
	 *             to be pre-registered anymore. Will be removed in 2.0
	 */
	public static void bind(Application application, Class scope, Pattern pattern, boolean recurse)
	{
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
	public IResourceStream getResourceStream()
	{
		// Locate resource
		IResourceStream resourceStream = Application.get().getResourceSettings()
				.getResourceStreamLocator().locate(scope, absolutePath, style, locale, null);

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
