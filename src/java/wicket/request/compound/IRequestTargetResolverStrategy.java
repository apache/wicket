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

import wicket.IRequestTarget;
import wicket.RequestCycle;

/**
 * Strategy interface for implementing
 * {@link wicket.request.compound.AbstractCompoundRequestCycleProcessor#resolve(RequestCycle)}.
 * 
 * @author Eelco Hillenius
 */
public interface IRequestTargetResolverStrategy
{
	/**
	 * <p>
	 * Resolves the request and returns the request target.
	 * </p>
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * @return the root component
	 * @see AbstractCompoundRequestCycleProcessor#resolve(RequestCycle)
	 */
	IRequestTarget resolve(RequestCycle requestCycle);
}
