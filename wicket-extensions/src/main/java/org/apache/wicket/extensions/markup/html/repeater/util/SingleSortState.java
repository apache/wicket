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
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Args;

/**
 * Implementation of ISortState that can keep track of sort information for a single property.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            the type of the sort property
 * 
 */
public class SingleSortState<T> implements ISortState<T>, IClusterable
{
	private static final long serialVersionUID = 1L;

	SortParam<T> param;

	@Override
	public void setPropertySortOrder(final T property, final SortOrder order)
	{
		Args.notNull(property, "property");
		Args.notNull(order, "order");

		if (order == SortOrder.NONE)
		{
			if ((param != null) && property.equals(param.getProperty()))
			{
				param = null;
			}
		}
		else
		{
			param = new SortParam<>(property, order == SortOrder.ASCENDING);
		}
	}

	@Override
	public SortOrder getPropertySortOrder(final T property)
	{
		Args.notNull(property, "property");

		if ((param == null) || (param.getProperty().equals(property) == false))
		{
			return SortOrder.NONE;
		}
		return param.isAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING;
	}

	/**
	 * @return current sort state
	 */
	public SortParam<T> getSort()
	{
		return param;
	}

	/**
	 * Sets the current sort state
	 * 
	 * @param param
	 *            parameter containing new sorting information
	 */
	public void setSort(final SortParam<T> param)
	{
		this.param = param;
	}

	@Override
	public String toString()
	{
		return "[SingleSortState sort=" + ((param == null) ? "null" : param.toString()) + ']';
	}

}
