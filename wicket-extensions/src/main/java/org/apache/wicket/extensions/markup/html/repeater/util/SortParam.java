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

import org.apache.wicket.IClusterable;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;

/**
 * Represents sorting information of a property
 *
 * @author Igor Vaynberg ( ivaynberg )
 */
public class SortParam implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private String property;
	private SortOrder order;

	/**
	 * @param property
	 *            sort property
	 * @param order
	 *            sort order
	 */
	public SortParam(String property, SortOrder order)
	{
		this.property = property;
		this.order = order;
	}

	/**
	 * get sort order
	 *
	 * @return sort order
	 */
	public SortOrder getOrder()
	{
		return order;
	}

	/**
	 * @return sort property
	 */
	public String getProperty()
	{
		return property;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if ((o instanceof SortParam) == false)
			return false;

		SortParam sortParam = (SortParam)o;

		return order == sortParam.order && property.equals(sortParam.property);
	}

	@Override
	public int hashCode()
	{
		int result = property.hashCode();
		result = 31 * result + order.hashCode();
		return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return new StringBuilder().append("[SortParam property=").append(getProperty()).append(
				" order=").append(order.name()).append("]").toString();
	}
}
