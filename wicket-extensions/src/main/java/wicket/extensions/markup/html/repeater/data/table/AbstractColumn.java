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

import wicket.Component;
import wicket.markup.html.basic.Label;
import wicket.model.IModel;

/**
 * A helper implementation for the IColumn interface
 * 
 * @author Igor Vaynberg ( ivaynberg )
 */
public abstract class AbstractColumn implements IColumn
{
	private IModel displayModel;
	private String sortProperty;

	/**
	 * @param displayModel
	 *            model used to generate header text
	 * @param sortProperty
	 *            sort property this column represents
	 */
	public AbstractColumn(IModel displayModel, String sortProperty)
	{
		this.displayModel = displayModel;
		this.sortProperty = sortProperty;
	}

	/**
	 * @param displayModel
	 *            model used to generate header text
	 */
	public AbstractColumn(IModel displayModel)
	{
		this(displayModel, null);
	}

	/**
	 * @return returns display model to be used for the header component
	 */
	public IModel getDisplayModel()
	{
		return displayModel;
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.table.IColumn#getSortProperty()
	 */
	public String getSortProperty()
	{
		return sortProperty;
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.table.IColumn#isSortable()
	 */
	public boolean isSortable()
	{
		return sortProperty != null;
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.table.IColumn#getHeader(java.lang.String)
	 */
	public Component getHeader(String componentId)
	{
		return new Label(componentId, getDisplayModel());
	}


}
