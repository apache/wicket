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
package wicket.markup.html.ajax;

import java.io.OutputStream;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.IEventRequestHandler;
import wicket.Page;
import wicket.RequestCycle;
import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.markup.html.HtmlHeaderContainer;
import wicket.markup.html.IBodyOnloadContributor;
import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.PackageResourceReference;
import wicket.util.io.Streams;
import wicket.util.resource.IResourceStream;

/**
 * Handles event requests, like AJAX (XmlHttp) requests.
 *
 * @author Eelco Hillenius
 */
//TODO should we support other callbacks, like onComponentBody?
public abstract class AbstractEventRequestHandler
	implements Serializable, IEventRequestHandler, IHeaderContributor, IBodyOnloadContributor
{
	/** log. */
	private static Log log = LogFactory.getLog(AbstractEventRequestHandler.class);

	/** The actual raw resource this class is rendering */
	protected IResourceStream resourceStream;

	/** thread local for onload contributions. */
	private static final ThreadLocal bodyOnloadContribHolder = new ThreadLocal();

	/** thread local for head contributions. */
	private static final ThreadLocal headContribHolder = new ThreadLocal();

	/** we just need one simple indicator object to put in our thread locals. */
	private static final Object dummy = new Object();

	/**
	 * Construct.
	 */
	public AbstractEventRequestHandler()
	{
	}

	/**
	 * @see wicket.IEventRequestHandler#getId()
	 */
	public String getId()
	{
		return Integer.toString(hashCode());
	}

	/**
	 * @see wicket.markup.html.IBodyOnloadContributor#getBodyOnload()
	 */
	public final String getBodyOnload()
	{
		String staticContrib = null;
		if (bodyOnloadContribHolder.get() == null)
		{
			bodyOnloadContribHolder.set(dummy);
			staticContrib = getBodyOnloadInitContribution();
		}
		String contrib = doGetBodyOnload();
		if (staticContrib != null)
		{
			return (contrib != null) ? staticContrib + contrib : staticContrib;
		}
		return contrib;
	}

	/**
	 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.markup.html.HtmlHeaderContainer)
	 */
	public final void renderHead(HtmlHeaderContainer container)
	{
		if (headContribHolder.get() == null)
		{
			headContribHolder.set(dummy);
			printHeadInitContribution(container);
		}
		doPrintHead(container);
	}

	/**
	 * One time (per page) body onload contribution that is the same for all ajax variant
	 * implementations (e.g. Dojo, Rico, Qooxdoo).
	 * @return the onload statement(s) for the body component
	 */
	public String getBodyOnloadInitContribution()
	{
		return null;
	}

	/**
	 * One time (per page) header contribution that is the same for all ajax variant
	 * implementations (e.g. Dojo, Rico, Qooxdoo).
	 * @param container head container
	 */
	public void printHeadInitContribution(HtmlHeaderContainer container)
	{
	}

	/**
	 * Gets the onload statement(s) for the body component.
	 * @return the onload statement(s) for the body component
	 */
	public String doGetBodyOnload()
	{
		return null;
	}

	/**
	 * Let this handler print out the needed header contributions.
	 * @param container head container
	 */
	public void doPrintHead(HtmlHeaderContainer container)
	{
	}

	/**
	 * @see wicket.markup.html.ajax.AbstractEventRequestHandler#onEventRequest()
	 */
	public void onEventRequest()
	{
		respond();
	}

	/**
	 * @see wicket.IEventRequestHandler#rendered(wicket.Component)
	 */
	public void rendered(Component component)
	{
		bodyOnloadContribHolder.set(null);
		headContribHolder.set(null);
		onRendered(component);
	}

	/**
	 * Called to indicate that the component that has this handler registered has been rendered.
	 * Use this method to do any cleaning up of temporary state
	 * @param component the component
	 */
	protected void onRendered(Component component)
	{
	}

	/**
	 * Configures the response, default by setting the content type and length.
	 * @param response the response
	 */
	protected void configure(final Response response)
	{
		// Configure response with content type of resource
		response.setContentType(getResponseType());
		response.setContentLength((int)resourceStream.length());
	}

	/**
	 * Gets the response type mime, e.g. 'text/html' or 'text/javascript'.
	 * @return the response type mime
	 */
	protected String getResponseType()
	{
		return "text/html";
	}

	/**
	 * Gets the response to render to the requester.
	 * @return the response to render to the requester
	 */
	protected abstract IResourceStream getResponse();

	/**
	 * Convenience method to add a javascript reference.
	 * @param container the header container
	 * @param ref reference to add
	 */
	protected void addJsReference(HtmlHeaderContainer container, PackageResourceReference ref)
	{
		String url = container.getPage().urlFor(ref.getPath());
		String s = 
			"\t<script language=\"JavaScript\" type=\"text/javascript\" " +
			"src=\"" + url + "\"></script>\n";
		write(container, s);
	}

	/**
	 * Writes the given string to the header container.
	 * @param container the header container
	 * @param s the string to write
	 */
	private void write(HtmlHeaderContainer container, String s)
	{
		container.getResponse().write(s);
	}

	/**
	 * Responds on the event request.
	 */
	private final void respond()
	{
		try
		{
			// Get request cycle
			final RequestCycle cycle = RequestCycle.get();

			// The cycle's page is set to null so that it won't be rendered back to
			// the client since the resource being requested has nothing to do with pages
			cycle.setResponsePage((Page)null);

			this.resourceStream = getResponse();
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
	 * Respond.
	 * @param response the response to write to
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
}
