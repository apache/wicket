/*
 * $Id$ $Revision$ $Date$
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
package wicket;

/**
 * <p>
 * A request target is the base entity that can be the subject of a request.
 * </p>
 * <p>
 * A request target is different from {@link wicket.IRequestListener} in that it
 * is not nescesarily the target of events. E.g. when you have a link on a page,
 * and that link is clicked, the request target will be the page (or a wrapper
 * for it) that link is on, and the {@link wicket.markup.html.link.Link}'s
 * event listener interface {@link wicket.markup.html.link.ILinkListener} link
 * will be called as part of the event processing.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public interface IRequestTarget
{
	/**
	 * Generate a response.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 */
	void respond(RequestCycle requestCycle);

	/**
	 * Gets the lock for synchronizing the request cycle processing other than
	 * resolving this target. If this method returns null, no synchronization
	 * will be used. Typically, if synchonization is wanted, this method should
	 * return an instance of the current session. The latter would be the case
	 * for e.g. a page request target. Non-synchornization is desirable for
	 * instance with static resources or external resources.
	 * 
	 * @return the lock to use for synchronizing the event handling and
	 *         rendering states of the request handling, or null if
	 *         synchronization should not be done.
	 */
	Object getSynchronizationLock();

	/**
	 * This method is alled on the end of a request cycle to indicate that
	 * processing is done and that cleaning up of the subject(s) of this target
	 * may be done.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 */
	void cleanUp(RequestCycle requestCycle);
}
