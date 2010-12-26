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
package org.apache.wicket.request.handler.resource;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.util.resource.IResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request target that responds by sending its resource stream.
 * 
 * @author Eelco Hillenius
 */
public class ResourceStreamRequestHandler implements IRequestHandler
{
	/** Logger */
	private static final Logger log = LoggerFactory.getLogger(ResourceStreamRequestHandler.class);

	/**
	 * Optional filename, used to set the content disposition header. Only meaningful when using
	 * with web requests.
	 */
	private String fileName;
	private ContentDisposition contentDisposition = ContentDisposition.INLINE;

	/** the resource stream for the response. */
	private final IResourceStream resourceStream;

	/**
	 * Construct.
	 * 
	 * @param resourceStream
	 *            the resource stream for the response
	 */
	public ResourceStreamRequestHandler(IResourceStream resourceStream)
	{
		this.resourceStream = resourceStream;
	}

	/**
	 * Construct.
	 * 
	 * @param resourceStream
	 *            the resource stream for the response
	 * @param fileName
	 */
	public ResourceStreamRequestHandler(IResourceStream resourceStream, String fileName)
	{
		this.resourceStream = resourceStream;
		this.fileName = fileName;
	}

	public void detach(IRequestCycle requestCycle)
	{

	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof ResourceStreamRequestHandler)
		{
			ResourceStreamRequestHandler that = (ResourceStreamRequestHandler)obj;
			return resourceStream.equals(that.resourceStream) &&
				((fileName != null) ? fileName.equals(that.fileName) : true);
		}
		return false;
	}

	/**
	 * @return Optional filename, used to set the content disposition header. Only meaningful when
	 *         using with web requests.
	 */
	public final String getFileName()
	{
		return fileName;
	}

	/**
	 * Gets the resource stream for the response.
	 * 
	 * @return the resource stream for the response
	 */
	public final IResourceStream getResourceStream()
	{
		return resourceStream;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = "ResourceStreamRequestTarget".hashCode();
		result += resourceStream.hashCode();
		result += (fileName != null) ? fileName.hashCode() : 0;
		return 17 * result;
	}

	/**
	 * Responds by sending the contents of the resource stream.
	 * 
	 * @see org.apache.wicket.request.IRequestHandler#respond(org.apache.wicket.request.IRequestCycle)
	 */
	public void respond(IRequestCycle requestCycle)
	{
		Attributes attributes = new Attributes(requestCycle.getRequest(),
			requestCycle.getResponse());

		ResourceStreamResource resource = new ResourceStreamResource(resourceStream);
		resource.setFileName(fileName);
		resource.setContentDisposition(contentDisposition);
		resource.respond(attributes);
	}

	/**
	 * @param fileName
	 *            Optional filename, used to set the content disposition header. Only meaningful
	 *            when using with web requests.
	 * 
	 * @return The this.
	 */
	public final ResourceStreamRequestHandler setFileName(String fileName)
	{
		this.fileName = fileName;
		return this;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[ResourceStreamRequestTarget[resourceStream=" + resourceStream + ",fileName=" +
			fileName + ", contentDisposition=" + contentDisposition + "]";
	}

	public final ContentDisposition getContentDisposition()
	{
		return contentDisposition;
	}

	public final ResourceStreamRequestHandler setContentDisposition(
		ContentDisposition contentDisposition)
	{
		this.contentDisposition = contentDisposition;
		return this;
	}


}
