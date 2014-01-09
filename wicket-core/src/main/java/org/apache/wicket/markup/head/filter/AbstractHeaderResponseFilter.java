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
package org.apache.wicket.markup.head.filter;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IWrappedHeaderItem;
import org.apache.wicket.markup.head.filter.FilteringHeaderResponse.IHeaderResponseFilter;

/**
 * A default implementation of IHeaderResponseFilter that returns true for everything. It is defined
 * as abstract because you are not supposed to use it directly, but use it as a base and override
 * any methods that you need to return something other than true from (whether that's always false
 * or conditional logic).
 * 
 * @author Jeremy Thomerson
 */
public abstract class AbstractHeaderResponseFilter implements IHeaderResponseFilter
{

	private final String name;

	/**
	 * Create a response filter.
	 * 
	 * @param name
	 */
	public AbstractHeaderResponseFilter(String name)
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean accepts(HeaderItem item)
	{
		while (item instanceof IWrappedHeaderItem)
		{
			item = ((IWrappedHeaderItem)item).getWrapped();
		}
		return acceptsWrapped(item);
	}

	protected boolean acceptsWrapped(HeaderItem item)
	{
		return true;
	}
}
