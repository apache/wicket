/*
 * $Id$
 * $Revision$
 * $Date$
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
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.util.io.Streams;
import wicket.util.resource.IResourceStream;

/**
 * Handles event requests, like AJAX (XmlHttp) requests.
 *
 * @author Eelco Hillenius
 */
//TODO should we support other callbacks, like onComponentBody?
public abstract class EventRequestHandler implements Serializable
{
	/** log. */
	private static Log log = LogFactory.getLog(EventRequestHandler.class);

	/** The actual raw resource this class is rendering */
	protected IResourceStream resourceStream;
	
	/**
	 * Construct.
	 */
	public EventRequestHandler()
	{
	}

	/**
	 * gets the id of this handler. defaults to the objects' hashcode.
	 * @return the id of this handler
	 */
	protected String getId()
	{
		return Integer.toString(hashCode());
	}

	/**
	 * Gets the name of the event, like onchange or onclick.
	 * @return the name of the event, like onchange or onclick
	 */
	protected abstract String getEventName();

	/**
	 * Gets the resource to render to the requester.
	 * @return the resource to render to the requester
	 */
	protected abstract IResourceStream getResourceStream();

	/**
	 * This method is called everytime a this handler is added to a component.
	 * By default this does nothing.
	 * @param component the component this handler is binded to
	 */
	protected void bind(final Component component)
	{		
	}

	/**
	 * Called any time a component that has this handler registered is rendering the component tag.
	 * Use this method e.g. to bind to javascript event handlers of the tag
	 * @param component the component
	 * @param tag the tag that is rendered
	 */
	protected void onRenderComponentTag(final Component component, final ComponentTag tag)
	{
	}

	/**
	 * Responds on the event request.
	 */
	protected final void respond()
	{
		try
		{
			// Get request cycle
			final RequestCycle cycle = RequestCycle.get();

			// The cycle's page is set to null so that it won't be rendered back to
			// the client since the resource being requested has nothing to do with pages
			cycle.setResponsePage((Page)null);

			this.resourceStream = getResourceStream();
			if (this.resourceStream == null)
			{
				throw new WicketRuntimeException("Could not get resource stream");
			}

			// Get servlet response to use when responding with resource
			final Response response = cycle.getResponse();

			configure(response);

			// Respond with resource
			respond(response);
		}
		finally
		{
			resourceStream = null;
		}
	}

	/**
	 * Configures the response, default by setting the content type and length.
	 * @param response the response
	 */
	protected void configure(final Response response)
	{
		// Configure response with content type of resource
		response.setContentType("text/html");
		response.setContentLength((int)resourceStream.length());
	}

	/**
	 * Respond with resource
	 * 
	 * @param response
	 *            The response to write to
	 */
	protected final void respond(final Response response)
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

	/**
	 * Called when a event request (e.g. XmlHttpRequest) was received.
	 */
	final void onEventRequest()
	{
		respond();
	}
}
