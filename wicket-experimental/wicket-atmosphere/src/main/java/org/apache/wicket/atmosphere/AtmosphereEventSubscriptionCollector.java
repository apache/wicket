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

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.behavior.Behavior;

/**
 * Collects {@linkplain Subscribe event subscriptions} on components. Subscriptions are refreshed on
 * every render of component. If a page contains a component with a subscription, an
 * {@link AtmosphereBehavior} is added to the page. There is no need to register this listener, it
 * is added automatically by {@link EventBus}.
 * 
 * @author papegaaij
 */
public class AtmosphereEventSubscriptionCollector implements IComponentOnBeforeRenderListener
{
	private EventBus eventBus;

	/**
	 * Construct.
	 * 
	 * @param eventBus
	 */
	public AtmosphereEventSubscriptionCollector(EventBus eventBus)
	{
		this.eventBus = eventBus;
	}

	@Override
	public void onBeforeRender(Component component)
	{
		for (Method curMethod : component.getClass().getMethods())
		{
			if (curMethod.isAnnotationPresent(Subscribe.class))
			{
				verifyMethodParameters(curMethod);
				subscribeComponent(component, null, curMethod);
			}
		}
		for (Behavior curBehavior : component.getBehaviors())
		{
			for (Method curMethod : curBehavior.getClass().getMethods())
			{
				if (curMethod.isAnnotationPresent(Subscribe.class))
				{
					verifyMethodParameters(curMethod);
					subscribeComponent(component, curBehavior, curMethod);
				}
			}
		}
	}

	private void verifyMethodParameters(Method method)
	{
		Class<?>[] params = method.getParameterTypes();
		if (params.length != 2 || !params[0].equals(AjaxRequestTarget.class))
			throw new WicketRuntimeException("@Subscribe can only be used on " +
				"methods with 2 params, of which the first is AjaxRequestTarget. " + method +
				" does conform to this signature.");
	}

	private void subscribeComponent(Component component, Behavior behavior, Method method)
	{
		EventSubscription subscription = new EventSubscription(component, behavior, method);
		Page page = component.getPage();
		eventBus.register(page, subscription);
		if (page.getBehaviors(AtmosphereBehavior.class).isEmpty())
			page.add(new AtmosphereBehavior());
	}
}
