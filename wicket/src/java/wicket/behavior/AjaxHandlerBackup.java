/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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
import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.Response;
import wicket.markup.ComponentTag;
import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.internal.HtmlHeaderContainer;
import wicket.request.target.EmptyRequestTarget;
import wicket.request.target.ResourceStreamRequestTarget;
import wicket.util.resource.IResourceStream;

/**
 * Abstract class for handling Ajax roundtrips. This class serves as a base for
 * javascript specific implementations, like ones based on Dojo or
 * Scriptaculous.
 * 
 * @author Eelco Hillenius
 * @author Ralf Ebert
 * @author Igor Vaynberg
 */
public abstract class AjaxHandlerBackup implements IBehavior, IBehaviorListener, IHeaderContributor
{
	/** thread local for onload contributions. */
	private static final ThreadLocal bodyOnloadContribHolder = new ThreadLocal();

	/** thread local for head contributions. */
	private static final ThreadLocal headContribHolder = new ThreadLocal();

	/** the component that this handler is bound to. */
	private Component component;

	/**
	 * Construct.
	 */
	public AjaxHandlerBackup()
	{
	}

	/**
	 * Bind this handler to the given component.
	 * 
	 * @param hostComponent
	 *            the component to bind to
	 */
	public final void bind(Component hostComponent)
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
	 * @see wicket.markup.html.IHeaderContributor#getBodyOnLoad()
	 */
	public final String getBodyOnLoad()
	{
		String staticContrib = null;
		Set contributors = (Set)bodyOnloadContribHolder.get();

		// were any contributors set?
		if (contributors == null)
		{
			contributors = new HashSet(1);
			bodyOnloadContribHolder.set(contributors);
		}

		// get the id of the implementation; we need this trick to be
		// able to support multiple implementations
		String implementationId = getImplementationId();

		// was a contribution for this specific implementation done yet?
		if (!contributors.contains(implementationId))
		{
			staticContrib = getBodyOnloadInitContribution();
			contributors.add(implementationId);
		}

		String contrib = getBodyOnloadContribution();
		if (staticContrib != null)
		{
			return (contrib != null) ? staticContrib + contrib : staticContrib;
		}
		return contrib;
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

		return getComponent().urlFor(IBehaviorListener.class) + "&behaviorId=" + index;
	}

	/**
	 * @see wicket.behavior.IBehavior#onComponentTag(wicket.Component,
	 *      wicket.markup.ComponentTag)
	 */
	public final void onComponentTag(Component component, ComponentTag tag)
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
	public void onComponentTag(ComponentTag tag)
	{
	}

	/**
	 * Called when an Ajax request is to be handled.
	 */
	public final void onRequest()
	{
		respond();
	}

	/**
	 * @see wicket.behavior.IBehavior#rendered(wicket.Component)
	 */
	public void rendered(Component hostComponent)
	{
		onComponentRendered();
	}

	/**
	 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.Response)
	 */
	public final void renderHead(Response response)
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
			renderHeadInitContribution(response);
			contributors.add(implementationId);
		}

		renderHeadContribution(response);
	}

	/**
	 * Convenience method to add a javascript reference.
	 * 
	 * @param container
	 *            the header container
	 * @param ref
	 *            reference to add
	 */
	protected void addJsReference(HtmlHeaderContainer container, PackageResourceReference ref)
	{
		String url = container.getPage().urlFor(ref.getPath());
		String s = "\t<script language=\"JavaScript\" type=\"text/javascript\" " + "src=\"" + url
				+ "\"></script>\n";
		write(container, s);
	}

	/**
	 * Gets the onload statement(s) for the body component. Override this method
	 * to provide custom contributions.
	 * 
	 * @return the onload statement(s) for the body component
	 */
	protected String getBodyOnloadContribution()
	{
		return null;
	}

	/**
	 * One time (per page) body onload contribution that is the same for all
	 * ajax variant implementations (e.g. Dojo, Rico, Qooxdoo).
	 * 
	 * @return the onload statement(s) for the body component
	 */
	protected String getBodyOnloadInitContribution()
	{
		return null;
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
	 * Gets the response to render to the requester. This is used by AjaxHandler
	 * default request target implementation,
	 * {@link wicket.request.target.ResourceStreamRequestTarget}. If you
	 * override {@link #respond()} and provide another kind of target, this
	 * method will not be used.
	 * 
	 * @return the response to render to the requester
	 */
	protected IResourceStream getResponse()
	{
		return null;
	}

	/**
	 * Gets the response type mime, e.g. 'text/html' or 'text/javascript'.
	 * 
	 * @return the response type mime
	 */
	protected String getResponseType()
	{
		return "text/html";
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
		bodyOnloadContribHolder.set(null);
		headContribHolder.set(null);
	}

	/**
	 * Let this handler print out the needed header contributions. This
	 * implementation does nothing.
	 * 
	 * @param response
	 *            head container
	 */
	protected void renderHeadContribution(Response response)
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
	protected void renderHeadInitContribution(Response response)
	{
	}

	/**
	 * Respond to this request. If you override this method, make sure you set a
	 * proper request target (call
	 * {@link RequestCycle#setRequestTarget(IRequestTarget)}. If
	 * {@link #getResponse()} returns a non-null resource stream, this
	 * implementation will set the target to
	 * {@link wicket.request.target.ResourceStreamRequestTarget}. If
	 * {@link #getResponse()} returns null, the target will be set to an
	 * instance of {@link EmptyRequestTarget}.
	 */
	protected void respond()
	{
		RequestCycle requestCycle = RequestCycle.get();
		IResourceStream resourceStream = getResponse();
		if (resourceStream != null)
		{
			requestCycle.setRequestTarget(new ResourceStreamRequestTarget(resourceStream,
					getResponseType()));
		}
		else
		{
			requestCycle.setRequestTarget(EmptyRequestTarget.getInstance());
		}
	}

	/**
	 * Writes the given string to the header container.
	 * 
	 * @param container
	 *            the header container
	 * @param s
	 *            the string to write
	 */
	private final void write(HtmlHeaderContainer container, String s)
	{
		container.getResponse().write(s);
	}
}
