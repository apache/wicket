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

import org.apache.wicket.request.Response;
import org.apache.wicket.util.time.Time;

/**
 * An abstract resource that can deliver static data - passed to the constructor,
 * or dynamic - returned by {@link #getData(org.apache.wicket.request.resource.IResource.Attributes)}
 *
 * @param <T> The type of the data this resource can deliver
 */
public abstract class BaseDataResource<T> extends AbstractResource
{
	private static final long serialVersionUID = 1L;

	/** the content type */
	private final String contentType;

	/** the data to deliver */
	private T data;

	/** the time that this resource was last modified; same as construction time. */
	private final Time lastModified = Time.now();

	private final String filename;

	/**
	 * Creates a {@link org.apache.wicket.request.resource.BaseDataResource} which will
	 * provide its data dynamically with
	 * {@link #getData(org.apache.wicket.request.resource.IResource.Attributes)}
	 *
	 * @param contentType
	 *            The Content type of the array.
	 */
	public BaseDataResource(final String contentType)
	{
		this(contentType, null, null);
	}

	/**
	 * Creates a Resource from the given data with its content type
	 *
	 * @param contentType
	 *            The Content type of the array.
	 * @param data
	 *            The data
	 */
	public BaseDataResource(final String contentType, final T data)
	{
		this(contentType, data, null);
	}

	/**
	 * Creates a Resource from the given data with its content type and filename
	 *
	 * @param contentType
	 *            The Content type of the array.
	 * @param data
	 *            The data
	 * @param filename
	 *            The filename that will be set as the Content-Disposition header.
	 */
	public BaseDataResource(final String contentType, final T data, final String filename)
	{
		this.contentType = contentType;
		this.data = data;
		this.filename = filename;
	}

	/**
	 * Post-configures the given response, e.g. set/override response headers.
	 *
	 * @param response
	 *              The response to configure
	 * @param attributes
	 *              The request attributes (web request, web response, parameters)
	 */
	protected void configureResponse(final ResourceResponse response, final Attributes attributes)
	{
	}

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

		final T data = getData(attributes);
		if (data == null)
		{
			response.setError(HttpServletResponse.SC_NOT_FOUND);
		}
		else
		{
			Long length = getLength(data);
			if (length != null)
			{
				response.setContentLength(length);
			}

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
						BaseDataResource.this.writeData(attributes.getResponse(), data);
					}
				});

				configureResponse(response, attributes);
			}
		}

		return response;
	}

	/**
	 * Writes the given data to the response
	 * @param response
	 *              The response to write to
	 * @param data
	 *              The data to write
	 */
	protected abstract void writeData(Response response, T data);

	/**
	 * @param data
	 *              The data to be written
	 * @return The length of the data to be written. Used to set "Content-Length" response header
	 */
	protected abstract Long getLength(T data);

	/**
	 * Gets the data for this resource.
	 * 
	 * @param attributes
	 *            the context bringing the request, response and the parameters
	 * 
	 * @return The data for this resource
	 */
	protected T getData(final Attributes attributes)
	{
		return data;
	}
}
