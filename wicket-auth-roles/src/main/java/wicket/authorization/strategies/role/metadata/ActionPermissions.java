package wicket.authorization.strategies.role.metadata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import wicket.authorization.Action;

/**
 * For each Action, holds a set of roles that can perform that action. Roles can
 * be granted access to a given action via authorize(Action, String role) and
 * denied access via unauthorize(Action, String role). All permissions can be
 * removed for a given action via authorizeAll(Action).
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
final class ActionPermissions implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Map from an action to a set of role strings */
	private final Map<Action, Set<String>> rolesForAction = new HashMap<Action, Set<String>>();

	/**
	 * Gives permission for the given role to perform the given action
	 * 
	 * @param action
	 *            The action
	 * @param role
	 *            The role
	 */
	public final void authorize(final Action action, final String role)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("Argument action cannot be null");
		}
		
		if (role == null)
		{
			throw new IllegalArgumentException("Argument role cannot be null");
		}
		
		Set<String> roles = rolesForAction.get(action);
		if (roles == null)
		{
			roles = new HashSet<String>();
			rolesForAction.put(action, roles);
		}
		roles.add(role);
	}

	/**
	 * Remove all authorization for the given action.
	 * 
	 * @param action
	 *            The action to clear
	 */
	public final void authorizeAll(final Action action)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("Argument action cannot be null");
		}
		
		rolesForAction.remove(action);
	}

	/**
	 * Remove the given authorized role from an action.
	 * 
	 * @param action
	 *            The action
	 * @param role
	 *            The role to remove
	 */
	public final void unauthorize(final Action action, final String role)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("Argument action cannot be null");
		}
		
		if (role == null)
		{
			throw new IllegalArgumentException("Argument role cannot be null");
		}
		
		Set<String> roles = rolesForAction.get(action);
		if (roles != null)
		{
			roles.remove(role);
		}

		// If we removed the last authorized role, we authorize the empty role
		// so that removing authorization can't suddenly open something up to
		// everyone.
		if (roles.size() == 0)
		{
			roles.add(MetaDataRoleAuthorizationStrategy.NO_ROLE);
		}
	}

	/**
	 * Gets the roles that have a binding for the given action.
	 * 
	 * @param action
	 *            The action
	 * @return The roles authorized for the given action
	 */
	public final Set<String> rolesFor(final Action action)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("Argument action cannot be null");
		}
		
		return rolesForAction.get(action);
	}
}
