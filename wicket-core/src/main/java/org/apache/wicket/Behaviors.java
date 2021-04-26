/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.behavior.InvalidBehaviorIdException;
import org.apache.wicket.util.lang.Args;

/**
 * Manages behaviors for {@link Component} instances
 * 
 * @author igor
 */
final class Behaviors
{

	private Behaviors()
	{
		// utility class
	}

	public static void add(Component component, Behavior... behaviors)
	{
		Args.notNull(behaviors, "behaviors");

		for (Behavior behavior : behaviors)
		{
			Args.notNull(behavior, "behavior");

			internalAdd(component, behavior);

			if (!behavior.isTemporary(component))
			{
				component.addStateChange();
			}

			// Give handler the opportunity to bind this component
			behavior.bind(component);
		}
	}

	private static void internalAdd(Component component, Behavior behavior)
	{
		component.data_add(behavior);
		if (behavior.getStatelessHint(component) == false)
		{
			getBehaviorId(component, behavior);
		}
	}

	@SuppressWarnings("unchecked")
	public static <M extends Behavior> List<M> getBehaviors(Component component, Class<M> type)
	{
		int len = component.data_length();
		if (len == 0)
		{
			return Collections.emptyList();
		}
		int start = component.data_start();
		if (len < start)
		{
			return Collections.emptyList();
		}

		List<M> subset = null;
		for (int i = start; i < len; i++)
		{
			Object obj = component.data_get(i);
			if (obj instanceof Behavior)
			{
				if (type == null || type.isAssignableFrom(obj.getClass()))
				{
					if (subset == null)
					{
						subset = new ArrayList<>(len);
					}
					subset.add((M)obj);
				}
			}
		}
		if (subset == null || subset.isEmpty())
		{
			return Collections.emptyList();
		}
		else
		{
			return Collections.unmodifiableList(subset);
		}
	}


	public static void remove(Component component, Behavior behavior)
	{
		Args.notNull(behavior, "behavior");

		if (internalRemove(component, behavior))
		{
			if (!behavior.isTemporary(component))
			{
				component.addStateChange();
			}
			behavior.detach(component);
		}
		else
		{
			throw new IllegalStateException(
				"Tried to remove a behavior that was not added to the component. Behavior: " +
					behavior.toString());
		}
	}

	/**
	 * THIS IS WICKET INTERNAL ONLY. DO NOT USE IT.
	 *
	 * Traverses all behaviors and calls detachModel() on them. This is needed to cleanup behavior
	 * after render. This method is necessary for {@link org.apache.wicket.ajax.AjaxRequestTarget} to be able to cleanup
	 * component's behaviors after header contribution has been done (which is separated from
	 * component render).
	 */
	public static void detach(Component component)
	{
		int len = component.data_length();
		if (len == 0)
		{
			return;
		}
		int start = component.data_start();
		if (len < start)
		{
			return;
		}
		for (int i = start; i < len; i++)
		{
			Object obj = component.data_get(i);
			if (obj instanceof Behavior)
			{
				final Behavior behavior = (Behavior)obj;

				behavior.detach(component);

				final int currentLength = component.data_length();
				if (len != currentLength)
				{
					// if the length has changed then reset 'i' and 'len'
					for (int j = start; j < currentLength; j++)
					{
						// find the new index of the current behavior by identity
						if (behavior == component.data_get(j))
						{
							i = j;
							len = currentLength;
							break;
						}
					}
				}

				if (behavior.isTemporary(component))
				{
					internalRemove(component, behavior);
					i--;
					len--;
				}
			}
		}
	}

	private static boolean internalRemove(Component component, Behavior behavior)
	{
		final int len = component.data_length();
		for (int i = component.data_start(); i < len; i++)
		{
			Object o = component.data_get(i);
			if (o != null && o.equals(behavior))
			{
				component.data_remove(i);
				behavior.unbind(component);

				// remove behavior from behavior-ids
				ArrayList<Behavior> ids = getBehaviorsIdList(component, false);
				if (ids != null)
				{
					int idx = ids.indexOf(behavior);
					if (idx == ids.size() - 1)
					{
						ids.remove(idx);
					}
					else if (idx >= 0)
					{
						ids.set(idx, null);
					}
					ids.trimToSize();

					if (ids.isEmpty())
					{
						removeBehaviorsIdList(component);
					}

				}
				return true;
			}
		}
		return false;
	}

	private static void removeBehaviorsIdList(Component component)
	{
		for (int i = component.data_start(); i < component.data_length(); i++)
		{
			Object obj = component.data_get(i);
			if (obj instanceof BehaviorIdList)
			{
				component.data_remove(i);
				return;
			}
		}
	}

	private static BehaviorIdList getBehaviorsIdList(Component component, boolean createIfNotFound)
	{
		int len = component.data_length();
		for (int i = component.data_start(); i < len; i++)
		{
			Object obj = component.data_get(i);
			if (obj instanceof BehaviorIdList)
			{
				return (BehaviorIdList)obj;
			}
		}
		if (createIfNotFound)
		{
			BehaviorIdList list = new BehaviorIdList();
			component.data_add(list);
			return list;
		}
		return null;
	}

	/**
	 * Called when the component is going to be removed. Notifies all
	 * behaviors assigned to this component.
	 *
	 * @param component
	 *      the component that will be removed from its parent
	 */
	public static void onRemove(Component component)
	{
		int len = component.data_length();
		if (len == 0)
		{
			return;
		}
		int start = component.data_start();
		if (len < start)
		{
			return;
		}
		for (int i = start; i < len; i++)
		{
			Object obj = component.data_get(i);
			if (obj instanceof Behavior)
			{
				final Behavior behavior = (Behavior)obj;

				behavior.onRemove(component);
			}
		}
	}

	private static class BehaviorIdList extends ArrayList<Behavior>
	{
		private static final long serialVersionUID = 1L;

		public BehaviorIdList()
		{
			super(1);
		}
	}

	public static int getBehaviorId(Component component, Behavior behavior)
	{
		Args.notNull(behavior, "behavior");

		boolean found = false;
		for (int i = component.data_start(); i < component.data_length(); i++)
		{
			if (behavior == component.data_get(i))
			{
				found = true;
				break;
			}
		}
		if (!found)
		{
			throw new IllegalStateException(
				"Behavior must be added to component before its id can be generated. Behavior: " +
					behavior + ", Component: " + component);
		}

		ArrayList<Behavior> ids = getBehaviorsIdList(component, true);

		int id = ids.indexOf(behavior);

		if (id < 0)
		{
			// try to find an unused slot
			for (int i = 0; i < ids.size(); i++)
			{
				if (ids.get(i) == null)
				{
					ids.set(i, behavior);
					id = i;
					break;
				}
			}
		}

		if (id < 0)
		{
			// no unused slots, add to the end
			id = ids.size();
			ids.add(behavior);
			ids.trimToSize();
		}

		return id;
	}

	public static Behavior getBehaviorById(Component component, int id)
	{
		Behavior behavior = null;

		ArrayList<Behavior> ids = getBehaviorsIdList(component, false);
		if (ids != null)
		{
			if (id >= 0 && id < ids.size())
			{
				behavior = ids.get(id);
			}
		}

		if (behavior != null)
		{
			return behavior;
		}
		throw new InvalidBehaviorIdException(component, id);
	}


}
