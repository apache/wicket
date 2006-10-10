/*
 * $Id: DefaultResponseStrategy.java 4773 2006-03-06 01:08:33 +0000 (Mon, 06 Mar
 * 2006) joco01 $ $Revision$ $Date: 2006-03-06 01:08:33 +0000 (Mon, 06
 * Mar 2006) $
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

import wicket.Application;
import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.protocol.http.IRequestLogger;

/**
 * Default implementation of response strategy that just calls
 * {@link wicket.IRequestTarget#respond(RequestCycle)}.
 * 
 * @author Eelco Hillenius
 */
public final class DefaultResponseStrategy implements IResponseStrategy
{
	/**
	 * Construct.
	 */
	public DefaultResponseStrategy()
	{
	}

	/**
	 * @see wicket.request.compound.IResponseStrategy#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		IRequestTarget requestTarget = requestCycle.getRequestTarget();
		if (requestTarget != null)
		{
			IRequestLogger logger = Application.get().getRequestLogger();
			if(logger != null)
			{
				logger.logResponseTarget(requestTarget);
			}
			
			requestTarget.respond(requestCycle);
		}
	}
}
