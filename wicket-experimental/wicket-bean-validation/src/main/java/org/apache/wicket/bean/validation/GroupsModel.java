package org.apache.wicket.bean.validation;

import java.util.ArrayList;

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.reference.ClassReference;

/**
 * A model that can hold on to an array of classes. Useful for keeping a definition of validation
 * groups to be used to restrict validation.
 * 
 * @author igor
 */
public class GroupsModel extends LoadableDetachableModel<Class<?>[]>
{
	private static Class<?>[] EMPTY = new Class<?>[0];

	private ArrayList<ClassReference<?>> groups = null;

	/**
	 * Constructor
	 * 
	 * @param groups
	 *            an array of groups or {@code null} for none
	 */
	public GroupsModel(Class<?>... groups)
	{
		if (groups == null || groups.length == 0)
		{
			this.groups = null;
		}
		else
		{
			this.groups = new ArrayList<ClassReference<?>>();
			for (Class<?> group : groups)
			{
				this.groups.add(ClassReference.of(group));
			}
			this.groups.trimToSize();
		}
	}

	@Override
	protected Class<?>[] load()
	{
		if (groups == null)
		{
			return EMPTY;
		}

		Class<?>[] classes = new Class[groups.size()];
		for (int i = 0; i < groups.size(); i++)
		{
			classes[i] = groups.get(i).get();
		}

		return classes;
	}

}
