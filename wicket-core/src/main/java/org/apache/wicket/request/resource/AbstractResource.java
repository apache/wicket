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
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.HttpHeaderCollection;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.Strings;
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

	/** The meta data key of the content range start byte **/
	public static final MetaDataKey<Long> CONTENT_RANGE_STARTBYTE = new MetaDataKey<Long>()
	{
		private static final long serialVersionUID = 1L;
	};

	/** The meta data key of the content range end byte **/
	public static final MetaDataKey<Long> CONTENT_RANGE_ENDBYTE = new MetaDataKey<Long>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * All available content range types. The type name represents the name used in header
	 * information.
	 */
	public enum ContentRangeType
	{
		BYTES("bytes"), NONE("none");

		private final String typeName;

		private ContentRangeType(String typeName)
		{
			this.typeName = typeName;
		}

		public String getTypeName()
		{
			return typeName;
		}
	}

	static
	{
		INTERNAL_HEADERS = new HashSet<>();
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
		INTERNAL_HEADERS.add("content-range");
		INTERNAL_HEADERS.add("accept-range");
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
		private Integer statusCode;
		private String errorMessage;
		private String fileName = null;
		private ContentDisposition contentDisposition = ContentDisposition.INLINE;
		private String contentType = null;
		private String contentRange = null;
		private ContentRangeType contentRangeType = null;
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
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse setError(Integer errorCode)
		{
			setError(errorCode, null);
			return this;
		}

		/**
		 * Sets the error code and message for resource. If there is an error code set the data will
		 * not be rendered and the code and message will be sent to client.
		 * 
		 * @param errorCode
		 *            error code
		 * @param errorMessage
		 *            error message
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse setError(Integer errorCode, String errorMessage)
		{
			this.errorCode = errorCode;
			this.errorMessage = errorMessage;
			return this;
		}

		/**
		 * @return error code or <code>null</code>
		 */
		public Integer getErrorCode()
		{
			return errorCode;
		}

		/**
		 * Sets the status code for resource.
		 *
		 * @param statusCode
		 *            status code
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse setStatusCode(Integer statusCode)
		{
			this.statusCode = statusCode;
			return this;
		}

		/**
		 * @return status code or <code>null</code>
		 */
		public Integer getStatusCode()
		{
			return statusCode;
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
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse setFileName(String fileName)
		{
			this.fileName = fileName;
			return this;
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
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse setContentDisposition(ContentDisposition contentDisposition)
		{
			Args.notNull(contentDisposition, "contentDisposition");
			this.contentDisposition = contentDisposition;
			return this;
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
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse setContentType(String contentType)
		{
			this.contentType = contentType;
			return this;
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
		 * Gets the content range of the resource. If no content range is set the client assumes the
		 * whole content.
		 *
		 * @return the content range
		 */
		public String getContentRange()
		{
			return contentRange;
		}

		/**
		 * Sets the content range of the resource. If no content range is set the client assumes the
		 * whole content. Please note that if the content range is set, the content length, the
		 * status code and the accept range must be set right, too.
		 *
		 * @param contentRange
		 *            the content range
		 */
		public void setContentRange(String contentRange)
		{
			this.contentRange = contentRange;
		}

		/**
		 * If the resource accepts ranges
		 *
		 * @return the type of range (e.g. bytes)
		 */
		public ContentRangeType getAcceptRange()
		{
			return contentRangeType;
		}

		/**
		 * Sets the accept range header (e.g. bytes)
		 *
		 * @param contentRangeType
		 *            the content range header information
		 */
		public void setAcceptRange(ContentRangeType contentRangeType)
		{
			this.contentRangeType = contentRangeType;
		}

		/**
		 * Sets the text encoding for the resource. This setting must only used if the resource
		 * response represents text.
		 * 
		 * @param textEncoding
		 *            character encoding of text body
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse setTextEncoding(String textEncoding)
		{
			this.textEncoding = textEncoding;
			return this;
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
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse setContentLength(long contentLength)
		{
			this.contentLength = contentLength;
			return this;
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
		 * <code>If-Modified-Since</code> to determine if the actual data really needs to be sent
		 * to client.
		 * 
		 * @param lastModified
		 *            last modification timestamp
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse setLastModified(Time lastModified)
		{
			this.lastModified = lastModified;
			return this;
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
		 * Disables caching.
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse disableCaching()
		{
			return setCacheDuration(Duration.NONE);
		}

		/**
		 * Sets caching to maximum available duration.
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse setCacheDurationToMaximum()
		{
			cacheDuration = WebResponse.MAX_CACHE_DURATION;
			return this;
		}

		/**
		 * Controls how long this response may be cached.
		 * 
		 * @param duration
		 *            caching duration in seconds
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse setCacheDuration(Duration duration)
		{
			cacheDuration = Args.notNull(duration, "duration");
			return this;
		}

		/**
		 * Returns how long this resource may be cached for.
		 * <p/>
		 * The special value Duration.NONE means caching is disabled.
		 * 
		 * @return duration for caching
		 * 
		 * @see org.apache.wicket.settings.ResourceSettings#setDefaultCacheDuration(org.apache.wicket.util.time.Duration)
		 * @see org.apache.wicket.settings.ResourceSettings#getDefaultCacheDuration()
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
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse setCacheScope(WebResponse.CacheScope scope)
		{
			cacheScope = Args.notNull(scope, "scope");
			return this;
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
		 *
		 * @return {@code this}, for chaining.
		 */
		public ResourceResponse setWriteCallback(final WriteCallback writeCallback)
		{
			Args.notNull(writeCallback, "writeCallback");
			this.writeCallback = writeCallback;
			return this;
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
		// Sets the request attributes
		setRequestMetaData(attributes);

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

		if (!data.dataNeedsToBeWritten(attributes) || data.getErrorCode() != null ||
			needsBody(data.getStatusCode()) == false)
		{
			return;
		}

		if (data.getWriteCallback() == null)
		{
			throw new IllegalStateException("ResourceResponse#setWriteCallback() must be set.");
		}

		try
		{
			data.getWriteCallback().writeData(attributes);
		}
		catch (IOException iox)
		{
			throw new WicketRuntimeException(iox);
		}
	}

	/**
	 * Decides whether a response body should be written back to the client depending on the set
	 * status code
	 *
	 * @param statusCode
	 *            the status code set by the application
	 * @return {@code true} if the status code allows response body, {@code false} - otherwise
	 */
	private boolean needsBody(Integer statusCode)
	{
		return statusCode == null ||
								(statusCode < 300 &&
								statusCode != HttpServletResponse.SC_NO_CONTENT &&
								statusCode != HttpServletResponse.SC_RESET_CONTENT);
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
						Classes.simpleName(getClass()) + " to get or modify its value");
		}
	}

	/**
	 * Reads the plain request header information and applies enriched information as meta data to
	 * the current request. Those information are available for the whole request cycle.
	 *
	 * @param attributes
	 *            the attributes to get the plain request header information
	 */
	protected void setRequestMetaData(Attributes attributes)
	{
		Request request = attributes.getRequest();
		if (request instanceof WebRequest)
		{
			WebRequest webRequest = (WebRequest)request;

			setRequestRangeMetaData(webRequest);
		}
	}

	protected void setRequestRangeMetaData(WebRequest webRequest)
	{
		String rangeHeader = webRequest.getHeader("range");

		// The content range header is only be calculated if a range is given
		if (!Strings.isEmpty(rangeHeader) &&
				rangeHeader.contains(ContentRangeType.BYTES.getTypeName()))
		{
			// fixing white spaces
			rangeHeader = rangeHeader.replaceAll(" ", "");

			String range = rangeHeader.substring(rangeHeader.indexOf('=') + 1,
					rangeHeader.length());

			String[] rangeParts = Strings.split(range, '-');

			String startByteString = rangeParts[0];
			String endByteString = rangeParts[1];

			Long startbyte = startByteString != null && !startByteString.trim().equals("")
					? Long.parseLong(startByteString) : 0;
			Long endbyte = endByteString != null && !endByteString.trim().equals("")
					? Long.parseLong(endByteString) : -1;

			// Make the content range information available for the whole request cycle
			RequestCycle.get().setMetaData(CONTENT_RANGE_STARTBYTE, startbyte);
			RequestCycle.get().setMetaData(CONTENT_RANGE_ENDBYTE, endbyte);
		}
	}

	/**
	 * Sets the response header of resource response to the response received from the attributes
	 *
	 * @param resourceResponse
	 *            the resource response to get the header fields from
	 * @param attributes
	 *            the attributes to get the response from to which the header information are going
	 *            to be applied
	 */
	protected void setResponseHeaders(final ResourceResponse resourceResponse,
		final Attributes attributes)
	{
		Response response = attributes.getResponse();
		if (response instanceof WebResponse)
		{
			WebResponse webResponse = (WebResponse)response;

			// 1. Last Modified
			Time lastModified = resourceResponse.getLastModified();
			if (lastModified != null)
			{
				webResponse.setLastModifiedTime(lastModified);
			}

			// 2. Caching
			configureCache(resourceResponse, attributes);

			if (resourceResponse.getErrorCode() != null)
			{
				webResponse.sendError(resourceResponse.getErrorCode(),
					resourceResponse.getErrorMessage());
				return;
			}

			if (resourceResponse.getStatusCode() != null)
			{
				webResponse.setStatus(resourceResponse.getStatusCode());
			}

			if (!resourceResponse.dataNeedsToBeWritten(attributes))
			{
				webResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}

			// 3. Content Disposition
			String fileName = resourceResponse.getFileName();
			ContentDisposition disposition = resourceResponse.getContentDisposition();
			if (ContentDisposition.ATTACHMENT == disposition)
			{
				webResponse.setAttachmentHeader(fileName);
			}
			else if (ContentDisposition.INLINE == disposition)
			{
				webResponse.setInlineHeader(fileName);
			}

			// 4. Mime Type (+ encoding)
			String mimeType = resourceResponse.getContentType();
			if (mimeType != null)
			{
				final String encoding = resourceResponse.getTextEncoding();

				if (encoding == null)
				{
					webResponse.setContentType(mimeType);
				}
				else
				{
					webResponse.setContentType(mimeType + "; charset=" + encoding);
				}
			}

			// 5. Accept Range
			ContentRangeType acceptRange = resourceResponse.getAcceptRange();
			if (acceptRange != null)
			{
				webResponse.setAcceptRange(acceptRange.getTypeName());
			}

			long contentLength = resourceResponse.getContentLength();
			boolean contentRangeApplied = false;

			// 6. Content Range
			// for more information take a look here:
			// http://stackoverflow.com/questions/8293687/sample-http-range-request-session
			// if the content range header has been set directly
			// to the resource response use it otherwise calculate it
			String contentRange = resourceResponse.getContentRange();
			if (contentRange != null)
			{
				webResponse.setContentRange(contentRange);
			}
			else
			{
				// content length has to be set otherwise the content range header can not be
				// calculated - accept range must be set to bytes - others are not supported at the
				// moment
				if (contentLength != -1 && ContentRangeType.BYTES.equals(acceptRange))
				{
					contentRangeApplied = setResponseContentRangeHeaderFields(webResponse,
						attributes, contentLength);
				}
			}

			// 7. Content Length
			if (contentLength != -1 && !contentRangeApplied)
			{
				webResponse.setContentLength(contentLength);
			}

			// add custom headers and values
			final HttpHeaderCollection headers = resourceResponse.getHeaders();

			for (String name : headers.getHeaderNames())
			{
				checkHeaderAccess(name);

				for (String value : headers.getHeaderValues(name))
				{
					webResponse.addHeader(name, value);
				}
			}
		}
	}

	/**
	 * Sets the content range header fields to the given web response
	 *
	 * @param webResponse
	 *            the web response to apply the content range information to
	 * @param attributes
	 *            the attributes to get the request from
	 * @param contentLength
	 *            the content length of the response
	 * @return if the content range header information has been applied
	 */
	protected boolean setResponseContentRangeHeaderFields(WebResponse webResponse,
		Attributes attributes, long contentLength)
	{
		boolean contentRangeApplied = false;
		if (attributes.getRequest() instanceof WebRequest)
		{
			Long startbyte = RequestCycle.get().getMetaData(CONTENT_RANGE_STARTBYTE);
			Long endbyte = RequestCycle.get().getMetaData(CONTENT_RANGE_ENDBYTE);

			if (startbyte != null && endbyte != null)
			{
				// if end byte hasn't been set
				if (endbyte == -1)
				{
					endbyte = contentLength - 1;
				}

				// Change the status code to 206 partial content
				webResponse.setStatus(206);
				// currently only bytes are supported.
				webResponse.setContentRange(ContentRangeType.BYTES.getTypeName() + " " + startbyte +
					'-' + endbyte + '/' + contentLength);
				// content length must be overridden by the recalculated one
				webResponse.setContentLength((endbyte - startbyte) + 1);

				// content range has been applied do not set the content length again!
				contentRangeApplied = true;
			}
		}
		return contentRangeApplied;
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
		public abstract void writeData(Attributes attributes) throws IOException;

		/**
		 * Convenience method to write an {@link InputStream} to response.
		 * 
		 * @param attributes
		 *            request attributes
		 * @param stream
		 *            input stream
		 */
		protected final void writeStream(Attributes attributes, InputStream stream) throws IOException
		{
			final Response response = attributes.getResponse();
			Streams.copy(stream, response.getOutputStream());
		}
	}
}
