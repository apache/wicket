/*
 * $Id$ $Revision$
 * $Date$
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

import wicket.extensions.markup.html.repeater.data.DataView;
import wicket.extensions.markup.html.repeater.data.sort.ISortableDataProvider;
import wicket.markup.html.WebComponent;
import wicket.markup.html.navigation.paging.PagingNavigator;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Provides a default implementation of AbstractDataTable with a paging
 * navigator and a paging navigator label
 * 
 * @see AbstractDataTable
 * 
 * @author Igor Vaynberg ( ivaynberg )
 */
public class DataTable extends AbstractDataTable
{
	private static final long serialVersionUID = 1L;

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
	public DataTable(String id, final List columns, ISortableDataProvider dataProvider,
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
	public DataTable(String id, final List columns, IModel dataProvider, int rowsPerPage)
	{
		super(id, columns, dataProvider, rowsPerPage);

		add(newPagingNavigator("navigator", getDataView()));

		add(newNavigatorLabel("navigatorLabel", getDataView()));
	}


	/**
	 * Factory method used to create the paging navigator that will be used by
	 * the datatable
	 * 
	 * @param navigatorId
	 *            component id the navigator should be created with
	 * @param dataView
	 *            dataview used by datatable
	 * @return paging navigator that will be used by the datatable
	 */
	protected PagingNavigator newPagingNavigator(String navigatorId, final DataView dataView)
	{
		return new PagingNavigator(navigatorId, dataView)
		{
			private static final long serialVersionUID = 1L;

			public boolean isVisible()
			{
				return dataView.getItemCount() > 0;
			}
		};
	}

	/**
	 * Factory method used to create the navigator label that will be used by
	 * the datatable
	 * 
	 * @param navigatorId
	 *            component id navigator label should be created with
	 * @param dataView
	 *            dataview used by datatable
	 * @return navigator label that will be used by the datatable
	 * 
	 */
	protected WebComponent newNavigatorLabel(String navigatorId, final DataView dataView)
	{
		return new NavigatorLabel(navigatorId, dataView);
	}
}
