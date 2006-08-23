/*
 * $Id: DefaultDataTable.java 5840 2006-05-24 13:49:09 -0700 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-24 13:49:09 -0700 (Wed, 24 May
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

import wicket.MarkupContainer;
import wicket.extensions.markup.html.repeater.refreshing.Item;
import wicket.extensions.markup.html.repeater.refreshing.OddEvenItem;
import wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;

/**
 * An implementation of the DataTable that aims to solve the 90% usecase by
 * adding navigation, headers, an no-records-found toolbars to a standard
 * {@link DataTable}.
 * <p>
 * The {@link NavigationToolbar} and the {@link HeadersToolbar} are added as top
 * toolbars, while the {@link NoRecordsToolbar} toolbar is added as a bottom
 * toolbar. *
 * 
 * @param <T>
 *            type of model object this item holds
 * @see DataTable
 * @see HeadersToolbar
 * @see NavigationToolbar
 * @see NoRecordsToolbar
 * 
 * @author Igor Vaynberg ( ivaynberg )
 */
public class DefaultDataTable<T> extends DataTable<T>
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
	public DefaultDataTable(MarkupContainer parent, final String id, final List<IColumn<T>> columns,
			SortableDataProvider<T> dataProvider, int rowsPerPage)
	{
		this(parent, id, (IColumn<T>[])columns.toArray(new IColumn[columns.size()]), dataProvider,
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
	public DefaultDataTable(MarkupContainer parent, final String id, final IColumn<T>[] columns,
			final SortableDataProvider<T> dataProvider, int rowsPerPage)
	{
		super(parent, id, columns, dataProvider, rowsPerPage);

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
				return new HeadersToolbar(parent, id, dataTable, dataProvider);
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
	protected Item<T> newRowItem(MarkupContainer parent, final String id, int index, IModel<T> model)
	{
		return new OddEvenItem<T>(parent, id, index, model);
	}

}
