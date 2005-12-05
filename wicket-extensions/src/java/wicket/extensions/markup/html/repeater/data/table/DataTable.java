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

import wicket.extensions.markup.html.repeater.data.sort.SortableDataProvider;

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
	public DataTable(String id, final List columns, SortableDataProvider dataProvider,
			int rowsPerPage)
	{
		super(id, columns, dataProvider, rowsPerPage);
		
		addTopToolbar(new NavigationToolbar(this));
		addTopToolbar(new HeadersToolbar(this, dataProvider));
	}


}
