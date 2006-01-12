package wicket.authorization;

import java.util.ArrayList;

import wicket.Component;

/**
 * Compound implementation of the IAuthorizationStrategy that lets you chain two
 * or more strategies together.
 * 
 * @author ivaynberg
 */
public class CompoundAuthorizationStrategy implements IAuthorizationStrategy
{
	/** List of strategies to consult */
	private ArrayList strategies = new ArrayList();

	/**
	 * Adds a strategy to the chain
	 * 
	 * @param strategy
	 *            Strategy to add
	 */
	public void add(IAuthorizationStrategy strategy)
	{
		if (strategy == null)
		{
			throw new IllegalArgumentException("Strategy argument cannot be null");
		}
		strategies.add(strategy);
	}

	/**
	 * @see wicket.authorization.IAuthorizationStrategy#authorizeInstantiation(java.lang.Class)
	 */
	public final boolean authorizeInstantiation(Class componentClass)
	{
		for (int i = 0; i < strategies.size(); i++)
		{
			IAuthorizationStrategy strategy = (IAuthorizationStrategy)strategies.get(i);
			if (!strategy.authorizeInstantiation(componentClass))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @see wicket.authorization.IAuthorizationStrategy#authorizeAction(wicket.Component,
	 *      wicket.authorization.Action)
	 */
	public final boolean authorizeAction(Component component, Action action)
	{
		for (int i = 0; i < strategies.size(); i++)
		{
			IAuthorizationStrategy strategy = (IAuthorizationStrategy)strategies.get(i);
			if (!strategy.authorizeAction(component, action))
			{
				return false;
			}
		}
		return true;
	}

}
