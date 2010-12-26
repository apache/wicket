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

/**
 * Adapter implementation of {@link org.apache.wicket.behavior.Behavior}. It is recommended to
 * extend from this class instead of directly implementing
 * {@link org.apache.wicket.behavior.Behavior} as this class has an extra {@link #cleanup()} call.
 * 
 * @author Ralf Ebert
 * @author Eelco Hillenius
 * 
 * @deprecated extend {@link Behavior} instead
 */
// TODO WICKET-1.6: remove this class
@Deprecated
public abstract class AbstractBehavior extends Behavior
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public AbstractBehavior()
	{
	}

	/**
	 * This method is called either by {@link #onRendered(Component)} or
	 * {@link #onHandleException(Component, RuntimeException)} AFTER they called their respective
	 * template methods. Override this template method to do any necessary cleanup.
	 */
	public void cleanup()
	{
	}

	/**
	 * @see org.apache.wicket.behavior.Behavior#onException(org.apache.wicket.Component,
	 *      java.lang.RuntimeException)
	 */
	@Override
	public final void onException(Component component, RuntimeException exception)
	{
		try
		{
			onHandleException(component, exception);
		}
		finally
		{
			cleanup();
		}
	}

	/**
	 * In case an unexpected exception happened anywhere between onComponentTag() and rendered(),
	 * onException() will be called for any behavior.
	 * 
	 * @param component
	 *            the component that has a reference to this behavior and during which processing
	 *            the exception occurred
	 * @param exception
	 *            the unexpected exception
	 */
	public void onHandleException(Component component, RuntimeException exception)
	{
	}

	/**
	 * Called when a component that has this behavior coupled was rendered.
	 * 
	 * @param component
	 *            the component that has this behavior coupled
	 */
	public void onRendered(Component component)
	{
	}

	/**
	 * @see org.apache.wicket.behavior.Behavior#afterRender(org.apache.wicket.Component)
	 */
	@Override
	public final void afterRender(final Component component)
	{
		try
		{
			onRendered(component);
		}
		finally
		{
			cleanup();
		}
	}
}
