/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.examples.signin2;

import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.examples.panels.signin.SignInPanel;

/**
 * Simple example of a sign in page. It extends SignInPage, a base class which
 * provide standard functionality for typical log-in pages
 * 
 * @author Jonathan Locke
 */
public final class SignIn2 extends WicketExamplePage
{
	/**
	 * Construct
	 */
	public SignIn2()
	{
		this(null);
	}

	/**
	 * Constructor
	 * 
	 * @param parameters
	 *            The page parameters
	 */
	public SignIn2(final PageParameters parameters)
	{
		add(new SignInPanel("signInPanel")
		{
			public boolean signIn(String username, String password)
			{
				return ((SignIn2Session)getSession()).authenticate(username, password);
			}
		});
	}
}
