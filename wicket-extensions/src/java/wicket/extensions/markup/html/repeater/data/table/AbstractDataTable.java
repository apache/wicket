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

import java.util.Collections;
import java.util.List;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.extensions.markup.html.repeater.OrderedRepeatingView;
import wicket.extensions.markup.html.repeater.data.DataView;
import wicket.extensions.markup.html.repeater.data.IDataProvider;
import wicket.extensions.markup.html.repeater.refreshing.Item;
import wicket.markup.html.WebMarkupContainer;
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
 *       &lt;thead&gt;
 *           &lt;tr&gt;
 *               &lt;span wicket:id=&quot;headers&quot;&gt;
 *                   &lt;th wicket:id=&quot;header&quot;&gt;
 *                       &lt;span wicket:id=&quot;label&quot;&gt;[header-label]&lt;/span&gt;
 *                   &lt;/th&gt;
 *               &lt;/span&gt;
 *           &lt;/tr&gt;
 *       &lt;/thead&gt;
 *       &lt;tbody&gt;
 *           &lt;tr wicket:id=&quot;rows&quot;&gt;
 *               &lt;td wicket:id=&quot;cells&quot;&gt;
 *                   &lt;span wicket:id=&quot;cell&quot;&gt;[cell]&lt;/span&gt;
 *               &lt;/td&gt;
 *           &lt;/tr&gt;
 *       &lt;/tbody&gt;
 * </pre>
 * 
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class AbstractDataTable extends Panel
{
	/**
	 * The component id that toolbars must be created with in order to be added
	 * to the data table
	 */
	public static final String TOOLBAR_COMPONENT_ID = "toolbar";

	private static final long serialVersionUID = 1L;

	private static final String CELL_ITEM_ID = "cell";

	private final DataView dataView;

	private List/* <IColumn> */columns;

	private final OrderedRepeatingView topToolbars;
	private final OrderedRepeatingView bottomToolbars;
	
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
	public AbstractDataTable(String id, final List columns, IDataProvider dataProvider,
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
	public AbstractDataTable(String id, List columns, IModel dataProvider, int rowsPerPage)
	{
		super(id);

		this.columns = Collections.unmodifiableList(columns);

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

				item.add(new ListView("cells", getColumns())
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

		topToolbars = new OrderedRepeatingView("topToolbars") {
			private static final long serialVersionUID = 1L;
			
			public boolean isVisible()
			{
				return size()>0;
			}
			
		};
		
		bottomToolbars=new OrderedRepeatingView("bottomToolbars") {

			private static final long serialVersionUID = 1L;
			
			public boolean isVisible()
			{
				return size()>0;
			}
		};

		add(topToolbars);
		add(bottomToolbars);
	}

	protected final DataView getDataView()
	{
		return dataView;
	}

	public final List/* <IColumn> */getColumns()
	{
		return columns;
	}

	/**
	 * Adds a toolbar to the datatable that will be displayed before the data
	 * 
	 * @param toolbar
	 *            toolbar to be added
	 * 
	 * @see Toolbar
	 */
	public void addTopToolbar(Toolbar toolbar)
	{
		addToolbar(toolbar, topToolbars);
	}

	/**
	 * Adds a toolbar to the datatable that will be displayed after the data
	 * 
	 * @param toolbar
	 *            toolbar to be added
	 * 
	 * @see Toolbar
	 */
	public void addBottomToolbar(Toolbar toolbar)
	{
		addToolbar(toolbar, bottomToolbars);
	}

	private void addToolbar(Toolbar toolbar, OrderedRepeatingView container) {
		if (toolbar==null) {
			throw new IllegalArgumentException("argument [toolbar] cannot be null");
		}
		
		if (!toolbar.getId().equals(TOOLBAR_COMPONENT_ID))
		{
			throw new IllegalArgumentException(
					"Toolbar must have component id equal to AbstractDataTable.TOOLBAR_COMPONENT_ID");
		}
		
		// create a container item for the toolbar (required by repeating view)
		WebMarkupContainer item = new WebMarkupContainer(container.newChildId());
		item.setRenderBodyOnly(true);
		item.add(toolbar);
		
		container.add(item);

		
	}
	
	
	/**
	 * Sets the current page
	 * 
	 * @param page
	 */
	public final void setCurrentPage(int page)
	{
		getDataView().setCurrentPage(page);
	}
}
