/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.servlet.http.HttpServletResponse;

import wicket.protocol.http.WebResponse;
import wicket.util.io.Streams;
import wicket.util.resource.IResource;

/**
 * Base class for components which are also resources in their own right.
 * 
 * @author Jonathan Locke
 */
public abstract class ResourceComponent extends Component implements IResourceListener
{
	/**
	 * @see Component#Component(String)
	 */
	public ResourceComponent(String name)
	{
		super(name);
	}

	/**
	 * @see Component#Component(String, Serializable)
	 */
	public ResourceComponent(final String name, final Serializable object)
	{
		super(name, object);
	}

	/**
	 * @see Component#Component(String, Serializable, String)
	 */
	public ResourceComponent(final String name, final Serializable object, final String expression)
	{
		super(name, object, expression);
	}

	/**
	 * Called when a resource is requested.
	 */
	public void onResourceRequested()
	{
		// Get request cycle
		final RequestCycle cycle = getRequestCycle();

		// The cycle's page is set to null so that it won't be rendered back to
		// the client since the resource being requested has nothing to do with
		// pages
		cycle.setPage((Page)null);

		// Get servlet response to use when responding with resource
		final HttpServletResponse httpServletResponse = ((WebResponse)getResponse())
				.getHttpServletResponse();
		
		// Configure response header
		configureResponse(httpServletResponse);
		
		// Respond with resource
		respond(httpServletResponse);
	}

	/**
	 * Set-up response with appropriate content type
	 * 
	 * @param httpServletResponse
	 *            Servlet response object to configure
	 */
	protected void configureResponse(final HttpServletResponse httpServletResponse)
	{
		final IResource resource = getResource();
	    if (resource == null)
	    {
	        throw new WicketRuntimeException("Could not get resource");
	    }
	    else
	    {
			// Respond with image
			httpServletResponse.setContentType(resource.getContentType());
	    }
	}

	/**
	 * @return Gets the image resource to render
	 */
	protected abstract IResource getResource();

	/**
	 * Renders this component.
	 */
	protected void onRender()
	{
		renderComponent(findMarkupStream());
	}

	/**
	 * Respond with resource
	 * 
	 * @param httpServletResponse
	 *            Servlet response object to write to
	 */
	protected void respond(final HttpServletResponse httpServletResponse)
	{
		final IResource resource = getResource();
	    if (resource == null)
	    {
	        throw new WicketRuntimeException("Could not get resource");
	    }
	    else
	    {
			try
			{
				final OutputStream out = new BufferedOutputStream(httpServletResponse.getOutputStream());
				try
				{
					Streams.writeStream(new BufferedInputStream(resource.getInputStream()), out);
				}
				finally
				{
					resource.close();
					out.flush();
				}
			}
			catch (Exception e)
			{
				throw new WicketRuntimeException("Unable to render resource " + resource, e);
			}
	    }
	}

	static
    {
        RequestCycle.registerRequestListenerInterface(IResourceListener.class);
    }
}
