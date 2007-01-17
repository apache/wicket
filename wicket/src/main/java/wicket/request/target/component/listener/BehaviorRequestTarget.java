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
package wicket.request.target.component.listener;

import wicket.Component;
import wicket.Page;
import wicket.RequestCycle;
import wicket.RequestListenerInterface;
import wicket.behavior.IBehaviorListener;
import wicket.request.RequestParameters;

/**
 * Target that denotes a page instance and a call to a component on that page
 * using an listener interface method.
 * 
 * @author Eelco Hillenius
 */
public class BehaviorRequestTarget extends AbstractListenerInterfaceRequestTarget
{
	/**
	 * Construct.
	 * 
	 * @param page
	 *            the page instance
	 * @param component
	 *            the target component
	 * @param listener
	 *            the listener method
	 */
	public BehaviorRequestTarget(final Page page, final Component component,
			final RequestListenerInterface listener)
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
	 *            the request parameters
	 */
	public BehaviorRequestTarget(final Page page, final Component component,
			final RequestListenerInterface listener, final RequestParameters requestParameters)
	{
		super(page, component, listener, requestParameters);
	}

	/**
	 * @see wicket.request.target.IEventProcessor#processEvents(wicket.RequestCycle)
	 */
	public final void processEvents(final RequestCycle requestCycle)
	{
		// Preprocess like standard component request. Do all the initialization
		// necessary
		onProcessEvents(requestCycle);

		// Get the IBehavior for the component based on the request parameters
		final Component component = getTarget();
		final String id = getRequestParameters().getBehaviorId();
		if (id == null)
		{
			throw new IllegalStateException(
					"Parameter behaviorId was not provided: unable to locate listener. Component: "
							+ component.toString());
		}

		final int idAsInt = Integer.parseInt(id);
		final IBehaviorListener behaviorListener = (IBehaviorListener)component.getBehaviors().get(
				idAsInt);
		if (behaviorListener == null)
		{
			throw new IllegalStateException("No behavior listener found with behaviorId " + id
					+ "; Component: " + component.toString());
		}

		// Invoke the interface method
		component.getPage().beforeCallComponent(component, IBehaviorListener.INTERFACE);
		try
		{
			behaviorListener.onRequest();
		}
		finally
		{
			component.getPage().afterCallComponent(component, IBehaviorListener.INTERFACE);
		}
	}
}