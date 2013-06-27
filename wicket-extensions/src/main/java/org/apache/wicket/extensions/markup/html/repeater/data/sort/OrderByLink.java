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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.settings.ICssSettings;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * A component that represents a sort header. When the link is clicked it will toggle the state of a
 * sortable property within the sort state object.
 * 
 * @param <S>
 *            the type of the sorting parameter
 * @author Phil Kulak
 * @author Igor Vaynberg (ivaynberg)
 */
public class OrderByLink<S> extends Link<Void>
{
	private static final long serialVersionUID = 1L;

	public static final String SORT_ASCENDING_CSS_CLASS_KEY = "wicket-extensions-order-by-link-sort-ascending-css-class";

	public static final String SORT_DESCENDING_CSS_CLASS_KEY = "wicket-extensions-order-by-link-sort-descending-css-class";

	public static final String SORT_NONE_CSS_CLASS_KEY = "wicket-extensions-order-by-link-sort-none-css-class";

	/** sortable property */
	private final S property;

	/** locator for sort state object */
	private final ISortStateLocator<S> stateLocator;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the component id of the link
	 * @param property
	 *            the name of the sortable property this link represents. this value will be used as
	 *            parameter for sort state object methods. sort state object will be located via the
	 *            stateLocator argument.
	 * @param stateLocator
	 *            locator used to locate sort state object that this will use to read/write state of
	 *            sorted properties
	 *
	 */
	public OrderByLink(final String id, final S property, final ISortStateLocator<S> stateLocator)
	{
		super(id);

		Args.notNull(property, "property");

		this.property = property;
		this.stateLocator = stateLocator;
	}

	/**
	 * @see org.apache.wicket.markup.html.link.Link
	 */
	@Override
	public final void onClick()
	{
		sort();
		onSortChanged();
	}

	/**
	 * This method is a hook for subclasses to perform an action after sort has changed
	 */
	protected void onSortChanged()
	{
		// noop
	}

	/**
	 * Re-sort data provider according to this link
	 * 
	 * @return this
	 */
	public final OrderByLink<S> sort()
	{
		if (isVersioned())
		{
			// version the old state
			addStateChange();
		}

		ISortState<S> state = stateLocator.getSortState();

		// get current sort order
		SortOrder order = state.getPropertySortOrder(property);

		// set next sort order
		state.setPropertySortOrder(property, nextSortOrder(order));

		return this;
	}

	/**
	 * returns the next sort order when changing it
	 * 
	 * @param order
	 *            previous sort order
	 * @return next sort order
	 */
	protected SortOrder nextSortOrder(final SortOrder order)
	{
		// init / flip order
		if (order == SortOrder.NONE)
		{
			return SortOrder.ASCENDING;
		}
		else
		{
			return order == SortOrder.ASCENDING ? SortOrder.DESCENDING : SortOrder.ASCENDING;
		}
	}

	@Override
	public void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);

		final ISortState<S> sortState = stateLocator.getSortState();

		ICssSettings cssSettings = getApplication().getCssSettings();
		SortOrder dir = sortState.getPropertySortOrder(property);
		String cssClass;
		if (dir == SortOrder.ASCENDING)
		{
			cssClass = cssSettings.getCssClass(SORT_ASCENDING_CSS_CLASS_KEY);
		}
		else if (dir == SortOrder.DESCENDING)
		{
			cssClass = cssSettings.getCssClass(SORT_DESCENDING_CSS_CLASS_KEY);
		}
		else
		{
			cssClass = cssSettings.getCssClass(SORT_NONE_CSS_CLASS_KEY);
		}

		if (!Strings.isEmpty(cssClass))
		{
			tag.append("class", cssClass, " ");
		}

	}
}
