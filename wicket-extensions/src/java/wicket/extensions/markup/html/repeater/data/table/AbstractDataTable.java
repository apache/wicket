/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.markup.html.repeater.data.table;

import java.util.List;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.extensions.markup.html.repeater.data.DataView;
import wicket.extensions.markup.html.repeater.data.sort.ISortableDataProvider;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.extensions.markup.html.repeater.pageable.Item;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Base class for components that wish to display a table of data by specifying
 * a list of columns pragmatically rather then through html.
 * 
 * Provides useful features such as sortable header and even/odd css row classes
 * 
 * Example of minimum markup needed by subclasses:
 * 
 * 
 * <pre>
 *    &lt;thead&gt;
 *        &lt;tr&gt;
 *            &lt;span wicket:id=&quot;headers&quot;&gt;
 *                &lt;th wicket:id=&quot;header&quot;&gt;
 *                    &lt;span wicket:id=&quot;label&quot;&gt;[header-label]&lt;/span&gt;
 *                &lt;/th&gt;
 *            &lt;/span&gt;
 *        &lt;/tr&gt;
 *    &lt;/thead&gt;
 *    &lt;tbody&gt;
 *        &lt;tr wicket:id=&quot;rows&quot;&gt;
 *            &lt;td wicket:id=&quot;cells&quot;&gt;
 *                &lt;span wicket:id=&quot;cell&quot;&gt;[cell]&lt;/span&gt;
 *            &lt;/td&gt;
 *        &lt;/tr&gt;
 *    &lt;/tbody&gt;
 * </pre>
 * 
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class AbstractDataTable extends Panel
{
	private static final long serialVersionUID = 1L;

	private static final String CELL_ITEM_ID = "cell";

	private final DataView dataView;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param columns
	 *            list of IColumn objects
	 * @param dataProvider
	 *            data provider
	 * @param rowsPerPage
	 *            number of rows per page
	 */
	public AbstractDataTable(String id, final List columns, ISortableDataProvider dataProvider,
			int rowsPerPage)
	{
		this(id, columns, new Model(dataProvider), rowsPerPage);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param columns
	 *            list of IColumn objects
	 * @param dataProvider
	 *            imodel for data provider
	 * @param rowsPerPage
	 *            number of rows per page
	 */
	public AbstractDataTable(String id, final List columns, IModel dataProvider, int rowsPerPage)
	{
		super(id);

		dataView = new DataView("rows", dataProvider)
		{
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item item)
			{
				final IModel rowModel = item.getModel();

				item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel()
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
						column.populateItem(item, CELL_ITEM_ID, rowModel);

						if (item.get("cell") == null)
						{
							throw new WicketRuntimeException(column.getClass().getName()
									+ ".populateItem() failed to add a component with id ["
									+ CELL_ITEM_ID + "] to the provided [cellItem] argument");
						}

						item.get(CELL_ITEM_ID).setRenderBodyOnly(true);
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

	}

	protected final DataView getDataView()
	{
		return dataView;
	}
}
