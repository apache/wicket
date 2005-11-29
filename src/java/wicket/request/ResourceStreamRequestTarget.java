/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.request;

import java.io.OutputStream;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.util.io.Streams;
import wicket.util.resource.IResourceStream;

/**
 * Request target that responds by sending it's resources stream.
 * 
 * @author Eelco Hillenius
 */
public class ResourceStreamRequestTarget implements IRequestTarget
{
	/** the resource stream for the response. */
	private final IResourceStream resourceStream;

	/** the response type, eg 'text/html' . */
	private final String responseType;

	/**
	 * Construct.
	 * 
	 * @param resourceStream
	 *            the resource stream for the response
	 * @param responseType
	 *            the response type, eg 'text/html'
	 */
	public ResourceStreamRequestTarget(IResourceStream resourceStream, String responseType)
	{
		if (resourceStream == null)
		{
			throw new NullPointerException("argument resourceStream must be not null");
		}

		if (responseType == null)
		{
			throw new NullPointerException("argument responseType must be not null");
		}

		this.resourceStream = resourceStream;
		this.responseType = responseType;
	}

	/**
	 * Does nothing at all.
	 * 
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		// Get servlet response to use when responding with resource
		final Response response = requestCycle.getResponse();

		configure(response, resourceStream);

		// Respond with resource
		try
		{
			final OutputStream out = response.getOutputStream();
			try
			{
				Streams.copy(resourceStream.getInputStream(), out);
			}
			finally
			{
				resourceStream.close();
				out.flush();
			}
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Unable to render resource stream " + resourceStream,
					e);
		}
	}

	/**
	 * Configures the response, default by setting the content type and length.
	 * 
	 * @param response
	 *            the response
	 * @param resourceStream
	 *            the resource stream that will be rendered
	 */
	protected void configure(final Response response, final IResourceStream resourceStream)
	{
		// Configure response with content type of resource
		response.setContentType(responseType + ";charset=" + response.getCharacterEncoding());
		// and the content length
		response.setContentLength((int)resourceStream.length());
	}

	/**
	 * @see wicket.IRequestTarget#cleanUp(wicket.RequestCycle)
	 */
	public void cleanUp(RequestCycle requestCycle)
	{
	}
}
