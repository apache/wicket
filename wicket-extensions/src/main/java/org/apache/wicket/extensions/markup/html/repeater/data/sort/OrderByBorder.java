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
package org.apache.wicket.extensions.markup.html.repeater.data.sort;

import org.apache.wicket.core.util.string.CssUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.util.string.Strings;

/**
 * A component that wraps markup with an OrderByLink. This has the advantage of being able to add
 * the attribute modifier to the wrapping element as opposed to the link, so that it can be attached
 * to &lt;th&gt; or any other element.
 * 
 * For example:
 * 
 * &lt;th wicket:id="order-by-border"&gt;Heading&lt;/th&gt;
 *
 * @param <S>
 *      the type of the sorting parameter
 * @author Igor Vaynberg ( ivaynberg )
 * 
 */
public class OrderByBorder<S> extends Border
{
	private static final long serialVersionUID = 1L;

	public static final String SORT_ASCENDING_CSS_CLASS_KEY = CssUtils.key(OrderByLink.class, "ascending");

	public static final String SORT_DESCENDING_CSS_CLASS_KEY = CssUtils.key(OrderByLink.class, "descending");

	public static final String SORT_NONE_CSS_CLASS_KEY = CssUtils.key(OrderByLink.class, "none");

	private final ISortStateLocator<S> stateLocator;

	private final S property;

	/**
	 * @param id
	 *            see
	 *            {@link OrderByLink#OrderByLink(java.lang.String, java.lang.Object, org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator) }
	 * @param property
	 *            see
	 *            {@link OrderByLink#OrderByLink(java.lang.String, java.lang.Object, org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator) }
	 * @param stateLocator
	 *            see
	 *            {@link OrderByLink#OrderByLink(java.lang.String, java.lang.Object, org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator) }
	 */
	public OrderByBorder(final String id, final S property,
		final ISortStateLocator<S> stateLocator)
	{
		super(id);

		this.stateLocator = stateLocator;
		this.property = property;

		OrderByLink<S> link = newOrderByLink("orderByLink", property, stateLocator);
		addToBorder(link);
		link.add(getBodyContainer());
	}

	/**
	 * create new sort order toggling link
	 * 
	 * @param id
	 *            component id
	 * @param property
	 *            sort property
	 * @param stateLocator
	 *            sort state locator
	 * @return link
	 */
	protected OrderByLink<S> newOrderByLink(final String id, final S property,
		final ISortStateLocator<S> stateLocator)
	{
		return new OrderByLink<S>(id, property, stateLocator)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSortChanged()
			{
				OrderByBorder.this.onSortChanged();
			}
		};
	}

	/**
	 * This method is a hook for subclasses to perform an action after sort has changed
	 */
	protected void onSortChanged()
	{
		// noop
	}

	@Override
	public void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);

		final ISortState<S> sortState = stateLocator.getSortState();

		SortOrder dir = sortState.getPropertySortOrder(property);
		String cssClass;
		if (dir == SortOrder.ASCENDING)
		{
			cssClass = getString(SORT_ASCENDING_CSS_CLASS_KEY);
		}
		else if (dir == SortOrder.DESCENDING)
		{
			cssClass = getString(SORT_DESCENDING_CSS_CLASS_KEY);
		}
		else
		{
			cssClass = getString(SORT_NONE_CSS_CLASS_KEY);
		}

		if (!Strings.isEmpty(cssClass))
		{
			tag.append("class", cssClass, " ");
		}

	}

}
