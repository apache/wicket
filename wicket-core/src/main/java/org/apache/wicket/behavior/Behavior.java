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

import java.lang.reflect.Method;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IComponentAwareEventSink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IComponentAwareHeaderContributor;
import org.apache.wicket.util.io.IClusterable;

/**
 * Behaviors are kind of plug-ins for Components. They allow functionality to be added to a
 * component and get essential events forwarded by the component. They can be bound to a concrete
 * component (using the bind method which is called when the behavior is attached), but they don't
 * need to. They can modify the components markup by changing the rendered ComponentTag. Behaviors
 * can have their own models as well, and they are notified when these are to be detached by the
 * component.
 * <p>
 * You also cannot modify a components model with a behavior.
 * </p>
 * 
 * @see org.apache.wicket.behavior.IBehaviorListener
 * @see org.apache.wicket.markup.html.IHeaderContributor
 * @see org.apache.wicket.behavior.AbstractAjaxBehavior
 * @see org.apache.wicket.AttributeModifier
 * 
 * @author Ralf Ebert
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class Behavior
	implements
		IClusterable,
		IComponentAwareEventSink,
		IComponentAwareHeaderContributor
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public Behavior()
	{
		if (Application.exists())
		{
			Application.get().getBehaviorInstantiationListeners().onInstantiation(this);
		}
	}

	/**
	 * Called when a component is about to render.
	 * 
	 * @param component
	 *            the component that has this behavior coupled
	 */
	public void beforeRender(Component component)
	{
	}

	/**
	 * Called when a component that has this behavior coupled was rendered.
	 * 
	 * @param component
	 *            the component that has this behavior coupled
	 */
	public void afterRender(Component component)
	{
	}

	/**
	 * Bind this handler to the given component. This method is called by the host component
	 * immediately after this behavior is added to it. This method is useful if you need to do
	 * initialization based on the component it is attached and you can't wait to do it at render
	 * time. Keep in mind that if you decide to keep a reference to the host component, it is not
	 * thread safe anymore, and should thus only be used in situations where you do not reuse the
	 * behavior for multiple components.
	 * 
	 * @param component
	 *            the component to bind to
	 */
	public void bind(Component component)
	{
	}

	/**
	 * Notifies the behavior it is removed from the specified component
	 * 
	 * @param component
	 *            the component this behavior is unbound from
	 */
	public void unbind(Component component)
	{
	}

	/**
	 * Allows the behavior to detach any state it has attached during request processing.
	 * 
	 * @param component
	 *            the component that initiates the detachment of this behavior
	 */
	public void detach(Component component)
	{
	}

	/**
	 * In case an unexpected exception happened anywhere between
	 * {@linkplain #onComponentTag(org.apache.wicket.Component, org.apache.wicket.markup.ComponentTag)} and
	 * {@linkplain #afterRender(org.apache.wicket.Component)},
	 * onException() will be called for any behavior. Typically, if you clean up resources in
	 * {@link #afterRender(Component)}, you should do the same in the implementation of this method.
	 * 
	 * @param component
	 *            the component that has a reference to this behavior and during which processing
	 *            the exception occurred
	 * @param exception
	 *            the unexpected exception
	 */
	public void onException(Component component, RuntimeException exception)
	{
	}

	/**
	 * This method returns false if the behavior generates a callback url (for example ajax
	 * behaviors)
	 * 
	 * @param component
	 *            the component that has this behavior coupled.
	 * 
	 * @return boolean true or false.
	 */
	public boolean getStatelessHint(Component component)
	{
		if (this instanceof IBehaviorListener)
		{
			// this behavior implements a callback interface, so it cannot be stateless
			return false;
		}
		return true;
	}

	/**
	 * Called when a components is rendering and wants to render this behavior. If false is returned
	 * this behavior will be ignored.
	 * 
	 * @param component
	 *            the component that has this behavior coupled
	 * 
	 * @return true if this behavior must be executed/rendered
	 */
	public boolean isEnabled(Component component)
	{
		return true;
	}

	/**
	 * Called any time a component that has this behavior registered is rendering the component tag.
	 * 
	 * @param component
	 *            the component that renders this tag currently
	 * @param tag
	 *            the tag that is rendered
	 */
	public void onComponentTag(Component component, ComponentTag tag)
	{
	}

	/**
	 * Specifies whether or not this behavior is temporary. Temporary behaviors are removed at the
	 * end of request and never reattached. Such behaviors are useful for modifying component
	 * rendering only when it renders next. Usecases include javascript effects, initial clientside
	 * dom setup, etc.
	 * 
	 * @param component
	 * 
	 * @return true if this behavior is temporary
	 */
	public boolean isTemporary(Component component)
	{
		return false;
	}

	/**
	 * Checks whether or not a listener interface can be invoked on this behavior. For further
	 * information please read the javadoc on {@link Component#canCallListenerInterface(Method)},
	 * this method has the same semantics.
	 * 
	 * WARNING: Read the javadoc of {@link Component#canCallListenerInterface(Method)} for important
	 * security-related information.
	 * 
	 * @param component
	 *            component this behavior is attached to
	 * @param method
	 *            listener method being invoked
	 * @return {@literal true} iff the listener method can be invoked
	 */
	public boolean canCallListenerInterface(Component component, Method method)
	{
		return isEnabled(component) && component.canCallListenerInterface(method);
	}


	/**
	 * Render to the web response whatever the component wants to contribute to the head section.
	 * 
	 * @param component
	 * 
	 * @param response
	 *            Response object
	 */
	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
	}

	/**
	 * Called immediately after the onConfigure method in a component. Since this is before the
	 * rendering cycle has begun, the behavior can modify the configuration of the component (i.e.
	 * setVisible(false))
	 * 
	 * @param component
	 *            the component being configured
	 */
	public void onConfigure(Component component)
	{
	}

	/**
	 * Called to notify the behavior about any events sent to the component
	 * 
	 * @see org.apache.wicket.IComponentAwareEventSink#onEvent(org.apache.wicket.Component,
	 *      org.apache.wicket.event.IEvent)
	 */
	@Override
	public void onEvent(Component component, IEvent<?> event)
	{
	}

	/**
	 * Called to notify that the component is being removed from its parent
	 * @param component
	 *      the removed component
	 */
	public void onRemove(Component component)
	{
	}
}