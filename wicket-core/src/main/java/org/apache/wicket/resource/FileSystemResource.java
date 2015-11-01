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
package org.apache.wicket.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.PartWriterCallback;

/**
 * Used to provide resources based on the on Java NIO FileSystem API.<br>
 * <br>
 * For more information see {@link org.apache.wicket.markup.html.media.FileSystemResourceReference}
 * 
 * @author Tobias Soloschenko
 *
 */
public class FileSystemResource extends AbstractResource
{
	private static final long serialVersionUID = 1L;

	private Path path;

	/**
	 * Creates a new file system resource based on the given path
	 * 
	 * @param path
	 *            the path to be read for the resource
	 */
	public FileSystemResource(Path path)
	{
		this.path = path;
	}

	/**
	 * Creates a new resource response and reads the given path
	 */
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes)
	{
		try
		{
			long size = getSize();
			ResourceResponse resourceResponse = new ResourceResponse();
			resourceResponse.setContentType(getMimeType());
			resourceResponse.setAcceptRange(ContentRangeType.BYTES);
			resourceResponse.setContentLength(size);
			resourceResponse = adjustResourceResponse(resourceResponse);
			RequestCycle cycle = RequestCycle.get();
			Long startbyte = cycle.getMetaData(CONTENT_RANGE_STARTBYTE);
			Long endbyte = cycle.getMetaData(CONTENT_RANGE_ENDBYTE);
			resourceResponse.setWriteCallback(new PartWriterCallback(getInputStream(), size,
				startbyte, endbyte));
			return resourceResponse;
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(
				"An error occured while processing the media resource response", e);
		}
	}

	/**
	 * Adjusts the resource response by overriding it with additional information
	 * 
	 * @param resourceResponse
	 *            the resource response to adjust
	 * 
	 * @return the resource response to be adjusted
	 */
	protected ResourceResponse adjustResourceResponse(ResourceResponse resourceResponse)
	{
		return resourceResponse;
	}

	/**
	 * Gets the size of the resource
	 * 
	 * @return the size of the resource
	 * @throws IOException
	 *             if the size attribute can't be read
	 */
	protected long getSize() throws IOException
	{
		return Files.readAttributes(path, BasicFileAttributes.class).size();
	}

	/**
	 * Gets the mime type to be used for the response
	 * 
	 * @return the mime type to be used for the response
	 * @throws IOException
	 *             if the mime type could'nt be resoulved
	 */
	protected String getMimeType() throws IOException
	{
		return Files.probeContentType(path);
	}

	/**
	 * Gets the input stream of the given path
	 * 
	 * @return the input stream of the given path
	 * @throws IOException
	 *             if there is an exception while receiving the input stream
	 */
	protected InputStream getInputStream() throws IOException
	{
		return Files.newInputStream(path);
	}
}
