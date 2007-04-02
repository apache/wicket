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
package wicket.request.target.component.listener;

import wicket.Application;
import wicket.Component;
import wicket.Page;
import wicket.RequestCycle;
import wicket.RequestListenerInterface;
import wicket.Session;
import wicket.request.RequestParameters;
import wicket.request.target.IEventProcessor;
import wicket.request.target.component.PageRequestTarget;
import wicket.settings.IRequestCycleSettings;

/**
 * The abstract implementation of
 * {@link wicket.request.target.component.listener.IListenerInterfaceRequestTarget}.
 * Target that denotes a page instance and a call to a component on that page
 * using an listener interface method.
 * 
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
public abstract class AbstractListenerInterfaceRequestTarget extends PageRequestTarget
		implements
			IListenerInterfaceRequestTarget,
			IEventProcessor
{
	/** The request parameters. */
	private final RequestParameters requestParameters;

	/** the target component. */
	private final Component component;

	/** the listener method. */
	private final RequestListenerInterface listener;

	/**
	 * Construct.
	 * 
	 * @param page
	 *            the page instance
	 * @param component
	 *            the target component
	 * @param listener
	 *            the listener interface
	 */
	public AbstractListenerInterfaceRequestTarget(final Page page, final Component component,
			RequestListenerInterface listener)
	{
		this(page, component, listener, null);
	}


	/**
	 * Construct.
	 * 
	 * @param page
	 *            the page instance
	 * @param component
	 *            the target component
	 * @param listener
	 *            the listener method
	 * @param requestParameters
	 *            the request parameter
	 */
	public AbstractListenerInterfaceRequestTarget(final Page page, final Component component,
			final RequestListenerInterface listener, final RequestParameters requestParameters)
	{
		super(page);

		if (component == null)
		{
			throw new IllegalArgumentException("Argument component must be not null");
		}

		this.component = component;

		if (listener == null)
		{
			throw new IllegalArgumentException("Argument listenerMethod must be not null");
		}

		this.listener = listener;
		this.requestParameters = requestParameters;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		boolean equal = false;
		if (obj != null && obj.getClass().equals(getClass()))
		{
			AbstractListenerInterfaceRequestTarget that = (AbstractListenerInterfaceRequestTarget)obj;
			if (component.equals(that.component) && listener.equals(that.listener))
			{
				if (requestParameters != null)
				{
					return requestParameters.equals(that.requestParameters);
				}
				else
				{
					return that.requestParameters == null;
				}
			}
		}
		return equal;
	}

	/**
	 * @see wicket.request.target.component.listener.IListenerInterfaceRequestTarget#getRequestListenerInterface()
	 */
	public final RequestListenerInterface getRequestListenerInterface()
	{
		return listener;
	}

	/**
	 * @see wicket.request.target.component.listener.IListenerInterfaceRequestTarget#getRequestParameters()
	 */
	public final RequestParameters getRequestParameters()
	{
		return this.requestParameters;
	}

	/**
	 * @see wicket.request.target.component.listener.IListenerInterfaceRequestTarget#getTarget()
	 */
	public final Component getTarget()
	{
		return component;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result = getClass().hashCode();
		result += component.hashCode();
		result += listener.hashCode();
		result += requestParameters != null ? requestParameters.hashCode() : 0;
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer(getClass().getName()).append("@").append(hashCode())
				.append(getPage().toString()).append("->").append(getTarget().getId()).append("->")
				.append(getRequestListenerInterface().getMethod().getDeclaringClass()).append(".")
				.append(getRequestListenerInterface().getName());

		if (requestParameters != null)
		{
			buf.append(" (request paramaters: ").append(requestParameters.toString()).append(")");
		}
		return buf.toString();
	}

	/**
	 * Common functionality to be called by processEvents()
	 * 
	 * @param requestCycle
	 *            The request cycle
	 */
	protected void onProcessEvents(final RequestCycle requestCycle)
	{
		// Assume cluster needs to be updated now, unless listener
		// invocation changes this
		requestCycle.setUpdateSession(true);

		// Clear all feedback messages if it isn't a redirect
		Session.get().cleanupFeedbackMessages(getPage());

		getPage().startComponentRender(getTarget());

		final Application application = requestCycle.getApplication();
		// and see if we have to redirect the render part by default
		IRequestCycleSettings.RenderStrategy strategy = application.getRequestCycleSettings()
				.getRenderStrategy();
		boolean issueRedirect = (strategy == IRequestCycleSettings.REDIRECT_TO_RENDER || strategy == IRequestCycleSettings.REDIRECT_TO_BUFFER);

		requestCycle.setRedirect(issueRedirect);
	}
}