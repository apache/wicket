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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.table;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;


/**
 * Toolbar that displays (Ajax) links used to navigate the pages of the datatable as well as a
 * message about which rows are being displayed and their total number in the data table.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst (dashorst)
 * @since 1.2.1
 */
public class AjaxNavigationToolbar extends NavigationToolbar
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 */
	public AjaxNavigationToolbar(final DataTable<?, ?> table)
	{
		super(table);
	}

	/**
	 * Factory method used to create the paging navigator that will be used by the datatable.
	 * 
	 * @param navigatorId
	 *            component id the navigator should be created with
	 * @param table
	 *            dataview used by datatable
	 * @return paging navigator that will be used to navigate the data table
	 */
	@Override
	protected PagingNavigator newPagingNavigator(final String navigatorId, final DataTable<?, ?> table)
	{
		return new AjaxPagingNavigator(navigatorId, table)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * Implement our own ajax event handling in order to update the datatable itself, as the
			 * default implementation doesn't support DataViews.
			 * 
			 * @see AjaxPagingNavigator#onAjaxEvent(org.apache.wicket.ajax.AjaxRequestTarget)
			 */
			@Override
			protected void onAjaxEvent(final AjaxRequestTarget target)
			{
				target.add(table);
			}
		};
	}
}
