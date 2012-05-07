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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.HttpHeaderCollection;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;

/**
 * Convenience resource implementation. The subclass must implement
 * {@link #newResourceResponse(org.apache.wicket.request.resource.IResource.Attributes)} method.
 * 
 * @author Matej Knopp
 */
public abstract class AbstractResource implements IResource
{
	private static final long serialVersionUID = 1L;

	/** header values that are managed internally and must not be set directly */
	public static final Set<String> INTERNAL_HEADERS;

	static
	{
		INTERNAL_HEADERS = new HashSet<String>();
		INTERNAL_HEADERS.add("server");
		INTERNAL_HEADERS.add("date");
		INTERNAL_HEADERS.add("expires");
		INTERNAL_HEADERS.add("last-modified");
		INTERNAL_HEADERS.add("content-type");
		INTERNAL_HEADERS.add("content-length");
		INTERNAL_HEADERS.add("content-disposition");
		INTERNAL_HEADERS.add("transfer-encoding");
		INTERNAL_HEADERS.add("connection");
		INTERNAL_HEADERS.add("content-disposition");
	}

	/**
	 * Construct.
	 */
	public AbstractResource()
	{
	}

	/**
	 * Override this method to return a {@link ResourceResponse} for the request.
	 * 
	 * @param attributes
	 *            request attributes
	 * @return resource data instance
	 */
	protected abstract ResourceResponse newResourceResponse(Attributes attributes);

	/**
	 * Represents data used to configure response and write resource data.
	 * 
	 * @author Matej Knopp
	 */
	public static class ResourceResponse
	{
		private Integer errorCode;
		private String errorMessage;
		private String fileName = null;
		private ContentDisposition contentDisposition = ContentDisposition.INLINE;
		private String contentType = null;
		private String textEncoding;
		private long contentLength = -1;
		private Time lastModified = null;
		private WriteCallback writeCallback;
		private Duration cacheDuration;
		private WebResponse.CacheScope cacheScope;
		private final HttpHeaderCollection headers;

		/**
		 * Construct.
		 */
		public ResourceResponse()
		{
			// disallow caching for public caches. this behavior is similar to wicket 1.4:
			// setting it to [PUBLIC] seems to be sexy but could potentially cache confidential
			// data on public proxies for users migrating to 1.5
			cacheScope = WebResponse.CacheScope.PRIVATE;

			// collection of directly set response headers
			headers = new HttpHeaderCollection();
		}

		/**
		 * Sets the error code for resource. If there is an error code set the data will not be
		 * rendered and the code will be sent to client.
		 * 
		 * @param errorCode
		 *            error code
		 */
		public void setError(Integer errorCode)
		{
			setError(errorCode, null);
		}

		/**
		 * Sets the error code and message for resource. If there is an error code set the data will
		 * not be rendered and the code and message will be sent to client.
		 * 
		 * @param errorCode
		 *            error code
		 * @param errorMessage
		 *            error message
		 */
		public void setError(Integer errorCode, String errorMessage)
		{
			this.errorCode = errorCode;
			this.errorMessage = errorMessage;
		}

		/**
		 * @return error code or <code>null</code>
		 */
		public Integer getErrorCode()
		{
			return errorCode;
		}

		/**
		 * @return error message or <code>null</code>
		 */
		public String getErrorMessage()
		{
			return errorMessage;
		}

		/**
		 * Sets the file name of the resource.
		 * 
		 * @param fileName
		 *            file name
		 */
		public void setFileName(String fileName)
		{
			this.fileName = fileName;
		}

		/**
		 * @return resource file name
		 */
		public String getFileName()
		{
			return fileName;
		}

		/**
		 * Determines whether the resource will be inline or an attachment.
		 * 
		 * @see ContentDisposition
		 * 
		 * @param contentDisposition
		 *            content disposition (attachment or inline)
		 */
		public void setContentDisposition(ContentDisposition contentDisposition)
		{
			Args.notNull(contentDisposition, "contentDisposition");
			this.contentDisposition = contentDisposition;
		}

		/**
		 * @return whether the resource is inline or attachment
		 */
		public ContentDisposition getContentDisposition()
		{
			return contentDisposition;
		}

		/**
		 * Sets the content type for the resource. If no content type is set it will be determined
		 * by the extension.
		 * 
		 * @param contentType
		 *            content type (also known as mime type)
		 */
		public void setContentType(String contentType)
		{
			this.contentType = contentType;
		}

		/**
		 * @return resource content type
		 */
		public String getContentType()
		{
			if (contentType == null && fileName != null)
			{
				contentType = Application.get().getMimeType(fileName);
			}
			return contentType;
		}

		/**
		 * Sets the text encoding for the resource. The encoding is only used if the content type
		 * indicates a textual resource.
		 * 
		 * @param textEncoding
		 *            character encoding of text body
		 */
		public void setTextEncoding(String textEncoding)
		{
			this.textEncoding = textEncoding;
		}

		/**
		 * @return text encoding for resource
		 */
		protected String getTextEncoding()
		{
			return textEncoding;
		}

		/**
		 * Sets the content length (in bytes) of the data. Content length is optional but it's
		 * recommended to set it so that the browser can show download progress.
		 * 
		 * @param contentLength
		 *            length of response body
		 */
		public void setContentLength(long contentLength)
		{
			this.contentLength = contentLength;
		}

		/**
		 * @return content length (in bytes)
		 */
		public long getContentLength()
		{
			return contentLength;
		}

		/**
		 * Sets the last modified data of the resource. Even though this method is optional it is
		 * recommended to set the date. If the date is set properly Wicket can check the
		 * <code>If-Modified-Since</code> to determine if the actuall data really needs to be sent
		 * to client.
		 * 
		 * @param lastModified
		 *            last modification timestamp
		 */
		public void setLastModified(Time lastModified)
		{
			this.lastModified = lastModified;
		}

		/**
		 * @return last modification timestamp
		 */
		public Time getLastModified()
		{
			return lastModified;
		}

		/**
		 * Check to determine if the resource data needs to be written. This method checks the
		 * <code>If-Modified-Since</code> request header and compares it to lastModified property.
		 * In order for this method to work {@link #setLastModified(Time)} has to be called first.
		 * 
		 * @param attributes
		 *            request attributes
		 * @return <code>true</code> if the resource data does need to be written,
		 *         <code>false</code> otherwise.
		 */
		public boolean dataNeedsToBeWritten(Attributes attributes)
		{
			WebRequest request = (WebRequest)attributes.getRequest();
			Time ifModifiedSince = request.getIfModifiedSinceHeader();

			if (cacheDuration != Duration.NONE && ifModifiedSince != null && lastModified != null)
			{
				// [Last-Modified] headers have a maximum precision of one second
				// so we have to truncate the milliseconds part for a proper compare.
				// that's stupid, since changes within one second will not be reliably
				// detected by the client ... any hint or clarification to improve this
				// situation will be appreciated...
				Time roundedLastModified = Time.millis(lastModified.getMilliseconds() / 1000 * 1000);

				return ifModifiedSince.before(roundedLastModified);
			}
			else
			{
				return true;
			}
		}

		/**
		 * disable caching
		 */
		public void disableCaching()
		{
			setCacheDuration(Duration.NONE);
		}

		/**
		 * set caching to maximum available duration
		 */
		public void setCacheDurationToMaximum()
		{
			cacheDuration = WebResponse.MAX_CACHE_DURATION;
		}

		/**
		 * Controls how long this response may be cached
		 * 
		 * @param duration
		 *            caching duration in seconds
		 */
		public void setCacheDuration(Duration duration)
		{
			cacheDuration = Args.notNull(duration, "duration");
		}

		/**
		 * returns how long this resource may be cached
		 * <p/>
		 * The special value Duration.NONE means caching is disabled.
		 * 
		 * @return duration for caching
		 * 
		 * @see IResourceSettings#setDefaultCacheDuration(org.apache.wicket.util.time.Duration)
		 * @see IResourceSettings#getDefaultCacheDuration()
		 */
		public Duration getCacheDuration()
		{
			Duration duration = cacheDuration;
			if (duration == null && Application.exists())
			{
				duration = Application.get().getResourceSettings().getDefaultCacheDuration();
			}

			return duration;
		}

		/**
		 * returns what kind of caches are allowed to cache the resource response
		 * <p/>
		 * resources are only cached at all if caching is enabled by setting a cache duration.
		 * 
		 * @return cache scope
		 * 
		 * @see org.apache.wicket.request.resource.AbstractResource.ResourceResponse#getCacheDuration()
		 * @see org.apache.wicket.request.resource.AbstractResource.ResourceResponse#setCacheDuration(org.apache.wicket.util.time.Duration)
		 * @see org.apache.wicket.request.http.WebResponse.CacheScope
		 */
		public WebResponse.CacheScope getCacheScope()
		{
			return cacheScope;
		}

		/**
		 * controls what kind of caches are allowed to cache the response
		 * <p/>
		 * resources are only cached at all if caching is enabled by setting a cache duration.
		 * 
		 * @param scope
		 *            scope for caching
		 * 
		 * @see org.apache.wicket.request.resource.AbstractResource.ResourceResponse#getCacheDuration()
		 * @see org.apache.wicket.request.resource.AbstractResource.ResourceResponse#setCacheDuration(org.apache.wicket.util.time.Duration)
		 * @see org.apache.wicket.request.http.WebResponse.CacheScope
		 */
		public void setCacheScope(WebResponse.CacheScope scope)
		{
			cacheScope = Args.notNull(scope, "scope");
		}

		/**
		 * Sets the {@link WriteCallback}. The callback is responsible for generating the response
		 * data.
		 * <p>
		 * It is necessary to set the {@link WriteCallback} if
		 * {@link #dataNeedsToBeWritten(org.apache.wicket.request.resource.IResource.Attributes)}
		 * returns <code>true</code> and {@link #setError(Integer)} has not been called.
		 * 
		 * @param writeCallback
		 *            write callback
		 */
		public void setWriteCallback(final WriteCallback writeCallback)
		{
			Args.notNull(writeCallback, "writeCallback");
			this.writeCallback = writeCallback;
		}

		/**
		 * @return write callback.
		 */
		public WriteCallback getWriteCallback()
		{
			return writeCallback;
		}

		/**
		 * get custom headers
		 * 
		 * @return collection of the response headers
		 */
		public HttpHeaderCollection getHeaders()
		{
			return headers;
		}
	}

	/**
	 * Configure the web response header for client cache control.
	 * 
	 * @param data
	 *            resource data
	 * @param attributes
	 *            request attributes
	 */
	protected void configureCache(final ResourceResponse data, final Attributes attributes)
	{
		Response response = attributes.getResponse();

		if (response instanceof WebResponse)
		{
			Duration duration = data.getCacheDuration();
			WebResponse webResponse = (WebResponse)response;
			if (duration.compareTo(Duration.NONE) > 0)
			{
				webResponse.enableCaching(duration, data.getCacheScope());
			}
			else
			{
				webResponse.disableCaching();
			}
		}
	}

	protected IResourceCachingStrategy getCachingStrategy()
	{
		return Application.get().getResourceSettings().getCachingStrategy();
	}

	/**
	 * 
	 * @see org.apache.wicket.request.resource.IResource#respond(org.apache.wicket.request.resource.IResource.Attributes)
	 */
	@Override
	public void respond(final Attributes attributes)
	{
		// Get a "new" ResourceResponse to write a response
		ResourceResponse data = newResourceResponse(attributes);

		// is resource supposed to be cached?
		if (this instanceof IStaticCacheableResource)
		{
			final IStaticCacheableResource cacheable = (IStaticCacheableResource)this;

			// is caching enabled?
			if (cacheable.isCachingEnabled())
			{
				// apply caching strategy to response
				getCachingStrategy().decorateResponse(data, cacheable);
			}
		}
		// set response header
		setResponseHeaders(data, attributes);

		if (!data.dataNeedsToBeWritten(attributes) || data.getErrorCode() != null)
		{
			return;
		}

		if (data.getWriteCallback() == null)
		{
			throw new IllegalStateException("ResourceResponse#setWriteCallback() must be set.");
		}

		data.getWriteCallback().writeData(attributes);
	}

	/**
	 * check if header is directly modifyable
	 * 
	 * @param name
	 *            header name
	 * 
	 * @throws IllegalArgumentException
	 *             if access is forbidden
	 */
	private void checkHeaderAccess(String name)
	{
		name = Args.notEmpty(name.trim().toLowerCase(), "name");

		if (INTERNAL_HEADERS.contains(name))
		{
			throw new IllegalArgumentException("you are not allowed to directly access header [" +
				name + "], " + "use one of the other specialized methods of " +
				getClass().getSimpleName() + " to get or modify its value");
		}
	}

	/**
	 * @param data
	 * @param attributes
	 */
	protected void setResponseHeaders(final ResourceResponse data, final Attributes attributes)
	{
		Response response = attributes.getResponse();
		if (response instanceof WebResponse)
		{
			WebResponse webResponse = (WebResponse)response;

			// 1. Last Modified
			Time lastModified = data.getLastModified();
			if (lastModified != null)
			{
				webResponse.setLastModifiedTime(lastModified);
			}

			// 2. Caching
			configureCache(data, attributes);

			if (!data.dataNeedsToBeWritten(attributes))
			{
				webResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}

			if (data.getErrorCode() != null)
			{
				webResponse.sendError(data.getErrorCode(), data.getErrorMessage());
				return;
			}

			String fileName = data.getFileName();
			ContentDisposition disposition = data.getContentDisposition();
			String mimeType = data.getContentType();
			String encoding = null;

			if (mimeType != null && mimeType.contains("text"))
			{
				encoding = data.getTextEncoding();
			}

			long contentLength = data.getContentLength();

			// 3. Content Disposition
			if (ContentDisposition.ATTACHMENT == disposition)
			{
				webResponse.setAttachmentHeader(fileName);
			}
			else if (ContentDisposition.INLINE == disposition)
			{
				webResponse.setInlineHeader(fileName);
			}

			// 4. Mime Type (+ encoding)
			if (mimeType != null)
			{
				if (encoding == null)
				{
					webResponse.setContentType(mimeType);
				}
				else
				{
					webResponse.setContentType(mimeType + "; charset=" + encoding);
				}
			}

			// 5. Content Length
			if (contentLength != -1)
			{
				webResponse.setContentLength(contentLength);
			}

			// add custom headers and values
			final HttpHeaderCollection headers = data.getHeaders();

			for (String name : headers.getHeaderNames())
			{
				checkHeaderAccess(name);

				for (String value : headers.getHeaderValues(name))
				{
					webResponse.addHeader(name, value);
				}
			}

			// 6. Flush the response
			flushResponseAfterHeaders(webResponse);
		}
	}

	/**
	 * Flushes the response after setting the headers.
	 * This is necessary for Firefox if this resource is an image,
	 * otherwise it messes up other images on page.
	 *
	 * @param response
	 *      the current web response
	 */
	protected void flushResponseAfterHeaders(final WebResponse response)
	{
		response.flush();
	}

	/**
	 * Callback invoked when resource data needs to be written to response. Subclass needs to
	 * implement the {@link #writeData(org.apache.wicket.request.resource.IResource.Attributes)}
	 * method.
	 * 
	 * @author Matej Knopp
	 */
	public abstract static class WriteCallback
	{
		/**
		 * Write the resource data to response.
		 * 
		 * @param attributes
		 *            request attributes
		 */
		public abstract void writeData(Attributes attributes);

		/**
		 * Convenience method to write an {@link InputStream} to response.
		 * 
		 * @param attributes
		 *            request attributes
		 * @param stream
		 *            input stream
		 */
		protected final void writeStream(Attributes attributes, InputStream stream)
		{
			final Response response = attributes.getResponse();
			OutputStream s = new OutputStream()
			{
				@Override
				public void write(int b) throws IOException
				{
					response.write(new byte[] { (byte)b });
				}

				@Override
				public void write(byte[] b) throws IOException
				{
					response.write(b);
				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException
				{
					response.write(b, off, len);
				}
			};
			try
			{
				Streams.copy(stream, s);
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException(e);
			}
		}
	}
}