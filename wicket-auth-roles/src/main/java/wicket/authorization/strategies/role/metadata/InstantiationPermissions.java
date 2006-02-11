package wicket.authorization.strategies.role.metadata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import wicket.Component;
import wicket.authorization.strategies.role.Roles;

/**
 * An internal data structure that maps a given component class to a set of role
 * strings. Permissions can be granted to instantiate a given component class
 * via authorize(Class, Roles roles) and denied via unauthorize(Class, Roles
 * roles). All authorization can be removed for a given class with
 * authorizeAll(Class).
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
final class InstantiationPermissions implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Holds roles object for a given component class */
	private final Map<Class< ? extends Component>, Roles> rolesForComponentClass = new HashMap<Class< ? extends Component>, Roles>();

	/**
	 * Gives the given role permission to instantiate the given class.
	 * 
	 * @param componentClass
	 *            The component class
	 * @param rolesToAdd
	 *            The roles to add
	 */
	public final void authorize(final Class< ? extends Component> componentClass, final Roles rolesToAdd)
	{
		if (componentClass == null)
		{
			throw new IllegalArgumentException("Argument componentClass cannot be null");
		}

		if (rolesToAdd == null)
		{
			throw new IllegalArgumentException("Argument rolesToadd cannot be null");
		}

		Roles roles = rolesForComponentClass.get(componentClass);
		if (roles == null)
		{
			roles = new Roles();
			rolesForComponentClass.put(componentClass, roles);
		}
		roles.addAll(rolesToAdd);
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
	 * Gets the roles that have a binding with the given component class.
	 * 
	 * @param componentClass
	 *            the component class
	 * @return the roles that have a binding with the given component class, or
	 *         null if no entries are found
	 */
	public final Roles authorizedRoles(final Class< ? extends Component> componentClass)
	{
		if (componentClass == null)
		{
			throw new IllegalArgumentException("Argument componentClass cannot be null");
		}

		return rolesForComponentClass.get(componentClass);
	}

	/**
	 * Removes permission for the given role to instantiate the given class.
	 * 
	 * @param componentClass
	 *            The class
	 * @param rolesToRemove
	 *            The role to deny
	 */
	public final void unauthorize(final Class< ? extends Component> componentClass,
			final Roles rolesToRemove)
	{
		if (componentClass == null)
		{
			throw new IllegalArgumentException("Argument componentClass cannot be null");
		}

		if (rolesToRemove == null)
		{
			throw new IllegalArgumentException("Argument rolesToRemove cannot be null");
		}

		final Roles roles = rolesForComponentClass.get(componentClass);
		if (roles != null)
		{
			roles.removeAll(rolesToRemove);
		}

		// If we removed the last authorized role, we authorize the empty role
		// so that removing authorization can't suddenly open something up to
		// everyone.
		if (roles.size() == 0)
		{
			roles.add(MetaDataRoleAuthorizationStrategy.NO_ROLE);
		}
	}
}
