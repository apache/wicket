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
package org.apache.wicket.atmosphere;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles pseudo requests triggered by an event. An {@link AjaxRequestTarget} is scheduled and the
 * subscribed methods are invoked.
 *
 * @author papegaaij
 */
public class AtmosphereRequestHandler implements IRequestHandler
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AtmosphereRequestHandler.class);

	private final PageKey pageKey;

	private final AtmosphereEvent event;

	private final Collection<EventSubscription> subscriptions;

	private final EventSubscriptionInvoker eventSubscriptionInvoker;

	private boolean ajaxRequestScheduled = false;

	/**
	 * Construct.
	 *
	 * @param pageKey
	 * @param subscriptions
	 * @param event
	 * @param eventSubscriptionInvoker
	 */
	public AtmosphereRequestHandler(PageKey pageKey, Collection<EventSubscription> subscriptions,
		AtmosphereEvent event, EventSubscriptionInvoker eventSubscriptionInvoker)
	{
		this.pageKey = pageKey;
		this.subscriptions = subscriptions;
		this.event = event;
		this.eventSubscriptionInvoker = Args.notNull(eventSubscriptionInvoker, "eventSubscriptionInvoker");
	}

	@Override
	public void respond(IRequestCycle requestCycle)
	{
		WebApplication application = WebApplication.get();
		Page page = (Page)application.getMapperContext().getPageInstance(pageKey.getPageId());
		AjaxRequestTarget target = application.newAjaxRequestTarget(page);
		executeHandlers(target, page);
	}

	private void executeHandlers(AjaxRequestTarget target, Page page)
	{
		for (EventSubscription curSubscription : subscriptions)
		{
			if (curSubscription.getContextAwareFilter().apply(event))
			{
				String componentPath = curSubscription.getComponentPath();
				Component component = page.get(componentPath);
				if (component != null)
				{
					Integer behaviorIndex = curSubscription.getBehaviorIndex();
					if (behaviorIndex == null)
					{
						invokeMethod(target, curSubscription, component);
					}
					else
					{
						Behavior behavior = component.getBehaviorById(behaviorIndex);
						invokeMethod(target, curSubscription, behavior);
					}
				}
				else
				{
					LOGGER.debug("Cannot find component with path '{}' in page '{}'. Maybe it has been removed.",
							componentPath, page);
					EventBus.get().unregister(page, curSubscription);
				}
			}
		}
	}

	private void invokeMethod(final AjaxRequestTarget target, EventSubscription subscription,
		Object base)
	{
		AjaxRequestInitializer initializer = new AjaxRequestInitializer()
		{
			@Override
			public void initialize()
			{
				if (!ajaxRequestScheduled)
				{
					RequestCycle.get().scheduleRequestHandlerAfterCurrent(target);
					ajaxRequestScheduled = true;
				}
			}
		};
		eventSubscriptionInvoker.invoke(target, subscription, base, event, initializer);
	}

	@Override
	public void detach(IRequestCycle requestCycle)
	{
	}
}
