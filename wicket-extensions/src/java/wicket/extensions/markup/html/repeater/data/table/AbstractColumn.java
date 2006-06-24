/*
 * $Id: AbstractColumn.java 5860 2006-05-25 20:29:28 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu, 25 May
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

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.model.IModel;

/**
 * A helper implementation for the IColumn interface
 * 
 * @author Igor Vaynberg ( ivaynberg )
 */
public abstract class AbstractColumn<T> implements IColumn<T>
{
	private IModel displayModel;
	private String sortProperty;

	/**
	 * @param displayModel
	 *            model used to generate header text
	 * @param sortProperty
	 *            sort property this column represents
	 */
	public AbstractColumn(IModel<String> displayModel, String sortProperty)
	{
		this.displayModel = displayModel;
		this.sortProperty = sortProperty;
	}

	/**
	 * @param displayModel
	 *            model used to generate header text
	 */
	public AbstractColumn(IModel<String> displayModel)
	{
		this(displayModel, null);
	}

	/**
	 * @return returns display model to be used for the header component
	 */
	public IModel<String> getDisplayModel()
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
	 * @see wicket.extensions.markup.html.repeater.data.table.IColumn#getHeader(MarkupContainer, java.lang.String)
	 */
	public Component<String> getHeader(MarkupContainer parent, String componentId)
	{
		return (Component) new Label(parent, componentId, getDisplayModel());
	}


}
