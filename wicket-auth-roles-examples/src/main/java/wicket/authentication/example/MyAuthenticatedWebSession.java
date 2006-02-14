package wicket.authentication.example;

import wicket.authentication.AuthenticatedWebApplication;
import wicket.authentication.AuthenticatedWebSession;
import wicket.authorization.strategies.role.Roles;

/**
 * Authenticated session subclass
 * 
 * @author Jonathan Locke
 */
public class MyAuthenticatedWebSession extends AuthenticatedWebSession
{
	/**
	 * Construct.
	 * 
	 * @param application
	 *            The application
	 */
	public MyAuthenticatedWebSession(final AuthenticatedWebApplication application)
	{
		super(application);
	}

	/**
	 * @see wicket.authentication.AuthenticatedWebSession#authenticate(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public boolean authenticate(final String username, final String password)
	{
		// Check username and password
		return username.equals("wicket") && password.equals("wicket");
	}

	/**
	 * @see wicket.authentication.AuthenticatedWebSession#getRoles()
	 */
	@Override
	public Roles getRoles()
	{
		if (isSignedIn())
		{
			// If the user is signed in, they have these roles
			return new Roles(Roles.ADMIN);
		}
		return null;
	}
}
