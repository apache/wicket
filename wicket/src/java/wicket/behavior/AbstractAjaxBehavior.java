/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.behavior;

import java.util.HashSet;
import java.util.Set;

import wicket.Component;
import wicket.RequestCycle;
import wicket.Response;
import wicket.markup.ComponentTag;
import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.internal.HtmlHeaderContainer;

/**
 * Abstract class for handling Ajax roundtrips. This class serves as a base for
 * javascript specific implementations, like ones based on Dojo or
 * Scriptaculous, or Wicket's default.
 * 
 * @author Eelco Hillenius
 * @author Ralf Ebert
 * @author Igor Vaynberg
 */
public abstract class AbstractAjaxBehavior
		implements
			IBehavior,
			IBehaviorListener,
			IHeaderContributor
{
	/** thread local for head contributions. */
	private static final ThreadLocal headContribHolder = new ThreadLocal();

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
	 * @see wicket.behavior.IBehavior#detachModel()
	 */
	public void detachModel()
	{
	}

	/**
	 * Gets the url that references this handler.
	 * 
	 * @return the url that references this handler
	 */
	public final String getCallbackUrl()
	{
		if (getComponent() == null)
		{
			throw new IllegalArgumentException(
					"Behavior must be bound to a component to create the URL");
		}

		if (!(this instanceof IBehaviorListener))
		{
			throw new IllegalArgumentException(
					"The behavior must implement IBehaviorListener to accept requests");
		}

		int index = getComponent().getBehaviors().indexOf(this);
		if (index == -1)
		{
			throw new IllegalArgumentException("Behavior " + this
					+ " was not registered with this component: " + getComponent().toString());
		}

		return getComponent().urlFor(IBehaviorListener.INTERFACE) + "&behaviorId=" + index;
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
	 * @see wicket.behavior.IBehavior#rendered(wicket.Component)
	 */
	public final void rendered(final Component hostComponent)
	{
		headContribHolder.set(null);
		onComponentRendered();
	}

	/**
	 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.Response)
	 */
	public final void renderHead(final Response response)
	{
		Set contributors = (Set)headContribHolder.get();

		// were any contributors set?
		if (contributors == null)
		{
			contributors = new HashSet(1);
			headContribHolder.set(contributors);
		}

		// get the id of the implementation; we need this trick to be
		// able to support multiple implementations
		String implementationId = getImplementationId();

		// was a contribution for this specific implementation done yet?
		if (!contributors.contains(implementationId))
		{
			onRenderHeadInitContribution(response);
			contributors.add(implementationId);
		}

		onRenderHeadContribution(response);
	}

	/**
	 * Convenience method to add a javascript reference.
	 * 
	 * @param response
	 * 
	 * @param ref
	 *            reference to add
	 */
	protected void writeJsReference(final Response response, final PackageResourceReference ref)
	{
		String url = RequestCycle.get().urlFor(ref);
		response.write("\t<script language=\"JavaScript\" type=\"text/javascript\" " + "src=\"");
		response.write(url);
		response.write("\"></script>\n");
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
	 * Gets the unique id of an ajax implementation. This should be implemented
	 * by base classes only - like the dojo or scriptaculous implementation - to
	 * provide a means to differentiate between implementations while not going
	 * to the level of concrete implementations. It is used to ensure 'static'
	 * header contributions are done only once per implementation.
	 * 
	 * @return unique id of an ajax implementation
	 */
	protected abstract String getImplementationId();

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
	 * Let this handler print out the needed header contributions. This
	 * implementation does nothing.
	 * 
	 * @param response
	 *            head container
	 */
	protected void onRenderHeadContribution(final Response response)
	{
	}

	/**
	 * Do a one time (per page) header contribution that is the same for all
	 * ajax variant implementations (e.g. Dojo, Scriptaculous). This
	 * implementation does nothing.
	 * 
	 * @param response
	 *            head container
	 */
	protected void onRenderHeadInitContribution(final Response response)
	{
	}

	/**
	 * Writes the given string to the header container.
	 * 
	 * @param container
	 *            the header container
	 * @param str
	 *            the string to write
	 */
	private final void write(final HtmlHeaderContainer container, final String str)
	{
		container.getResponse().write(str);
	}
}
