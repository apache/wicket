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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.internal.HeaderResponse;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.handler.logger.PageLogData;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.response.filter.IResponseFilter;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A request target that produces ajax response envelopes used on the client side to update
 * component markup as well as evaluate arbitrary javascript.
 * <p>
 * A component whose markup needs to be updated should be added to this target via
 * AjaxRequestTarget#add(Component) method. Its body will be rendered and added to the
 * envelope when the target is processed, and refreshed on the client side when the ajax response is
 * received.
 * <p>
 * It is important that the component whose markup needs to be updated contains an id attribute in
 * the generated markup that is equal to the value retrieved from Component#getMarkupId(). This can
 * be accomplished by either setting the id attribute in the html template, or using an attribute
 * modifier that will add the attribute with value Component#getMarkupId() to the tag ( such as
 * MarkupIdSetter )
 * <p>
 * Any javascript that needs to be evaluated on the client side can be added using
 * AjaxRequestTarget#append/prependJavaScript(String). For example, this feature can be useful when
 * it is desirable to link component update with some javascript effects.
 * <p>
 * The target provides a listener interface {@link AjaxRequestTarget.IListener} that can be used to add code that
 * responds to various target events by adding listeners via
 * {@link #addListener(AjaxRequestTarget.IListener)}
 *
 * @since 1.2
 *
 * @author Igor Vaynberg (ivaynberg)
 * @author Eelco Hillenius
 */
public class AjaxRequestHandler implements AjaxRequestTarget
{

	/**
	 * Response that uses an encoder to encode its contents
	 *
	 * @author Igor Vaynberg (ivaynberg)
	 */
	private static final class AjaxResponse extends Response
	{
		private final AppendingStringBuffer buffer = new AppendingStringBuffer(256);

		private boolean escaped = false;

		private final Response originalResponse;

		/**
		 * Construct.
		 *
		 * @param originalResponse
		 */
		private AjaxResponse(Response originalResponse)
		{
			this.originalResponse = originalResponse;
		}

		/**
		 * @see org.apache.wicket.request.Response#encodeURL(CharSequence)
		 */
		@Override
		public String encodeURL(CharSequence url)
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
		 * @return true if any escaping has been performed, false otherwise
		 */
		public boolean isContentsEncoded()
		{
			return escaped;
		}

		/**
		 * @see org.apache.wicket.request.Response#write(CharSequence)
		 */
		@Override
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

		/**
		 * Resets the response to a clean state so it can be reused to save on garbage.
		 */
		@Override
		public void reset()
		{
			buffer.clear();
			escaped = false;
		}

		@Override
		public void write(byte[] array)
		{
			throw new UnsupportedOperationException("Cannot write binary data.");
		}

		@Override
		public void write(byte[] array, int offset, int length)
		{
			throw new UnsupportedOperationException("Cannot write binary data.");
		}

		@Override
		public Object getContainerResponse()
		{
			return originalResponse.getContainerResponse();
		}
	}

	private static final Logger log = LoggerFactory.getLogger(AjaxRequestHandler.class);

	private final List<CharSequence> appendJavaScripts = Generics.newArrayList();

	private final List<CharSequence> domReadyJavaScripts = Generics.newArrayList();

	/**
	 * Create a response for component body and javascript that will escape output to make it safe
	 * to use inside a CDATA block
	 */
	private final AjaxResponse encodingBodyResponse;

	/**
	 * Response for header contribution that will escape output to make it safe to use inside a
	 * CDATA block
	 */
	private final AjaxResponse encodingHeaderResponse;

	/** the component instances that will be rendered */
	private final Map<String, Component> markupIdToComponent = new LinkedHashMap<String, Component>();

	/** */
	private final List<CharSequence> prependJavaScripts = Generics.newArrayList();

	/** a list of listeners */
	private List<AjaxRequestTarget.IListener> listeners = null;

	/** */
	private final Set<ITargetRespondListener> respondListeners = new HashSet<ITargetRespondListener>();

	/** The associated Page */
	private final Page page;

	/** see https://issues.apache.org/jira/browse/WICKET-3564 */
	private transient boolean componentsFrozen;
	private transient boolean listenersFrozen;
	private transient boolean respondersFrozen;

	private PageLogData logData;

	/**
	 * Constructor
	 *
	 * @param page
	 */
	public AjaxRequestHandler(Page page)
	{
		this.page = Args.notNull(page, "page");
		Response response = page.getResponse();
		encodingBodyResponse = new AjaxResponse(response);
		encodingHeaderResponse = new AjaxResponse(response);
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageRequestHandler#getPage()
	 */
	@Override
	public Page getPage()
	{
		return page;
	}

	private void assertNotFrozen(boolean frozen, Class<?> clazz)
	{
		if (frozen)
		{
			throw new IllegalStateException(clazz.getSimpleName() + "s can no " +
				" longer be added");
		}
	}

	private void assertListenersNotFrozen()
	{
		assertNotFrozen(listenersFrozen, AjaxRequestTarget.IListener.class);
	}

	private void assertComponentsNotFrozen()
	{
		assertNotFrozen(componentsFrozen, Component.class);
	}

	private void assertRespondersNotFrozen()
	{
		assertNotFrozen(respondersFrozen, ITargetRespondListener.class);
	}

	@Override
	public void addListener(AjaxRequestTarget.IListener listener) throws IllegalStateException
	{
		Args.notNull(listener, "listener");
		assertListenersNotFrozen();

		if (listeners == null)
		{
			listeners = new LinkedList<AjaxRequestTarget.IListener>();
		}

		if (!listeners.contains(listener))
		{
			listeners.add(listener);
		}
	}

	@Override
	public final void addChildren(MarkupContainer parent, Class<?> childCriteria)
	{
		Args.notNull(parent, "parent");
		Args.notNull(childCriteria, "childCriteria");

		parent.visitChildren(childCriteria, new IVisitor<Component, Void>()
		{
			@Override
			public void component(final Component component, final IVisit<Void> visit)
			{
				add(component);
				visit.dontGoDeeper();
			}
		});
	}

	@Override
	public void add(Component... components)
	{
		for (final Component component : components)
		{
			Args.notNull(component, "component");

			if (component.getOutputMarkupId() == false)
			{
				throw new IllegalArgumentException(
					"cannot update component that does not have setOutputMarkupId property set to true. Component: " +
						component.toString());
			}
			add(component, component.getMarkupId());
		}
	}

	@Override
	public final void add(final Component component, final String markupId)
		throws IllegalArgumentException, IllegalStateException
	{
		Args.notEmpty(markupId, "markupId");
		Args.notNull(component, "component");

		if (component instanceof Page)
		{
			if (component != page)
			{
				throw new IllegalArgumentException("component cannot be a page");
			}
		}
		else if (component instanceof AbstractRepeater)
		{
			throw new IllegalArgumentException(
				"Component " +
					component.getClass().getName() +
					" has been added to the target. This component is a repeater and cannot be repainted via ajax directly. " +
					"Instead add its parent or another markup container higher in the hierarchy.");
		}

		assertComponentsNotFrozen();

		component.setMarkupId(markupId);
		markupIdToComponent.put(markupId, component);
	}

	@Override
	public final Collection<? extends Component> getComponents()
	{
		return Collections.unmodifiableCollection(markupIdToComponent.values());
	}

	@Override
	public final void focusComponent(Component component)
	{
		if (component != null && component.getOutputMarkupId() == false)
		{
			throw new IllegalArgumentException(
				"cannot update component that does not have setOutputMarkupId property set to true. Component: " +
					component.toString());
		}
		final String id = component != null ? ("'" + component.getMarkupId() + "'") : "null";
		appendJavaScript("Wicket.Focus.setFocusOnId(" + id + ");");
	}

	@Override
	public final void appendJavaScript(CharSequence javascript)
	{
		Args.notNull(javascript, "javascript");

		appendJavaScripts.add(javascript);
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageRequestHandler#detach(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public void detach(final IRequestCycle requestCycle)
	{
		if (logData == null)
			logData = new PageLogData(page);

		// detach the page if it was updated
		if (markupIdToComponent.size() > 0)
		{
			final Component component = markupIdToComponent.values().iterator().next();
			final Page page = component.findParent(Page.class);
			if (page != null)
			{
				page.detach();
			}
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof AjaxRequestHandler)
		{
			AjaxRequestHandler that = (AjaxRequestHandler)obj;
			return markupIdToComponent.equals(that.markupIdToComponent) &&
				prependJavaScripts.equals(that.prependJavaScripts) &&
				appendJavaScripts.equals(that.appendJavaScripts);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = "AjaxRequestHandler".hashCode();
		result += markupIdToComponent.hashCode() * 17;
		result += prependJavaScripts.hashCode() * 17;
		result += appendJavaScripts.hashCode() * 17;
		return result;
	}

	@Override
	public final void prependJavaScript(CharSequence javascript)
	{
		Args.notNull(javascript, "javascript");

		prependJavaScripts.add(javascript);
	}

	@Override
	public void registerRespondListener(ITargetRespondListener listener)
	{
		assertRespondersNotFrozen();
		respondListeners.add(listener);
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageRequestHandler#respond(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public final void respond(final IRequestCycle requestCycle)
	{
		final RequestCycle rc = (RequestCycle)requestCycle;
		final WebResponse response = (WebResponse)requestCycle.getResponse();

		if (markupIdToComponent.values().contains(page))
		{
			// the page itself has been added to the request target, we simply issue a redirect
			// back to the page
			IRequestHandler handler = new RenderPageRequestHandler(new PageProvider(page));
			final String url = rc.urlFor(handler).toString();
			response.sendRedirect(url);
			return;
		}

		respondersFrozen = true;

		for (ITargetRespondListener listener : respondListeners)
		{
			listener.onTargetRespond(this);
		}

		final Application app = Application.get();

		page.send(app, Broadcast.BREADTH, this);

		// Determine encoding
		final String encoding = app.getRequestCycleSettings().getResponseRequestEncoding();

		// Set content type based on markup type for page
		response.setContentType("text/xml; charset=" + encoding);

		// Make sure it is not cached by a client
		response.disableCaching();

		try
		{
			final StringResponse bodyResponse = new StringResponse();
			constructResponseBody(bodyResponse, encoding);
			CharSequence filteredResponse = invokeResponseFilters(bodyResponse);
			response.write(filteredResponse);
		}
		finally
		{
			// restore the original response
			RequestCycle.get().setResponse(response);
		}
	}

	/**
	 * Collects the response body (without the headers) so that it can be pre-processed before
	 * written down to the original response.
	 *
	 * @param bodyResponse
	 *            the buffering response
	 * @param encoding
	 *            the encoding that should be used to encode the body
	 */
	private void constructResponseBody(final Response bodyResponse, final String encoding)
	{
		bodyResponse.write("<?xml version=\"1.0\" encoding=\"");
		bodyResponse.write(encoding);
		bodyResponse.write("\"?>");
		bodyResponse.write("<ajax-response>");

		// invoke onbeforerespond event on listeners
		fireOnBeforeRespondListeners();

		// process added components
		respondComponents(bodyResponse);

		fireOnAfterRespondListeners(bodyResponse);

		// queue up prepend javascripts. unlike other steps these are executed out of order so that
		// components can contribute them from inside their onbeforerender methods.
		Iterator<CharSequence> it = prependJavaScripts.iterator();
		while (it.hasNext())
		{
			CharSequence js = it.next();
			respondPriorityInvocation(bodyResponse, js);
		}


		// execute the dom ready javascripts as first javascripts
		// after component replacement
		it = domReadyJavaScripts.iterator();
		while (it.hasNext())
		{
			CharSequence js = it.next();
			respondInvocation(bodyResponse, js);
		}
		it = appendJavaScripts.iterator();
		while (it.hasNext())
		{
			CharSequence js = it.next();
			respondInvocation(bodyResponse, js);
		}

		bodyResponse.write("</ajax-response>");
	}

	/**
	 * Runs the configured {@link IResponseFilter}s over the constructed Ajax response
	 *
	 * @param contentResponse
	 *            the Ajax {@link Response} body
	 * @return filtered response
	 */
	private AppendingStringBuffer invokeResponseFilters(final StringResponse contentResponse)
	{
		AppendingStringBuffer responseBuffer = new AppendingStringBuffer(
			contentResponse.getBuffer());

		List<IResponseFilter> responseFilters = Application.get()
			.getRequestCycleSettings()
			.getResponseFilters();

		if (responseFilters != null)
		{
			for (IResponseFilter filter : responseFilters)
			{
				responseBuffer = filter.filter(responseBuffer);
			}
		}
		return responseBuffer;
	}

	/**
	 * Freezes the {@link #listeners} before firing the event and un-freezes them afterwards to
	 * allow components to add more {@link AjaxRequestTarget.IListener}s for the second event.
	 */
	private void fireOnBeforeRespondListeners()
	{
		listenersFrozen = true;

		if (listeners != null)
		{
			final Map<String, Component> components = Collections.unmodifiableMap(markupIdToComponent);

			for (AjaxRequestTarget.IListener listener : listeners)
			{
				listener.onBeforeRespond(components, this);
			}
		}

		listenersFrozen = false;
	}

	/**
	 * Freezes the {@link #listeners}, and does not un-freeze them as the events will have been
	 * fired by now.
	 *
	 * @param response
	 */
	private void fireOnAfterRespondListeners(final Response response)
	{
		listenersFrozen = true;

		// invoke onafterresponse event on listeners
		if (listeners != null)
		{
			final Map<String, Component> components = Collections.unmodifiableMap(markupIdToComponent);

			// create response that will be used by listeners to append
			// javascript
			final IJavaScriptResponse jsresponse = new IJavaScriptResponse()
			{
				@Override
				public void addJavaScript(String script)
				{
					respondInvocation(response, script);
				}
			};

			for (AjaxRequestTarget.IListener listener : listeners)
			{
				listener.onAfterRespond(components, jsresponse);
			}
		}
	}

	/**
	 * Processes components added to the target. This involves attaching components, rendering
	 * markup into a client side xml envelope, and detaching them
	 *
	 * @param response
	 */
	private void respondComponents(Response response)
	{
		componentsFrozen = true;
		// TODO: We might need to call prepareRender on all components upfront

		// process component markup
		for (Map.Entry<String, Component> stringComponentEntry : markupIdToComponent.entrySet())
		{
			final Component component = stringComponentEntry.getValue();
			// final String markupId = stringComponentEntry.getKey();

			if (!containsAncestorFor(component))
			{
				respondComponent(response, component.getAjaxRegionMarkupId(), component);
			}
		}

		if (header != null)
		{
			// some header responses buffer all calls to render*** until close is called.
			// when they are closed, they do something (i.e. aggregate all JS resource urls to a
			// single url), and then "flush" (by writing to the real response) before closing.
			// to support this, we need to allow header contributions to be written in the close
			// tag, which we do here:
			headerRendering = true;
			// save old response, set new
			Response oldResponse = RequestCycle.get().setResponse(encodingHeaderResponse);
			encodingHeaderResponse.reset();

			// now, close the response (which may render things)
			header.getHeaderResponse().close();

			// revert to old response
			RequestCycle.get().setResponse(oldResponse);

			// write the XML tags and we're done
			writeHeaderContribution(response);
			headerRendering = false;
		}
	}

	private void writeHeaderContribution(Response response)
	{
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
			// because konqueror crashes when there is a <script> element
			response.write("><![CDATA[<head xmlns:wicket=\"http://wicket.apache.org\">");
			response.write(encodingHeaderResponse.getContents());
			response.write("</head>]]>");
			response.write("</header-contribution>");
		}
	}

	/**
	 * Checks if the target contains an ancestor for the given component
	 *
	 * @param component
	 * @return <code>true</code> if target contains an ancestor for the given component
	 */
	private boolean containsAncestorFor(Component component)
	{
		Component cursor = component.getParent();
		while (cursor != null)
		{
			if (markupIdToComponent.containsValue(cursor))
			{
				return true;
			}
			cursor = cursor.getParent();
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[AjaxRequestHandler@" + hashCode() + " markupIdToComponent [" + markupIdToComponent +
			"], prependJavaScript [" + prependJavaScripts + "], appendJavaScript [" +
			appendJavaScripts + "]";
	}

	/**
	 * Encodes a string so it is safe to use inside CDATA blocks
	 *
	 * @param str
	 * @return encoded string
	 */
	private static String encode(CharSequence str)
	{
		if (str == null)
		{
			return null;
		}

		return Strings.replaceAll(str, "]", "]^").toString();
	}

	/**
	 * @return name of encoding used to possibly encode the contents of the CDATA blocks
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
	private static boolean needsEncoding(CharSequence str)
	{
		/*
		 * TODO Post 1.2: Ajax: we can improve this by keeping a buffer of at least 3 characters and
		 * checking that buffer so that we can narrow down escaping occurring only for ']]>'
		 * sequence, or at least for ]] if ] is the last char in this buffer.
		 *
		 * but this improvement will only work if we write first and encode later instead of working
		 * on fragments sent to write
		 */
		return Strings.indexOf(str, ']') >= 0;
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
				"Ajax render cannot be called on component that has setRenderBodyOnly enabled. Component: " +
					component.toString());
		}

		component.setOutputMarkupId(true);

		// substitute our encoding response for the real one so we can capture
		// component's markup in a manner safe for transport inside CDATA block
		encodingBodyResponse.reset();
		RequestCycle.get().setResponse(encodingBodyResponse);

		// Initialize temporary variables
		final Page page = component.findParent(Page.class);
		if (page == null)
		{
			// dont throw an exception but just ignore this component, somehow
			// it got removed from the page.
			log.debug("component: " + component + " with markupid: " + markupId +
				" not rendered because it was already removed from page");
			return;
		}

		page.startComponentRender(component);

		try
		{
			component.prepareForRender();

			// render any associated headers of the component
			respondHeaderContribution(response, component);
		}
		catch (RuntimeException e)
		{
			try
			{
				component.afterRender();
			}
			catch (RuntimeException e2)
			{
				// ignore this one could be a result off.
			}
			// Restore original response
			RequestCycle.get().setResponse(response);
			encodingBodyResponse.reset();
			throw e;
		}

		try
		{
			component.render();
		}
		catch (RuntimeException e)
		{
			RequestCycle.get().setResponse(response);
			encodingBodyResponse.reset();
			throw e;
		}

		page.endComponentRender(component);

		// Restore original response
		RequestCycle.get().setResponse(response);

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
		@Override
		public void render(HeaderItem item)
		{
			if (item instanceof OnLoadHeaderItem)
			{
				if (!wasItemRendered(item))
				{
					appendJavaScripts.add(((OnLoadHeaderItem)item).getJavaScript());
					markItemRendered(item);
				}
			}
			else if (item instanceof OnDomReadyHeaderItem)
			{
				if (!wasItemRendered(item))
				{
					domReadyJavaScripts.add(((OnDomReadyHeaderItem)item).getJavaScript());
					markItemRendered(item);
				}
			}
			else if (headerRendering)
				super.render(item);
			else
				log.debug("Only methods that can be called on IHeaderResponse outside renderHead() are renderOnLoadJavaScript and renderOnDomReadyJavaScript");
		}

		/**
		 * Construct.
		 */
		public AjaxHeaderResponse()
		{
		}

		/**
		 *
		 * @see org.apache.wicket.markup.head.internal.HeaderResponse#getRealResponse()
		 */
		@Override
		protected Response getRealResponse()
		{
			return RequestCycle.get().getResponse();
		}
	}

	// whether a header contribution is being rendered
	private boolean headerRendering = false;
	private HtmlHeaderContainer header = null;

	private IHeaderResponse headerResponse;

	@Override
	public IHeaderResponse getHeaderResponse()
	{
		if (headerResponse == null)
		{
			// we don't need to decorate the header response here because this is called from
			// within AjaxHtmlHeaderContainer, which decorates the response
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
		public AjaxHtmlHeaderContainer(String id, AjaxRequestHandler target)
		{
			super(id);
			this.target = target;
		}

		/**
		 *
		 * @see org.apache.wicket.markup.html.internal.HtmlHeaderContainer#newHeaderResponse()
		 */
		@Override
		protected IHeaderResponse newHeaderResponse()
		{
			return target.getHeaderResponse();
		}

		private final transient AjaxRequestHandler target;
	}

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
			final Page page = component.getPage();
			page.addOrReplace(header);
		}

		// save old response, set new
		Response oldResponse = RequestCycle.get().setResponse(encodingHeaderResponse);

		encodingHeaderResponse.reset();

		// render the head of component and all it's children

		component.renderHead(header);

		if (component instanceof MarkupContainer)
		{
			((MarkupContainer)component).visitChildren(new IVisitor<Component, Void>()
			{
				@Override
				public void component(final Component component, final IVisit<Void> visit)
				{
					if (component.isVisibleInHierarchy())
					{
						component.renderHead(header);
					}
					else
					{
						visit.dontGoDeeper();
					}
				}
			});
		}

		// revert to old response
		RequestCycle.get().setResponse(oldResponse);

		writeHeaderContribution(response);

		headerRendering = false;
	}

	private void respondInvocation(final Response response, final CharSequence js)
	{
		respondJavascriptInvocation("evaluate", response, js);
	}

	private void respondPriorityInvocation(final Response response, final CharSequence js)
	{
		respondJavascriptInvocation("priority-evaluate", response, js);
	}


	/**
	 * @param invocation
	 *            type of invocation tag, usually {@literal evaluate} or
	 *            {@literal priority-evaluate}
	 * @param response
	 * @param js
	 */
	private void respondJavascriptInvocation(final String invocation, final Response response,
		final CharSequence js)
	{
		boolean encoded = false;
		CharSequence javascript = js;

		// encode the response if needed
		if (needsEncoding(js))
		{
			encoded = true;
			javascript = encode(js);
		}

		response.write("<");
		response.write(invocation);
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

		response.write("</");
		response.write(invocation);
		response.write(">");

		encodingBodyResponse.reset();
	}

	@Override
	public String getLastFocusedElementId()
	{
		WebRequest request = (WebRequest)RequestCycle.get().getRequest();
		String id = request.getHeader("Wicket-FocusedElementId");
		return Strings.isEmpty(id) ? null : id;
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageRequestHandler#getPageClass()
	 */
	@Override
	public Class<? extends IRequestablePage> getPageClass()
	{
		return page.getPageClass();
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageRequestHandler#getPageId()
	 */
	@Override
	public Integer getPageId()
	{
		return page.getPageId();
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageRequestHandler#getPageParameters()
	 */
	@Override
	public PageParameters getPageParameters()
	{
		return page.getPageParameters();
	}

	@Override
	public final boolean isPageInstanceCreated()
	{
		return true;
	}

	@Override
	public final Integer getRenderCount()
	{
		return page.getRenderCount();
	}

	/** {@inheritDoc} */
	@Override
	public PageLogData getLogData()
	{
		return logData;
	}
}
