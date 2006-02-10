package wicket.authorization.strategies.role.metadata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import wicket.Component;

/**
 * An internal data structure that maps a given component class to a set of role
 * strings. Permissions can be granted to instantiate a given component class
 * via authorize(Class, String role) and denied via unauthorize(Class, String
 * role). All authorization can be removed for a given class with
 * authorizeAll(Class).
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
final class InstantiationPermissions implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Holds roles object for a given component class */
	private final Map<Class< ? extends Component>, Set<String>> rolesForComponentClass = new HashMap<Class< ? extends Component>, Set<String>>();

	/**
	 * Gives the given role permission to instantiate the given class.
	 * 
	 * @param componentClass
	 *            The component class
	 * @param role
	 *            The role to add
	 */
	public final void authorize(final Class< ? extends Component> componentClass, final String role)
	{
		if (componentClass == null)
		{
			throw new IllegalArgumentException("Argument componentClass cannot be null");
		}

		if (role == null)
		{
			throw new IllegalArgumentException("Argument role cannot be null");
		}

		Set<String> roles = rolesForComponentClass.get(componentClass);
		if (roles == null)
		{
			roles = new HashSet<String>();
			rolesForComponentClass.put(componentClass, roles);
		}
		roles.add(role);
	}

	/**
	 * Gives all roles permission to instantiate the given class.
	 * 
	 * @param componentClass
	 *            The component class
	 */
	public final void authorizeAll(final Class< ? extends Component> componentClass)
	{
		if (componentClass == null)
		{
			throw new IllegalArgumentException("Argument componentClass cannot be null");
		}

		rolesForComponentClass.remove(componentClass);
	}

	/**
	 * Removes permission for the given role to instantiate the given class.
	 * 
	 * @param componentClass
	 *            The class
	 * @param role
	 *            The role to deny
	 */
	public final void unauthorize(final Class< ? extends Component> componentClass,
			final String role)
	{
		if (componentClass == null)
		{
			throw new IllegalArgumentException("Argument componentClass cannot be null");
		}

		if (role == null)
		{
			throw new IllegalArgumentException("Argument role cannot be null");
		}

		final Set<String> roles = rolesForComponentClass.get(componentClass);
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
	 * Gets the roles that have a binding with the given component class.
	 * 
	 * @param componentClass
	 *            the component class
	 * @return the roles that have a binding with the given component class, or
	 *         null if no entries are found
	 */
	public final Set<String> authorizedRoles(final Class< ? extends Component> componentClass)
	{
		if (componentClass == null)
		{
			throw new IllegalArgumentException("Argument componentClass cannot be null");
		}

		return rolesForComponentClass.get(componentClass);
	}
}