/*
 * $Id$
 * $Revision$
 * $Date$
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
			Application.get().logResponseTarget(requestTarget);
			requestTarget.respond(requestCycle);
		}
	}
}
