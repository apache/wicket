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

import java.util.Locale;

import org.apache.wicket.util.time.Time;

/**
 * @author Matej Knopp
 */
public class ByteArrayResource extends AbstractResource
{
	private static final long serialVersionUID = 1L;

	/** the content type. */
	private final String contentType;

	/** binary data. */
	private final byte[] array;

	/** the time that this resource was last modified; same as construction time. */
	private final Time lastModified = Time.now();

	private final String filename;

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
		this.contentType = contentType;
		this.array = array;
		filename = null;
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

	/**
	 * Creates a Resource from the given byte array with its content type and the locale for which
	 * it is valid.
	 * 
	 * @param contentType
	 *            The Content type of the array.
	 * @param array
	 *            The binary content.
	 * @param locale
	 *            The locale of this resource
	 */
	public ByteArrayResource(final String contentType, final byte[] array, final Locale locale)
	{
		this.contentType = contentType;
		this.array = array;
		filename = null;
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

		response.setContentType(contentType);
		response.setLastModified(lastModified.toDate());

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
					attributes.getResponse().write(array);
				}
			});

			configureResponse(response, attributes);
		}

		return response;
	}
}
