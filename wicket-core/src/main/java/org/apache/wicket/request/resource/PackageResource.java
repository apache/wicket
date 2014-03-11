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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.resource.IFixedLocationResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.ResourceStreamWrapper;
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
 * {@link org.apache.wicket.settings.ResourceSettings#getPackageResourceGuard()} as well.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Matej Knopp
 */
public class PackageResource extends AbstractResource implements IStaticCacheableResource
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
	 * A flag indicating whether {@code ITextResourceCompressor} can be used to compress this resource.
	 * Default is {@code false} because this resource may be used for binary data (e.g. an image).
	 * Specializations of this class should change this flag appropriately.
	 */
	private boolean compress = false;

	/**
	 * controls whether {@link org.apache.wicket.request.resource.caching.IResourceCachingStrategy}
	 * should be applied to resource
	 */
	
	private boolean cachingEnabled = true;
	
	/**
	 * text encoding (may be null) - only makes sense for character-based resources
	 */
	
	private String textEncoding = null;
	
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

		final String parentEscape = getParentFolderPlaceholder();

		if (Strings.isEmpty(parentEscape) == false)
		{
			path = Strings.replaceAll(name, "../", parentEscape + "/").toString();
		}
		else
		{
			path = name;
		}

		this.scopeName = scope.getName();
		this.locale = locale;
		this.style = style;
		this.variation = variation;
	}

	private Locale getCurrentLocale()
	{
		return locale != null ? locale : Session.get().getLocale();
	}

	private String getCurrentStyle()
	{
		return style != null ? style : Session.get().getStyle();
	}

	@Override
	public boolean isCachingEnabled()
	{
		return cachingEnabled;
	}

	public void setCachingEnabled(final boolean enabled)
	{
		this.cachingEnabled = enabled;
	}

	/**
	 * get text encoding (intented for character-based resources)
	 
	 * @return custom encoding or {@code null} to use default
	 */
	public String getTextEncoding()
	{
		return textEncoding;
	}

	/**
	 * set text encoding (intented for character-based resources)
	 *
	 * @param textEncoding
	 *            custom encoding or {@code null} to use default
	 */
	public void setTextEncoding(final String textEncoding)
	{
		this.textEncoding = textEncoding;
	}

	@Override
	public Serializable getCacheKey()
	{
		IResourceStream stream = getCacheableResourceStream();

		// if resource stream can not be found do not cache
		if (stream == null)
		{
			return null;
		}

		return new CacheKey(scopeName, absolutePath, stream.getLocale(), stream.getStyle(),
			stream.getVariation());
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

		final IResourceStream resourceStream = getResourceStream();

		// bail out if resource stream could not be found
		if (resourceStream == null)
		{
			return sendResourceError(resourceResponse, HttpServletResponse.SC_NOT_FOUND,
					"Unable to find resource");
		}

		// add Last-Modified header (to support HEAD requests and If-Modified-Since)
		final Time lastModified = resourceStream.lastModifiedTime();

		resourceResponse.setLastModified(lastModified);

		if (resourceResponse.dataNeedsToBeWritten(attributes))
		{
			String contentType = resourceStream.getContentType();

			if (contentType == null && Application.exists())
			{
				contentType = Application.get().getMimeType(path);
			}

			// set Content-Type (may be null)
			resourceResponse.setContentType(contentType);
			
			// set content encoding (may be null)
			resourceResponse.setTextEncoding(getTextEncoding());

			try
			{
				// read resource data
				final byte[] bytes = IOUtils.toByteArray(resourceStream.getInputStream());

				// send Content-Length header
				resourceResponse.setContentLength(bytes.length);

				// send response body with resource data
				resourceResponse.setWriteCallback(new WriteCallback()
				{
					@Override
					public void writeData(Attributes attributes)
					{
						attributes.getResponse().write(bytes);
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
			finally
			{
				try
				{
					resourceStream.close();
				}
				catch (IOException e)
				{
					log.warn("Unable to close the resource stream", e);
				}
			}
		}

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
	 * be aware that method takes the current wicket session's locale and style into account when
	 * locating the stream.
	 *
	 * @return resource stream
	 *
	 * @see org.apache.wicket.request.resource.caching.IStaticCacheableResource#getCacheableResourceStream()
	 * @see #getResourceStream()
	 */
	@Override
	public IResourceStream getCacheableResourceStream()
	{
		return internalGetResourceStream(getCurrentStyle(), getCurrentLocale());
	}
	
	/**
	 * locate resource stream for current resource
	 * 
	 * @return resource stream or <code>null</code> if not found
	 */
	public IResourceStream getResourceStream()
	{
		return internalGetResourceStream(style, locale);
	}

	/**
	 * @return whether {@link org.apache.wicket.resource.ITextResourceCompressor} can be used to compress the
	 *         resource.
	 */
	public boolean getCompress()
	{
		return compress;
	}

	/**
	 * @param compress
	 *            A flag indicating whether the resource should be compressed.
	 */
	public void setCompress(boolean compress)
	{
		this.compress = compress;
	}

	private IResourceStream internalGetResourceStream(final String style, final Locale locale)
	{
		IResourceStreamLocator resourceStreamLocator = Application.get()
				.getResourceSettings()
				.getResourceStreamLocator();
		IResourceStream resourceStream = resourceStreamLocator.locate(getScope(), absolutePath, style, variation, locale, null, false);

		Class<?> realScope = getScope();
		String realPath = absolutePath;
		if (resourceStream instanceof IFixedLocationResourceStream)
		{
			realPath = ((IFixedLocationResourceStream)resourceStream).locationAsString();
			if (realPath != null)
			{
				int index = realPath.indexOf(absolutePath);
				if (index != -1)
				{
					realPath = realPath.substring(index);
				}
				else
				{
					// just fall back on the full path without a scope..
					realScope = null;
				}
			}
			else
			{
				realPath = absolutePath;
			}

		}

		if (accept(realScope, realPath) == false)
		{
			throw new PackageResourceBlockedException(
					"Access denied to (static) package resource " + absolutePath +
						". See IPackageResourceGuard");
		}

		if (resourceStream != null)
		{
			resourceStream = new ProcessingResourceStream(resourceStream);
		}
		return resourceStream;
	}

	/**
	 * An IResourceStream that processes the input stream of the original
	 * IResourceStream
	 */
	private class ProcessingResourceStream extends ResourceStreamWrapper
	{
		private ProcessingResourceStream(IResourceStream delegate)
		{
			super(delegate);
		}

		@Override
		public InputStream getInputStream() throws ResourceStreamNotFoundException
		{
			byte[] bytes;
			InputStream inputStream = super.getInputStream();
			try
			{
				bytes = IOUtils.toByteArray(inputStream);
			} catch (IOException iox)
			{
				throw new WicketRuntimeException(iox);
			}

			RequestCycle cycle = RequestCycle.get();
			Attributes attributes;
			if (cycle != null)
			{
				attributes = new Attributes(cycle.getRequest(), cycle.getResponse());
			}
			else
			{
				// use empty request and response in case of non-http thread. WICKET-5532
				attributes = new Attributes(new MockWebRequest(Url.parse("")), new StringResponse());
			}
			byte[] processedBytes = processResponse(attributes, bytes);
			return new ByteArrayInputStream(processedBytes);
		}
	}

	/**
	 * Checks whether access is granted for this resource.
	 *
	 * By default IPackageResourceGuard is used to check the permissions but
	 * the resource itself can also make the check.
	 *
	 * @param scope
	 *            resource scope
	 * @param path
	 *            resource path
	 * @return <code>true<code> if resource access is granted
	 */
	protected boolean accept(Class<?> scope, String path)
	{
		IPackageResourceGuard guard = Application.get()
			.getResourceSettings()
			.getPackageResourceGuard();

		return guard.accept(scope, path);
	}

	/**
	 * Checks whether a resource for a given set of criteria exists.
	 *
	 * @param key
	 *            The key that contains all attributes about the requested resource
	 * @return {@code true} if there is a package resource with the given attributes
	 */
	public static boolean exists(final ResourceReference.Key key)
	{
		return exists(key.getScopeClass(), key.getName(), key.getLocale(), key.getStyle(), key.getVariation());
	}

	/**
	 * Checks whether a resource for a given set of criteria exists.
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
	 * @return {@code true} if a resource could be loaded, {@code false} otherwise
	 */
	public static boolean exists(final Class<?> scope, final String path, final Locale locale,
		final String style, final String variation)
	{
		String absolutePath = Packages.absolutePath(scope, path);
		return Application.get()
			.getResourceSettings()
			.getResourceStreamLocator()
			.locate(scope, absolutePath, style, variation, locale, null, false) != null;
	}

	@Override
	public String toString()
	{
		final StringBuilder result = new StringBuilder();
		result.append('[')
			.append(Classes.simpleName(getClass()))
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

	String getParentFolderPlaceholder()
	{
		String parentFolderPlaceholder;
		if (Application.exists())
		{
			parentFolderPlaceholder = Application.get()
				.getResourceSettings()
				.getParentFolderPlaceholder();
		}
		else
		{
			parentFolderPlaceholder = "..";
		}
		return parentFolderPlaceholder;
	}

	private static class CacheKey implements Serializable
	{
		private final String scopeName;
		private final String path;
		private final Locale locale;
		private final String style;
		private final String variation;

		public CacheKey(String scopeName, String path, Locale locale, String style, String variation)
		{
			this.scopeName = scopeName;
			this.path = path;
			this.locale = locale;
			this.style = style;
			this.variation = variation;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (!(o instanceof CacheKey))
				return false;

			CacheKey cacheKey = (CacheKey)o;

			if (locale != null ? !locale.equals(cacheKey.locale) : cacheKey.locale != null)
				return false;
			if (!path.equals(cacheKey.path))
				return false;
			if (!scopeName.equals(cacheKey.scopeName))
				return false;
			if (style != null ? !style.equals(cacheKey.style) : cacheKey.style != null)
				return false;
			if (variation != null ? !variation.equals(cacheKey.variation)
				: cacheKey.variation != null)
				return false;

			return true;
		}

		@Override
		public int hashCode()
		{
			int result = scopeName.hashCode();
			result = 31 * result + path.hashCode();
			result = 31 * result + (locale != null ? locale.hashCode() : 0);
			result = 31 * result + (style != null ? style.hashCode() : 0);
			result = 31 * result + (variation != null ? variation.hashCode() : 0);
			return result;
		}

		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder();
			sb.append("CacheKey");
			sb.append("{scopeName='").append(scopeName).append('\'');
			sb.append(", path='").append(path).append('\'');
			sb.append(", locale=").append(locale);
			sb.append(", style='").append(style).append('\'');
			sb.append(", variation='").append(variation).append('\'');
			sb.append('}');
			return sb.toString();
		}
	}
}
