/*
 * $Id: ListenerInterfaceRequestTarget.java,v 1.1 2005/11/27 23:22:45 eelco12
 * Exp $ $Revision$ $Date$
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
package wicket.request;

import java.lang.reflect.Method;

import wicket.Application;
import wicket.ApplicationSettings;
import wicket.Component;
import wicket.Page;
import wicket.RequestCycle;

/**
 * Default implementation of
 * {@link wicket.request.IListenerInterfaceRequestTarget}. Target that denotes
 * a page instance and a call to a component on that page using an listener
 * interface method.
 * 
 * @author Eelco Hillenius
 */
public class ListenerInterfaceRequestTarget extends AbstractListenerInterfaceRequestTarget
{
	/**
	 * Construct.
	 * 
	 * @param page
	 *            the page instance
	 * @param component
	 *            the target component
	 * @param listenerMethod
	 *            the listener method
	 */
	public ListenerInterfaceRequestTarget(Page page, Component component, Method listenerMethod)
	{
		this(page, component, listenerMethod, null);
	}


	/**
	 * Construct.
	 * 
	 * @param page
	 *            the page instance
	 * @param component
	 *            the target component
	 * @param listenerMethod
	 *            the listener method
	 * @param behaviourId
	 *            optionally the id of the behaviour to dispatch to
	 */
	public ListenerInterfaceRequestTarget(Page page, Component component, Method listenerMethod,
			String behaviourId)
	{
		super(page,component,listenerMethod,behaviourId);
	}
	
	/**
	 * @see wicket.request.IListenerInterfaceRequestTarget#processEvents(wicket.RequestCycle)
	 */
	public final void processEvents(final RequestCycle requestCycle)
	{
		// Assume cluster needs to be updated now, unless listener
		// invocation changes this
		requestCycle.setUpdateCluster(true);

		// Clear all feedback messages if it isn't a redirect
		getPage().getFeedbackMessages().clear();

		final Application application = requestCycle.getApplication();
		// and see if we have to redirect the render part by default
		ApplicationSettings.RenderStrategy strategy = application.getSettings().getRenderStrategy();
		boolean issueRedirect = (strategy == ApplicationSettings.REDIRECT_TO_RENDER || strategy == ApplicationSettings.REDIRECT_TO_BUFFER);

		requestCycle.setRedirect(issueRedirect);
		invokeInterface(getComponent(), getListenerMethod(), getPage());
	}
}