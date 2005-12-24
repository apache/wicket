/*
 * $Id$ $Revision:
 * 1.2 $ $Date$
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
 * A request target is the base entity that is the subject of a request.
 * </p>
 * <p>
 * It is very important (for mounting) that implementations implement
 * {@link java.lang.Object#equals(java.lang.Object)} and
 * {@link java.lang.Object#hashCode()} in a consistent way.
 * </p>
 * Typically, implementations of IRequestTarget are not meant to be used more
 * than once.
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
	 * This method is alled on the end of a request cycle to indicate that
	 * processing is done and that cleaning up of the subject(s) of this target
	 * may be done.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 */
	void cleanUp(RequestCycle requestCycle);

	/**
	 * Whether a request target needs to be synchronized on the session.  If 
	 * a {@link wicket.IRequestTarget} implements this interface, the event
	 * handling and response steps of the request cycle processing will be
	 * synchronized on the session object.
	 * 
	 * @param requestCycle
	 *            the current request cycle
	 * 
	 * @return whether this target should be synchronized on the user's session
	 */
	boolean synchronizeOnSession(RequestCycle requestCycle);
}
