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
package wicket.authorization.roles.example;

import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebSession;

/**
 * Web Session for this example.
 * 
 * @author Eelco Hillenius
 */
public class RolesAuthSession extends WebSession
{
	/** the current user. */
	private User user;

	/**
	 * Construct.
	 * 
	 * @param application
	 */
	public RolesAuthSession(WebApplication application)
	{
		super(application);
	}

	/**
	 * Gets user.
	 * 
	 * @return user
	 */
	public User getUser()
	{
		return user;
	}

	/**
	 * Sets user.
	 * 
	 * @param user
	 *            user
	 */
	public void setUser(User user)
	{
		this.user = user;
	}

}
