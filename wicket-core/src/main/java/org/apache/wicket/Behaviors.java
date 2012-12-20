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
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.util.lang.Args;

/**
 * Manages behaviors in a {@link Component} instance
 * 
 * @author igor
 */
final class Behaviors implements IDetachable
{
	private static final long serialVersionUID = 1L;
	private final Component component;

	public Behaviors(Component component)
	{
		this.component = component;
	}

	public void add(Behavior... behaviors)
	{
		Args.notNull(behaviors, "behaviors");

		for (Behavior behavior : behaviors)
		{
			Args.notNull(behavior, "behavior");

			internalAdd(behavior);

			if (!behavior.isTemporary(component))
			{
				component.addStateChange();
			}

			// Give handler the opportunity to bind this component
			behavior.bind(component);
		}
	}

	private void internalAdd(final Behavior behavior)
	{
		component.data_add(behavior);
		if (behavior.getStatelessHint(component))
		{
			getBehaviorId(behavior);
		}
	}

	@SuppressWarnings("unchecked")
	public <M extends Behavior> List<M> getBehaviors(Class<M> type)
	{
		final int len = component.data_length();
		final int start = component.data_start();
		if (len < start)
		{
			return Collections.emptyList();
		}

		List<M> subset = new ArrayList<M>(len);
		for (int i = component.data_start(); i < len; i++)
		{
			Object obj = component.data_get(i);
			if (obj != null && obj instanceof Behavior)
			{
				if (type == null || type.isAssignableFrom(obj.getClass()))
				{
					subset.add((M)obj);
				}
			}
		}
		return Collections.unmodifiableList(subset);
	}


	public void remove(Behavior behavior)
	{
		Args.notNull(behavior, "behavior");

		if (internalRemove(behavior))
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
	@Override
	public final void detach()
	{
		final int len = component.data_length();
		for (int i = component.data_start(); i < len; i++)
		{
			Object obj = component.data_get(i);
			if (obj != null && obj instanceof Behavior)
			{
				final Behavior behavior = (Behavior)obj;

				behavior.detach(component);

				if (behavior.isTemporary(component))
				{
					internalRemove(behavior);
				}
			}
		}
	}

	private boolean internalRemove(final Behavior behavior)
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
				ArrayList<Behavior> ids = getBehaviorsIdList(false);
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
						removeBehaviorsIdList();
					}

				}
				return true;
			}
		}
		return false;
	}

	private void removeBehaviorsIdList()
	{
		for (int i = component.data_start(); i < component.data_length(); i++)
		{
			Object obj = component.data_get(i);
			if (obj != null && obj instanceof BehaviorIdList)
			{
				component.data_remove(i);
				return;
			}
		}
	}

	private BehaviorIdList getBehaviorsIdList(boolean createIfNotFound)
	{
		int len = component.data_length();
		for (int i = component.data_start(); i < len; i++)
		{
			Object obj = component.data_get(i);
			if (obj != null && obj instanceof BehaviorIdList)
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

	private static class BehaviorIdList extends ArrayList<Behavior>
	{
		private static final long serialVersionUID = 1L;

		public BehaviorIdList()
		{
			super(1);
		}
	}

	public final int getBehaviorId(Behavior behavior)
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
					behavior + ", Component: " + this);
		}

		ArrayList<Behavior> ids = getBehaviorsIdList(true);

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

	public final Behavior getBehaviorById(int id)
	{
		Behavior behavior = null;

		ArrayList<Behavior> ids = getBehaviorsIdList(false);
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
