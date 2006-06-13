/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
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
package wicket.examples.stateless;

import wicket.Application;
import wicket.RequestCycle;
import wicket.model.AbstractReadOnlyModel;

/**
 * Model that displays whether a session was created yet, and if it was, prints
 * the session id.
 * 
 * @author Eelco Hillenius
 */
public class SessionModel extends AbstractReadOnlyModel<String>
{
	/**
	 * @see wicket.model.AbstractReadOnlyModel#getObject()
	 */
	public String getObject()
	{
		final String msg;
		String sessionId = Application.get().getSessionStore().getSessionId(
				RequestCycle.get().getRequest(), false);
		if (sessionId == null)
		{
			msg = "no concrete session is created yet (only a volatile one)";
		}
		else
		{
			msg = "a session exists for this client, with session id " + sessionId;
		}
		return msg;
	}

}
