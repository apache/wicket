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
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;

/**
 * Abstract class for handling Ajax roundtrips. This class serves as a base for javascript specific
 * implementations, like ones based on Dojo or Scriptaculous, or Wicket's default.
 * 
 * @author Eelco Hillenius
 * @author Ralf Ebert
 * @author Igor Vaynberg
 */
public abstract class AbstractAjaxBehavior extends Behavior implements IBehaviorListener
{
	private static final long serialVersionUID = 1L;

	/** the component that this handler is bound to. */
	private Component component;

	/**
	 * Constructor.
	 */
	public AbstractAjaxBehavior()
	{
	}

	/**
	 * Bind this handler to the given component.
	 * 
	 * @param hostComponent
	 *            the component to bind to
	 */
	@Override
	public final void bind(final Component hostComponent)
	{
		Args.notNull(hostComponent, "hostComponent");

		if (component != null)
		{
			throw new IllegalStateException("this kind of handler cannot be attached to " +
				"multiple components; it is already attached to component " + component +
				", but component " + hostComponent + " wants to be attached too");
		}

		component = hostComponent;

		// call the callback
		onBind();
	}

	/**
	 * Gets the url that references this handler.
	 * 
	 * @return the url that references this handler
	 */
	public CharSequence getCallbackUrl()
	{
		if (getComponent() == null)
		{
			throw new IllegalArgumentException(
				"Behavior must be bound to a component to create the URL");
		}

		final RequestListenerInterface rli;

		rli = IBehaviorListener.INTERFACE;

		return getComponent().urlFor(this, rli, new PageParameters());
	}

	/**
	 * @see org.apache.wicket.behavior.Behavior#onComponentTag(org.apache.wicket.Component,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	public final void onComponentTag(final Component component, final ComponentTag tag)
	{
		onComponentTag(tag);
	}

	/**
	 * @see org.apache.wicket.behavior.Behavior#afterRender(org.apache.wicket.Component)
	 */
	@Override
	public final void afterRender(final Component hostComponent)
	{
		onComponentRendered();
	}

	/**
	 * Gets the component that this handler is bound to.
	 * 
	 * @return the component that this handler is bound to
	 */
	protected final Component getComponent()
	{
		return component;
	}

	/**
	 * Called any time a component that has this handler registered is rendering the component tag.
	 * Use this method e.g. to bind to javascript event handlers of the tag
	 * 
	 * @param tag
	 *            the tag that is rendered
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
	}

	/**
	 * Called when the component was bound to it's host component. You can get the bound host
	 * component by calling getComponent.
	 */
	protected void onBind()
	{
	}

	/**
	 * Called to indicate that the component that has this handler registered has been rendered. Use
	 * this method to do any cleaning up of temporary state
	 */
	protected void onComponentRendered()
	{
	}

	@Override
	public final void unbind(Component component)
	{
		onUnbind();
		component = null;
		super.unbind(component);
	}

	/**
	 * Called when the behavior is removed from its component. The bound host component is
	 * still available through {@linkplain #getComponent()}. The relation to it will be removed
	 * right after the finish of the execution of this method.
	 */
	protected void onUnbind()
	{
	}
}
