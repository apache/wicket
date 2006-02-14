package wicket.authentication;

import wicket.Session;
import wicket.authorization.strategies.role.Roles;
import wicket.protocol.http.WebSession;

/**
 * Basic authenticated web session. Subclasses must provide a method that
 * authenticates the session based on a username and password, and a method
 * implementation that gets the Roles
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

	/** True when the user is signed in */
	private boolean signedIn;

	/**
	 * Construct.
	 * 
	 * @param application
	 *            The web application
	 */
	public AuthenticatedWebSession(final AuthenticatedWebApplication application)
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
	 * @return Get the roles that this session can play
	 */
	public abstract Roles getRoles();

	/**
	 * @return True if the user is signed in to this session
	 */
	public final boolean isSignedIn()
	{
		return signedIn;
	}

	/**
	 * Signs user in by authenticating them with a username and password
	 * 
	 * @param username
	 *            The username
	 * @param password
	 *            The password
	 * @return True if the user was signed in successfully
	 */
	public final boolean signIn(final String username, final String password)
	{
		return signedIn = authenticate(username, password);
	}
}
