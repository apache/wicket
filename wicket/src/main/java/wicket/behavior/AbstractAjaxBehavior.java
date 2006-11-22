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
import wicket.RequestListenerInterface;
import wicket.markup.ComponentTag;
import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.IHeaderResponse;
import wicket.protocol.http.request.WebRequestCodingStrategy;
import wicket.util.string.AppendingStringBuffer;

/**
 * Abstract class for handling Ajax roundtrips. This class serves as a base for
 * javascript specific implementations, like ones based on Dojo or
 * Scriptaculous, or Wicket's default.
 * 
 * @author Eelco Hillenius
 * @author Ralf Ebert
 * @author Igor Vaynberg
 */
public abstract class AbstractAjaxBehavior extends AbstractBehavior
		implements
			IBehaviorListener,
			IHeaderContributor
{
	/** the component that this handler is bound to. */
	private Component component;

	/**
	 * Construct.
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
	public final void bind(final Component hostComponent)
	{
		if (hostComponent == null)
		{
			throw new IllegalArgumentException("Argument hostComponent must be not null");
		}

		if (this.component != null)
		{
			throw new IllegalStateException("this kind of handler cannot be attached to "
					+ "multiple components; it is already attached to component " + this.component
					+ ", but component " + hostComponent + " wants to be attached too");

		}

		this.component = hostComponent;

		// call the calback
		onBind();
	}

	/**
	 * Gets the url that references this handler.
	 * 
	 * @return the url that references this handler
	 */
	public CharSequence getCallbackUrl()
	{
		return getCallbackUrl(false, true);
	}

	/**
	 * Gets the url that references this handler.
	 * 
	 * @param recordPageVersion
	 *            if true the url will be encoded to execute on the current page
	 *            version, otherwise url will be encoded to execute on the
	 *            latest page version
	 * @param onlyTargetActivePage
	 *            if true the callback to this behavior will be ignore if the
	 *            page is not the last one the user accessed
	 * 
	 * @return the url that references this handler
	 */
	public final CharSequence getCallbackUrl(final boolean recordPageVersion,
			final boolean onlyTargetActivePage)
	{
		if (getComponent() == null)
		{
			throw new IllegalArgumentException(
					"Behavior must be bound to a component to create the URL");
		}

		int index = getComponent().getBehaviors().indexOf(this);
		if (index == -1)
		{
			throw new IllegalArgumentException("Behavior " + this
					+ " was not registered with this component: " + getComponent().toString());
		}

		final RequestListenerInterface rli;
		if (recordPageVersion)
		{
			rli = IBehaviorListener.INTERFACE;
		}
		else
		{
			rli = IUnversionedBehaviorListener.INTERFACE;
		}

		// TODO Post 1.2: URL encoding strategies are not applied
		// And you can not simply call getResponse().encodeUrl() as the URL
		// might
		// already be encoded.
		AppendingStringBuffer url = new AppendingStringBuffer(getComponent().urlFor(rli)).append(
				'&').append(WebRequestCodingStrategy.BEHAVIOR_ID_PARAMETER_NAME).append('=')
				.append(index);

		if (onlyTargetActivePage)
		{
			url.append("&").append(WebRequestCodingStrategy.IGNORE_IF_NOT_ACTIVE_PARAMETER_NAME)
					.append("=true");
		}

		return url;
	}

	/**
	 * @see wicket.behavior.IBehavior#onComponentTag(wicket.Component,
	 *      wicket.markup.ComponentTag)
	 */
	public final void onComponentTag(final Component component, final ComponentTag tag)
	{
		onComponentTag(tag);
	}

	/**
	 * @see wicket.behavior.AbstractBehavior#onRendered(wicket.Component)
	 */
	public final void onRendered(final Component hostComponent)
	{
		onComponentRendered();
	}

	/**
	 * @see wicket.markup.html.IHeaderContributor#renderHead(IHeaderResponse)
	 */
	public void renderHead(final IHeaderResponse response)
	{
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
	 * Called any time a component that has this handler registered is rendering
	 * the component tag. Use this method e.g. to bind to javascript event
	 * handlers of the tag
	 * 
	 * @param tag
	 *            the tag that is rendered
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
	}

	/**
	 * Called when the component was bound to it's host component. You can get
	 * the bound host component by calling getComponent.
	 */
	protected void onBind()
	{
	}

	/**
	 * Called to indicate that the component that has this handler registered
	 * has been rendered. Use this method to do any cleaning up of temporary
	 * state
	 */
	protected void onComponentRendered()
	{
	}

	/**
	 * @see wicket.behavior.AbstractBehavior#getStatelessHint()
	 */
	public boolean getStatelessHint()
	{
		return false;
	}
}
