/*
 * $Id: LibraryApplication.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May
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
package wicket.examples.library;

import wicket.ISessionFactory;
import wicket.Page;
import wicket.Request;
import wicket.Session;
import wicket.authorization.IAuthorizationStrategy;
import wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import wicket.examples.WicketExampleApplication;
import wicket.settings.IRequestCycleSettings.RenderStrategy;

/**
 * WicketServlet class for example.
 * 
 * @author Jonathan Locke
 */
public final class LibraryApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public LibraryApplication()
	{
	}

	/**
	 * @see wicket.examples.WicketExampleApplication#init()
	 */
	@Override
	protected void init()
	{
		getExceptionSettings().setThrowExceptionOnMissingResource(false);
		getRequestCycleSettings().setRenderStrategy(RenderStrategy.REDIRECT_TO_RENDER);

		// Install a simple page authorization strategy, that checks all pages
		// of type AuthenticatedWebPage.
		IAuthorizationStrategy authorizationStrategy = new SimplePageAuthorizationStrategy(
				AuthenticatedWebPage.class, SignIn.class)
		{
			@Override
			protected boolean isAuthorized()
			{
				// check whether the user is logged on
				return (((LibrarySession)Session.get()).isSignedIn());
			}
		};
		getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);
	}


	/**
	 * @see wicket.protocol.http.WebApplication#getSessionFactory()
	 */
	@Override
	public ISessionFactory getSessionFactory()
	{
		return new ISessionFactory()
		{
			public Session newSession()
			{
				return new LibrarySession(LibraryApplication.this);
			}

			public Session newSession(Request request)
			{
				return new LibrarySession(LibraryApplication.this);
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
