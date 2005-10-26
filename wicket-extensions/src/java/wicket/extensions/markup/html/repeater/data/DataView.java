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
package wicket.extensions.markup.html.repeater.data;

import wicket.model.IModel;

/**
 * DataView is a pageable repeating view that uses the specified implementation
 * of IDataProvider to populate itself.
 * 
 * @author igor
 * 
 */
public abstract class DataView extends AbstractDataView
{

	/**
	 * @param id
	 *            component id
	 * @param dataProvider
	 *            data provider
	 */
	public DataView(String id, IDataProvider dataProvider)
	{
		super(id, dataProvider);
	}

	/**
	 * @param id
	 *            component id
	 * @param model
	 *            component model - model object must be instance of
	 *            IDataProvider
	 */
	public DataView(String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * @param id
	 *            component id
	 */
	public DataView(String id)
	{
		super(id);
	}

	/**
	 * Sets the number of items to be displayed per page
	 * 
	 * @param items
	 *            number of items to display per page
	 */
	public void setItemsPerPage(int items)
	{
		internalSetItemsPerPage(items);
	}

	/**
	 * @return number of items displayed per page
	 */
	public int getItemsPerPage()
	{
		return internalGetItemsPerPage();
	}

}
