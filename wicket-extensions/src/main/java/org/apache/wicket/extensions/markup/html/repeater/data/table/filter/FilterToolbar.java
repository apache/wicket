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
package org.apache.wicket.extensions.markup.html.repeater.data.table.filter;

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;


/**
 * Toolbar that creates a form to hold form components used to filter data in the data table. Form
 * components are provided by columns that implement IFilteredColumn.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class FilterToolbar extends AbstractToolbar
{
	private static final String FILTER_ID = "filter";
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param table
	 *            data table this toolbar will be added to
	 * @param form
	 *            the filter form
	 * @param <T>
	 *            type of filter state object
	 * 
	 */
	public <T, S, F> FilterToolbar(final DataTable<T, S> table, final FilterForm<F> form)
	{
		super(table);

		Args.notNull(table, "table");
		
		IModel<List<IColumn<T, S>>> model = new AbstractReadOnlyModel<List<IColumn<T,S>>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public List<IColumn<T, S>> getObject() {
				List<IColumn<T, S>> columnsModels = new LinkedList<>();

				for (IColumn<T, S> column : table.getColumns())
				{
					columnsModels.add(column);
				}
				return columnsModels;
			}
		};
		

		// populate the toolbar with components provided by filtered columns
		ListView<IColumn<T, S>> filters = new ListView<IColumn<T, S>>("filters", model)
		{
			private static final long serialVersionUID = 1L;


			@Override
			protected void populateItem(ListItem<IColumn<T, S>> item)
			{
				final IColumn<T, S> col = item.getModelObject();
				item.setRenderBodyOnly(true);

				Component filter = null;

				if (col instanceof IFilteredColumn)
				{
					IFilteredColumn<T, S> filteredCol = (IFilteredColumn<T, S>)col;
					filter = filteredCol.getFilter(FILTER_ID, form);
				}

				if (filter == null)
				{
					filter = new NoFilter(FILTER_ID);
				}
				else
				{
					if (!filter.getId().equals(FILTER_ID))
					{
						throw new IllegalStateException(
							"filter component returned  with an invalid component id. invalid component id [" +
								filter.getId() +
								"] required component id [" +
								getId() +
								"] generating column [" + col.toString() + "] ");
					}
				}

				if (col instanceof IStyledColumn)
				{
					filter.add(new Behavior()
					{
						private static final long serialVersionUID = 1L;

						/**
						 * @see Behavior#onComponentTag(Component, ComponentTag)
						 */
						@Override
						public void onComponentTag(final Component component, final ComponentTag tag)
						{
							String className = ((IStyledColumn<?, S>)col).getCssClass();
							if (!Strings.isEmpty(className))
							{
								tag.append("class", className, " ");
							}
						}
					});
				}

				item.add(filter);
			}
		};
		filters.setReuseItems(true);

		add(filters);
	}

	@Override
	protected void onBeforeRender()
	{
		if (findParent(FilterForm.class) == null)
		{
			throw new IllegalStateException("FilterToolbar must be contained within a Form");
		}
		super.onBeforeRender();
	}

}
