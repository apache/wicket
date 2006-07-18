/*
 * $Id: IRequestCycleFactory.java 3585 2006-01-02 07:37:31 +0000 (Mon, 02 Jan
 * 2006) jonathanlocke $ $Revision$ $Date: 2006-01-02 07:37:31 +0000
 * (Mon, 02 Jan 2006) $
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

import java.io.Serializable;

/**
 * Factory for creating request cycles for a session.
 * 
 * @author Jonathan Locke
 * 
 * 
 * TODO 2.0: Deprecate this interface and add newRequstCycle overridable methods
 * to Application and Session directly. This interface is just bloat since we do
 * not push it into settings and you still have to override methods to use it. I
 * dont see the need for extra indirection. Consinder the same for
 * {@link ISessionFactory}.
 */
public interface IRequestCycleFactory extends Serializable
{
	/**
	 * Creates a new RequestCycle object.
	 * 
	 * @param session
	 *            The session
	 * @param request
	 *            The request
	 * @param response
	 *            The response
	 * @return The cycle
	 */
	RequestCycle newRequestCycle(final Session session, final Request request,
			final Response response);
}
