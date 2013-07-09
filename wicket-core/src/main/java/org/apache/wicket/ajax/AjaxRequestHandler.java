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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.logger.PageLogData;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.response.filter.IResponseFilter;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * A request target that produces ajax response envelopes used on the client side to update
 * component markup as well as evaluate arbitrary javascript.
 * <p>
 * A component whose markup needs to be updated should be added to this target via
 * AjaxRequestTarget#add(Component) method. Its body will be rendered and added to the envelope when
 * the target is processed, and refreshed on the client side when the ajax response is received.
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
 * The target provides a listener interface {@link AjaxRequestTarget.IListener} that can be used to
 * add code that responds to various target events by adding listeners via
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
	 * A POJO-like that collects the data for the Ajax response written to the client and serializes
	 * it to specific String-based format (XML, JSON, ...).
	 */
	private final AbstractAjaxResponse responseObject;

	/** a list of listeners */
	private List<AjaxRequestTarget.IListener> listeners = null;

	/** */
	private final Set<ITargetRespondListener> respondListeners = new HashSet<ITargetRespondListener>();

	/** see https://issues.apache.org/jira/browse/WICKET-3564 */
	protected transient boolean respondersFrozen;
	protected transient boolean listenersFrozen;

	/** The associated Page */
	private final Page page;

	private PageLogData logData;

	/**
	 * Constructor
	 * 
	 * @param page
	 *      the currently active page
	 */
	public AjaxRequestHandler(final Page page)
	{
		this.page = Args.notNull(page, "page");

		responseObject = new XmlAjaxResponse(page)
		{

			/**
			 * Freezes the {@link AjaxRequestHandler#listeners}, and does not un-freeze them as the
			 * events will have been fired by now.
			 * 
			 * @param response
			 *      the response to write to
			 */
			@Override
			protected void fireOnAfterRespondListeners(final Response response)
			{
				listenersFrozen = true;

				// invoke onafterresponse event on listeners
				if (listeners != null)
				{
					final Map<String, Component> components = Collections.unmodifiableMap(markupIdToComponent);

					// create response that will be used by listeners to append
					// javascript
					final AjaxRequestTarget.IJavaScriptResponse jsresponse = new AjaxRequestTarget.IJavaScriptResponse()
					{
						@Override
						public void addJavaScript(String script)
						{
							writeNormalEvaluations(response, Collections.<CharSequence>singleton(script));
						}
					};

					for (AjaxRequestTarget.IListener listener : listeners)
					{
						listener.onAfterRespond(components, jsresponse);
					}
				}
			}

			/**
			 * Freezes the {@link AjaxRequestHandler#listeners} before firing the event and
			 * un-freezes them afterwards to allow components to add more
			 * {@link AjaxRequestTarget.IListener}s for the second event.
			 */
			@Override
			protected void fireOnBeforeRespondListeners()
			{
				listenersFrozen = true;

				if (listeners != null)
				{
					final Map<String, Component> components = Collections.unmodifiableMap(markupIdToComponent);

					for (AjaxRequestTarget.IListener listener : listeners)
					{
						listener.onBeforeRespond(components, AjaxRequestHandler.this);
					}
				}

				listenersFrozen = false;
			}

		};
	}

	/**
	 * @see org.apache.wicket.core.request.handler.IPageRequestHandler#getPage()
	 */
	@Override
	public Page getPage()
	{
		return page;
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

			if (component.getOutputMarkupId() == false && !(component instanceof Page))
			{
				throw new IllegalArgumentException(
					"cannot update component that does not have setOutputMarkupId property set to true. Component: " +
						component.toString());
			}
			add(component, component.getMarkupId());
		}
	}

	@Override
	public void add(Component component, String markupId)
	{
		responseObject.add(component, markupId);
	}

	@Override
	public final Collection<? extends Component> getComponents()
	{
		return responseObject.getComponents();
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
		responseObject.appendJavaScript(javascript);
	}

	/**
	 * @see org.apache.wicket.core.request.handler.IPageRequestHandler#detach(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public void detach(final IRequestCycle requestCycle)
	{
		if (logData == null)
		{
			logData = new PageLogData(page);
		}

		responseObject.detach(requestCycle);
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
			return responseObject.equals(that.responseObject);
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
		result += responseObject.hashCode() * 17;
		return result;
	}

	@Override
	public final void prependJavaScript(CharSequence javascript)
	{
		responseObject.prependJavaScript(javascript);
	}

	@Override
	public void registerRespondListener(ITargetRespondListener listener)
	{
		assertRespondersNotFrozen();
		respondListeners.add(listener);
	}

	/**
	 * @see org.apache.wicket.core.request.handler.IPageRequestHandler#respond(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public final void respond(final IRequestCycle requestCycle)
	{
		final RequestCycle rc = (RequestCycle)requestCycle;
		final WebResponse response = (WebResponse)requestCycle.getResponse();

		if (responseObject.containsPage())
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

		final Application app = page.getApplication();

		page.send(app, Broadcast.BREADTH, this);

		// Determine encoding
		final String encoding = app.getRequestCycleSettings().getResponseRequestEncoding();

		// Set content type based on markup type for page
		responseObject.setContentType(response, encoding);

		// Make sure it is not cached by a client
		response.disableCaching();

		final StringResponse bodyResponse = new StringResponse();
		responseObject.writeTo(bodyResponse, encoding);
		CharSequence filteredResponse = invokeResponseFilters(bodyResponse);
		response.write(filteredResponse);
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[AjaxRequestHandler@" + hashCode() + " responseObject [" + responseObject + "]";
	}

	@Override
	public IHeaderResponse getHeaderResponse()
	{
		return responseObject.getHeaderResponse();
	}

	/**
	 * @return the markup id of the focused element in the browser
	 */
	@Override
	public String getLastFocusedElementId()
	{
		WebRequest request = (WebRequest)page.getRequest();
		String id = request.getHeader("Wicket-FocusedElementId");
		return Strings.isEmpty(id) ? null : id;
	}

	/**
	 * @see org.apache.wicket.core.request.handler.IPageRequestHandler#getPageClass()
	 */
	@Override
	public Class<? extends IRequestablePage> getPageClass()
	{
		return page.getPageClass();
	}

	/**
	 * @see org.apache.wicket.core.request.handler.IPageRequestHandler#getPageId()
	 */
	@Override
	public Integer getPageId()
	{
		return page.getPageId();
	}

	/**
	 * @see org.apache.wicket.core.request.handler.IPageRequestHandler#getPageParameters()
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

	private void assertNotFrozen(boolean frozen, Class<?> clazz)
	{
		if (frozen)
		{
			throw new IllegalStateException(Classes.simpleName(clazz) + "s can no longer be added");
		}
	}

	private void assertRespondersNotFrozen()
	{
		assertNotFrozen(respondersFrozen, AjaxRequestTarget.ITargetRespondListener.class);
	}

	private void assertListenersNotFrozen()
	{
		assertNotFrozen(listenersFrozen, AjaxRequestTarget.IListener.class);
	}
}
