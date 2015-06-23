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

import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.request.ILoggableRequestHandler;

/**
 *
 * @since 6.0
 */
public interface AjaxRequestTarget extends IPartialPageRequestHandler, ILoggableRequestHandler
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
		 *            modifiable map (markupId -> component) of components already added to the target
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
		 * Triggered for every Ajax behavior. Can be used to configure common settings.
		 * 
		 * @param behavior
		 *            the behavior the attributes are updated for
		 * @param attributes
		 *            The attributes for the Ajax behavior
		 * @since 7.0.0
		 */
		void updateAjaxAttributes(AbstractDefaultAjaxBehavior behavior, AjaxRequestAttributes attributes);
	}

	/**
	 * Empty implementation of an {@link IListener} useful as a starting point for your own
	 * custom listener.
	 */
	class AbstractListener implements IListener
	{
		@Override
		public void updateAjaxAttributes(AbstractDefaultAjaxBehavior behavior, AjaxRequestAttributes attributes)
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
	 * Adds a listener to this target
	 *
	 * @param listener
	 * @throws IllegalStateException
	 *             if {@link AjaxRequestTarget.IListener}'s events are currently being fired or have both been fired
	 *             already
	 */
	void addListener(AjaxRequestTarget.IListener listener);

	/**
	 * Register the given respond listener. The listener's
	 * {@link org.apache.wicket.ajax.AjaxRequestTarget.ITargetRespondListener#onTargetRespond} method will be invoked when
	 * the {@link AjaxRequestTarget} starts to respond.
	 *
	 * @param listener
	 */
	void registerRespondListener(ITargetRespondListener listener);

	/**
	 * Returns the page. Be aware that the page can be instantiated if this wasn't the case already.
	 *
	 * @return page instance
	 */
	@Override
	Page getPage();
}
