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

import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.IResourceStreamWriter;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ResourceStreamResource extends AbstractResource
{
	private static final long serialVersionUID = 1L;

	private final IResourceStream stream;
	private String fileName;
	private ContentDisposition contentDisposition = ContentDisposition.INLINE;
	private String textEncoding;
	private String mimeType;

	public ResourceStreamResource(IResourceStream stream)
	{
		Checks.argumentNotNull(stream, "stream");
		this.stream = stream;
	}

	public ResourceStreamResource setFileName(String fileName)
	{
		this.fileName = fileName;
		return this;
	}

	public ResourceStreamResource setContentDisposition(ContentDisposition contentDisposition)
	{
		this.contentDisposition = contentDisposition;
		return this;
	}

	public ResourceStreamResource setTextEncoding(String textEncoding)
	{
		this.textEncoding = textEncoding;
		return this;
	}

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes)
	{
		ResourceResponse data = new ResourceResponse();
		data.setLastModified(stream.lastModifiedTime().toDate());

		// performance check; don't bother to do anything if the resource is still cached by client
		if (data.dataNeedsToBeWritten(attributes))
		{
			InputStream inputStream = null;
			if (stream instanceof IResourceStreamWriter == false)
			{
				try
				{
					inputStream = stream.getInputStream();
				}
				catch (ResourceStreamNotFoundException e)
				{
					data.setErrorCode(HttpServletResponse.SC_NOT_FOUND);
					close();
				}
			}

			data.setContentDisposition(contentDisposition);
			data.setContentLength(stream.length());
			data.setFileName(fileName);
			data.setContentType(stream.getContentType());
			data.setTextEncoding(textEncoding);

			if (stream instanceof IResourceStreamWriter)
			{
				data.setWriteCallback(new WriteCallback()
				{
					@Override
					public void writeData(Attributes attributes)
					{
						((IResourceStreamWriter)stream).write(attributes.getResponse());
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
			stream.close();
		}
		catch (IOException e)
		{
			logger.error("Couldn't close ResourceStream", e);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(ResourceStreamResource.class);
}
