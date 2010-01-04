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
package org.apache.wicket.ng.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.Response;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.io.Streams;

/**
 * Simple resource implementation.
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

	public static abstract class WriteCallback
	{
		public abstract void writeData(Attributes attributes);

		protected void writeStream(Attributes attributes, InputStream stream)
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
					if (off == 0 || len == b.length)
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

	public enum ContentDisposition {
		INLINE, ATTACHMENT;
	};

	public static class ResourceData
	{
		private Integer errorCode;
		private String fileName = null;
		private ContentDisposition contentDisposition = ContentDisposition.INLINE;
		private String contentType = null;
		private String textEncoding;
		private long contentLength = -1;
		private Date lastModified = null;
		private WriteCallback writeCallback;

		public void setErrorCode(Integer errorCode)
		{
			this.errorCode = errorCode;
		}

		public Integer getErrorCode()
		{
			return errorCode;
		}

		public void setFileName(String fileName)
		{
			this.fileName = fileName;
		}

		public String getFileName()
		{
			return fileName;
		}

		public void setContentDisposition(ContentDisposition contentDisposition)
		{
			this.contentDisposition = contentDisposition;
		}

		public ContentDisposition getContentDisposition()
		{
			return contentDisposition;
		}

		public void setContentType(String contentType)
		{
			this.contentType = contentType;
		}

		public String getContentType()
		{
			if (contentType == null && fileName != null)
			{
				contentType = Application.get().getMimeType(fileName);
			}
			return contentType;
		}

		public void setTextEncoding(String textEncoding)
		{
			this.textEncoding = textEncoding;
		}

		protected String getTextEncoding()
		{
			return textEncoding;
		}

		public void setContentLength(long contentLength)
		{
			this.contentLength = contentLength;
		}

		public long getContentLength()
		{
			return contentLength;
		}

		public void setLastModified(Date lastModified)
		{
			this.lastModified = lastModified;
		}

		public Date getLastModified()
		{
			return lastModified;
		}

		public boolean notModified(Attributes attributes)
		{
			WebRequest request = (WebRequest)attributes.getRequest();
			Date ifModifiedSince = request.getIfModifiedSinceHeader();
			Date lastModifed = getLastModified();

			if (ifModifiedSince != null && lastModifed != null &&
				lastModifed.before(ifModifiedSince))
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		public void setWriteCallback(WriteCallback writeCallback)
		{
			this.writeCallback = writeCallback;
		}

		public WriteCallback getWriteCallback()
		{
			return writeCallback;
		}
	};

	protected abstract ResourceData newResourceData(Attributes attributes);

	public void respond(Attributes attributes)
	{
		ResourceData data = newResourceData(attributes);

		WebRequest request = (WebRequest)attributes.getRequest();
		WebResponse response = (WebResponse)attributes.getResponse();

		if (data.notModified(attributes))
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

			String fileName = data.getFileName();
			ContentDisposition disposition = data.getContentDisposition();
			String mimeType = data.getContentType();
			String encoding = null;
			Date lastModified = data.getLastModified();

			if (mimeType != null && mimeType.indexOf("text") != -1)
			{
				encoding = data.getTextEncoding();
			}

			long contentLength = data.getContentLength();

			// 1. Content Disposition

			if (ContentDisposition.ATTACHMENT == disposition)
			{
				response.setAttachmentHeader(fileName);
			}
			else if (ContentDisposition.INLINE == disposition)
			{
				response.setInlineHeader(fileName);
			}

			// 2. Mime Type (+ encoding)

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

			// 3. Last Modified

			if (lastModified != null)
			{
				response.setLastModifiedTime(lastModified.getTime());
			}

			// 4. Content Length

			if (contentLength != -1)
			{
				response.setContentLength(contentLength);
			}

			// 5. Write Data
			data.getWriteCallback().writeData(attributes);
		}
	}

}
