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
package org.apache.wicket.core.request.handler;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.head.IHeaderResponse;

/**
 * Request handler that allows partial updates of the current page instance.
 */
public interface IPartialPageRequestHandler extends IPageRequestHandler
{

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
	 * Add JavasSript that will be evaluated on the client side after components are replaced
	 * <p>
	 * If the JavaScript needs to do something asynchronously (i.e. uses window.setTimeout(), for example
	 * to do animations), it may call <code>Wicket.Ajax.suspendCall()</code> to suspend the evaluation of the current
	 * Ajax call. The returned function has to be called when the asynchronous task is finished, after which the evaluation
	 * of the Ajax call is continued, e.g.
	 * <pre>
	 * target.appendJavaScript("var continueCall = Wicket.Ajax.suspendCall(); try { ... } finally { continueCall(); }");
	 * </pre>
	 * <strong>Important</strong>: it is recommended to execute your code in try/finally to make sure
	 * the function is executed even if an error happens in your code, otherwise all following scripts and
	 * component replacements wont be made.
	 *
	 * @param javascript
	 */
	void appendJavaScript(CharSequence javascript);

	/**
	 * Add JavaScript that will be evaluated on the client side before components are replaced.
	 * <p>
	 * If the JavaScript needs to do something asynchronously (i.e. uses window.setTimeout(), for example
	 * to do animations), it may call <code>Wicket.Ajax.suspendCall()</code> to suspend the evaluation of the current
	 * Ajax call. The returned function has to be called when the asynchronous task is finished, after which the evaluation
	 * of the Ajax call is continued, e.g.
	 * <pre>
	 * target.prependJavaScript("var continueCall = Wicket.Ajax.suspendCall(); try { ... } finally { continueCall(); }");
	 * </pre>
	 * <strong>Important</strong>: it is recommended to execute your code in try/finally to make sure
	 * the function is executed even if an error happens in your code, otherwise all following scripts and
	 * component replacements wont be made.
	 *
	 * @param javascript
	 */
	void prependJavaScript(CharSequence javascript);

	/**
	 * Adds a named data entry to the response, for client-side code that subscribed to it.
	 * <p>
	 * The value is delivered to the client as text, exactly as given: Wicket neither interprets,
	 * escapes nor evaluates it. On the client it is published on the topic
	 * <code>Wicket.Event.Topic.AJAX_CALL_DATA</code> (<code>'/ajax/call/data'</code>) with the
	 * arguments <code>(attrs, name, value)</code>, where <code>attrs</code> are the attributes of
	 * the Ajax call. A partial update pushed over a web socket belongs to no Ajax call, so its
	 * subscribers receive an empty object as <code>attrs</code>. Data entries are published in the
	 * order they were added, after the components have been replaced and the JavaScripts
	 * evaluated, and before the success handlers of the call run.
	 * <p>
	 * A subscriber that interprets the value, for example as JSON, has to treat it as data and
	 * never evaluate it as script or insert it as markup.
	 * <p>
	 * Every subscriber on the page sees every entry, and the name is all that tells them apart.
	 * Libraries should therefore use a name of their own, prefixed with the library's name, so
	 * their entries do not collide with those of another library.
	 *
	 * @param name
	 *            the name of the entry, used by subscribers to recognise it
	 * @param value
	 *            the value to deliver
	 * @throws IllegalStateException
	 *             if the response is already being written
	 * @since 12.0.0
	 */
	void addData(String name, CharSequence value);

	/**
	 * Sets the focus in the browser to the given component. The markup id must be set. If the
	 * component is null the focus will not be set to any component.
	 *
	 * @param component
	 *            The component to get the focus or null.
	 */
	void focusComponent(Component component);

	/**
	 * Returns an unmodifiable collection of all components added to this target
	 *
	 * @return unmodifiable collection of all components added to this target
	 */
	Collection<? extends Component> getComponents();

	/**
	 * Returns the header response associated with current handler.
	 *
	 * Beware that only renderOnDomReadyJavaScript and renderOnLoadJavaScript can be called outside
	 * the renderHeader(IHeaderResponse response) method. Calls to other render** methods will
	 * result in the call failing with a debug-level log statement to help you see why it failed.
	 *
	 * @return header response
	 */
	IHeaderResponse getHeaderResponse();
}
