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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.Locale;

import wicket.util.io.Streams;
import wicket.util.resource.IResource;

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
 * name) or Application.getResource(String name). The SharedResource class
 * enables easy access to such resources in a way that is light on clusters.
 * <p>
 * While resources can be shared between components, it is important to
 * emphasize that components <i>cannot </i> be shared among containers. For
 * example, you can create a button image resource with new
 * DefaultButtonImageResource(...) and store that in the Application with
 * addResource(). You can then assign that logical resource via SharedResource
 * to several ImageButton components. While the button image resource can be
 * shared between components like this, the ImageButton components in this
 * example are like all other components in Wicket and cannot be shared.
 * 
 * @author Jonathan Locke
 */
public abstract class Resource implements IResourceListener
{
	/** The actual raw resource this class is rendering */
	private IResource resource;

	/**
	 * Constructor
	 */
	protected Resource()
	{
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
		response.setContentType(resource.getContentType());

		// Respond with resource
		respond(response);
	}

	/**
	 * @param application
	 *            The application
	 * @param scope
	 *            The scope of sharing
	 * @param name
	 *            The logical name
	 * @return A shared resource reference to this resource
	 */
	public SharedResource getShared(final Application application, final Class scope,
			final String name)
	{
		return getShared(application, scope, name, null, null);
	}

	/**
	 * @param application
	 *            The application
	 * @param scope
	 *            The scope of sharing
	 * @param name
	 *            The logical name
	 * @param locale
	 *            The locale
	 * @return A shared resource reference to this resource
	 */
	public SharedResource getShared(final Application application, final Class scope,
			final String name, final Locale locale)
	{
		return getShared(application, scope, name, locale, null);
	}

	/**
	 * @param application
	 *            The application
	 * @param scope
	 *            The scope of sharing
	 * @param name
	 *            The logical name
	 * @param locale
	 *            The locale
	 * @param style
	 *            The style
	 * @return A shared resource reference to this resource
	 */
	public SharedResource getShared(final Application application, final Class scope,
			final String name, final Locale locale, final String style)
	{
		// Lazy loading of shared resource
		final Resource resource = application.getResource(scope, name, locale, style);
		if (resource == null)
		{
			application.addResource(scope, name, locale, style, this);
		}
		return new SharedResource(scope, name);
	}

	/**
	 * @return Gets the resource to render to the requester
	 */
	protected abstract IResource getResource();

	/**
	 * Sets any loaded resource to null, thus forcing a reload on the next
	 * request.
	 */
	protected void invalidate()
	{
		this.resource = null;
	}

	/**
	 * Set resource field by calling subclass
	 */
	private void init()
	{
		if (this.resource == null)
		{
			this.resource = getResource();
			if (this.resource == null)
			{
				throw new WicketRuntimeException("Could not get resource");
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
			final OutputStream out = new BufferedOutputStream(response.getOutputStream());
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

	static
	{
		RequestCycle.registerRequestListenerInterface(IResourceListener.class);
	}
}
