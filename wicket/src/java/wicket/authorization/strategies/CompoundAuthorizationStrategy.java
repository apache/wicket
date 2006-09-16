/*
 * $Id: CompoundAuthorizationStrategy.java,v 1.6 2006/01/14 22:45:04
 * jonathanlocke Exp $ $Revision$ $Date: 2006-05-19 12:04:06 +0000 (Fri,
 * 19 May 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.authorization.strategies;

import java.util.ArrayList;
import java.util.List;

import wicket.Component;
import wicket.authorization.Action;
import wicket.authorization.IAuthorizationStrategy;

/**
 * Compound implementation of the IAuthorizationStrategy that lets you chain two
 * or more strategies together.
 * 
 * @author ivaynberg
 */
public class CompoundAuthorizationStrategy implements IAuthorizationStrategy
{
	/** List of strategies to consult */
	private List<IAuthorizationStrategy> strategies = new ArrayList<IAuthorizationStrategy>();

	/**
	 * Adds a strategy to the chain
	 * 
	 * @param strategy
	 *            Strategy to add
	 */
	public final void add(IAuthorizationStrategy strategy)
	{
		if (strategy == null)
		{
			throw new IllegalArgumentException("Strategy argument cannot be null");
		}
		strategies.add(strategy);
	}

	/**
	 * @see wicket.authorization.IAuthorizationStrategy#isActionAuthorized(wicket.Component,
	 *      wicket.authorization.Action)
	 */
	public final boolean isActionAuthorized(Component component, Action action)
	{
		for (IAuthorizationStrategy strategy : strategies)
		{
			if (!strategy.isActionAuthorized(component, action))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @see wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
	 */
	public final boolean isInstantiationAuthorized(Class<? extends Component> componentClass)
	{
		for (IAuthorizationStrategy strategy : strategies)
		{
			if (!strategy.isInstantiationAuthorized(componentClass))
			{
				return false;
			}
		}
		return true;
	}
}
