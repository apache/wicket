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
package org.apache.wicket.extensions.markup.html.repeater.util;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.ISortableTreeProvider;

/**
 * Convenience implementation of a tree provider that can also act as a locator for a
 * {@link SingleSortState} object.
 * 
 * @author svenmeier
 * @param <T>
 */
public abstract class SortableTreeProvider<T, S> implements ISortableTreeProvider<T, S>
{
	private static final long serialVersionUID = 1L;

	private SingleSortState<S> state = new SingleSortState<S>();

	/**
	 * @see ISortableDataProvider#getSortState()
	 */
	@SuppressWarnings("unchecked")
	public final ISortState<S> getSortState()
	{
		return (ISortState<S>)state;
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider#getSortState()
	 * @param state
	 */
	@SuppressWarnings("unchecked")
	public final void setSortState(ISortState<S> state)
	{
		if (!(state instanceof SingleSortState))
		{
			throw new IllegalArgumentException(
				"argument [state] must be an instance of SingleSortState, but it is [" +
					state.getClass().getName() + "]:[" + state.toString() + "]");
		}
		this.state = (SingleSortState<S>)state;
	}

	/**
	 * Returns current sort state
	 * 
	 * @return current sort state
	 */
	public SortParam<S> getSort()
	{
		return state.getSort();
	}

	/**
	 * Sets the current sort state
	 * 
	 * @param param
	 *            parameter containing new sorting information
	 */
	public void setSort(SortParam<S> param)
	{
		state.setSort(param);
	}

	/**
	 * Sets the current sort state
	 * 
	 * @param property
	 *            sort property
	 * @param ascending
	 *            sort direction
	 */
	public void setSort(S property, boolean ascending)
	{
		setSort(new SortParam<S>(property, ascending));
	}

	/**
	 * @see ISortableDataProvider#detach()
	 */
	@Override
	public void detach()
	{
	}
}