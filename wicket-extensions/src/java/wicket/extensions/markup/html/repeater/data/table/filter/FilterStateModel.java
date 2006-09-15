/*
 * $Id: FilterStateModel.java 3399 2005-12-09 07:43:11 +0000 (Fri, 09 Dec 2005)
 * ivaynberg $ $Revision$ $Date: 2005-12-09 07:43:11 +0000 (Fri, 09 Dec
 * 2005) $
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

import wicket.model.AbstractModel;
import wicket.model.IModel;

/**
 * Model that wraps filter state locator to make its use transparent to wicket
 * components.
 * <p>
 * Example:
 * 
 * <pre>
 * IFilterStateLocator locator = getLocator();
 * TextField tf = new TextField(&quot;tf&quot;, new FilterStateModel(locator));
 * </pre>
 * 
 * Text field tf will now user the object that filter state locator locates as
 * its underlying model.
 * </p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
class FilterStateModel extends AbstractModel
{
	private static final long serialVersionUID = 1L;

	private IFilterStateLocator locator;

	/**
	 * Constructor
	 * 
	 * @param locator
	 *            IFilterStateLocator implementation used to provide model
	 *            object for this model
	 */
	public FilterStateModel(IFilterStateLocator locator)
	{
		this.locator = locator;
	}

	/**
	 * @see wicket.model.IModel#getObject()
	 */
	public Object getObject()
	{
		return locator.getFilterState();
	}

	/**
	 * @see wicket.model.IModel#setObject(java.lang.Object)
	 */
	public void setObject(Object object)
	{
		locator.setFilterState(object);
	}

}
