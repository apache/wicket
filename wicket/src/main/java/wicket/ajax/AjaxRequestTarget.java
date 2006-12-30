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
package wicket.ajax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.Application;
import wicket.Component;
import wicket.IRequestTarget;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.RequestCycle;
import wicket.Response;
import wicket.Component.IVisitor;
import wicket.feedback.IFeedback;
import wicket.markup.html.IHeaderResponse;
import wicket.markup.html.internal.HeaderResponse;
import wicket.protocol.http.WebResponse;
import wicket.response.StringResponse;
import wicket.util.string.Strings;

/**
 * A request target that produces ajax response envelopes used on the client
 * side to update component markup as well as evaluate arbitrary javascript.
 * <p>
 * A component whose markup needs to be updated should be added to this target
 * via AjaxRequestTarget#addComponent(Component) method. Its body will be
 * rendered and added to the envelope when the target is processed, and
 * refreshed on the client side when the ajax response is received.
 * <p>
 * It is important that the component whose markup needs to be updated contains
 * an id attribute in the generated markup that is equal to the value retrieved
 * from Component#getMarkupId(). This can be accomplished by either setting the
 * id attribute in the html template, or using an attribute modifier that will
 * add the attribute with value Component#getMarkupId() to the tag (such as
 * MarkupIdSetter)
 * <p>
 * Any javascript that needs to be evaluater on the client side can be added
 * using AjaxRequestTarget#addJavascript(String). For example, this feature can
 * be useful when it is desirable to link component update with some javascript
 * effects.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class AjaxRequestTarget implements IRequestTarget
{
	private static final Logger Log = LoggerFactory.getLogger(AjaxRequestTarget.class);

	/** */
	private final List<String> appendJavascripts = new ArrayList<String>();

	/** */
	private final List<String> prependJavascripts = new ArrayList<String>();

	/** the component instances that will be rendered */
	private final Map<String, Component> markupIdToComponent = new HashMap<String, Component>();

	/**
	 * Constructor
	 */
	public AjaxRequestTarget()
	{
	}

	/**
	 * Adds a component to the list of components to be rendered
	 * 
	 * @param component
	 *            component to be rendered
	 */
	public final void addComponent(Component component)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("component cannot be null");
		}
		if (component.getOutputMarkupId() == false
				&& !component.getMarkupAttributes().containsKey("id"))
		{
			throw new IllegalArgumentException(
					"cannot update component that does not have setOutputMarkupId property set to true or id attribute set in its markup attributes. Component: "
							+ component.toString());
		}
		addComponent(component, component.getMarkupId());
	}

	/**
	 * Adds a component to the list of components to be rendered
	 * 
	 * @param markupId
	 *            id of client-side dom element that will be updated
	 * 
	 * @param component
	 *            component to be rendered
	 */
	public final void addComponent(Component component, String markupId)
	{
		if (Strings.isEmpty(markupId))
		{
			throw new IllegalArgumentException("markupId cannot be empty");
		}
		if (component == null)
		{
			throw new IllegalArgumentException("component cannot be null");
		}
		else if (component instanceof Page)
		{
			throw new IllegalArgumentException("component cannot be a page");
		}

		markupIdToComponent.put(markupId, component);
	}

	/**
	 * Adds javascript that will be evaluated on the client side after
	 * components are replaced
	 * 
	 * @param javascript
	 */
	public final void appendJavascript(String javascript)
	{
		if (javascript == null)
		{
			throw new IllegalArgumentException("javascript cannot be null");
		}

		appendJavascripts.add(javascript);
	}

	/**
	 * @see wicket.IRequestTarget#detach(wicket.RequestCycle)
	 */
	public void detach(final RequestCycle requestCycle)
	{
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof AjaxRequestTarget)
		{
			AjaxRequestTarget that = (AjaxRequestTarget)obj;
			return markupIdToComponent.equals(that.markupIdToComponent)
					&& prependJavascripts.equals(that.prependJavascripts)
					&& appendJavascripts.equals(that.appendJavascripts);
		}
		return false;
	}

	/**
	 * @see wicket.IRequestTarget#getLock(RequestCycle)
	 */
	public Object getLock(final RequestCycle requestCycle)
	{
		return requestCycle.getSession();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = "AjaxRequestTarget".hashCode();
		result += markupIdToComponent.hashCode() * 17;
		result += prependJavascripts.hashCode() * 17;
		result += appendJavascripts.hashCode() * 17;
		return result;
	}

	/**
	 * Adds javascript that will be evaluated on the client side before
	 * components are replaced
	 * 
	 * @param javascript
	 */
	public final void prependJavascript(String javascript)
	{
		if (javascript == null)
		{
			throw new IllegalArgumentException("javascript cannot be null");
		}

		prependJavascripts.add(javascript);
	}

	/**
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public final void respond(final RequestCycle requestCycle)
	{
		final WebResponse response = (WebResponse)requestCycle.getResponse();
		try
		{
			final Application app = Application.get();

			// Determine encoding
			final String encoding = app.getRequestCycleSettings().getResponseRequestEncoding();

			// Set content type based on markup type for page
			response.setCharacterEncoding(encoding);
			response.setContentType("text/xml; charset=" + encoding);

			// Make sure it is not cached by a
			response.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
			response.setHeader("Cache-Control", "no-cache, must-revalidate");
			response.setHeader("Pragma", "no-cache");

			response.write("<?xml version=\"1.0\" encoding=\"");
			response.write(encoding);
			response.write("\"?>");
			response.write("<ajax-response>");

			// normal behavior

			for (String js : prependJavascripts)
			{
				respondInvocation(response, js);
			}

			respondComponents(response);

			for (String js : appendJavascripts)
			{
				respondInvocation(response, js);
			}

			response.write("</ajax-response>");
		}
		catch (RuntimeException ex)
		{
			// log the error but output nothing in the response, parse failure
			// of response will cause any javascript failureHandler to be
			// invoked
			Log.error("Error while responding to an AJAX request: " + toString(), ex);
		}
		finally
		{
			requestCycle.setResponse(response);
		}
	}

	/**
	 * Processes components added to the target. This involves attaching
	 * components, rendering markup into a client side xml envelope, and
	 * detaching them
	 * 
	 * @param response
	 */
	private void respondComponents(WebResponse response)
	{
		Iterator it;

		try
		{
			// process feedback
			it = markupIdToComponent.entrySet().iterator();
			while (it.hasNext())
			{
				final Component component = (Component)((Entry)it.next()).getValue();
				if (component instanceof MarkupContainer)
				{
					MarkupContainer container = (MarkupContainer)component;

					// collect feedback
					container.visitChildren(IFeedback.class, new IVisitor()
					{
						public Object component(Component component)
						{
							((IFeedback)component).updateFeedback();
							return IVisitor.CONTINUE_TRAVERSAL;
						}
					});
				}

				if (component instanceof IFeedback)
				{
					((IFeedback)component).updateFeedback();
				}
			}

			// attach components
			it = markupIdToComponent.entrySet().iterator();
			while (it.hasNext())
			{
				final Component component = (Component)((Entry)it.next()).getValue();
				component.internalAttach();
			}

			// process component markup
			it = markupIdToComponent.entrySet().iterator();
			while (it.hasNext())
			{
				final Map.Entry entry = (Entry)it.next();
				final Component component = (Component)entry.getValue();
				final String markupId = (String)entry.getKey();

				respondComponent(response, markupId, component);
			}

		}
		finally
		{
			// detach
			it = markupIdToComponent.entrySet().iterator();
			if (it.hasNext())
			{
				final Component component = (Component)((Entry)it.next()).getValue();
				component.getPage().internalDetach();
			}
		}
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[AjaxRequestTarget@" + hashCode() + " markupIdToComponent [" + markupIdToComponent
				+ "], prependJavascript [" + prependJavascripts + "], appendJavascript ["
				+ appendJavascripts + "]";
	}

	/**
	 * Encodes a string so it is safe to use inside CDATA blocks
	 * 
	 * @param str
	 * @return encoded string
	 */
	protected String encode(String str)
	{
		return str.replace("]", "]^");
	}

	/**
	 * @return name of encoding used to possibly encode the contents of the
	 *         CDATA blocks
	 */
	protected String getEncodingName()
	{
		return "wicket1";
	}

	/**
	 * 
	 * @param str
	 * @return true if string needs to be encoded, false otherwise
	 */
	protected boolean needsEncoding(String str)
	{
		/*
		 * TODO Post 1.2: Ajax: we can improve this by keeping a buffer of at
		 * least 3 characters and checking that buffer so that we can narrow
		 * down escaping occuring only for ']]>' sequence, or at least for ]] if ]
		 * is the last char in this buffer.
		 * 
		 * but this improvement will only work if we write first and encode
		 * later instead of working on fragments sent to write
		 */

		return str.indexOf(']') >= 0;
	}

	/**
	 * 
	 * @param response
	 * @param markupId
	 *            id of client-side dom element
	 * @param component
	 *            component to render
	 */
	private void respondComponent(final Response response, final String markupId,
			final Component component)
	{
		if (component.getRenderBodyOnly() == true)
		{
			throw new IllegalStateException(
					"Ajax render cannot be called on component that has setRenderBodyOnly enabled. Component: "
							+ component.toString());
		}

		component.setOutputMarkupId(true);

		// Initialize temporary variables
		final Page page = component.getPage();
		if (page == null)
		{
			throw new IllegalStateException(
					"Ajax request attempted on a component that is not associated with a Page");
		}

		final boolean versioned = page.isVersioned();
		page.setVersioned(false);

		page.startComponentRender(component);

		// render any associated headers of the component
		respondHeaderContribution(response, component);

		Response componentResponse = new StringResponse();
		try
		{
			RequestCycle.get().setResponse(componentResponse);

			component.renderComponent();
		}
		finally
		{
			// Restore original response
			RequestCycle.get().setResponse(response);
		}

		page.endComponentRender(component);
		page.setVersioned(versioned);

		response.write("<component id=\"");
		response.write(markupId);
		response.write("\" ");
		String data = componentResponse.toString();
		if (needsEncoding(componentResponse.toString()))
		{
			response.write(" encoding=\"");
			response.write(getEncodingName());
			response.write("\" ");
			data = encode(data);
		}
		response.write("><![CDATA[");
		response.write(data);
		response.write("]]></component>");
	}

	/**
	 * 
	 * @param response
	 * @param component
	 */
	private void respondHeaderContribution(final Response response, final Component component)
	{
		Response encodingResponse = new StringResponse();

		try
		{
			final IHeaderResponse headerResponse = new HeaderResponse(encodingResponse);
			RequestCycle.get().setResponse(headerResponse.getResponse());
			component.renderHead(headerResponse);
		}
		finally
		{
			RequestCycle.get().setResponse(response);
		}

		String data = encodingResponse.toString();

		if (data.length() != 0)
		{
			response.write("<header-contribution");

			if (needsEncoding(encodingResponse.toString()))
			{
				response.write(" encoding=\"");
				response.write(getEncodingName());
				response.write("\" ");
				data = encode(data);
			}

			// we need to write response as CDATA and parse it on client,
			// because konqueror crashes when there is a <script> element
			response.write("><![CDATA[<head xmlns:wicket=\"http://wicket.sourceforge.net\">");
			response.write(data);
			response.write("</head>]]>");
			response.write("</header-contribution>");
		}
	}

	/**
	 * 
	 * @param response
	 * @param js
	 */
	private void respondInvocation(final Response response, final String js)
	{
		boolean encoded = false;
		String javascript = js;

		// encode the response if needed
		if (needsEncoding(js))
		{
			encoded = true;
			javascript = encode(js);
		}

		response.write("<evaluate");
		if (encoded)
		{
			response.write(" encoding=\"");
			response.write(getEncodingName());
			response.write("\"");
		}
		response.write(">");
		response.write("<![CDATA[");
		response.write(javascript);
		response.write("]]>");
		response.write("</evaluate>");
	}
}