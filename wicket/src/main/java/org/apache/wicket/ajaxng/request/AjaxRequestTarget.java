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
package org.apache.wicket.ajaxng.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Response;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajaxng.AjaxBehavior;
import org.apache.wicket.ajaxng.json.JSONArray;
import org.apache.wicket.ajaxng.json.JSONObject;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.internal.HeaderResponse;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.parser.filter.HtmlHeaderSectionHandler;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.response.StringResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A request target that produces ajax response envelopes used on the client side to update
 * component markup as well as evaluate arbitrary javascript.
 * <p>
 * A component whose markup needs to be updated should be added to this target via
 * AjaxRequestTarget#addComponent(Component) method. Its body will be rendered and added to the
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
 * AjaxRequestTarget#append/prependJavascript(String). For example, this feature can be useful when
 * it is desirable to link component update with some javascript effects.
 * <p>
 * The target provides a listener interface {@link IListener} that can be used to add code that
 * responds to various target events by adding listeners via
 * {@link #addListener(IListener)} 
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Eelco Hillenius
 * @author Matej Knopp
 */
public class AjaxRequestTarget implements IRequestTarget
{

	private AjaxRequestTarget()
	{
		this.component = null;
		this.page = null;
		this.behaviorIndex = -1;
	}

	/**
	 * An {@link AjaxRequestTarget} listener that can be used to respond to various target-related
	 * events
	 */
	public static interface IListener
	{
		/**
		 * Triggered before ajax request target begins its response cycle
		 * 
		 * @param components
		 *            read-only list of component entries already added to the target
		 * @param target
		 *            the target itself. Could be used to add components or to append/prepend
		 *            javascript
		 * 
		 */
		public void onBeforeRespond(List<ComponentEntry> components, AjaxRequestTarget target);

		/**
		 * Triggered after ajax request target is notify with its response cycle. At this point only
		 * additional javascript can be output to the response using the provided
		 * {@link IJavascriptResponse} object
		 * 
		 * NOTE: During this stage of processing any calls to target that manipulate the response
		 * (adding components, javascript) will have no effect
		 * 
		 * @param components
		 *            read-only list of component entries added to the target
		 * @param response
		 *            response object that can be used to output javascript
		 */
		public void onAfterRespond(List<ComponentEntry> components, IJavascriptResponse response);
	}

	/**
	 * An ajax javascript response that allows users to add javascript to be executed on the client
	 * side
	 * 
	 * @author ivaynberg
	 */
	public static interface IJavascriptResponse
	{
		/**
		 * Adds more javascript to the ajax response that will be executed on the client side
		 * 
		 * @param script
		 *            javascript
		 */
		public void addJavascript(String script);
	}

	private final Page page;
	private final Component component;
	private final int behaviorIndex;

	private final List<ComponentEntry> entries = new ArrayList<ComponentEntry>();

	private final List<JavascriptEntry> prependJavascripts = new ArrayList<JavascriptEntry>();
	private final List<JavascriptEntry> appendJavascripts = new ArrayList<JavascriptEntry>();

	private final List<JavascriptEntry> domReadyJavascripts = new ArrayList<JavascriptEntry>();

	private final List<IListener> listeners = new ArrayList<IListener>();

	private static final Logger log = LoggerFactory.getLogger(AjaxRequestTarget.class);

	private String redirect = null;

	// whether a header contribution is being rendered
	private boolean headerRendering = false;
	private HtmlHeaderContainer header = null;

	private IHeaderResponse headerResponse;

	/**
	 * Construct.
	 * 
	 * @param component
	 * @param behaviorIndex
	 */
	public AjaxRequestTarget(Component component, int behaviorIndex)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("Argument 'component' may not be null.");
		}
		page = component.getPage();
		if (page == null)
		{
			throw new IllegalArgumentException("Component must belong to a page.");
		}
		this.component = component;
		this.behaviorIndex = behaviorIndex;
	}

	public void detach(RequestCycle requestCycle)
	{
		if (!entries.isEmpty())
		{
			entries.iterator().next().getComponent().getPage().detach();
		}
	}

	/**
	 * Returns component that has behavior which initiated this Ajax request.
	 * 
	 * @return component
	 */
	public Component getComponent()
	{
		return component;
	}

	/**
	 * Entry for a single component. Allows to specify custom javascript handlers executed before
	 * and after replacing the component. Also the actual component replacement can be overriden.
	 * 
	 * @author Matej Knopp
	 */
	public static class ComponentEntry
	{
		private final Component component;
		private String beforeReplaceJavascript;
		private String afterReplaceJavascript;
		private String replaceJavascript;

		/**
		 * Construct.
		 * 
		 * @param component
		 */
		public ComponentEntry(Component component)
		{
			this.component = component;
		}

		/**
		 * Construct.
		 * 
		 * @param entry
		 *            entry to copy
		 */
		public ComponentEntry(ComponentEntry entry)
		{
			this.component = entry.component;
			this.beforeReplaceJavascript = entry.beforeReplaceJavascript;
			this.afterReplaceJavascript = entry.afterReplaceJavascript;
			this.replaceJavascript = entry.replaceJavascript;
		}

		/**
		 * Returns component that will be updated.
		 * 
		 * @return component
		 */
		public Component getComponent()
		{
			return component;
		}

		/**
		 * Sets the javascript executed right before replacing the component.
		 * <p>
		 * The javascript can use following variables:
		 * <dl>
		 * <dt>requestQueueItem</dt>
		 * <dd>RequestQueueItem instance for current request</dd>
		 * <dt>componentId</dt>
		 * <dd>MarkupId of component that is about to be replaced
		 * <dt>notify</dt>
		 * <dd>Method that javascript needs to execute after it has finished. Note that it is
		 * mandatory to call this method otherwise the processing pipeline will stop</dd>
		 * </dl>
		 * 
		 * @param beforeReplaceJavascript
		 *            the javascript
		 */
		public void setBeforeReplaceJavascript(String beforeReplaceJavascript)
		{
			this.beforeReplaceJavascript = beforeReplaceJavascript;
		}

		/**
		 * Returns the javascript executed before replacing the component.
		 * 
		 * @see #setBeforeReplaceJavascript(String)
		 * @return javascript
		 */
		public String getBeforeReplaceJavascript()
		{
			return beforeReplaceJavascript;
		}

		/**
		 * Sets the javascript executed right after replacing the component.
		 * <p>
		 * The javascript can use following variables:
		 * <dl>
		 * <dt>requestQueueItem</dt>
		 * <dd>RequestQueueItem instance for current request</dd>
		 * <dt>componentId</dt>
		 * <dd>MarkupId of component that has been replaced
		 * <dt>insertedElements</dt>
		 * <dd>Array of newly inserted elements</dd>
		 * <dt>notify</dt>
		 * <dd>Method that javascript needs to execute after it has finished. Note that it is
		 * mandatory to call this method otherwise the processing pipeline will stop</dd>
		 * </dl>
		 * 
		 * @param afterReplaceJavascript
		 *            the javascript
		 */
		public void setAfterReplaceJavascript(String afterReplaceJavascript)
		{
			this.afterReplaceJavascript = afterReplaceJavascript;
		}

		/**
		 * Returns the javascript executed after replacing the component.
		 * 
		 * @see #setAfterReplaceJavascript(String)
		 * @return javascript
		 */
		public String getAfterReplaceJavascript()
		{
			return afterReplaceJavascript;
		}

		/**
		 * Sets the javascript executed to replace the component.
		 * <p>
		 * The javascript can use following variables:
		 * <dl>
		 * <dt>requestQueueItem</dt>
		 * <dd>RequestQueueItem instance for current request</dd>
		 * <dt>componentId</dt>
		 * <dd>MarkupId of component that has been replaced
		 * <dt>markup</dt>
		 * <dd>The new markup that should replace current markup</dd>
		 * <dt>notify</dt>
		 * <dd>Method that javascript needs to execute after the component has been replaced. Note
		 * that it is mandatory to call this method otherwise the processing pipeline will stop.
		 * Array of newly inserted elements should be passed as argument to the notify method.</dd>
		 * </dl>
		 * 
		 * An example javascript:
		 * 
		 * <pre>
		 * var element = W.$(componentId);
		 * var insertedElements = W.replaceOuterHtml(element, markup);
		 * notify(insertedElements);
		 * 
		 * </pre>
		 * 
		 * @param replaceJavascript
		 *            the javascript
		 */
		public void setReplaceJavascript(String replaceJavascript)
		{
			this.replaceJavascript = replaceJavascript;
		}

		/**
		 * Returns the javascript executed to replace component.
		 * 
		 * @see #setReplaceJavascript(String)
		 * @return javsacript executed to replace component
		 */
		public String getReplaceJavascript()
		{
			return replaceJavascript;
		}
	}

	private static class UnmodifiableComponentEntry extends ComponentEntry
	{

		public UnmodifiableComponentEntry(ComponentEntry entry)
		{
			super(entry);
		}

		@Override
		public void setAfterReplaceJavascript(String javascript)
		{
			throw new UnsupportedOperationException("ComponentEntry is can not be modified.");
		}

		@Override
		public void setBeforeReplaceJavascript(String javascript)
		{
			throw new UnsupportedOperationException("ComponentEntry is can not be modified.");
		}

		@Override
		public void setReplaceJavascript(String javascript)
		{
			throw new UnsupportedOperationException("ComponentEntry is can not be modified.");
		}
	}

	private boolean isParent(Component parent, Component component)
	{
		Component p = component.getParent();
		while (p != null && p != parent)
		{
			p = p.getParent();
		}
		return p == null;
	}

	private void checkComponent(Component component)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("Component may not be null.");
		}
		else if (component instanceof Page)
		{
			throw new IllegalArgumentException("Component cannot be a page");
		}
		else if (!component.getOutputMarkupId())
		{
			throw new IllegalStateException("Component " + component.getClass().getName() +
				" must have setOuputMarkupId set in order to be updated via ajax.");
		}
		else if (component.getRenderBodyOnly())
		{
			throw new IllegalStateException("Component " + component.getClass().getName() +
				" must not have setRenderBodyOnly set in order to be updated via ajax.");
		}
		else if (component instanceof AbstractRepeater)
		{
			throw new IllegalArgumentException(
				"Component " +
					component.getClass().getName() +
					" has been added to the target. This component is a repeater and cannot be repainted via ajax directly. Instead add its parent or another markup container higher in the hierarchy.");
		}
	}

	/**
	 * Adds a component entry to the list of components to be rendered
	 * 
	 * @param entry
	 *            component entry to be rendered
	 * 
	 * @return <code>true</code> if the component was added, <code>false</code> if the
	 *          component or some of it's parents is already in the list
	 */
	public boolean addComponent(ComponentEntry entry)
	{
		if (entry == null)
		{
			throw new IllegalArgumentException("Argument 'entry' may not be null.");
		}

		final Component component = entry.getComponent();
		checkComponent(component);

		for (ComponentEntry e : entries)
		{
			if (e.getComponent() == component)
			{
				return false;
			}
			// check if component's parent is already in queue
			else if (isParent(e.getComponent(), component))
			{
				return false;
			}
			// check if new component is parent of existing component
			else if (isParent(component, e.getComponent()))
			{
				entries.remove(e);
				break;
			}
		}
		entries.add(entry);
		return true;
	}

	/**
	 * Adds a component to the list of components to be rendered
	 * 
	 * @param component
	 *            component to be rendered
	 * 
	 * @return <code>true</code> if the component was added, <code>false</code> if the
	 *          component or some of it's parents is already in the list
	 */

	public boolean addComponent(Component component)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("Argument 'component' may not be null.");
		}
		return addComponent(new ComponentEntry(component));
	}

	private static class JavascriptEntry
	{
		private final String javascript;
		private final boolean async;

		public JavascriptEntry(String javascript, boolean async)
		{
			this.javascript = javascript;
			this.async = async;
		}

		public String getJavascript()
		{
			return javascript;
		}

		public boolean isAsync()
		{
			return async;
		}
	};

	/**
	 * Adds javascript that will be evaluated on the client side before components are replaced
	 * <p>
	 * The javascript can use following variables:
	 * <dl>
	 * <dt>requestQueueItem</dt>
	 * <dd>RequestQueueItem instance for current request</dd>
	 * <dt>notify</dt>
	 * <dd>Must be called for asynchronous javascript
	 * </dl>
	 * 
	 * @param javascript
	 *            javascript to be evaluated
	 * @param async
	 *            indicates if the javascript should be evaluated asynchrously. If
	 *            <code>async</code> is <code>true</code>, the javascript must invoke the
	 *            <code>notify</code> function that it gets passed for the processing queue to
	 *            continue.
	 */
	public void prependJavascript(String javascript, boolean async)
	{
		if (javascript == null)
		{
			throw new IllegalArgumentException("Argument 'javascript' may not be null.");
		}
		prependJavascripts.add(new JavascriptEntry(javascript, async));
	}

	/**
	 * Adds javascript that will be evaluated on the client side before components are replaced. The
	 * javascript will be executed synchronously which means that the processing queue will be held
	 * until the javascript finishes.
	 * <p>
	 * The javascript can use following variables:
	 * <dl>
	 * <dt>requestQueueItem</dt>
	 * <dd>RequestQueueItem instance for current request</dd>
	 * </dl>
	 * 
	 * @param javascript
	 *            javascript to be evaluated
	 */
	public void prependJavascript(String javascript)
	{
		prependJavascript(javascript, false);
	}

	/**
	 * Adds javascript that will be evaluated on the client side after components are replaced
	 * <p>
	 * The javascript can use following variables:
	 * <dl>
	 * <dt>requestQueueItem</dt>
	 * <dd>RequestQueueItem instance for current request</dd>
	 * <dt>notify</dt>
	 * <dd>Must be called for asynchronous javascript
	 * </dl>
	 * 
	 * @param javascript
	 *            javascript to be evaluated
	 * @param async
	 *            indicates if the javascript should be evaluated asynchrously. If
	 *            <code>async</code> is <code>true</code>, the javascript must invoke the
	 *            <code>notify</code> function that it gets passed for the processing queue to
	 *            continue.
	 */
	public void appendJavascript(String javascript, boolean async)
	{
		if (javascript == null)
		{
			throw new IllegalArgumentException("Argument 'javascript' may not be null.");
		}
		appendJavascripts.add(new JavascriptEntry(javascript, async));
	}

	/**
	 * Adds javascript that will be evaluated on the client side after components are replaced. The
	 * javascript will be executed synchronously which means that the processing queue will be held
	 * until the javascript finishes.
	 * <p>
	 * The javascript can use following variables:
	 * <dl>
	 * <dt>requestQueueItem</dt>
	 * <dd>RequestQueueItem instance for current request</dd>
	 * </dl>
	 * 
	 * @param javascript
	 *            javascript to be evaluated
	 */
	public void appendJavascript(String javascript)
	{
		appendJavascript(javascript, false);
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
		listeners.add(listener);
	}

	private List<ComponentEntry> entriesCopy()
	{
		List<ComponentEntry> list = new ArrayList<ComponentEntry>(entries.size());
		for (ComponentEntry e : entries)
		{
			list.add(new UnmodifiableComponentEntry(e));
		}
		return Collections.unmodifiableList(list);
	}

	private void fireOnBeforeRespondListeners(List<ComponentEntry> entries)
	{
		if (!listeners.isEmpty())
		{
			for (IListener l : listeners)
			{
				l.onBeforeRespond(entries, this);
			}
		}
	}

	private void fireOnAfterRespondListeners(List<ComponentEntry> entries)
	{
		// invoke onafterresponse event on listeners
		if (!(listeners.isEmpty()))
		{
			// create response that will be used by listeners to append
			// javascript
			final IJavascriptResponse jsresponse = new IJavascriptResponse()
			{

				public void addJavascript(String script)
				{
					appendJavascript(script, false);
				}
			};

			for (IListener listener : listeners)
			{
				listener.onAfterRespond(entries, jsresponse);
			}
		}
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

		@Override
		public void renderCSSReference(ResourceReference reference, String media)
		{
			checkHeaderRendering();
			super.renderCSSReference(reference, media);
		}

		@Override
		public void renderCSSReference(String url)
		{
			checkHeaderRendering();
			super.renderCSSReference(url);
		}

		@Override
		public void renderCSSReference(String url, String media)
		{
			checkHeaderRendering();
			super.renderCSSReference(url, media);
		}

		@Override
		public void renderJavascript(CharSequence javascript, String id)
		{
			checkHeaderRendering();
			super.renderJavascript(javascript, id);
		}

		@Override
		public void renderCSSReference(ResourceReference reference)
		{
			checkHeaderRendering();
			super.renderCSSReference(reference);
		}

		@Override
		public void renderJavascriptReference(ResourceReference reference)
		{
			checkHeaderRendering();
			super.renderJavascriptReference(reference);
		}

		@Override
		public void renderJavascriptReference(ResourceReference reference, String id)
		{
			checkHeaderRendering();
			super.renderJavascriptReference(reference, id);
		}

		@Override
		public void renderJavascriptReference(String url)
		{
			checkHeaderRendering();
			super.renderJavascriptReference(url);
		}

		@Override
		public void renderJavascriptReference(String url, String id)
		{
			checkHeaderRendering();
			super.renderJavascriptReference(url, id);
		}

		@Override
		public void renderString(CharSequence string)
		{
			checkHeaderRendering();
			super.renderString(string);
		}

		/**
		 * Construct.
		 */
		public AjaxHeaderResponse()
		{

		}

		/**
		 * 
		 * @see org.apache.wicket.markup.html.internal.HeaderResponse#renderOnDomReadyJavascript(java.lang.String)
		 */
		@Override
		public void renderOnDomReadyJavascript(String javascript)
		{
			List<String> token = Arrays.asList(new String[] { "javascript-event", "window",
					"domready", javascript });
			if (wasRendered(token) == false)
			{
				domReadyJavascripts.add(new JavascriptEntry(javascript, false));
				markRendered(token);
			}
		}

		/**
		 * 
		 * @see org.apache.wicket.markup.html.internal.HeaderResponse#renderOnLoadJavascript(java.lang.String)
		 */
		@Override
		public void renderOnLoadJavascript(String javascript)
		{
			List<String> token = Arrays.asList(new String[] { "javascript-event", "window", "load",
					javascript });
			if (wasRendered(token) == false)
			{
				// execute the javascript after all other scripts are executed
				appendJavascript(javascript, false);
				markRendered(token);
			}
		}

		/**
		 * 
		 * @see org.apache.wicket.markup.html.internal.HeaderResponse#getRealResponse()
		 */
		@Override
		protected Response getRealResponse()
		{
			return RequestCycle.get().getResponse();
		}
	};

	/**
	 * Returns the header response associated with current AjaxRequestTarget.
	 * 
	 * Beware that only renderOnDomReadyJavascript and renderOnLoadJavascript can be called outside
	 * the renderHeader(IHeaderResponse response) method. Calls to other render** methods will
	 * result in an exception being thrown.
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

		/**
		 * 
		 * @see org.apache.wicket.markup.html.internal.HtmlHeaderContainer#newHeaderResponse()
		 */
		@Override
		protected IHeaderResponse newHeaderResponse()
		{
			return target.getHeaderResponse();
		}

		private final transient AjaxRequestTarget target;
	};

	/**
	 * 
	 * @param response
	 * @param component
	 */
	private void respondHeaderContribution(final Response response, final Component component)
	{
		// render the head of component and all it's children

		component.renderHead(header);

		if (component instanceof MarkupContainer)
		{
			((MarkupContainer)component).visitChildren(new Component.IVisitor<Component>()
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
	}

	private String respondHeaderContribution()
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
		StringResponse stringResponse = new StringResponse();
		Response oldResponse = RequestCycle.get().setResponse(stringResponse);

		for (ComponentEntry e : entries)
		{
			respondHeaderContribution(stringResponse, component);
		}

		// revert to old response
		RequestCycle.get().setResponse(oldResponse);

		headerRendering = false;
		return stringResponse.toString();
	}

	/**
	 * 
	 * @param redirect
	 */
	public void setRedirect(String redirect)
	{
		// TODO: Implement redirect
		this.redirect = redirect;
	}

	private void prepareRender()
	{
		for (Iterator<ComponentEntry> i = entries.iterator(); i.hasNext();)
		{
			ComponentEntry entry = i.next();
			Component component = entry.getComponent();

			final Page page = (Page)component.findParent(Page.class);
			if (page == null)
			{
				// dont throw an exception but just ignore this component, somehow
				// it got removed from the page.
				log.debug("component: " + component + " with markupid: " + component.getMarkupId() +
					" not rendered because it was already removed from page");
				i.remove();
				continue;
			}

			checkComponent(component);

			try
			{
				component.prepareForRender();
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
				throw e;
			}
		}
	}

	private String renderComponent(Component component)
	{
		StringResponse stringResponse = new StringResponse();
		Response originalResponse = RequestCycle.get().setResponse(stringResponse);

		page.startComponentRender(component);
		component.renderComponent();
		page.endComponentRender(component);

		RequestCycle.get().setResponse(originalResponse);
		return stringResponse.toString();
	}

	private JSONObject renderComponentEntry(ComponentEntry componentEntry)
	{
		JSONObject object = new JSONObject();

		Component component = componentEntry.getComponent();
		object.put("componentId", component.getMarkupId());
		object.put("beforeReplaceJavascript", componentEntry.getBeforeReplaceJavascript());
		object.put("afterReplaceJavascript", componentEntry.getAfterReplaceJavascript());
		object.put("replaceJavascript", componentEntry.getReplaceJavascript());
		object.put("markup", renderComponent(component));

		return object;
	}

	private JSONObject renderJavascriptEntry(JavascriptEntry javascriptEntry)
	{
		JSONObject object = new JSONObject();

		object.put("async", javascriptEntry.isAsync());
		object.put("javascript", javascriptEntry.getJavascript());

		return object;
	}

	public void respond(RequestCycle requestCycle)
	{
		IBehavior behavior = component.getBehaviors().get(behaviorIndex);
		if (behavior instanceof AjaxBehavior == false)
		{
			throw new WicketRuntimeException("Behavior must be instance of AjaxBehavior.");
		}
		((AjaxBehavior)behavior).respond(this);

		List<ComponentEntry> entriesCopy = entriesCopy();
		fireOnBeforeRespondListeners(entriesCopy);

		JSONObject response = new JSONObject();

		if (redirect != null)
		{
			response.put("redirect", redirect);
		}
		else
		{
			JSONArray components = new JSONArray();
			response.put("components", components);

			if (!entries.isEmpty())
			{
				prepareRender();

				response.put("header", respondHeaderContribution());

				for (ComponentEntry entry : entries)
				{
					components.put(renderComponentEntry(entry));
				}
			}

			fireOnAfterRespondListeners(entries);

			JSONArray prependJavascripts = new JSONArray();
			response.put("prependJavascript", prependJavascripts);

			for (JavascriptEntry e : this.prependJavascripts)
			{
				prependJavascripts.put(renderJavascriptEntry(e));
			}

			JSONArray appendJavascripts = new JSONArray();
			response.put("appendJavascript", appendJavascripts);

			for (JavascriptEntry e : this.domReadyJavascripts)
			{
				appendJavascripts.put(renderJavascriptEntry(e));
			}

			for (JavascriptEntry e : this.appendJavascripts)
			{
				appendJavascripts.put(renderJavascriptEntry(e));
			}
		}

		WebResponse webResponse = (WebResponse)requestCycle.getResponse();
		prepareResponse(webResponse);

		webResponse.write("if (false) (");
		webResponse.write(response.toString());
		webResponse.write(")");
	}

	private void prepareResponse(WebResponse response)
	{
		final Application app = Application.get();

		// Determine encoding
		final String encoding = app.getRequestCycleSettings().getResponseRequestEncoding();

		// Set content type based on markup type for page
		response.setCharacterEncoding(encoding);
		response.setContentType("text/xml; charset=" + encoding);

		// Make sure it is not cached by a client
		response.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Pragma", "no-cache");
	}

	/**
	 * Dummy AJAX request target instance used by {@link AjaxBehavior} to generate AJAX URL prefix.
	 */
	public static final AjaxRequestTarget DUMMY = new AjaxRequestTarget();
}
