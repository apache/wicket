package wicket.authentication.example;

import wicket.authentication.AuthenticatedWebApplication;
import wicket.authentication.AuthenticatedWebSession;
import wicket.authentication.SignInPage;
import wicket.authorization.strategies.role.Roles;
import wicket.authorization.strategies.role.example.pages.AdminBookmarkablePage;
import wicket.authorization.strategies.role.example.pages.AdminInternalPage;
import wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;

/**
 * A role-authorized, authenticated web application in just a few lines of code.
 * 
 * @author Jonathan Locke
 */
public class MyAuthenticatedWebApplication extends AuthenticatedWebApplication
{
	/**
	 * Authenticated session subclass
	 * 
	 * @author Jonathan Locke
	 */
	public static class MyAuthenticatedWebSession extends AuthenticatedWebSession
	{
		/** True when the user is signed in */
		private boolean signedIn;

		/**
		 * Construct.
		 * 
		 * @param application
		 *            The application
		 */
		public MyAuthenticatedWebSession(AuthenticatedWebApplication application)
		{
			super(application);
		}

		/**
		 * @see wicket.authentication.AuthenticatedWebSession#authenticate(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public boolean authenticate(String username, String password)
		{
			return signedIn = username.equals("jonathan") && password.equals("jonathan");
		}

		/**
		 * @see wicket.authentication.AuthenticatedWebSession#isSignedIn()
		 */
		@Override
		public boolean isSignedIn()
		{
			return signedIn;
		}

		/**
		 * @see wicket.authentication.AuthenticatedWebSession#getRoles()
		 */
		@Override
		public Roles getRoles()
		{
			if (signedIn)
			{
				return new Roles("ADMIN");
			}
			return null;
		}
	}

	@Override
	protected Class< ? extends AuthenticatedWebSession> getWebSessionClass()
	{
		return MyAuthenticatedWebSession.class;
	}
	
	@Override
	protected Class< ? extends SignInPage> getSignInPageClass()
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
	
	@Override
	protected void init()
	{
		MetaDataRoleAuthorizationStrategy.authorize(AdminBookmarkablePage.class, "ADMIN");
		MetaDataRoleAuthorizationStrategy.authorize(AdminInternalPage.class, "ADMIN");
	}
}
