package wicket.authentication.example;

import wicket.authentication.AuthenticatedWebApplication;
import wicket.authentication.AuthenticatedWebSession;
import wicket.authentication.pages.SignInPage;
import wicket.markup.html.WebPage;

/**
 * A role-authorized, authenticated web application in just a few lines of code.
 * 
 * @author Jonathan Locke
 */
public class MyAuthenticatedWebApplication extends AuthenticatedWebApplication
{
	@Override
	protected Class< ? extends AuthenticatedWebSession> getWebSessionClass()
	{
		return MyAuthenticatedWebSession.class;
	}

	@Override
	protected Class< ? extends WebPage> getSignInPageClass()
	{
		return SignInPage.class;
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	@Override
	public Class getHomePage()
	{
		return HomePage.class;
	}
}
