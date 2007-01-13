/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.hangman;

import wicket.Request;
import wicket.Session;
import wicket.examples.WicketExampleApplication;

/**
 * Class defining the main Game application.
 * 
 * @author Chris Turner
 * @author Jonathan Locke
 */
public class HangmanApplication extends WicketExampleApplication
{
	/**
	 * Create the hangman application.
	 */
	public HangmanApplication()
	{
	}

	/**
	 * @see wicket.protocol.http.WebApplication#newSession(wicket.Request)
	 */
	public Session newSession(Request request)
	{
		return new HangmanSession(HangmanApplication.this, request);
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return Home.class;
	}
}
