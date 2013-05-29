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
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.ILoggableRequestHandler;
import org.apache.wicket.core.request.handler.IPageRequestHandler;

/**
 *
 * @since 6.0
 */
public interface AjaxRequestTarget extends IPageRequestHandler, ILoggableRequestHandler
{
	/**
	 * An {@link AjaxRequestTarget} listener that can be used to respond to various target-related
	 * events
	 *
	 */
	interface IListener
	{
		/**
		 * Triggered before ajax request target begins its response cycle
		 *
		 * @param map
		 *            read-only map:markupId->component of components already added to the target
		 * @param target
		 *            the target itself. Could be used to add components or to append/prepend
		 *            javascript
		 *
		 */
		void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget target);

		/**
		 * Triggered after ajax request target is done with its response cycle. At this point only
		 * additional javascript can be output to the response using the provided
		 * {@link AjaxRequestTarget.IJavaScriptResponse} object
		 *
		 * NOTE: During this stage of processing any calls to target that manipulate the response
		 * (adding components, javascript) will have no effect
		 *
		 * @param map
		 *            read-only map:markupId->component of components already added to the target
		 * @param response
		 *            response object that can be used to output javascript
		 */
		void onAfterRespond(Map<String, Component> map, AjaxRequestTarget.IJavaScriptResponse response);

		/**
		 * Triggered for every Ajax behavior.
		 * Can be used to configure common settings.
		 *
		 * @param attributes
		 *            The attributes for the Ajax behavior
		 */
		void updateAjaxAttributes(AjaxRequestAttributes attributes);
	}

	/**
	 * Empty implementation of an {@link IListener} useful as a starting point for your own
	 * custom listener.
	 */
	class AbstractListener implements IListener
	{
		@Override
		public void updateAjaxAttributes(AjaxRequestAttributes attributes)
		{
		}

		@Override
		public void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget target)
		{
		}

		@Override
		public void onAfterRespond(Map<String, Component> map, IJavaScriptResponse response)
		{
		}
	}

	/**
	 * An ajax javascript response that allows users to add javascript to be executed on the client
	 * side
	 *
	 * @author ivaynberg
	 */
	interface IJavaScriptResponse
	{
		/**
		 * Adds more javascript to the ajax response that will be executed on the client side
		 *
		 * @param script
		 *            javascript
		 */
		void addJavaScript(String script);
	}

	/**
	 * Components can implement this interface to get a notification when AjaxRequestTarget begins
	 * to respond. This can be used to postpone adding components to AjaxRequestTarget until the
	 * response begins.
	 *
	 * @author Matej Knopp
	 */
	interface ITargetRespondListener
	{
		/**
		 * Invoked when AjaxRequestTarget is about the respond.
		 *
		 * @param target
		 */
		void onTargetRespond(AjaxRequestTarget target);
	}


	/**
	 * Adds a component to the list of components to be rendered
	 *
	 * @param markupId
	 *            id of client-side dom element that will be updated
	 * @param component
	 *            component to be rendered
	 * @throws IllegalArgumentException
	 *             if the component is a {@link org.apache.wicket.Page} or an {@link org.apache.wicket.markup.repeater.AbstractRepeater}
	 * @throws IllegalStateException
	 *             if the components are currently being rendered, or have already been rendered
	 */
	void add(final Component component, final String markupId);

	/**
	 * Adds components to the list of components to be rendered.
	 *
	 * @param components
	 *            components to be rendered
	 */
	void add(Component... components);


	/**
	 * Visits all children of the specified parent container and adds them to the target if they are
	 * of same type as <code>childCriteria</code>
	 *
	 * @param parent
	 *            Must not be null.
	 * @param childCriteria
	 *            Must not be null. If you want to traverse all components use ` Component.class as
	 *            the value for this argument.
	 */
	void addChildren(MarkupContainer parent, Class<?> childCriteria);


	/**
	 * Adds a listener to this target
	 *
	 * @param listener
	 * @throws IllegalStateException
	 *             if {@link AjaxRequestTarget.IListener}'s events are currently being fired or have both been fired
	 *             already
	 */
	void addListener(AjaxRequestTarget.IListener listener);

	/**
	 * Adds javascript that will be evaluated on the client side after components are replaced
	 *
	 * <p>If the javascript needs to do something asynchronously (i.e. needs to use window.setTimeout(), for example
	 * to do animations) then the following special syntax may be used: <code>someFunctionName|myJsLogic(someFunctionName);</code>.
	 * Wicket will transform it to: <code>function(someFunctionName){myJsLogic(someFunctionName);}</code> and your code
	 * is responsible to execute <em>someFunctionName()</em> when the asynchronous task is finished. Once <em>someFunctionName</em>
	 * is executed the next appended script will be executed. <strong>Important</strong>: it is highly recommended to
	 * execute your code in try/finally to make sure <em>someFunctionName</em> is executed even an error happens in
	 * your code, otherwise all following scripts wont be executed.</p>
	 *
	 * @param javascript
	 */
	void appendJavaScript(CharSequence javascript);

	/**
	 * Adds javascript that will be evaluated on the client side before components are replaced.
	 *
	 * <p>If the javascript needs to do something asynchronously (i.e. needs to use window.setTimeout(), for example
	 * to do animations) then the following special syntax may be used: <code>someFunctionName|myJsLogic(someFunctionName);</code>.
	 * Wicket will transform it to: <code>function(someFunctionName){myJsLogic(someFunctionName);}</code> and your code
	 * is responsible to execute <em>someFunctionName()</em> when the asynchronous task is finished. Once <em>someFunctionName</em>
	 * is executed the next prepended script will be executed. <strong>Important</strong>: it is highly recommended to
	 * execute your code in try/finally to make sure <em>someFunctionName</em> is executed even an error happens in
	 * your code, otherwise all following scripts and component replacements wont be made.</p>
	 *
	 * @param javascript
	 */
	void prependJavaScript(CharSequence javascript);

	/**
	 * Register the given respond listener. The listener's
	 * {@link org.apache.wicket.ajax.AjaxRequestTarget.ITargetRespondListener#onTargetRespond} method will be invoked when
	 * the {@link AjaxRequestTarget} starts to respond.
	 *
	 * @param listener
	 */
	void registerRespondListener(ITargetRespondListener listener);

	/**
	 * Returns an unmodifiable collection of all components added to this target
	 *
	 * @return unmodifiable collection of all components added to this target
	 */
	Collection<? extends Component> getComponents();

	/**
	 * Sets the focus in the browser to the given component. The markup id must be set. If the
	 * component is null the focus will not be set to any component.
	 *
	 * @param component
	 *            The component to get the focus or null.
	 */
	void focusComponent(Component component);

	/**
	 * Returns the header response associated with current AjaxRequestTarget.
	 *
	 * Beware that only renderOnDomReadyJavaScript and renderOnLoadJavaScript can be called outside
	 * the renderHeader(IHeaderResponse response) method. Calls to other render** methods will
	 * result in the call failing with a debug-level log statement to help you see why it failed.
	 *
	 * @return header response
	 */
	IHeaderResponse getHeaderResponse();

	/**
	 * Returns the HTML id of the last focused element.
	 *
	 * @return markup id of last focused element, <code>null</code> if none
	 */
	String getLastFocusedElementId();


	/**
	 * Returns the page. Be aware that the page can be instantiated if this wasn't the case already.
	 *
	 * @return page instance
	 */
	@Override
	Page getPage();
}
