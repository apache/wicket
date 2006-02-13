package wicket.authentication;

import wicket.Session;
import wicket.authorization.strategies.role.Roles;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebSession;

/**
 * Basic authenticated web session.
 * 
 * @author Jonathan Locke
 */
public abstract class AuthenticatedWebSession extends WebSession
{
	/**
	 * @return Current authenticated web session
	 */
	public static AuthenticatedWebSession get()
	{
		return (AuthenticatedWebSession)Session.get();
	}

	/**
	 * Construct.
	 * 
	 * @param application
	 *            The web application
	 */
	protected AuthenticatedWebSession(WebApplication application)
	{
		super(application);
	}

	/**
	 * Authenticates this session using the given username and password
	 * 
	 * @param username
	 *            The username
	 * @param password
	 *            The password
	 * @return True if the user was authenticated successfully
	 */
	public abstract boolean authenticate(final String username, final String password);

	/**
	 * @return True if the user is signed in to this session
	 */
	public abstract boolean isSignedIn();

	/**
	 * @return Get the roles that this session can play
	 */
	public abstract Roles getRoles();
}
