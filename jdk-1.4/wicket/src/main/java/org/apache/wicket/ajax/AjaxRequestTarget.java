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
package org.apache.wicket.ajax;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Response;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.Component.IVisitor;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.internal.HeaderResponse;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;


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
 * add the attribute with value Component#getMarkupId() to the tag ( such as
 * MarkupIdSetter )
 * <p>
 * Any javascript that needs to be evaluated on the client side can be added
 * using AjaxRequestTarget#append/prependJavascript(String). For example, this
 * feature can be useful when it is desirable to link component update with some
 * javascript effects.
 * <p>
 * The target provides a listener interface {@link IListener} that can be used
 * to add code that responds to various target events by adding listeners via
 * {@link #addListener(org.apache.wicket.ajax.AjaxRequestTarget.IListener)}
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Eelco Hillenius
 */
public class AjaxRequestTarget implements IRequestTarget
{
	/**
	 * An {@link AjaxRequestTarget} listener that can be used to respond to
	 * various target-related events
	 * 
	 */
	public static interface IListener
	{
		/**
		 * Triggered before ajax request target begins its response cycle
		 * 
		 * @param map
		 *            read-only map:markupId->component of components already
		 *            added to the target
		 * @param target
		 *            the target itself. Could be used to add components or to
		 *            append/prepend javascript
		 * 
		 */
		public void onBeforeRespond(Map map, AjaxRequestTarget target);

		/**
		 * Triggered after ajax request target is done with its response cycle.
		 * At this point only additional javascript can be output to the
		 * response using the provided {@link IJavascriptResponse} object
		 * 
		 * NOTE: During this stage of processing any calls to target that
		 * manipulate the response (adding components, javascript) will have no
		 * effect
		 * 
		 * @param map
		 *            read-only map:markupId->component of components already
		 *            added to the target
		 * @param response
		 *            response object that can be used to output javascript
		 */
		public void onAfterRespond(Map map, IJavascriptResponse response);
	}

	/**
	 * An ajax javascript response that allows users to add javascript to be
	 * executed on the client side
	 * 
	 * @author ivaynberg
	 */
	public static interface IJavascriptResponse
	{
		/**
		 * Adds more javascript to the ajax response that will be executed on
		 * the client side
		 * 
		 * @param script
		 *            javascript
		 */
		public void addJavascript(String script);
	}

	/**
	 * Response that uses an encoder to encode its contents
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 */
	private final class AjaxResponse extends WebResponse
	{
		private final AppendingStringBuffer buffer = new AppendingStringBuffer(256);

		private boolean escaped = false;

		private final Response originalResponse;

		/**
		 * Construct.
		 * 
		 * @param originalResponse
		 */
		public AjaxResponse(Response originalResponse)
		{
			super(((WebResponse)originalResponse).getHttpServletResponse());
			this.originalResponse = originalResponse;
			setAjax(true);
		}

		/**
		 * @see org.apache.wicket.Response#encodeURL(CharSequence)
		 */
		public CharSequence encodeURL(CharSequence url)
		{
			return originalResponse.encodeURL(url);
		}

		/**
		 * @return contents of the response
		 */
		public CharSequence getContents()
		{
			return buffer;
		}

		/**
		 * NOTE: this method is not supported
		 * 
		 * @see org.apache.wicket.Response#getOutputStream()
		 */
		public OutputStream getOutputStream()
		{
			throw new UnsupportedOperationException("Cannot get output stream on StringResponse");
		}

		/**
		 * @return true if any escaping has been performed, false otherwise
		 */
		public boolean isContentsEncoded()
		{
			return escaped;
		}

		/**
		 * Resets the response to a clean state so it can be reused to save on
		 * garbage.
		 */
		public void reset()
		{
			buffer.clear();
			escaped = false;

		}

		/**
		 * @see org.apache.wicket.Response#write(CharSequence)
		 */
		public void write(CharSequence cs)
		{
			String string = cs.toString();
			if (needsEncoding(string))
			{
				string = encode(string);
				escaped = true;
				buffer.append(string);
			}
			else
			{
				buffer.append(cs);
			}
		}

	}

	private static final Log LOG = LogFactory.getLog(AjaxRequestTarget.class);

	private final List/* <String> */appendJavascripts = new ArrayList();

	/**
	 * Create a response for component body and javascript that will escape
	 * output to make it safe to use inside a CDATA block
	 */
	private final AjaxResponse encodingBodyResponse;

	/**
	 * Response for header contributon that will escape output to make it safe
	 * to use inside a CDATA block
	 */
	private final AjaxResponse encodingHeaderResponse;


	/** the component instances that will be rendered */
	private final Map/* <String,Component> */markupIdToComponent = new LinkedHashMap();

	private final List/* <String> */prependJavascripts = new ArrayList();

	/** a list of listeners */
	private List listeners = null;

	/**
	 * Constructor
	 */
	public AjaxRequestTarget()
	{
		Response response = RequestCycle.get().getResponse();
		encodingBodyResponse = new AjaxResponse(response);
		encodingHeaderResponse = new AjaxResponse(response);
	}

	/**
	 * Adds a listener to this target
	 * 
	 * @param listener
	 */
	public void addListener(IListener listener)
	{
		if (listener == null)
		{
			throw new IllegalArgumentException("Argument `listener` cannot be null");
		}

		if (listeners == null)
		{
			listeners = new LinkedList();
		}

		if (!listeners.contains(listener))
		{
			listeners.add(listener);
		}
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
		if (component.getOutputMarkupId() == false)
		{
			throw new IllegalArgumentException(
					"cannot update component that does not have setOutputMarkupId property set to true. Component: "
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
	 * @deprecated use appendJavascript(String javascript) instead
	 * @param javascript
	 */
	public final void addJavascript(String javascript)
	{
		appendJavascript(javascript);
	}

	/**
	 * Sets the focus in the browser to the given component. The markup id must
	 * be set.
	 * 
	 * @param component
	 *            The component to get the focus.
	 */
	public final void focusComponent(Component component)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("component cannot be null");
		}
		if (component.getOutputMarkupId() == false)
		{
			throw new IllegalArgumentException(
					"cannot update component that does not have setOutputMarkupId property set to true. Component: "
							+ component.toString());
		}
		appendJavascript("Wicket.Focus.setFocusOnId('" + component.getMarkupId() + "');");
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
	 * @see org.apache.wicket.IRequestTarget#detach(org.apache.wicket.RequestCycle)
	 */
	public void detach(final RequestCycle requestCycle)
	{
		// detach the page if it was updated
		if (markupIdToComponent.size() > 0)
		{
			final Component component = (Component)markupIdToComponent.values().iterator().next();
			component.getPage().detach();
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
	 * @see java.lang.Object#hashCode()
	 */
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
	 * @see org.apache.wicket.IRequestTarget#respond(org.apache.wicket.RequestCycle)
	 */
	public final void respond(final RequestCycle requestCycle)
	{
		final Application app = Application.get();

		// Determine encoding
		final String encoding = app.getRequestCycleSettings().getResponseRequestEncoding();

		// Set content type based on markup type for page
		final WebResponse response = (WebResponse)requestCycle.getResponse();
		response.setCharacterEncoding(encoding);
		response.setContentType("text/xml; charset=" + encoding);

		// Make sure it is not cached by a client
		response.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Pragma", "no-cache");

		response.write("<?xml version=\"1.0\" encoding=\"");
		response.write(encoding);
		response.write("\"?>");
		response.write("<ajax-response>");

		// invoke onbeforerespond event on listeners
		fireOnBeforeRespondListeners();

		// normal behavior
		Iterator it = prependJavascripts.iterator();
		while (it.hasNext())
		{
			String js = (String)it.next();
			respondInvocation(response, js);
		}

		// process added components
		respondComponents(response);

		fireOnAfterRespondListeners(response);

		it = appendJavascripts.iterator();
		while (it.hasNext())
		{
			String js = (String)it.next();
			respondInvocation(response, js);
		}

		response.write("</ajax-response>");
	}

	private void fireOnBeforeRespondListeners()
	{
		if (listeners != null)
		{
			final Map components = Collections.unmodifiableMap(markupIdToComponent);

			Iterator it = listeners.iterator();
			while (it.hasNext())
			{
				((IListener)it.next()).onBeforeRespond(components, this);
			}
		}
	}

	private void fireOnAfterRespondListeners(final WebResponse response)
	{
		Iterator it;
		// invoke onafterresponse event on listeners
		if (listeners != null)
		{
			final Map components = Collections.unmodifiableMap(markupIdToComponent);

			// create response that will be used by listeners to append
			// javascript
			final IJavascriptResponse jsresponse = new IJavascriptResponse()
			{

				public void addJavascript(String script)
				{
					respondInvocation(response, script);
				}

			};

			it = listeners.iterator();
			while (it.hasNext())
			{
				((IListener)it.next()).onAfterRespond(components, jsresponse);
			}
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

		// process feedback

		// TODO do we need these special pre-attach feedback traversals all over
		// the place?
		it = markupIdToComponent.values().iterator();
		while (it.hasNext())
		{
			final Component component = (Component)it.next();

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

		// we need to attach feedback components here because they are not
		// attached in MarkupContainer#attachChildren()
		it = markupIdToComponent.values().iterator();
		while (it.hasNext())
		{
			final Component component = (Component)it.next();

			if (component instanceof IFeedback)
			{
				component.attach();
			}

			if (component instanceof MarkupContainer)
			{
				MarkupContainer container = (MarkupContainer)component;

				// collect feedback
				container.visitChildren(IFeedback.class, new IVisitor()
				{
					public Object component(Component component)
					{
						component.attach();
						return IVisitor.CONTINUE_TRAVERSAL;
					}
				});
			}

		}


		// attach components
		it = markupIdToComponent.values().iterator();
		while (it.hasNext())
		{
			((Component)it.next()).attach();
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

	/**
	 * @see java.lang.Object#toString()
	 */
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
		// TODO Post 1.2: Java5: we can use str.replace(charseq, charseq) for
		// more efficient replacement
		return str.replaceAll("]", "]^");
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

		// substitute our encoding response for the real one so we can capture
		// component's markup in a manner safe for transport inside CDATA block
		final Response originalResponse = response;
		encodingBodyResponse.reset();
		RequestCycle.get().setResponse(encodingBodyResponse);

		// Initialize temporary variables
		final Page page = component.getPage();
		if (page == null)
		{
			throw new IllegalStateException(
					"Ajax request attempted on a component that is not associated with a Page");
		}

		page.startComponentRender(component);
		component.renderComponent();

		// render any associated headers of the component
		respondHeaderContribution(response, component);

		page.endComponentRender(component);

		// Restore original response
		RequestCycle.get().setResponse(originalResponse);

		response.write("<component id=\"");
		response.write(markupId);
		response.write("\" ");
		if (encodingBodyResponse.isContentsEncoded())
		{
			response.write(" encoding=\"");
			response.write(getEncodingName());
			response.write("\" ");
		}
		response.write("><![CDATA[");
		response.write(encodingBodyResponse.getContents());
		response.write("]]></component>");

		encodingBodyResponse.reset();
	}

	/**
	 * Header response for an ajax request.
	 * 
	 * @author Matej Knopp
	 */
	private class AjaxHeaderResponse extends HeaderResponse
	{

		private static final long serialVersionUID = 1L;

		private void checkHeaderRendering()
		{
			if (headerRendering == false)
			{
				throw new WicketRuntimeException(
						"Only methods that can be called on IHeaderResponse outside renderHead() are renderOnLoadJavascript and renderOnDomReadyJavascript");
			}
		}

		public void renderCSSReference(ResourceReference reference, String media)
		{
			checkHeaderRendering();
			super.renderCSSReference(reference, media);
		}

		public void renderCSSReference(String url)
		{
			checkHeaderRendering();
			super.renderCSSReference(url);
		}

		public void renderCSSReference(String url, String media)
		{
			checkHeaderRendering();
			super.renderCSSReference(url, media);
		}

		public void renderJavascript(CharSequence javascript, String id)
		{
			checkHeaderRendering();
			super.renderJavascript(javascript, id);
		}

		public void renderCSSReference(ResourceReference reference)
		{
			checkHeaderRendering();
			super.renderCSSReference(reference);
		}

		public void renderJavascriptReference(ResourceReference reference)
		{
			checkHeaderRendering();
			super.renderJavascriptReference(reference);
		}

		public void renderJavascriptReference(String url)
		{
			checkHeaderRendering();
			super.renderJavascriptReference(url);
		}

		public void renderString(CharSequence string)
		{
			checkHeaderRendering();
			super.renderString(string);
		}

		/**
		 * Construct.
		 * 
		 * @param response
		 */
		public AjaxHeaderResponse()
		{

		}

		public void renderOnDomReadyJavascript(String javascript)
		{
			List token = Arrays.asList(new Object[] { "javascript-event", "domready", javascript });
			if (wasRendered(token) == false)
			{
				// execute the javascript as first javascript after component
				// replacement
				appendJavascripts.add(0, javascript);
				markRendered(token);
			}
		}

		public void renderOnLoadJavascript(String javascript)
		{
			List token = Arrays.asList(new Object[] { "javascript-event", "load", javascript });
			if (wasRendered(token) == false)
			{
				// execute the javascript after all other scripts are executed
				appendJavascripts.add(javascript);
				markRendered(token);
			}
		}

		public Response getResponse()
		{
			return RequestCycle.get().getResponse();
		}
	};

	// whether a header contribution is being rendered
	private boolean headerRendering = false;
	private HtmlHeaderContainer header = null;

	private IHeaderResponse headerResponse;

	/**
	 * Returns the header response associated with current AjaxRequestTarget.
	 * 
	 * Beware that only renderOnDomReadyJavascript and renderOnLoadJavascript
	 * can be called outside the renderHeader(IHeaderResponse response) method.
	 * Calls to other render** methods will result in an exception being thrown.
	 * 
	 * @return header response
	 */
	public IHeaderResponse getHeaderResponse()
	{
		if (headerResponse == null)
		{
			headerResponse = new AjaxHeaderResponse();
		}
		return headerResponse;
	}

	/**
	 * Header container component for ajax header contributions
	 * 
	 * @author Matej Knopp
	 */
	private static class AjaxHtmlHeaderContainer extends HtmlHeaderContainer
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 * @param target
		 */
		public AjaxHtmlHeaderContainer(String id, AjaxRequestTarget target)
		{
			super(id);
			this.target = target;
		}

		protected IHeaderResponse newHeaderResponse()
		{
			return target.getHeaderResponse();
		}

		private transient AjaxRequestTarget target;
	};

	/**
	 * 
	 * @param response
	 * @param component
	 */
	private void respondHeaderContribution(final Response response, final Component component)
	{
		headerRendering = true;

		// create the htmlheadercontainer if needed
		if (header == null)
		{
			header = new AjaxHtmlHeaderContainer(HtmlHeaderSectionHandler.HEADER_ID, this);
		}

		// add or replace the container to page
		Component oldHeader = component.getPage().get(HtmlHeaderSectionHandler.HEADER_ID);

		if (oldHeader != null)
		{
			component.getPage().replace(header);
		}
		else
		{
			component.getPage().add(header);
		}

		// save old response, set new
		Response oldResponse = RequestCycle.get().setResponse(encodingHeaderResponse);

		encodingHeaderResponse.reset();

		// render the head of component and all it's children

		component.renderHead(header);

		if (component instanceof MarkupContainer)
		{
			((MarkupContainer)component).visitChildren(new Component.IVisitor()
			{
				public Object component(Component component)
				{
					if (component.isVisible())
					{
						component.renderHead(header);
						return CONTINUE_TRAVERSAL;
					}
					else
					{
						return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
					}
				}
			});
		}

		// revert to old response

		RequestCycle.get().setResponse(oldResponse);

		if (encodingHeaderResponse.getContents().length() != 0)
		{
			response.write("<header-contribution");

			if (encodingHeaderResponse.isContentsEncoded())
			{
				response.write(" encoding=\"");
				response.write(getEncodingName());
				response.write("\" ");
			}

			// we need to write response as CDATA and parse it on client,
			// because
			// konqueror crashes when there is a <script> element
			response.write("><![CDATA[<head xmlns:wicket=\"http://wicket.apache.org\">");

			response.write(encodingHeaderResponse.getContents());

			response.write("</head>]]>");

			response.write("</header-contribution>");
		}

		headerRendering = false;

		if (oldHeader == null)
			component.getPage().remove(header);
		else
			component.getPage().replace(oldHeader);
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

		encodingBodyResponse.reset();
	}
}
