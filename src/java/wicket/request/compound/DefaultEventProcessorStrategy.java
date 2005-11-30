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
package wicket.request.compound;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.request.IListenerInterfaceRequestTarget;

/**
 * TODO docme
 * 
 * @author Eelco Hillenius
 */
public final class DefaultEventProcessorStrategy implements IEventProcessorStrategy
{
	/**
	 * Construct.
	 */
	public DefaultEventProcessorStrategy()
	{
	}

	/**
	 * @see wicket.request.compound.IEventProcessorStrategy#processEvents(wicket.RequestCycle)
	 */
	public final void processEvents(final RequestCycle requestCycle)
	{
		IRequestTarget target = requestCycle.getRequestTarget();

		if (target instanceof IListenerInterfaceRequestTarget)
		{
			((IListenerInterfaceRequestTarget)target).processEvents(requestCycle);
		}
	}
}
