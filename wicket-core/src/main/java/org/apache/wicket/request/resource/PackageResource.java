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
package org.apache.wicket.request.resource;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.lang.WicketObjects;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a localizable static resource.
 * <p>
 * Use like eg:
 * 
 * <pre>
 * MyPackageResource IMG_UNKNOWN = new MyPackageResource(EditPage.class, &quot;questionmark.gif&quot;);
 * </pre>
 * 
 * where the static resource references image 'questionmark.gif' from the the package that EditPage
 * is in to get a package resource.
 * </p>
 * 
 * Access to resources can be granted or denied via a {@link IPackageResourceGuard}. Please see
 * {@link IResourceSettings#getPackageResourceGuard()} as well.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Matej Knopp
 */
public class PackageResource extends AbstractResource
{
	private static final Logger log = LoggerFactory.getLogger(PackageResource.class);

	private static final long serialVersionUID = 1L;

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
		 *            error message
		 */
		public PackageResourceBlockedException(String message)
		{
			super(message);
		}
	}

	/**
	 * The path to the resource
	 */
	private final String absolutePath;

	/**
	 * The resource's locale
	 */
	private final Locale locale;

	/**
	 * The path this resource was created with.
	 */
	private final String path;

	/**
	 * The scoping class, used for class loading and to determine the package.
	 */
	private final String scopeName;

	/**
	 * The resource's style
	 */
	private final String style;

	/**
	 * The component's variation (of the style)
	 */
	private final String variation;


	/**
	 * Hidden constructor.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading the package
	 *            resource, and to determine what package it is in
	 * @param name
	 *            The relative path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource
	 * @param variation
	 *            The component's variation (of the style)
	 */
	protected PackageResource(final Class<?> scope, final String name, final Locale locale,
		final String style, final String variation)
	{
		// Convert resource path to absolute path relative to base package
		absolutePath = Packages.absolutePath(scope, name);

		final String parentEscape = Application.get()
			.getResourceSettings()
			.getParentFolderPlaceholder();

		if (Strings.isEmpty(parentEscape) == false)
		{
			path = Strings.replaceAll(name, "../", parentEscape + "/").toString();
		}
		else
		{
			path = name;
		}

		if (!accept(scope, path))
		{
			throw new PackageResourceBlockedException(
				"Access denied to (static) package resource " + absolutePath +
					". See IPackageResourceGuard");
		}

		scopeName = scope.getName();
		this.locale = locale;
		this.style = style;
		this.variation = variation;
	}

	/**
	 * Gets the scoping class, used for class loading and to determine the package.
	 * 
	 * @return the scoping class
	 */
	public final Class<?> getScope()
	{
		return WicketObjects.resolveClass(scopeName);
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

	/**
	 * creates a new resource response based on the request attributes
	 * 
	 * @param attributes
	 *            current request attributes from client
	 * @return resource response for answering request
	 */
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes)
	{
		final ResourceResponse resourceResponse = new ResourceResponse();

		if (resourceResponse.dataNeedsToBeWritten(attributes))
		{
			// get resource stream
			final IResourceStream resourceStream = getResourceStream();

			// bail out if resource stream could not be found
			if (resourceStream == null)
				return sendResourceError(resourceResponse, HttpServletResponse.SC_NOT_FOUND,
					"Unable to find resource");

			// set Content-Type (may be null)
			resourceResponse.setContentType(resourceStream.getContentType());

			// add Last-Modified header (to support HEAD requests and If-Modified-Since)
			final Time lastModified = resourceStream.lastModifiedTime();

			if (lastModified != null)
				resourceResponse.setLastModified(lastModified);

			try
			{
				// read resource data
				final byte[] bytes;

				try
				{
					bytes = IOUtils.toByteArray(resourceStream.getInputStream());
				}
				finally
				{
					resourceStream.close();
				}

				final byte[] processed = processResponse(attributes, bytes);

				// send Content-Length header
				resourceResponse.setContentLength(processed.length);

				// send response body with resource data
				resourceResponse.setWriteCallback(new WriteCallback()
				{
					@Override
					public void writeData(Attributes attributes)
					{
						attributes.getResponse().write(processed);
					}
				});
			}
			catch (IOException e)
			{
				log.debug(e.getMessage(), e);
				return sendResourceError(resourceResponse, 500, "Unable to read resource stream");
			}
			catch (ResourceStreamNotFoundException e)
			{
				log.debug(e.getMessage(), e);
				return sendResourceError(resourceResponse, 500, "Unable to open resource stream");
			}
		}

		// modify the resource response depending on the current caching strategy needs
		Application.get()
			.getResourceSettings()
			.getCachingStrategy()
			.decorateResponse(resourceResponse);

		return resourceResponse;
	}

	/**
	 * Gives a chance to modify the resource going to be written in the response
	 * 
	 * @param attributes
	 *            current request attributes from client
	 * @param original
	 *            the original response
	 * @return the processed response
	 */
	protected byte[] processResponse(final Attributes attributes, final byte[] original)
	{
		return original;
	}

	/**
	 * send resource specific error message and write log entry
	 * 
	 * @param resourceResponse
	 *            resource response
	 * @param errorCode
	 *            error code (=http status)
	 * @param errorMessage
	 *            error message (=http error message)
	 * @return resource response for method chaining
	 */
	private ResourceResponse sendResourceError(ResourceResponse resourceResponse, int errorCode,
		String errorMessage)
	{
		String msg = String.format(
			"resource [path = %s, style = %s, variation = %s, locale = %s]: %s (status=%d)",
			absolutePath, style, variation, locale, errorMessage, errorCode);

		log.warn(msg);

		resourceResponse.setError(errorCode, errorMessage);
		return resourceResponse;
	}

	/**
	 * locate resource stream for current resource
	 * 
	 * @return resource stream or <code>null</code> if not found
	 */
	protected IResourceStream getResourceStream()
	{
		// Locate resource
		return ThreadContext.getApplication()
			.getResourceSettings()
			.getResourceStreamLocator()
			.locate(getScope(), absolutePath, style, variation, locale, null, false);
	}

	/**
	 * @param scope
	 *            resource scope
	 * @param path
	 *            resource path
	 * @return <code>true<code> if resource access is granted
	 */
	private boolean accept(Class<?> scope, String path)
	{
		IPackageResourceGuard guard = ThreadContext.getApplication()
			.getResourceSettings()
			.getPackageResourceGuard();

		return guard.accept(scope, path);
	}

	/**
	 * Gets whether a resource for a given set of criteria exists.
	 * 
	 * @param scope
	 *            This argument will be used to get the class loader for loading the package
	 *            resource, and to determine what package it is in. Typically this is the class in
	 *            which you call this method
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The style of the resource (see {@link org.apache.wicket.Session})
	 * @param variation
	 *            The component's variation (of the style)
	 * @return true if a resource could be loaded, false otherwise
	 */
	public static boolean exists(final Class<?> scope, final String path, final Locale locale,
		final String style, final String variation)
	{
		String absolutePath = Packages.absolutePath(scope, path);
		return ThreadContext.getApplication()
			.getResourceSettings()
			.getResourceStreamLocator()
			.locate(scope, absolutePath, style, variation, locale, null, false) != null;
	}

	@Override
	public String toString()
	{
		final StringBuilder result = new StringBuilder();
		result.append('[')
			.append(getClass().getSimpleName())
			.append(' ')
			.append("name = ")
			.append(path)
			.append(", scope = ")
			.append(scopeName)
			.append(", locale = ")
			.append(locale)
			.append(", style = ")
			.append(style)
			.append(", variation = ")
			.append(variation)
			.append(']');
		return result.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((absolutePath == null) ? 0 : absolutePath.hashCode());
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((scopeName == null) ? 0 : scopeName.hashCode());
		result = prime * result + ((style == null) ? 0 : style.hashCode());
		result = prime * result + ((variation == null) ? 0 : variation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PackageResource other = (PackageResource)obj;
		if (absolutePath == null)
		{
			if (other.absolutePath != null)
				return false;
		}
		else if (!absolutePath.equals(other.absolutePath))
			return false;
		if (locale == null)
		{
			if (other.locale != null)
				return false;
		}
		else if (!locale.equals(other.locale))
			return false;
		if (path == null)
		{
			if (other.path != null)
				return false;
		}
		else if (!path.equals(other.path))
			return false;
		if (scopeName == null)
		{
			if (other.scopeName != null)
				return false;
		}
		else if (!scopeName.equals(other.scopeName))
			return false;
		if (style == null)
		{
			if (other.style != null)
				return false;
		}
		else if (!style.equals(other.style))
			return false;
		if (variation == null)
		{
			if (other.variation != null)
				return false;
		}
		else if (!variation.equals(other.variation))
			return false;
		return true;
	}


}
