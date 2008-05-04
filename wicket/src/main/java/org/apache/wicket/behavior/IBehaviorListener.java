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
package org.apache.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.component.listener.BehaviorRequestTarget;

/**
 * Listens for requests to behaviors. When {@link org.apache.wicket.behavior.IBehavior}s are
 * 'enriched' with this interface, they can receive requests themselves. You can use this for
 * example to implement AJAX behavior, though you'll probably want to extend from
 * {@link org.apache.wicket.behavior.AbstractAjaxBehavior} directly instead in that case.
 * 
 * @author Eelco Hillenius
 */
public interface IBehaviorListener extends IRequestListener
{
	/** Behavior listener interface */
	public static final RequestListenerInterface INTERFACE = new RequestListenerInterface(
		IBehaviorListener.class)
	{
		/**
		 * 
		 * @see org.apache.wicket.RequestListenerInterface#newRequestTarget(org.apache.wicket.Page,
		 *      org.apache.wicket.Component, org.apache.wicket.RequestListenerInterface,
		 *      org.apache.wicket.request.RequestParameters)
		 */
		@Override
		public IRequestTarget newRequestTarget(Page< ? > page, Component< ? > component,
			RequestListenerInterface listener, RequestParameters requestParameters)
		{
			return new BehaviorRequestTarget(page, component, listener, requestParameters);
		}
	};

	/**
	 * Called when a request to a behavior is received.
	 */
	void onRequest();
}
