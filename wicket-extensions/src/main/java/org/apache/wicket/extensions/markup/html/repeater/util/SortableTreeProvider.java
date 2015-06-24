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
 * @param <S>
 *            the type of the sorting parameter
 */
public abstract class SortableTreeProvider<T, S> implements ISortableTreeProvider<T, S>
{
	private static final long serialVersionUID = 1L;

	private final SingleSortState<S> state = new SingleSortState<>();

	/**
	 * @see ISortableDataProvider#getSortState()
	 */
	@Override
	public final ISortState<S> getSortState()
	{
		return (ISortState<S>) state;
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
		setSort(new SortParam<>(property, ascending));
	}

	/**
	 * @see ISortableDataProvider#detach()
	 */
	@Override
	public void detach()
	{
	}
}
