/*
 * $Id: SignInApplication.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-12-31 18:58:21 +0700 (Sun, 31 Dec
 * 2006) $
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
package wicket.examples.signin;

import wicket.Component;
import wicket.Page;
import wicket.Request;
import wicket.RestartResponseAtInterceptPageException;
import wicket.Session;
import wicket.authorization.Action;
import wicket.authorization.IAuthorizationStrategy;
import wicket.examples.WicketExampleApplication;

/**
 * Forms example.
 * 
 * @author Jonathan Locke
 */
public final class SignInApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public SignInApplication()
	{
	}

	/**
	 * @see wicket.examples.WicketExampleApplication#init()
	 */
	@Override
	protected void init()
	{
		getSecuritySettings().setAuthorizationStrategy(new IAuthorizationStrategy()
		{
			public boolean isActionAuthorized(final Component component, final Action action)
			{
				return true;
			}

			public boolean isInstantiationAuthorized(final Class componentClass)
			{
				if (AuthenticatedWebPage.class.isAssignableFrom(componentClass))
				{
					// Is user signed in?
					if (((SignInSession)Session.get()).isSignedIn())
					{
						// okay to proceed
						return true;
					}

					// Force sign in
					throw new RestartResponseAtInterceptPageException(SignIn.class);
				}
				return true;
			}
		});
	}

	/**
	 * @see wicket.protocol.http.WebApplication#getSessionFactory()
	 */
	@Override
	public Session newSession(final Request request)
	{
		return new SignInSession(SignInApplication.this, request);
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
