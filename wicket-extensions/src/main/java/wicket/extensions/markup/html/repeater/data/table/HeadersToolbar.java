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
package wicket.extensions.markup.html.repeater.data.table;

import java.util.Iterator;

import wicket.MarkupContainer;
import wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.extensions.markup.html.repeater.refreshing.Item;
import wicket.extensions.markup.html.repeater.refreshing.RefreshingView;
import wicket.extensions.markup.html.repeater.util.ArrayIteratorAdapter;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Toolbars that displays column headers. If the column is sortable a sortable
 * header will be displayed.
 * 
 * @see DefaultDataTable
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class HeadersToolbar extends AbstractToolbar
{
	private static final long serialVersionUID = 1L;


	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent component
	 * @param id
	 *            component id
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 * @param stateLocator
	 *            locator for the ISortState implementation used by sortable
	 *            headers
	 */
	public HeadersToolbar(MarkupContainer parent, final String id, final DataTable table,
			final ISortStateLocator stateLocator)
	{
		super(parent, id, table);

		new RefreshingView(this, "headers")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Iterator getItemModels()
			{
				return new ArrayIteratorAdapter(table.getColumns())
				{

					@Override
					protected IModel model(Object object, int index)
					{
						return new Model((IColumn)object);
					}

				};
			}

			@Override
			protected void populateItem(Item item)
			{
				item.setRenderBodyOnly(true);


				IColumn column = (IColumn)item.getModelObject();
				WebMarkupContainer header = null;
				if (column.isSortable())
				{
					header = newSortableHeader(item, "header", column.getSortProperty(),
							stateLocator);
				}
				else
				{
					header = new WebMarkupContainer(item, "header");
				}

				//TODO General: ivaynberg: rename ICOlumn.getHeader() to newHeader()
				column.getHeader(header, "label");

			}

		};

	}

	/**
	 * Factory method for sortable header components. A sortable header
	 * component must have id of <code>headerId</code> and conform to markup
	 * specified in <code>HeadersToolbar.html</code>
	 * 
	 * @param parent
	 *            parent container
	 * @param headerId
	 *            header component id
	 * @param property
	 *            propert this header represents
	 * @param locator
	 *            sort state locator
	 * @return created header component
	 */
	protected WebMarkupContainer newSortableHeader(WebMarkupContainer parent, String headerId,
			String property, ISortStateLocator locator)
	{
		return new OrderByBorder(parent, headerId, property, locator)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSortChanged()
			{
				getTable().setCurrentPage(0);
			}
		};

	}

}
