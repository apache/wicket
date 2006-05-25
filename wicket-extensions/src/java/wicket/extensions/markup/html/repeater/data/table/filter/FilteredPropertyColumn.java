/*
 * $Id: FilteredPropertyColumn.java 3399 2005-12-09 07:43:11 +0000 (Fri, 09 Dec
 * 2005) ivaynberg $ $Revision$ $Date: 2005-12-09 07:43:11 +0000 (Fri, 09
 * Dec 2005) $
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
package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import wicket.model.IModel;
import wicket.model.PropertyModel;

/**
 * Like {@link PropertyColumn} but with support for filters.
 * 
 * @see PropertyColumn
 * @see IFilteredColumn
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class FilteredPropertyColumn extends PropertyColumn implements IFilteredColumn
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param displayModel
	 *            model used to construct header text
	 * @param sortProperty
	 *            sort property this column represents, if null this column will
	 *            not be sortable
	 * @param propertyExpression
	 *            wicket property expression for the column, see
	 *            {@link PropertyModel} for details
	 */
	public FilteredPropertyColumn(IModel displayModel, String sortProperty,
			String propertyExpression)
	{
		super(displayModel, sortProperty, propertyExpression);
	}

	/**
	 * @param displayModel
	 *            model used to construct header text
	 * @param propertyExpression
	 *            wicket property expression for the column, see
	 *            {@link PropertyModel} for details
	 */
	public FilteredPropertyColumn(IModel displayModel, String propertyExpression)
	{
		super(displayModel, propertyExpression);
	}

}
