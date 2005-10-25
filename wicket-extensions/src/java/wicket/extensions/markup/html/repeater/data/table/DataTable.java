/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.repeater.data.table;

import java.util.List;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.extensions.markup.html.repeater.data.DataView;
import wicket.extensions.markup.html.repeater.data.sort.ISortableDataProvider;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.extensions.markup.html.repeater.pageable.Item;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * 
 */
public class DataTable extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 *            component id
	 * @param columns
	 *            list of IColumn objects
	 * @param dataProvider
	 *            data provider
	 * @param rowsPerPage
	 *            number of rows per page
	 */
	public DataTable(String id, final List columns, ISortableDataProvider dataProvider,
			int rowsPerPage)
	{
		super(id);

		final DataView dataView = new DataView("rows", dataProvider)
		{
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item item)
			{
				final IModel rowModel = item.getModel();

				// TODO change model to abstractreadonlymodel if patch applied
				item.add(new AttributeModifier("class", true, new Model()
				{
					private static final long serialVersionUID = 1L;

					public Object getObject(Component component)
					{
						return (item.getIndex() % 2 == 0) ? "odd" : "even";
					}

				}));

				item.add(new ListView("cells", columns)
				{
					private static final long serialVersionUID = 1L;

					protected void populateItem(ListItem item)
					{
						final IColumn column = (IColumn)item.getModelObject();
						column.populateItem(item, "cell", rowModel);
						item.get("cell").setRenderBodyOnly(true);
					}

				});

			}

		};

		dataView.setItemsPerPage(rowsPerPage);

		add(dataView);


		add(new ListView("headers", columns)
		{
			private static final long serialVersionUID = 1L;

			protected void populateItem(ListItem item)
			{
				final IColumn column = (IColumn)item.getModelObject();
				WebMarkupContainer header = null;
				if (column.isSortable())
				{
					header = new OrderByBorder("header", column.getSortProperty(), dataView);
				}
				else
				{
					header = new WebMarkupContainer("header");
				}
				item.add(header);
				header.add(new Label("label", column.getDisplayModel()));
			}

		});

		add(new PagingNavigator("navigator", dataView)
		{
			private static final long serialVersionUID = 1L;

			public boolean isVisible()
			{
				return dataView.getItemCount() > 0;
			}
		});

		add(new NavigatorLabel("navigatorLabel", dataView));
	}


}
