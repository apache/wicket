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
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Checks;

/**
 * Convenience resource implementation. The subclass must implement
 * {@link #newResourceResponse(org.apache.wicket.ng.resource.IResource.Attributes)} method.
 * 
 * @author Matej Knopp
 */
public abstract class AbstractResource implements IResource
{
	private static final long serialVersionUID = 1L;

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
		private String fileName = null;
		private ContentDisposition contentDisposition = ContentDisposition.INLINE;
		private String contentType = null;
		private String textEncoding;
		private long contentLength = -1;
		private Date lastModified = null;
		private WriteCallback writeCallback;
		private boolean cacheable = true;
		private long cacheDuration;

		/**
		 * Construct.
		 */
		public ResourceResponse()
		{
			cacheDuration = Application.get().getResourceSettings().getDefaultCacheDuration();
		}

		/**
		 * Sets the error code for resource. If there is an error code set the data will not be
		 * rendered and the code will be sent to client.
		 * 
		 * @param errorCode
		 */
		public void setErrorCode(Integer errorCode)
		{
			this.errorCode = errorCode;
		}

		/**
		 * @return error code or <code>null</code>
		 */
		public Integer getErrorCode()
		{
			return errorCode;
		}

		/**
		 * Sets the file name of the resource.
		 * 
		 * @param fileName
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
		 */
		public void setContentDisposition(ContentDisposition contentDisposition)
		{
			Checks.argumentNotNull(contentDisposition, "contentDisposition");
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
		 */
		public void setLastModified(Date lastModified)
		{
			this.lastModified = lastModified;
		}

		/**
		 * @return last modified date
		 */
		public Date getLastModified()
		{
			return lastModified;
		}

		/**
		 * Check to determine if the resource data needs to be written. This method checks the
		 * <code>If-Modified-Since</code> request header and compares it to lastModified property.
		 * In order for this method to work {@link #setLastModified(Date)} has to be called first.
		 * 
		 * @param attributes
		 * @return <code>true</code> if the resource data does need to be written,
		 *         <code>false</code> otherwise.
		 */
		public boolean dataNeedsToBeWritten(Attributes attributes)
		{
			WebRequest request = (WebRequest)attributes.getRequest();
			Date ifModifiedSince = request.getIfModifiedSinceHeader();
			Date lastModifed = getLastModified();

			if (ifModifiedSince != null && lastModifed != null &&
				lastModifed.before(ifModifiedSince))
			{
				return false;
			}
			else
			{
				return true;
			}
		}

		/**
		 * Cachable resources are cached on client. This flag affects the <code>Expires</code> and
		 * <code>Cache-Control</code> headers.
		 * 
		 * @see #setCacheDuration(int)
		 * 
		 * @param cacheable
		 */
		public void setCacheable(boolean cacheable)
		{
			this.cacheable = cacheable;
		}

		/**
		 * @return returns whether this resource is cacheable
		 */
		public boolean isCacheable()
		{
			return cacheable;
		}

		/**
		 * Sets the duration for which this resource should be cached on client (in seconds). #see
		 * {@link IResourceSettings#setDefaultCacheDuration(int)}
		 * 
		 * @param cacheDuration
		 */
		public void setCacheDuration(long cacheDuration)
		{
			this.cacheDuration = cacheDuration;
		}

		/**
		 * @return duration for which the resource shoudl be cached on client (in seconds)
		 */
		public long getCacheDuration()
		{
			return cacheDuration;
		}

		/**
		 * Sets the {@link WriteCallback}. The callback is responsible for generating the response
		 * data.
		 * <p>
		 * It is necessary to set the {@link WriteCallback} if
		 * {@link #dataNeedsToBeWritten(org.apache.wicket.ng.resource.IResource.Attributes)} returns
		 * <code>true</code> and {@link #setErrorCode(Integer)} has not been called.
		 * 
		 * @param writeCallback
		 */
		public void setWriteCallback(WriteCallback writeCallback)
		{
			Checks.argumentNotNull(writeCallback, "writeCallback");
			this.writeCallback = writeCallback;
		}

		/**
		 * @return write callback.
		 */
		public WriteCallback getWriteCallback()
		{
			return writeCallback;
		}
	};

	protected void configureCache(WebRequest request, WebResponse response, ResourceResponse data,
		Attributes attributes)
	{
		if (data.isCacheable())
		{
			// If time is set also set cache headers.
			response.setDateHeader("Expires", System.currentTimeMillis() +
				(data.getCacheDuration() * 1000L));
			response.setHeader("Cache-Control", "max-age=" + data.getCacheDuration());
		}
		else
		{
			response.setHeader("Cache-Control", "no-cache, must-revalidate");
		}
	}

	public final void respond(Attributes attributes)
	{
		ResourceResponse data = newResourceResponse(attributes);

		WebRequest request = (WebRequest)attributes.getRequest();
		WebResponse response = (WebResponse)attributes.getResponse();


		// 1. Last Modified
		Date lastModified = data.getLastModified();
		if (lastModified != null)
		{
			response.setLastModifiedTime(lastModified.getTime());
		}

		// 2. Caching

		configureCache(request, response, data, attributes);

		if (!data.dataNeedsToBeWritten(attributes))
		{
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		else if (data.getErrorCode() != null)
		{
			response.sendError(data.getErrorCode(), null);
		}
		else
		{
			if (data.getWriteCallback() == null)
			{
				throw new IllegalStateException(
					"ResourceData#setWriteCallback must be called for AbstractResource.");
			}

			String fileName = data.getFileName();
			ContentDisposition disposition = data.getContentDisposition();
			String mimeType = data.getContentType();
			String encoding = null;


			if (mimeType != null && mimeType.indexOf("text") != -1)
			{
				encoding = data.getTextEncoding();
			}

			long contentLength = data.getContentLength();

			// 3. Content Disposition

			if (ContentDisposition.ATTACHMENT == disposition)
			{
				response.setAttachmentHeader(fileName);
			}
			else if (ContentDisposition.INLINE == disposition)
			{
				response.setInlineHeader(fileName);
			}

			// 4. Mime Type (+ encoding)

			if (mimeType != null)
			{
				if (encoding == null)
				{
					response.setContentType(mimeType);
				}
				else
				{
					response.setContentType(mimeType + "; charset=" + encoding);
				}
			}


			// 5. Content Length

			if (contentLength != -1)
			{
				response.setContentLength(contentLength);
			}

			// 6. Flush the response
			// This is necessary for firefox if this resource is an image, otherwise it messes up
			// other images on page
			response.flush();

			// 7. Write Data
			data.getWriteCallback().writeData(attributes);
		}
	}

	/**
	 * Callback invoked when resource data needs to be written to response. Subclass needs to
	 * implement the {@link #writeData(org.apache.wicket.ng.resource.IResource.Attributes)} method.
	 * 
	 * @author Matej Knopp
	 */
	public static abstract class WriteCallback
	{
		/**
		 * Write the resource data to response.
		 * 
		 * @param attributes
		 */
		public abstract void writeData(Attributes attributes);

		/**
		 * Convenience method to write an {@link InputStream} to response.
		 * 
		 * @param attributes
		 * @param stream
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
					if (off == 0 && len == b.length)
					{
						write(b);
					}
					else
					{
						byte copy[] = new byte[len];
						System.arraycopy(b, off, copy, 0, len);
						write(copy);
					}
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
	};
}
