/*
 * $Id: IEventProcessorStrategy.java 4520 2006-02-17 07:33:23 +0000 (Fri, 17 Feb
 * 2006) eelco12 $ $Revision$ $Date: 2006-02-17 07:33:23 +0000 (Fri, 17
 * Feb 2006) $
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

import wicket.RequestCycle;

/**
 * Strategy interface for implementing
 * {@link wicket.request.compound.AbstractCompoundRequestCycleProcessor#processEvents(RequestCycle)}.
 * 
 * @author Eelco Hillenius
 */
public interface IEventProcessorStrategy
{
	/**
	 * After a request target is resolved, this method is responsible for
	 * calling any event handling code based on that target.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 */
	void processEvents(RequestCycle requestCycle);
}
