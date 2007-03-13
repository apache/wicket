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
package wicket.request.target.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebResponse;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;

/**
 * Request target that responds by sending it's resources stream.
 * 
 * @author Eelco Hillenius
 */
public class ResourceStreamRequestTarget implements IRequestTarget
{
	/** Logger */
	private static final Log log = LogFactory.getLog(ResourceStreamRequestTarget.class);

	/**
	 * Optional filename, used to set the content disposition header. Only
	 * meaningful when using with web requests.
	 */
	private String fileName;

	/** the resource stream for the response. */
	private final IResourceStream resourceStream;

	/**
	 * Construct.
	 * 
	 * @param resourceStream
	 *            the resource stream for the response
	 */
	public ResourceStreamRequestTarget(IResourceStream resourceStream)
	{
		this.resourceStream = resourceStream;
	}

	/**
	 * @see wicket.IRequestTarget#detach(wicket.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
	{
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof ResourceStreamRequestTarget)
		{
			ResourceStreamRequestTarget that = (ResourceStreamRequestTarget)obj;
			return resourceStream.equals(that.resourceStream)
					&& ((fileName != null) ? fileName.equals(this.fileName) : true);
		}
		return false;
	}

	/**
	 * @return Optional filename, used to set the content disposition header.
	 *         Only meaningful when using with web requests.
	 */
	public String getFileName()
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
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		// Get servlet response to use when responding with resource
		final Response response = requestCycle.getResponse();

		configure(requestCycle, response, resourceStream);

		try
		{
			response.write(resourceStream.getInputStream());
		}
		catch (ResourceStreamNotFoundException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * @param fileName
	 *            Optional filename, used to set the content disposition header.
	 *            Only meaningful when using with web requests.
	 *            
	 * @return The this.
	 */
	public ResourceStreamRequestTarget setFileName(String fileName)
	{
		this.fileName = fileName;
		return this;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[ResourceStreamRequestTarget[resourceStream=" + resourceStream + ",fileName="
				+ fileName + "]";
	}

	/**
	 * Configures the response, default by setting the content type and length
	 * and content disposition (in case the fileName property was set).
	 * 
	 * @param requestCycle
	 * @param response
	 *            the response
	 * @param resourceStream
	 *            the resource stream that will be rendered
	 */
	protected void configure(final RequestCycle requestCycle, final Response response, final IResourceStream resourceStream)
	{
		// Configure response with content type of resource, if available
		String responseType = resourceStream.getContentType();
		if (responseType != null) {
			response.setContentType(responseType + "; charset=" + response.getCharacterEncoding());
		}
		else
		{
			// otherwise detect content-type automatically
			if (getFileName() != null)
				response.detectContentType(requestCycle, getFileName());
			else
				response.detectContentType(requestCycle, requestCycle.getRequest().getRelativeURL());
		}

		// and the content length
		response.setContentLength((int)resourceStream.length());

		// and content disposition if any
		String file = getFileName();
		if (file != null && (response instanceof WebResponse))
		{
			((WebResponse)response).setAttachmentHeader(file);
		}
	}
}
