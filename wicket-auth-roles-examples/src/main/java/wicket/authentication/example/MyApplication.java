package wicket.authentication.example;

import wicket.authentication.AuthenticatedWebApplication;
import wicket.authentication.AuthenticatedWebSession;
import wicket.authorization.strategies.role.Roles;
import wicket.protocol.http.WebApplication;

/**
 * A role-authorized, authenticated web application in just a few lines of code.
 * 
 * @author Jonathan
 */
public class MyApplication extends AuthenticatedWebApplication
{
	public static class MySession extends AuthenticatedWebSession
	{
		protected MySession(WebApplication application)
		{
			super(application);
		}

		private boolean signedIn;
		
		@Override
		public boolean authenticate(String username, String password)
		{
			return signedIn = username.equals("jonathan") && password.equals("jonathan");
		}
		
		@Override
		public boolean isSignedIn()
		{
			return signedIn;
		}

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

	@Override
	public Class getHomePage()
	{
		return HomePage.class;
	}

}
