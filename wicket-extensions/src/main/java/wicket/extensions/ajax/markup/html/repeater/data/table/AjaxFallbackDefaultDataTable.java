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
package wicket.extensions.ajax.markup.html.repeater.data.table;

import java.util.List;

import wicket.MarkupContainer;
import wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import wicket.extensions.markup.html.repeater.data.table.DataTable;
import wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import wicket.extensions.markup.html.repeater.data.table.IColumn;
import wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.repeater.Item;
import wicket.markup.repeater.OddEvenItem;
import wicket.model.IModel;

/**
 * Ajaxified implementation of the {@link DefaultDataTable}
 * 
 * @see DefaultDataTable
 * @author Igor Vaynberg ( ivaynberg )
 */
public class AjaxFallbackDefaultDataTable extends DataTable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            component id
	 * @param columns
	 *            list of columns
	 * @param dataProvider
	 *            data provider
	 * @param rowsPerPage
	 *            number of rows per page
	 */
	public AjaxFallbackDefaultDataTable(MarkupContainer parent, final String id, final List<IColumn> columns,
			SortableDataProvider dataProvider, int rowsPerPage)
	{
		this(parent, id, (IColumn[])columns.toArray(new IColumn[columns.size()]), dataProvider,
				rowsPerPage);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            component id
	 * @param columns
	 *            array of columns
	 * @param dataProvider
	 *            data provider
	 * @param rowsPerPage
	 *            number of rows per page
	 */
	public AjaxFallbackDefaultDataTable(MarkupContainer parent, final String id, final IColumn[] columns,
			final SortableDataProvider dataProvider, int rowsPerPage)
	{
		super(parent, id, columns, dataProvider, rowsPerPage);
		setVersioned(false);

		/*
		 * TODO General: ivaynberg: this is rediculous, would be better to have
		 * a onPopulateTopToolbars() callback
		 */

		addTopToolbar(new IToolbarFactory()
		{

			private static final long serialVersionUID = 1L;

			public AbstractToolbar newToolbar(WebMarkupContainer parent, String id, DataTable dataTable)
			{
				return new NavigationToolbar(parent, id, dataTable);
			}

		});

		addTopToolbar(new IToolbarFactory()
		{
			private static final long serialVersionUID = 1L;

			public AbstractToolbar newToolbar(WebMarkupContainer parent, String id, DataTable dataTable)
			{
				return new AjaxFallbackHeadersToolbar(parent, id, dataTable, dataProvider);
			}

		});

		addBottomToolbar(new IToolbarFactory()
		{
			private static final long serialVersionUID = 1L;

			public AbstractToolbar newToolbar(WebMarkupContainer parent, String id, DataTable dataTable)
			{
				return new NoRecordsToolbar(parent, id, dataTable);
			}

		});
	}

	@Override
	protected Item newRowItem(MarkupContainer parent, final String id, int index, IModel model)
	{
		return new OddEvenItem(parent, id, index, model);
	}

}
