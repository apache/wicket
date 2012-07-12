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
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.behavior.Behavior;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * The subscription of a method on a component to certain events. This is used by {@link EventBus}
 * to track the subscriptions.
 * 
 * @author papegaaij
 */
public class EventSubscription
{
	private String componentPath;

	private Integer behaviorIndex;

	private String methodName;

	private Predicate<Object> filter;

	/**
	 * Construct.
	 * 
	 * @param component
	 * @param behavior
	 * @param method
	 */
	public EventSubscription(Component component, Behavior behavior, Method method)
	{
		componentPath = component.getPageRelativePath();
		behaviorIndex = behavior == null ? null : component.getBehaviorId(behavior);
		Class<?> eventType = method.getParameterTypes()[1];
		filter = Predicates.and(Predicates.instanceOf(eventType), createFilter(method));
		methodName = method.getName();
	}

	@SuppressWarnings("unchecked")
	private static Predicate<Object> createFilter(Method method)
	{
		Subscribe subscribe = method.getAnnotation(Subscribe.class);
		try
		{
			return (Predicate<Object>)subscribe.filter().newInstance();
		}
		catch (InstantiationException e)
		{
			throw new WicketRuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * @return The path of the subscribed component
	 */
	public String getComponentPath()
	{
		return componentPath;
	}

	/**
	 * @return The index of the subscribed behavior, or null if the subscription is for the
	 *         component itself
	 */
	public Integer getBehaviorIndex()
	{
		return behaviorIndex;
	}

	/**
	 * @return The filter on incomming events, a combination of the type and the
	 *         {@link Subscribe#filter()} parameter.
	 */
	public Predicate<Object> getFilter()
	{
		return filter;
	}

	/**
	 * @return The method that is subscribed
	 */
	public String getMethodName()
	{
		return methodName;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(componentPath, behaviorIndex, methodName);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof EventSubscription)
		{
			EventSubscription other = (EventSubscription)obj;
			return Objects.equal(componentPath, other.getComponentPath()) &&
				Objects.equal(behaviorIndex, other.getBehaviorIndex()) &&
				Objects.equal(methodName, other.getMethodName());
		}
		return false;
	}
}
