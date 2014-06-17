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

import org.apache.wicket.Component;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.behavior.Behavior;

/**
 * Thrown when a listener invocation is attempted on a component or behavior that does not allow it.
 * For example, somehow the user attempted to invoke link's onclick method on a disabled link.
 *
 * @author igor
 */
public class ListenerInvocationNotAllowedException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private final Component component;
	private final Behavior behavior;

	/**
	 * Constructor
	 *
	 * @param iface
	 * @param component
	 * @param behavior
	 * @param message
	 */
	public ListenerInvocationNotAllowedException(RequestListenerInterface iface,
		Component component, Behavior behavior, String message)
	{
		super(message + detail(iface, component, behavior));
		this.component = component;
		this.behavior = behavior;
	}

	private static String detail(RequestListenerInterface iface, Component component,
		Behavior behavior)
	{
		StringBuilder detail = new StringBuilder("Component: ").append(component.toString(false));
		if (behavior != null)
		{
			detail.append(" Behavior: ").append(behavior.toString());
		}
		detail.append(" Listener: ").append(iface.toString());
		return detail.toString();
	}

	/**
	 * @return component that was the target of invocation or hosted the behavior that was
	 */
	public Component getComponent()
	{
		return component;
	}

	/**
	 * @return behavior that was the target of invocation or {@code null}
	 */
	public Behavior getBehavior()
	{
		return behavior;
	}


}
