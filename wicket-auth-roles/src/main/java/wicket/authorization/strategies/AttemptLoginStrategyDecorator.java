package wicket.authorization.strategies;

import wicket.Component;
import wicket.RestartResponseAtSignInPageException;
import wicket.authorization.Action;
import wicket.authorization.IAuthorizationStrategy;

/**
 * If the user <strong>is not</strong> signed in and delegate strategy
 * authorization fails the user will be redirected to the sign in page instead
 * of the authorization failed page.
 * 
 * @author Igor Vaynberg (ivaynberg)(
 * 
 */
public abstract class AttemptLoginStrategyDecorator implements IAuthorizationStrategy
{
	private IAuthorizationStrategy delegate;

	/**
	 * Constructor
	 * 
	 * @param delegate
	 *            delegate authorization strategy
	 */
	public AttemptLoginStrategyDecorator(IAuthorizationStrategy delegate)
	{
		if (delegate == null)
		{
			throw new IllegalArgumentException("delegate strategy cannot be null");
		}
		this.delegate = delegate;
	}


	/**
	 * @see wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
	 */
	final public boolean isInstantiationAuthorized(Class componentClass)
	{
		return processResponse(delegate.isInstantiationAuthorized(componentClass));
	}

	/**
	 * @see wicket.authorization.IAuthorizationStrategy#isActionAuthorized(wicket.Component,
	 *      wicket.authorization.Action)
	 */
	final public boolean isActionAuthorized(Component component, Action action)
	{
		return processResponse(delegate.isActionAuthorized(component, action));
	}

	private boolean processResponse(boolean isAuthorized)
	{
		if (isAuthorized == false && isUserSignedIn() == false)
		{
			throw new RestartResponseAtSignInPageException();
		}
		return isAuthorized;
	}

	/**
	 * @return true if a user is currently signed into the session, false
	 *         otherwise
	 */
	protected abstract boolean isUserSignedIn();

}
