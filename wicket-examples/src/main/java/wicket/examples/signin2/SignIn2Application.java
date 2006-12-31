/*
 * $Id: SignIn2Application.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May
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
package wicket.examples.signin2;

import wicket.Component;
import wicket.ISessionFactory;
import wicket.Page;
import wicket.Request;
import wicket.RestartResponseAtInterceptPageException;
import wicket.Session;
import wicket.authorization.Action;
import wicket.authorization.IAuthorizationStrategy;
import wicket.examples.WicketExampleApplication;
import wicket.protocol.http.request.CryptedUrlWebRequestCodingStrategy;
import wicket.protocol.http.request.WebRequestCodingStrategy;
import wicket.request.IRequestCycleProcessor;
import wicket.request.compound.CompoundRequestCycleProcessor;

/**
 * Forms example.
 * 
 * @author Jonathan Locke
 */
public final class SignIn2Application extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public SignIn2Application()
	{
	}

	/**
	 * @see wicket.examples.WicketExampleApplication#init()
	 */
	@Override
	protected void init()
	{
		super.init();

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
					if (((SignIn2Session)Session.get()).isSignedIn())
					{
						// okay to proceed
						return true;
					}

					// Force sign in
					throw new RestartResponseAtInterceptPageException(SignIn2.class);
				}
				return true;
			}
		});
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
				return new SignIn2Session(SignIn2Application.this);
			}
		};
	}

	/**
	 * @see wicket.protocol.http.WebApplication#newRequestCycleProcessor()
	 */
	@Override
	protected IRequestCycleProcessor newRequestCycleProcessor()
	{
		return new CompoundRequestCycleProcessor(new CryptedUrlWebRequestCodingStrategy(
				new WebRequestCodingStrategy()), null, null, null, null);
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
