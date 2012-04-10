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

import java.net.URLConnection;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.util.time.Time;

/**
 * An {@link IResource} for byte arrays. The byte array can be static - passed to the constructor,
 * or dynamic - by overriding
 * {@link #getData(org.apache.wicket.request.resource.IResource.Attributes)}
 * 
 * @author Matej Knopp
 */
public class ByteArrayResource extends AbstractResource
{
	private static final long serialVersionUID = 1L;

	/** the content type. */
	private final String contentType;

	/** the binary data. */
	private byte[] array;

	/** the time that this resource was last modified; same as construction time. */
	private final Time lastModified = Time.now();

	private final String filename;

	/**
	 * Creates a {@link ByteArrayResource} which will provide its data dynamically with
	 * {@link #getData(org.apache.wicket.request.resource.IResource.Attributes)}
	 * 
	 * @param contentType
	 *            The Content type of the array.
	 */
	public ByteArrayResource(final String contentType)
	{
		this(contentType, null, null);
	}

	/**
	 * Creates a Resource from the given byte array with its content type
	 * 
	 * @param contentType
	 *            The Content type of the array.
	 * @param array
	 *            The binary content
	 */
	public ByteArrayResource(final String contentType, final byte[] array)
	{
		this(contentType, array, null);
	}

	/**
	 * Creates a Resource from the given byte array with its content type
	 * 
	 * @param contentType
	 *            The Content type of the array.
	 * @param array
	 *            The binary content
	 * @param filename
	 *            The filename that will be set as the Content-Disposition header.
	 */
	public ByteArrayResource(final String contentType, final byte[] array, final String filename)
	{
		this.contentType = contentType;
		this.array = array;
		this.filename = filename;
	}

	protected void configureResponse(final ResourceResponse response, final Attributes attributes)
	{
	}

	/**
	 * @see org.apache.wicket.request.resource.AbstractResource#newResourceResponse(org.apache.wicket.request.resource.IResource.Attributes)
	 */
	@Override
	protected ResourceResponse newResourceResponse(final Attributes attributes)
	{
		final ResourceResponse response = new ResourceResponse();

		String contentType = this.contentType;

		if (contentType == null)
		{
			if (filename != null)
			{
				contentType = URLConnection.getFileNameMap().getContentTypeFor(filename);
			}

			if (contentType == null)
			{
				contentType = "application/octet-stream";
			}
		}


		response.setContentType(contentType);
		response.setLastModified(lastModified);

		final byte[] data = getData(attributes);
		if (data == null)
		{
			response.setError(HttpServletResponse.SC_NOT_FOUND);
		}
		else
		{
			response.setContentLength(data.length);

			if (response.dataNeedsToBeWritten(attributes))
			{
				if (filename != null)
				{
					response.setFileName(filename);
					response.setContentDisposition(ContentDisposition.ATTACHMENT);
				}
				else
				{
					response.setContentDisposition(ContentDisposition.INLINE);
				}

				response.setWriteCallback(new WriteCallback()
				{
					@Override
					public void writeData(final Attributes attributes)
					{
						attributes.getResponse().write(data);
					}
				});

				configureResponse(response, attributes);
			}
		}

		return response;
	}

	/**
	 * Gets the data for this resource.
	 * 
	 * @param attributes
	 *            the context bringing the request, response and the parameters
	 * 
	 * @return The byte array data for this resource
	 */
	protected byte[] getData(final Attributes attributes)
	{
		return array;
	}
}
