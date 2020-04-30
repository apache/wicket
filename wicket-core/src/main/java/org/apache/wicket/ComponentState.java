/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package org.apache.wicket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.behavior.InvalidBehaviorIdException;
import org.apache.wicket.model.IModel;

abstract class ComponentState implements Serializable
{
	private static final long serialVersionUID = 1L;

	abstract IModel< ? > getModel();

	abstract Object getBehaviors();

	abstract Object getMetaData();

	static class ModelBehaviorComponentState extends ComponentState
	{
		private static final long serialVersionUID = 1L;

		private IModel< ? > model;

		private Object behaviors;

		private ModelBehaviorComponentState(IModel< ? > model, Object behaviors)
		{
			this.model = model;
			this.behaviors = behaviors;
		}

		@Override
		IModel< ? > getModel()
		{
			return model;
		}

		@Override
		Object getBehaviors()
		{
			return behaviors;
		}

		@Override
		Object getMetaData()
		{
			return null;
		}
	}

	static class ModelMetaDataComponentState extends ComponentState
	{
		private static final long serialVersionUID = 1L;

		private IModel< ? > model;

		private Object metaData;

		private ModelMetaDataComponentState(IModel< ? > model, Object metaData)
		{
			this.model = model;
			this.metaData = metaData;
		}

		@Override
		IModel< ? > getModel()
		{
			return model;
		}

		@Override
		Object getBehaviors()
		{
			return null;
		}

		@Override
		Object getMetaData()
		{
			return metaData;
		}
	}

	static class BehaviorsMetaDataComponentState extends ComponentState
	{
		private static final long serialVersionUID = 1L;

		private Object behaviors;

		private Object metaData;

		private BehaviorsMetaDataComponentState(Object behaviors, Object metaData)
		{
			this.behaviors = behaviors;
			this.metaData = metaData;
		}

		@Override
		IModel< ? > getModel()
		{
			return null;
		}

		@Override
		Object getBehaviors()
		{
			return behaviors;
		}

		@Override
		Object getMetaData()
		{
			return metaData;
		}
	}

	static class ModelBehaviorsMetaDataComponentState extends ComponentState
	{
		private static final long serialVersionUID = 1L;

		private IModel< ? > model;

		private Object behaviors;

		private Object metaData;

		private ModelBehaviorsMetaDataComponentState(IModel< ? > model, Object behaviors,
				Object metaData)
		{
			this.model = model;
			this.behaviors = behaviors;
			this.metaData = metaData;
		}

		@Override
		IModel< ? > getModel()
		{
			return model;
		}

		@Override
		Object getBehaviors()
		{
			return behaviors;
		}

		@Override
		Object getMetaData()
		{
			return metaData;
		}
	}

	static IModel< ? > getModel(Object state, boolean modelSet)
	{
		if (!modelSet || state == null)
		{
			return null;
		}
		if (state instanceof IModel)
		{
			return (IModel< ? >) state;
		}
		return ((ComponentState) state).getModel();
	}

	static Object getBehaviors(Object state, boolean modelSet)
	{
		if (state instanceof ComponentState)
		{
			return ((ComponentState) state).getBehaviors();
		}
		return modelSet || !(state instanceof Behavior || state instanceof Behavior[]) ? null
			: state;
	}

	static Object getMetaData(Object state, boolean modelSet)
	{
		if (state instanceof ComponentState)
		{
			return ((ComponentState) state).getMetaData();
		}
		return modelSet || !(state instanceof MetaDataEntry || state instanceof MetaDataEntry[])
			? null : state;
	}

	static Object setModel(IModel< ? > model, Object state, boolean modelSet)
	{
		Object stateBehavior;
		Object stateMetaData;
		if (state instanceof ComponentState)
		{
			ComponentState compState = (ComponentState) state;
			stateBehavior = compState.getBehaviors();
			stateMetaData = compState.getMetaData();
		}
		else if (modelSet)
		{
			stateBehavior = null;
			stateMetaData = null;
		}
		else if (state instanceof MetaDataEntry || state instanceof MetaDataEntry[])
		{
			stateBehavior = null;
			stateMetaData = state;
		}
		else
		{
			stateBehavior = state;
			stateMetaData = null;
		}

		return createState(state, model, stateBehavior, stateMetaData);
	}

	static Object addBehaviors(Component component, Object state, boolean modelSet,
			Behavior... behaviorsToAdd)
	{
		IModel< ? > stateModel;
		Object stateBehavior;
		Object stateMetaData;
		if (state instanceof ComponentState)
		{
			ComponentState compState = (ComponentState) state;
			stateModel = compState.getModel();
			stateBehavior = compState.getBehaviors();
			stateMetaData = compState.getMetaData();
		}
		else if (modelSet)
		{
			stateModel = (IModel< ? >) state;
			stateBehavior = null;
			stateMetaData = null;
		}
		else if (state instanceof MetaDataEntry || state instanceof MetaDataEntry[])
		{
			stateModel = null;
			stateBehavior = null;
			stateMetaData = state;
		}
		else
		{
			stateModel = null;
			stateBehavior = state;
			stateMetaData = null;
		}

		return createState(state, stateModel,
			addBehaviors(component, stateBehavior, behaviorsToAdd), stateMetaData);
	}

	static Object removeBehaviors(Component component, Object state, boolean modelSet,
			Behavior... behaviorsToRemove)
	{
		IModel< ? > stateModel;
		Object stateBehavior;
		Object stateMetaData;
		if (state instanceof ComponentState)
		{
			ComponentState compState = (ComponentState) state;
			stateModel = compState.getModel();
			stateBehavior = compState.getBehaviors();
			stateMetaData = compState.getMetaData();
		}
		else if (modelSet)
		{
			stateModel = (IModel< ? >) state;
			stateBehavior = null;
			stateMetaData = null;
		}
		else if (state instanceof MetaDataEntry || state instanceof MetaDataEntry[])
		{
			stateModel = null;
			stateBehavior = null;
			stateMetaData = state;
		}
		else
		{
			stateModel = null;
			stateBehavior = state;
			stateMetaData = null;
		}

		return createState(state, stateModel,
			removeBehaviors(component, stateBehavior, behaviorsToRemove), stateMetaData);
	}

	static <T> Object setMetaData(Object state, boolean modelSet, MetaDataKey<T> key, T data)
	{
		IModel< ? > stateModel;
		Object stateBehavior;
		Object stateMetaData;
		if (state instanceof ComponentState)
		{
			ComponentState compState = (ComponentState) state;
			stateModel = compState.getModel();
			stateBehavior = compState.getBehaviors();
			stateMetaData = compState.getMetaData();
		}
		else if (modelSet)
		{
			stateModel = (IModel< ? >) state;
			stateBehavior = null;
			stateMetaData = null;
		}
		else if (state instanceof MetaDataEntry || state instanceof MetaDataEntry[])
		{
			stateModel = null;
			stateBehavior = null;
			stateMetaData = state;
		}
		else
		{
			stateModel = null;
			stateBehavior = state;
			stateMetaData = null;
		}

		return createState(state, stateModel, stateBehavior, setMetaData(stateMetaData, key, data));
	}

	private static Object createState(Object oldState, IModel< ? > model, Object behaviors,
			Object metaData)
	{
		if (model == null)
		{
			if (behaviors == null)
			{
				if (metaData == null)
				{
					return null;
				}
				else
				{
					return metaData;
				}
			}
			else
			{
				if (metaData == null)
				{
					return behaviors;
				}
				else
				{
					if (oldState instanceof BehaviorsMetaDataComponentState)
					{
						BehaviorsMetaDataComponentState old =
							(BehaviorsMetaDataComponentState) oldState;
						old.behaviors = behaviors;
						old.metaData = metaData;
						return old;
					}
					return new BehaviorsMetaDataComponentState(behaviors, metaData);
				}
			}
		}
		else
		{
			if (behaviors == null)
			{
				if (metaData == null)
				{
					return model;
				}
				else
				{
					if (oldState instanceof ModelMetaDataComponentState)
					{
						ModelMetaDataComponentState old = (ModelMetaDataComponentState) oldState;
						old.model = model;
						old.metaData = metaData;
						return old;
					}
					return new ModelMetaDataComponentState(model, metaData);
				}
			}
			else
			{
				if (metaData == null)
				{
					if (oldState instanceof ModelBehaviorComponentState)
					{
						ModelBehaviorComponentState old = (ModelBehaviorComponentState) oldState;
						old.model = model;
						old.behaviors = behaviors;
						return old;
					}
					return new ModelBehaviorComponentState(model, behaviors);
				}
				else
				{
					if (oldState instanceof ModelBehaviorsMetaDataComponentState)
					{
						ModelBehaviorsMetaDataComponentState old =
							(ModelBehaviorsMetaDataComponentState) oldState;
						old.model = model;
						old.behaviors = behaviors;
						old.metaData = metaData;
						return old;
					}
					return new ModelBehaviorsMetaDataComponentState(model, behaviors, metaData);
				}
			}
		}
	}

	private static Object addBehaviors(Component component, Object behaviors,
			Behavior... behaviorsToAdd)
	{
		// nothing to add
		if (behaviorsToAdd.length == 0)
		{
			return behaviors;
		}

		// the existing array is compact, adding cannot shrink it
		int curLength = getBehaviorsLength(behaviors);
		int newSize = Math.max(curLength, behaviorsToAdd.length + getBehaviorsLength(behaviors)
			- getEmptyBehaviorsSlots(behaviors));

		// new size is 1, it must be we are adding 1 to 0
		if (newSize == 1)
		{
			return behaviorsToAdd[0];
		}

		// construct the return array and copy existing behaviors
		Behavior[] ret = new Behavior[newSize];
		if (behaviors instanceof Behavior[])
		{
			System.arraycopy(behaviors, 0, ret, 0, curLength);
		}
		else
		{
			ret[0] = (Behavior) behaviors;
		}

		// fill empty slots with behaviors to add
		int checkSlot = 0;
		for (Behavior behaviorToAdd : behaviorsToAdd)
		{
			while (ret[checkSlot] != null)
			{
				checkSlot++;
			}
			ret[checkSlot] = behaviorToAdd;
		}
		return ret;
	}

	static void bindBehavior(Component component, Behavior behavior)
	{
		if (!behavior.isTemporary(component))
		{
			component.addStateChange();
		}
		behavior.bind(component);
	}

	private static Object removeBehaviors(Component component, Object behaviors,
			Behavior... behaviorsToRemove)
	{
		// nothing to remove
		if (behaviorsToRemove.length == 0)
		{
			return behaviors;
		}
		if (behaviors == null)
		{
			throw cannotRemove(behaviorsToRemove[0]);
		}

		if (behaviors instanceof Behavior)
		{
			if (!behaviorsToRemove[0].equals(behaviors))
			{
				throw cannotRemove(behaviorsToRemove[0]);
			}
			if (behaviorsToRemove.length > 1)
			{
				throw cannotRemove(behaviorsToRemove[1]);
			}
			unbindBehavior(component, (Behavior) behaviors);
			return null;
		}

		Behavior[] behaviorArr = (Behavior[]) behaviors;
		for (Behavior behaviorToRemove : behaviorsToRemove)
		{
			boolean found = false;
			for (int i = 0; i < behaviorArr.length; i++)
			{
				Behavior curBehavior = behaviorArr[i];
				if (curBehavior != null && behaviorToRemove.equals(curBehavior))
				{
					found = true;
					unbindBehavior(component, curBehavior);
					behaviorArr[i] = null;
					break;
				}
			}
			if (!found)
			{
				throw cannotRemove(behaviorToRemove);
			}
		}
		return compactBehaviors(component, behaviorArr);
	}

	private static IllegalStateException cannotRemove(Behavior behavior)
	{
		return new IllegalStateException(
			"Tried to remove a behavior that was not added to the component. Behavior: "
				+ behavior.toString());
	}

	private static void unbindBehavior(Component component, Behavior behavior)
	{
		behavior.unbind(component);
		if (!behavior.isTemporary(component))
		{
			component.addStateChange();
		}
		behavior.detach(component);
	}

	private static Object compactBehaviors(Component component, Behavior[] behaviors)
	{
		// first find the number of behaviors and the highest statefull one
		Behavior singleBehavior = null;
		int highestId = -1;
		int filledSlots = 0;
		for (int i = 0; i < behaviors.length; i++)
		{
			Behavior curBehavior = behaviors[i];
			if (curBehavior != null)
			{
				singleBehavior = curBehavior;
				filledSlots++;
				if (!curBehavior.getStatelessHint(component))
				{
					highestId = i;
				}
			}
		}

		int newSize = Math.max(highestId + 1, filledSlots);
		if (newSize == 0)
		{
			return null;
		}
		if (newSize == 1)
		{
			return singleBehavior;
		}

		// multiple behaviors (or one with an id > 0)
		Behavior[] ret = new Behavior[newSize];
		int checkSlot = 0;
		for (int i = 0; i < behaviors.length; i++)
		{
			Behavior curBehavior = behaviors[i];
			if (curBehavior == null)
			{
				continue;
			}
			// statefull behaviors stay at their index
			if (!curBehavior.getStatelessHint(component))
			{
				ret[i] = curBehavior;
			}
			else
			{
				// for all others, find the first free slot
				while (ret[checkSlot] != null)
				{
					checkSlot++;
				}
				ret[checkSlot] = curBehavior;
			}
		}
		return ret;
	}

	private static int getBehaviorsLength(Object behaviors)
	{
		if (behaviors == null)
		{
			return 0;
		}
		return behaviors instanceof Behavior[] ? ((Behavior[]) behaviors).length : 1;
	}

	private static int getEmptyBehaviorsSlots(Object behaviors)
	{
		if (!(behaviors instanceof Behavior[]))
		{
			return 0;
		}
		Behavior[] arr = (Behavior[]) behaviors;
		int emptyCount = 0;
		for (Behavior curBehavior : arr)
		{
			if (curBehavior == null)
			{
				emptyCount++;
			}
		}
		return emptyCount;
	}

	private static <T> Object setMetaData(Object metadata, MetaDataKey<T> key, T data)
	{
		if (metadata == null)
		{
			if (data == null)
			{
				return null;
			}
			else
			{
				return new MetaDataEntry<>(key, data);
			}
		}
		else if (metadata instanceof MetaDataEntry)
		{
			MetaDataEntry< ? > curEntry = (MetaDataEntry< ? >) metadata;
			if (curEntry.key.equals(key))
			{
				if (data == null)
				{
					return null;
				}
				else
				{
					curEntry.object = data;
					return curEntry;
				}
			}
			else
			{
				if (data == null)
				{
					return metadata;
				}
				else
				{
					MetaDataEntry< ? >[] ret = new MetaDataEntry< ? >[2];
					ret[0] = (MetaDataEntry< ? >) metadata;
					ret[1] = new MetaDataEntry<>(key, data);
					return ret;
				}
			}
		}
		else
		{
			MetaDataEntry< ? >[] metadataArr = (MetaDataEntry< ? >[]) metadata;
			for (int i = 0; i < metadataArr.length; i++)
			{
				MetaDataEntry< ? > curEntry = metadataArr[i];
				if (curEntry.key.equals(key))
				{
					if (data == null)
					{
						if (metadataArr.length == 2)
						{
							return metadataArr[i == 0 ? 1 : 0];
						}
						else
						{
							MetaDataEntry< ? >[] ret =
								new MetaDataEntry< ? >[metadataArr.length - 1];
							System.arraycopy(metadataArr, 0, ret, 0, i);
							System.arraycopy(metadataArr, i + 1, ret, i, ret.length - i);
							return ret;
						}
					}
					else
					{
						curEntry.object = data;
						return metadataArr;
					}
				}
			}
			if (data == null)
			{
				return metadataArr;
			}
			MetaDataEntry< ? >[] ret = new MetaDataEntry< ? >[metadataArr.length + 1];
			System.arraycopy(metadataArr, 0, ret, 0, metadataArr.length);
			ret[metadataArr.length] = new MetaDataEntry<>(key, data);
			return ret;
		}
	}

	static Behavior getBehaviorById(Component component, int id, Object state, boolean modelSet)
	{
		Object behaviors = getBehaviors(state, modelSet);
		if (behaviors instanceof Behavior)
		{
			if (id == 0)
			{
				return (Behavior) behaviors;
			}
		}
		else if (behaviors instanceof Behavior[])
		{
			Behavior[] behaviorsArr = (Behavior[]) behaviors;
			if (behaviorsArr.length > id && behaviorsArr[id] != null)
			{
				return behaviorsArr[id];
			}
		}
		throw new InvalidBehaviorIdException(component, id);
	}

	static int getBehaviorId(Component component, Behavior behavior, Object state, boolean modelSet)
	{
		Object behaviors = getBehaviors(state, modelSet);
		if (behavior.equals(behaviors))
		{
			return 0;
		}
		else if (behaviors instanceof Behavior[])
		{
			Behavior[] behaviorsArr = (Behavior[]) behaviors;
			for (int i = 0; i < behaviorsArr.length; i++)
			{
				if (behavior.equals(behaviorsArr[i]))
				{
					return i;
				}
			}
		}
		throw new IllegalStateException(
			"Behavior must be added to component before its id can be generated. Behavior: "
				+ behavior + ", Component: " + component);
	}

	@SuppressWarnings("unchecked")
	static <M extends Behavior> List<M> getBehaviors(Class<M> type, Object state, boolean modelSet)
	{
		Object behaviors = getBehaviors(state, modelSet);
		if (behaviors == null)
		{
			return List.of();
		}

		if (behaviors instanceof Behavior)
		{
			if (type == null || type.isInstance(behaviors))
			{
				return List.of((M) behaviors);
			}
			return List.of();
		}

		Behavior[] behaviorsArr = (Behavior[]) behaviors;
		List<M> subset = new ArrayList<>(behaviorsArr.length);
		for (Behavior curBehavior : behaviorsArr)
		{
			if (curBehavior != null && (type == null || type.isInstance(curBehavior)))
			{
				subset.add((M) curBehavior);
			}
		}
		if (subset.isEmpty())
		{
			return List.of();
		}
		return Collections.unmodifiableList(subset);
	}

	static void onRemoveBehaviors(Component component, Object state, boolean modelSet)
	{
		Object behaviors = getBehaviors(state, modelSet);
		if (behaviors instanceof Behavior)
		{
			((Behavior) behaviors).onRemove(component);
		}
		else if (behaviors instanceof Behavior[])
		{
			Behavior[] behaviorsArr = (Behavior[]) behaviors;
			for (Behavior curBehavior : behaviorsArr)
			{
				if (curBehavior != null)
				{
					curBehavior.onRemove(component);
				}
			}
		}
	}

	static Object detachBehaviors(Component component, Object state, boolean modelSet)
	{
		Object behaviors = getBehaviors(state, modelSet);
		if (behaviors instanceof Behavior)
		{
			Behavior behavior = (Behavior) behaviors;
			behavior.detach(component);
			if (behavior.isTemporary(component))
			{
				behavior.unbind(component);
				return createState(state, getModel(state, modelSet), null,
					getMetaData(state, modelSet));
			}
		}
		else if (behaviors instanceof Behavior[])
		{
			boolean changed = false;
			Behavior[] behaviorsArr = (Behavior[]) behaviors;
			for (int i = 0; i < behaviorsArr.length; i++)
			{
				Behavior curBehavior = behaviorsArr[i];
				if (curBehavior != null)
				{
					curBehavior.detach(component);
					if (curBehavior.isTemporary(component))
					{
						curBehavior.unbind(component);
						behaviorsArr[i] = null;
						changed = true;
					}
				}
			}
			if (changed)
			{
				return createState(state, getModel(state, modelSet),
					compactBehaviors(component, behaviorsArr), getMetaData(state, modelSet));
			}
		}
		return state;
	}
}
