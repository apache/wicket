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
package wicket.examples.library;

import wicket.Component;
import wicket.ISessionFactory;
import wicket.Page;
import wicket.RestartResponseAtInterceptPageException;
import wicket.Session;
import wicket.authorization.IUnauthorizedComponentInstantiationListener;
import wicket.authorization.UnauthorizedInstantiationException;
import wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import wicket.examples.WicketExampleApplication;
import wicket.settings.IRequestCycleSettings;

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
	protected void init()
	{
		getExceptionSettings().setThrowExceptionOnMissingResource(false);
		getRequestCycleSettings().setRenderStrategy(IRequestCycleSettings.REDIRECT_TO_RENDER);

		// Handle unauthorized access to pages
		getSecuritySettings().setUnauthorizedComponentInstantiationListener(new IUnauthorizedComponentInstantiationListener()
		{
			public void onUnauthorizedInstantiation(final Component component)
			{
				// If there is a sign in page class declared, and the unauthorized
				// component is a page, but it's not the sign in page
				if (component instanceof Page)
				{
					// Redirect to intercept page to let the user sign in
					throw new RestartResponseAtInterceptPageException(SignIn.class);
				}
				else
				{
					// The component was not a page, so throw an exception
					throw new UnauthorizedInstantiationException(component.getClass());
				}
			}
		});

		// Create a simple authorization strategy, that checks all pages of type
		// Authenticated web page.
		SimplePageAuthorizationStrategy authorizationStrategy = new SimplePageAuthorizationStrategy(
				AuthenticatedWebPage.class)
		{
			protected boolean isAuthorized()
			{
				// check whether the user is logged on
				return (((LibrarySession)Session.get()).isSignedIn());
			}
		};

		// set the strategy
		getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);
	}


	/**
	 * @see wicket.protocol.http.WebApplication#getSessionFactory()
	 */
	public ISessionFactory getSessionFactory()
	{
		return new ISessionFactory()
		{
			public Session newSession()
			{
				return new LibrarySession(LibraryApplication.this);
			}
		};
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return Home.class;
	}
}
