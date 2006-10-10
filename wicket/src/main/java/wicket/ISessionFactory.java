/*
 * $Id: ISessionFactory.java 5802 2006-05-22 09:46:00 +0000 (Mon, 22 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-22 09:46:00 +0000 (Mon, 22 May
 * 2006) $
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
 * A factory interface used by Applications to create Sessions.
 * 
 * @author Jonathan Locke
 */
public interface ISessionFactory
{

	/**
	 * Creates a new session
	 * 
	 * @param request
	 *            The request that will create this session.
	 * 
	 * @return The session
	 * 
	 * @since 2.0
	 */
	Session newSession(Request request);
}
