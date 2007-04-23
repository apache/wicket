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
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * Adapter implementation of {@link org.apache.wicket.behavior.IBehavior}. It is
 * recommended to extend from this class instead of directly implementing
 * {@link org.apache.wicket.behavior.IBehavior} as this class has an extra clean
 * 
 * @author Ralf Ebert
 * @author Eelco Hillenius
 */
public abstract class AbstractBehavior implements IBehavior, IHeaderContributor
{
	/**
	 * Construct.
	 */
	public AbstractBehavior()
	{
	}

	/**
	 * @see org.apache.wicket.behavior.IBehavior#beforeRender(org.apache.wicket.Component)
	 */
	public void beforeRender(Component component)
	{
	}

	/**
	 * @see org.apache.wicket.behavior.IBehavior#bind(org.apache.wicket.Component)
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
	 * @see org.apache.wicket.behavior.IBehavior#detachModel(Component)
	 */
	public void detachModel(Component component)
	{
		
	}

	/**
	 * @see org.apache.wicket.behavior.IBehavior#exception(org.apache.wicket.Component,
	 *      java.lang.RuntimeException)
	 */
	public final void exception(Component component, RuntimeException exception)
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
	 * @see org.apache.wicket.behavior.IBehavior#getStatelessHint(org.apache.wicket.Component)
	 */
	public boolean getStatelessHint(Component component)
	{
		return true;
	}

	/**
	 * @see org.apache.wicket.behavior.IBehavior#onComponentTag(org.apache.wicket.Component,
	 *      org.apache.wicket.markup.ComponentTag)
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
	 * @see org.apache.wicket.behavior.IBehavior#rendered(org.apache.wicket.Component)
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
	
	/**
	 * @see org.apache.wicket.markup.html.IHeaderContributor#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
	 */
	public void renderHead(IHeaderResponse response)
	{
	}
	
	/**
	 * @see org.apache.wicket.behavior.IBehavior#isEnabled(Component)
	 */
	public boolean isEnabled(Component component)
	{
		return true;
	}
}
