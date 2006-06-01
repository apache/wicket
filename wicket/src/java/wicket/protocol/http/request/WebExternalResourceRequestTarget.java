/*
 * $Id: WebExternalResourceRequestTarget.java,v 1.1 2005/11/24 20:59:02 eelco12
 * Exp $ $Revision$ $Date$
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
package wicket.protocol.http.request;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebResponse;
import wicket.util.io.Streams;

/**
 * Request target that is not a Wicket resource. For example, such a resource
 * could denote an image in the web application directory (not mapped to a
 * Wicket servlet). NOTE: this target can only be used in a servlet environment
 * with {@link wicket.protocol.http.WebRequestCycle}s.
 * 
 * @author Eelco Hillenius
 */
public class WebExternalResourceRequestTarget implements IRequestTarget
{
	/** log. */
	private static final Log log = LogFactory.getLog(WebExternalResourceRequestTarget.class);

	/** the relative url of the external resource. */
	private final String url;

	/**
	 * Construct.
	 * 
	 * @param url
	 *            the relative url of the external resource
	 */
	public WebExternalResourceRequestTarget(String url)
	{
		if (url == null)
		{
			throw new IllegalArgumentException("Argument url must be not null");
		}

		this.url = url;
	}

	/**
	 * Respond by trying to delegate getting the resource from the
	 * {@link ServletContext} object and stream that to the client. If such a
	 * resource is not found, a warning will be logged, and a 404 will be
	 * issued.
	 * 
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		try
		{
			WebResponse webResponse = ((WebRequestCycle)requestCycle).getWebResponse();
			final ServletContext context = ((WebApplication)requestCycle.getApplication())
					.getServletContext();

			// Set content type
			webResponse.setContentType(context.getMimeType(url));
			final InputStream in = context.getResourceAsStream(url);
			if (in != null)
			{
				try
				{
					// Copy resource input stream to servlet output stream
					Streams.copy(in, webResponse.getHttpServletResponse().getOutputStream());
				}
				finally
				{
					// NOTE: We only close the InputStream. The servlet
					// container should close the output stream.
					in.close();
				}
			}
			else
			{
				log.warn("the resource requested by request " + requestCycle.getRequest()
						+ " was not found");
				HttpServletResponse httpServletResponse = webResponse.getHttpServletResponse();
				httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Cannot load static content for request "
					+ requestCycle.getRequest(), e);
		}
	}

	/**
	 * Gets the url to the external resource.
	 * 
	 * @return the url to the external resource
	 */
	public final String getUrl()
	{
		return url;
	}

	/**
	 * @see wicket.IRequestTarget#detach(wicket.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
	{
	}

	/**
	 * @see wicket.IRequestTarget#getLock(RequestCycle)
	 */
	public Object getLock(RequestCycle requestCycle)
	{
		return null;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof WebExternalResourceRequestTarget)
		{
			WebExternalResourceRequestTarget that = (WebExternalResourceRequestTarget)obj;
			return url.equals(that.url);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = "WebExternalResourceRequestTarget".hashCode();
		result += url.hashCode();
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[WebExternalResourceRequestTarget@" + hashCode() + " " + url + "]";
	}
}