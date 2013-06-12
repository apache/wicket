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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * Handles invocations of methods annotated with {@link Subscribe} annotation.
 */
public class SubscribeAnnotationEventSubscriptionInvoker implements EventSubscriptionInvoker
{
	@Override
	public void invoke(AjaxRequestTarget target, EventSubscription subscription, Object base,
		AtmosphereEvent event, AjaxRequestInitializer ajaxRequestInitializer)
	{
		for (Method curMethod : base.getClass().getMethods())
		{
			if (curMethod.isAnnotationPresent(Subscribe.class) &&
				curMethod.getName().equals(subscription.getMethodName()))
			{
				ajaxRequestInitializer.initialize();
				try
				{
					curMethod.setAccessible(true);
					curMethod.invoke(base, target, event.getPayload());
				}
				catch (IllegalAccessException e)
				{
					throw new WicketRuntimeException(e);
				}
				catch (IllegalArgumentException e)
				{
					throw new WicketRuntimeException(e);
				}
				catch (InvocationTargetException e)
				{
					throw new WicketRuntimeException(e);
				}
			}
		}
	}

}
