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
package org.apache.wicket.examples.resourcedecoration;

import org.apache.wicket.examples.resourcedecoration.GroupedAndOrderedResourceReference.ResourceGroup;

/**
 * @author jthomerson
 */
public class BasicGroupingKey implements Comparable<BasicGroupingKey>
{

	private final ResourceGroup group;
	private final int loadOrder;
	private final boolean css;

	/**
	 * Construct.
	 * 
	 * @param group
	 * @param loadOrder
	 * @param css
	 */
	public BasicGroupingKey(ResourceGroup group, int loadOrder, boolean css)
	{
		this.group = group;
		this.loadOrder = loadOrder;
		this.css = css;
	}

	public ResourceGroup getGroup()
	{
		return group;
	}

	public int getLoadOrder()
	{
		return loadOrder;
	}

	public boolean isCss()
	{
		return css;
	}

	@Override
	public String toString()
	{
		return "BasicGroupingKey [group=" + group + ", loadOrder=" + loadOrder + ", css=" + css +
			"]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (css ? 1231 : 1237);
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + loadOrder;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicGroupingKey other = (BasicGroupingKey)obj;
		if (css != other.css)
			return false;
		if (group != other.group)
			return false;
		if (loadOrder != other.loadOrder)
			return false;
		return true;
	}

	public int compareTo(BasicGroupingKey o)
	{
		int comp = css == o.css ? 0 : (css ? 1 : -1);
		if (comp == 0)
		{
			comp = group.compareTo(o.group);
		}
		if (comp == 0)
		{
			comp = loadOrder < o.loadOrder ? -1 : (loadOrder == o.loadOrder ? 0 : 1);
		}
		return comp;
	}

}
