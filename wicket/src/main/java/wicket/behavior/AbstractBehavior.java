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
package wicket.behavior;

import wicket.Component;
import wicket.markup.ComponentTag;

/**
 * Adapter implementation of {@link wicket.behavior.IBehavior}. It is
 * recommended to extend from this class instead of directly implementing
 * {@link wicket.behavior.IBehavior} as this class has an extra clean
 * 
 * @author Ralf Ebert
 * @author Eelco Hillenius
 */
public abstract class AbstractBehavior implements IBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public AbstractBehavior()
	{
	}

	/**
	 * @see wicket.behavior.IBehavior#beforeRender(wicket.Component)
	 */
	public void beforeRender(Component component)
	{
	}

	/**
	 * @see wicket.behavior.IBehavior#bind(wicket.Component)
	 */
	public void bind(final Component component)
	{
	}

	/**
	 * This method is called either by {@link #onRendered(Component)} or
	 * {@link #onException(Component, RuntimeException)} AFTER they called their
	 * respective template methods. Override this template method to do any
	 * necessary cleanup.
	 */
	public void cleanup()
	{
	}

	/**
	 * @see wicket.behavior.IBehavior#detach(Component)
	 */
	public void detach(Component component)
	{
	}

	/**
	 * @see wicket.behavior.IBehavior#exception(wicket.Component,
	 *      java.lang.RuntimeException)
	 */
	public void exception(Component component, RuntimeException exception)
	{
		try
		{
			onException(component, exception);
		}
		finally
		{
			cleanup();
		}
	}

	/**
	 * @see wicket.behavior.IBehavior#getStatelessHint()
	 */
	public boolean getStatelessHint()
	{
		return true;
	}

	/**
	 * @see wicket.behavior.IBehavior#onComponentTag(wicket.Component,
	 *      wicket.markup.ComponentTag)
	 */
	public void onComponentTag(final Component component, final ComponentTag tag)
	{
	}

	/**
	 * In case an unexpected exception happened anywhere between
	 * onComponentTag() and rendered(), onException() will be called for any
	 * behavior.
	 * 
	 * @param component
	 *            the component that has a reference to this behavior and during
	 *            which processing the exception occured
	 * @param exception
	 *            the unexpected exception
	 */
	public void onException(Component component, RuntimeException exception)
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
	 * @see wicket.behavior.IBehavior#rendered(wicket.Component)
	 */
	public final void rendered(final Component component)
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
