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

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.IResourceStreamWriter;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO javadoc
 */
public class ResourceStreamResource extends AbstractResource
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(ResourceStreamResource.class);

	private IResourceStream stream;
	private String fileName;
	private ContentDisposition contentDisposition = ContentDisposition.INLINE;
	private String textEncoding;

	private Duration cacheDuration;

	/**
	 * Construct.
	 * 
	 * @param stream
	 */
	public ResourceStreamResource(IResourceStream stream)
	{
		this.stream = stream;
	}

	/**
	 * @param fileName
	 * @return this
	 */
	public ResourceStreamResource setFileName(String fileName)
	{
		this.fileName = fileName;
		return this;
	}

	/**
	 * @param contentDisposition
	 * @return thsi
	 */
	public ResourceStreamResource setContentDisposition(ContentDisposition contentDisposition)
	{
		this.contentDisposition = contentDisposition;
		return this;
	}

	/**
	 * @param textEncoding
	 * @return this
	 */
	public ResourceStreamResource setTextEncoding(String textEncoding)
	{
		this.textEncoding = textEncoding;
		return this;
	}

	/**
	 * @return the duration for which the resource will be cached by the browser
	 */
	public Duration getCacheDuration()
	{
		return cacheDuration;
	}

	/**
	 * @param cacheDuration
	 *            the duration for which the resource will be cached by the browser
	 * @return this component
	 */
	public ResourceStreamResource setCacheDuration(Duration cacheDuration)
	{
		this.cacheDuration = cacheDuration;
		return this;
	}

	/**
	 * Lazy or dynamic initialization of the wrapped IResourceStream(Writer)
	 * @return the underlying IResourceStream
	 */
	protected IResourceStream getResourceStream()
	{
		return stream;
	}

	private IResourceStream internalGetResourceStream()
	{
		final IResourceStream resourceStream = getResourceStream();
		Checks.notNull(resourceStream, "%s#getResourceStream() should not return null!", ResourceStreamResource.class.getName());
		return resourceStream;
	}

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes)
	{
		final IResourceStream resourceStream = internalGetResourceStream();
		ResourceResponse data = new ResourceResponse();
		Time lastModifiedTime = resourceStream.lastModifiedTime();
		if (lastModifiedTime != null)
		{
			data.setLastModified(lastModifiedTime);
		}

		if (cacheDuration != null)
		{
			data.setCacheDuration(cacheDuration);
		}

		// performance check; don't bother to do anything if the resource is still cached by client
		if (data.dataNeedsToBeWritten(attributes))
		{
			InputStream inputStream = null;
			if (stream instanceof IResourceStreamWriter == false)
			{
				try
				{
					inputStream = resourceStream.getInputStream();
				}
				catch (ResourceStreamNotFoundException e)
				{
					data.setError(HttpServletResponse.SC_NOT_FOUND);
					close();
				}
			}

			data.setContentDisposition(contentDisposition);
			Bytes length = resourceStream.length();
			if (length != null)
			{
				data.setContentLength(length.bytes());
			}
			data.setFileName(fileName);

			String contentType = resourceStream.getContentType();
			if (contentType == null && fileName != null && Application.exists())
			{
				contentType = Application.get().getMimeType(fileName);
			}
			data.setContentType(contentType);
			data.setTextEncoding(textEncoding);

			if (resourceStream instanceof IResourceStreamWriter)
			{
				data.setWriteCallback(new WriteCallback()
				{
					@Override
					public void writeData(Attributes attributes)
					{
						((IResourceStreamWriter)resourceStream).write(attributes.getResponse());
						close();
					}
				});
			}
			else
			{
				final InputStream s = inputStream;
				data.setWriteCallback(new WriteCallback()
				{
					@Override
					public void writeData(Attributes attributes)
					{
						try
						{
							writeStream(attributes, s);
						}
						finally
						{
							close();
						}
					}
				});
			}
		}

		return data;
	}

	private void close()
	{
		try
		{
			internalGetResourceStream().close();
		}
		catch (IOException e)
		{
			logger.error("Couldn't close ResourceStream", e);
		}
	}
}
