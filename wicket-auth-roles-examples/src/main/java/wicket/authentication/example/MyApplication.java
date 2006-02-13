package wicket.authentication.example;

import wicket.authentication.AuthenticatedWebApplication;
import wicket.authentication.AuthenticatedWebSession;
import wicket.authorization.strategies.role.Roles;
import wicket.protocol.http.WebApplication;

/**
 * A role-authorized, authenticated web application in just a few lines of code.
 * 
 * @author Jonathan Locke
 */
public class MyApplication extends AuthenticatedWebApplication
{
	/**
	 * Authenticated session subclass
	 * 
	 * @author Jonathan Locke
	 */
	public static class MySession extends AuthenticatedWebSession
	{
		private boolean signedIn;

		/**
		 * Construct.
		 * 
		 * @param application
		 *            The application
		 */
		protected MySession(WebApplication application)
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
		return MySession.class;
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
