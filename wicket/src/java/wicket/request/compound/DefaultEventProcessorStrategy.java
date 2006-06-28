/*
 * $Id: DefaultEventProcessorStrategy.java 4773 2006-03-06 01:08:33 +0000 (Mon,
 * 06 Mar 2006) joco01 $ $Revision$ $Date: 2006-03-06 01:08:33 +0000
 * (Mon, 06 Mar 2006) $
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.request.target.IEventProcessor;

/**
 * The default event processing strategy looks whether the current target is of
 * type {@link wicket.request.target.IEventProcessor} and, if so, calls method
 * {@link wicket.request.target.IEventProcessor#processEvents(RequestCycle)} on
 * them.
 * 
 * @author Eelco Hillenius
 */
public final class DefaultEventProcessorStrategy implements IEventProcessorStrategy
{
	/** log. */
	private static final Log log = LogFactory.getLog(DefaultEventProcessorStrategy.class);

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

		if (target instanceof IEventProcessor)
		{
			if (log.isDebugEnabled())
			{
				log.debug("commencing event handling for " + target);
			}

			Application.get().logEventTarget(target);

			((IEventProcessor)target).processEvents(requestCycle);
		}
	}
}
