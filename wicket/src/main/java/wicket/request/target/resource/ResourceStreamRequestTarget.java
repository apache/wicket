/*
 * $Id: ResourceStreamRequestTarget.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20
 * May 2006) joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat,
 * 20 May 2006) $
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
package wicket.request.target.resource;

import java.io.OutputStream;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebResponse;
import wicket.util.io.Streams;
import wicket.util.resource.IResourceStream;

/**
 * Request target that responds by sending it's resources stream.
 * 
 * @author Eelco Hillenius
 */
public class ResourceStreamRequestTarget implements IRequestTarget
{
	/** Logger */
	private static final Logger log = LoggerFactory.getLogger(ResourceStreamRequestTarget.class);

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
		if (resourceStream == null)
		{
			throw new IllegalArgumentException("Argument resourceStream must be not null");
		}

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
	@Override
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
	 * @see wicket.IRequestTarget#getLock(RequestCycle)
	 */
	public Object getLock(RequestCycle requestCycle)
	{
		return null;
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
			Throwable throwable = e;
			boolean ignoreException = false;
			while (throwable != null)
			{
				if (throwable instanceof SocketException)
				{
					String message = throwable.getMessage();
					ignoreException = message != null
							&& (message.indexOf("Connection reset by peer") != -1 || message
									.indexOf("Software caused connection abort") != -1);
				}
				else
				{
					ignoreException = throwable.getClass().getName()
							.indexOf("ClientAbortException") >= 0;
					if (ignoreException)
					{
						if (log.isDebugEnabled())
						{
							log.debug("Socket exception ignored for sending Resource "
									+ "response to client (ClientAbort)", e);
						}
						break;
					}
				}
				throwable = throwable.getCause();
			}
			if (!ignoreException)
			{
				throw new WicketRuntimeException("Unable to render resource stream "
						+ resourceStream, e);
			}
		}
	}

	/**
	 * @param fileName
	 *            Optional filename, used to set the content disposition header.
	 *            Only meaningful when using with web requests.
	 * @return this
	 *         
	 */
	public ResourceStreamRequestTarget setFileName(String fileName)
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
		return "[ResourceStreamRequestTarget[resourceStream=" + resourceStream + ",fileName="
				+ fileName + "]";
	}

	/**
	 * Configures the response, default by setting the content type and length
	 * and content disposition (in case the fileName property was set).
	 * 
	 * @param response
	 *            the response
	 * @param resourceStream
	 *            the resource stream that will be rendered
	 */
	protected void configure(final Response response, final IResourceStream resourceStream)
	{
		// Configure response with content type of resource
		response.setContentType(resourceStream.getContentType() + ";charset="
				+ response.getCharacterEncoding());

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
