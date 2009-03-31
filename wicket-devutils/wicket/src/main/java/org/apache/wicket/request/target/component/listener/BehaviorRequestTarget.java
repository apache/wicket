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
package org.apache.wicket.request.target.component.listener;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.RequestParameters;

/**
 * Target that denotes a page instance and a call to a component on that page using an listener
 * interface method.
 * 
 * @author Eelco Hillenius
 */
public class BehaviorRequestTarget extends AbstractListenerInterfaceRequestTarget
{
	/**
	 * Construct.
	 * 
	 * @param page
	 *            the page instance
	 * @param component
	 *            the target component
	 * @param listener
	 *            the listener method
	 */
	public BehaviorRequestTarget(final Page page, final Component component,
		final RequestListenerInterface listener)
	{
		this(page, component, listener, null);
	}

	/**
	 * Construct.
	 * 
	 * @param page
	 *            the page instance
	 * @param component
	 *            the target component
	 * @param listener
	 *            the listener method
	 * @param requestParameters
	 *            the request parameters
	 */
	public BehaviorRequestTarget(final Page page, final Component component,
		final RequestListenerInterface listener, final RequestParameters requestParameters)
	{
		super(page, component, listener, requestParameters);
	}

	/**
	 * @see org.apache.wicket.request.target.IEventProcessor#processEvents(org.apache.wicket.RequestCycle)
	 */
	public final void processEvents(final RequestCycle requestCycle)
	{
		// Preprocess like standard component request. Do all the initialization
		// necessary
		onProcessEvents(requestCycle);

		// Get the IBehavior for the component based on the request parameters
		final Component component = getTarget();
		final String id = getRequestParameters().getBehaviorId();
		if (id == null)
		{
			// wicket-2107
			throw new PageExpiredException(
				"Parameter behaviorId was not provided: unable to locate listener. Component: " +
					component.toString());
		}

		final int idAsInt = Integer.parseInt(id);
		final List<IBehavior> behaviors = component.getBehaviorsRawList();
		IBehaviorListener behaviorListener = null;

		if (behaviors.size() > idAsInt)
		{
			IBehavior behavior = behaviors.get(idAsInt);
			if (behavior instanceof IBehaviorListener)
			{
				behaviorListener = (IBehaviorListener)behavior;
			}
		}

		if (behaviorListener == null)
		{
			// wicket-2107
			throw new PageExpiredException("No behavior listener found with behaviorId " + id +
				"; Component: " + component.toString());
		}

		// Invoke the interface method
		behaviorListener.onRequest();
	}
}