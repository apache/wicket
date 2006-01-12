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
	private ArrayList strategies = new ArrayList();

	/**
	 * Constructor
	 * 
	 * @param strat
	 *            authorization strategy
	 */
	public CompoundAuthorizationStrategy(IAuthorizationStrategy strat)
	{
		add(strat);
	}

	/**
	 * Convinience constructor that adds two strategies to the chain
	 * 
	 * @param strat1
	 *            authorization strategy
	 * @param strat2
	 *            authorization strategy
	 */
	public CompoundAuthorizationStrategy(IAuthorizationStrategy strat1,
			IAuthorizationStrategy strat2)
	{
		add(strat1);
		add(strat2);
	}

	/**
	 * Adds a strategy to the chain
	 * 
	 * @param strat
	 */
	public void add(IAuthorizationStrategy strat)
	{
		if (strat == null)
		{
			throw new IllegalArgumentException("argument [strat] cannot be null");
		}
		strategies.add(strat);
	}

	/**
	 * @see wicket.authorization.IAuthorizationStrategy#authorizeInstantiation(java.lang.Class)
	 */
	public boolean authorizeInstantiation(Class componentClass)
	{
		for (int i = 0; i < strategies.size(); i++)
		{
			IAuthorizationStrategy strat = (IAuthorizationStrategy)strategies.get(i);
			if (!strat.authorizeInstantiation(componentClass))
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
	public boolean authorizeAction(Component component, Action action)
	{
		for (int i = 0; i < strategies.size(); i++)
		{
			IAuthorizationStrategy strat = (IAuthorizationStrategy)strategies.get(i);
			if (!strat.authorizeAction(component, action))
			{
				return false;
			}
		}
		return true;
	}

}
