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

import java.io.Serializable;

import wicket.markup.html.list.ListItem;
import wicket.model.IModel;

/**
 * An inteface that represents a column in the DataTable component
 * 
 * @author Igor Vaynberg ( ivaynberg )
 */
public interface IColumn extends Serializable
{
	/**
	 * Retrieves the model used to display the name of the header
	 * 
	 * @return display model
	 */
	IModel getDisplayModel();

	/**
	 * Method used to populate a cell in the generated table.
	 * 
	 * @param cellItem
	 *            the list item representing the current table cell being
	 *            rendered
	 * @param componentId
	 *            the id of the component used to render the cell (only one
	 *            component can be added to the cell)
	 * @param model
	 *            the object that represents the current row being processed
	 */
	void populateItem(final ListItem cellItem, final String componentId, final IModel model);

	/**
	 * Returns the name of the property that this header sorts. If null is
	 * returned the header will be unsortable.
	 * 
	 * @return a string representing the sort property
	 */
	String getSortProperty();

	/**
	 * Returns true if this header should be a sortable header
	 * 
	 * @return true if header should be sortable
	 */
	boolean isSortable();
}
