/*
 * $Id: IListenerInterfaceRequestTarget.java,v 1.2 2005/11/30 21:43:01 joco01
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
package wicket.request.target;

import wicket.RequestCycle;

/**
 * Targets that implement this interface announce that they can process events.
 * This interface is not meant to be used on its own, but rather to be mixed in
 * with other interfaces, like
 * {@link wicket.request.target.component.listener.IListenerInterfaceRequestTarget}.
 * 
 * @author Eelco Hillenius
 */
public interface IEventProcessor
{
	/**
	 * After a page is restored, this method is responsible for calling any
	 * event handling code based on the request.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 */
	void processEvents(final RequestCycle requestCycle);
}