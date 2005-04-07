/*
 * $Id$ $Revision:
 * 1.4 $ $Date$
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
package wicket;

import java.io.OutputStream;

import wicket.util.io.Streams;
import wicket.util.resource.IResourceStream;

/**
 * A Resource is something that implements IResourceListener and provides a
 * getResource() method which returns the raw IResource to be rendered back to
 * the client browser.
 * <p>
 * Resources themselves do not currently have URLs. Instead, they are referred
 * to by components that have URLs.
 * <p>
 * Resources can be shared throughout an application by adding them to
 * Application with addResource(Class scope, String name) or addResource(String
 * name). A resource added in such a way is a named resource and is accessible
 * throughout the application via Application.getResource(Class scope, String
 * name) or Application.getResource(String name). The ResourceReference class
 * enables easy access to such resources in a way that is light on clusters.
 * <p>
 * While resources can be shared between components, it is important to
 * emphasize that components <i>cannot </i> be shared among containers. For
 * example, you can create a button image resource with new
 * DefaultButtonImageResource(...) and store that in the Application with
 * addResource(). You can then assign that logical resource via ResourceReference
 * to several ImageButton components. While the button image resource can be
 * shared between components like this, the ImageButton components in this
 * example are like all other components in Wicket and cannot be shared.
 * 
 * @author Jonathan Locke
 */
public abstract class Resource implements IResourceListener
{
	/** The actual raw resource this class is rendering */
	protected IResourceStream resourceStream;

	private boolean cacheable;
	
	/**
	 * Constructor
	 */
	protected Resource()
	{
		// default nothing is cacheable
		cacheable = false;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Called when a resource is requested.
	 */
	public final void onResourceRequested()
	{
		// Get request cycle
		final RequestCycle cycle = RequestCycle.get();

		// The cycle's page is set to null so that it won't be rendered back to
		// the client since the resource being requested has nothing to do with
		// pages
		cycle.setResponsePage((Page)null);

		// Fetch resource from subclass if necessary
		init();

		// Get servlet response to use when responding with resource
		final Response response = cycle.getResponse();

		// Configure response with content type of resource
		response.setContentType(resourceStream.getContentType());
		response.setContentLength((int)resourceStream.length());

		if(cacheable)
		{
			// Don't set this above setContentLength call above. 
			// The call above could create and set the last modified time.  
			response.setLastModifiedTime(resourceStream.lastModifiedTime());
		}

		// Respond with resource
		respond(response);
	}

	/**
	 * @return Gets the resource to render to the requester
	 */
	public abstract IResourceStream getResourceStream();

	/**
	 * Sets any loaded resource to null, thus forcing a reload on the next
	 * request.
	 */
	protected void invalidate()
	{
		this.resourceStream = null;
	}

	/**
	 * Set resource field by calling subclass
	 */
	private void init()
	{
		if (this.resourceStream == null)
		{
			this.resourceStream = getResourceStream();
			if (this.resourceStream == null)
			{
				throw new WicketRuntimeException("Could not get resource stream");
			}
		}
	}

	/**
	 * Respond with resource
	 * 
	 * @param response
	 *            The response to write to
	 */
	private final void respond(final Response response)
	{
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
			throw new WicketRuntimeException("Unable to render resource stream " + resourceStream, e);
		}
	}

	static
	{
		RequestCycle.registerRequestListenerInterface(IResourceListener.class);
	}

	/**
	 * @return boolean True or False if this resource is cacheable
	 */
	public boolean isCacheable()
	{
		return cacheable;
	}

	/**
	 * Should this resource be cacheable, so will it set the last modified and the some cache headers in the response.
	 * @param cacheable
	 * 			boolean if the lastmodified and cache headers must be set.
	 */
	public void setCacheable(boolean cacheable)
	{
		this.cacheable = cacheable;
	}
}
