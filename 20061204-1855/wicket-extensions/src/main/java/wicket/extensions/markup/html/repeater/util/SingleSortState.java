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
package wicket.extensions.markup.html.repeater.util;

import java.io.Serializable;

import wicket.extensions.markup.html.repeater.data.sort.ISortState;

/**
 * Implementation of ISortState that can keep track of sort information for a
 * single property.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class SingleSortState implements ISortState, Serializable
{
	private static final long serialVersionUID = 1L;

	SortParam param;

	/**
	 * @see wicket.extensions.markup.html.repeater.data.sort.ISortState#setPropertySortOrder(java.lang.String,
	 *      int)
	 */
	public void setPropertySortOrder(String property, int dir)
	{
		if (property == null)
		{
			throw new IllegalArgumentException("argument [property] cannot be null");
		}

		param = new SortParam(property, dir == ISortState.ASCENDING);
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.sort.ISortState#getPropertySortOrder(java.lang.String)
	 */
	public int getPropertySortOrder(String property)
	{
		if (property == null)
		{
			throw new IllegalArgumentException("argument [property] cannot be null");
		}

		if (param == null || !param.getProperty().equals(property))
		{
			return NONE;
		}
		else if (param.isAscending())
		{
			return ASCENDING;
		}
		else
		{
			return DESCENDING;
		}

	}

	/**
	 * @return current sort state
	 */
	public SortParam getSort()
	{
		return param;
	}

	/**
	 * Sets the current sort state
	 * 
	 * @param param
	 *            parameter containing new sorting information
	 */
	public void setSort(SortParam param)
	{
		this.param = param;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[SingleSortState sort=" + ((param == null) ? "null" : param.toString()) + "]";
	}

}
