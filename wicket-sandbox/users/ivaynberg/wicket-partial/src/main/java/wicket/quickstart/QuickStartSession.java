/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.quickstart;

import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebSession;

/**
 * Subclass of WebSession for QuickStartApplication to allow easy and typesafe
 * access to session properties.
 * 
 * @author Jonathan Locke
 */
public final class QuickStartSession extends WebSession
{
	//  TODO Add any session properties here

	/**
	 * Constructor
	 * 
	 * @param application
	 *            The application
	 */
	protected QuickStartSession(final WebApplication application)
	{
		super(application);
	}
}
