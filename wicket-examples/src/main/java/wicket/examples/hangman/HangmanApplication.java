/*
 * $Id: HangmanApplication.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed, 24
 * May 2006) $
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

import wicket.ISessionFactory;
import wicket.Page;
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
	 * @see wicket.protocol.http.WebApplication#getSessionFactory()
	 */
	@Override
	public ISessionFactory getSessionFactory()
	{
		return new ISessionFactory()
		{
			public Session newSession(final Request request)
			{
				return new HangmanSession(HangmanApplication.this);
			}
		};
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	@Override
	public Class< ? extends Page> getHomePage()
	{
		return Home.class;
	}
}
