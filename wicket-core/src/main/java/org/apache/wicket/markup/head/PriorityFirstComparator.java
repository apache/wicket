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
package org.apache.wicket.markup.head;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.wicket.Page;
import org.apache.wicket.markup.head.ResourceAggregator.RecordedHeaderItem;
import org.apache.wicket.markup.head.ResourceAggregator.RecordedHeaderItemLocation;

/**
 * Implements the default sorting algorithm for {@link HeaderItem}s. {@link PriorityHeaderItem}s are
 * moved to the front, inverting the component order to convert the child-first into a parent-first
 * order. If {@code renderPageFirst} is true, the head from the markup of a page is moved to the
 * front of the header, directly after the priority header items.
 * 
 * @author papegaaij
 */
public class PriorityFirstComparator implements Comparator<RecordedHeaderItem>, Serializable
{
	protected static enum HeaderItemType {
		PRIORITY, PAGE, COMPONENT;
	}

	private final boolean renderPageFirst;

	/**
	 * Construct.
	 * 
	 * @param renderPageFirst
	 *            when true, the header of the page is moved to the front.
	 */
	public PriorityFirstComparator(boolean renderPageFirst)
	{
		this.renderPageFirst = renderPageFirst;
	}

	@Override
	public int compare(RecordedHeaderItem o1, RecordedHeaderItem o2)
	{
		HeaderItemType o1Type = getItemType(o1);
		HeaderItemType o2Type = getItemType(o2);

		if (o1Type != o2Type)
		{
			return o1Type.ordinal() - o2Type.ordinal();
		}

		if (o1Type == HeaderItemType.PRIORITY)
		{
			return inversedComponentOrder(o1, o2);
		}
		return compareWithinGroup(o1, o2);
	}

	/**
	 * Compares two header items that belong in the same group.
	 * 
	 * @param item1
	 * @param item2
	 * @return 0 by default to preserve the order
	 */
	protected int compareWithinGroup(RecordedHeaderItem item1, RecordedHeaderItem item2)
	{
		return 0;
	}

	/**
	 * Compare two recorded {@link PriorityHeaderItem}s, converting the child-first order into parent-first.
	 * 
	 * @param item1 first item
	 * @param item2 second item
	 * @return -1, 0 or 1 if item1 needs to be rendered before, unchanged or after item2.
	 * 
	 * @see RecordedHeaderItemLocation#getDepth()
	 */
	protected int inversedComponentOrder(RecordedHeaderItem item1, RecordedHeaderItem item2)
	{
		RecordedHeaderItemLocation location1 = item1.getLocations().get(item1.getLocations().size() - 1);
		RecordedHeaderItemLocation location2 = item2.getLocations().get(item2.getLocations().size() - 1);

		return location1.getDepth() - location2.getDepth();
	}

	/**
	 * Determines the type of the item: priority, page or component.
	 * 
	 * @param item
	 * @return the type of the item
	 */
	protected HeaderItemType getItemType(RecordedHeaderItem item)
	{
		if (item.getItem() instanceof PriorityHeaderItem)
		{
			return HeaderItemType.PRIORITY;
		}
		
		if (renderPageFirst)
		{
			if (item.getItem() instanceof PageHeaderItem)
			{
				return HeaderItemType.PAGE;
			}
			
			for (RecordedHeaderItemLocation curLocation : item.getLocations())
			{
				if (curLocation.getRenderBase() instanceof Page)
				{
					return HeaderItemType.PAGE;
				}
			}
		}

		return HeaderItemType.COMPONENT;
	}
}
