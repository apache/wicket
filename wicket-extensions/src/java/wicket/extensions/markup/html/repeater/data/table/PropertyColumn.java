/*
 * $Id$ $Revision:
 * 1.2 $ $Date$
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

import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.model.IModel;
import wicket.model.PropertyModel;

/**
 * A convinience implementation of column that adds a label to the cell whose
 * model is determined by the provided OGNL property expression that is
 * evaluated agains the current row model.
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * 
 */
public class PropertyColumn extends AbstractColumn
{
	private static final long serialVersionUID = 1L;

	private final String ognlExpression;

	/**
	 * Creates a property column that is also sortable
	 * 
	 * @param displayModel
	 *            display model
	 * @param sortProperty
	 *            sort property
	 * @param ognlExpression
	 *            OGNL property expression
	 */
	public PropertyColumn(IModel displayModel, String sortProperty, String ognlExpression)
	{
		super(displayModel, sortProperty);
		this.ognlExpression = ognlExpression;
	}

	/**
	 * Creates a non sortable property column
	 * 
	 * @param displayModel
	 *            display model
	 * @param ognlExpression
	 *            OGNL property expression
	 */
	public PropertyColumn(IModel displayModel, String ognlExpression)
	{
		super(displayModel, null);
		this.ognlExpression = ognlExpression;
	}

	/**
	 * Implementation of populateItem which adds a label to the cell whose model
	 * is the provided OGNL property expression evaluated agains rowModelObject
	 * 
	 * @see IColumn#populateItem(ListItem, String, IModel)
	 */
	public void populateItem(ListItem item, String componentId, IModel model)
	{
		item.add(new Label(componentId, createLabelModel(model)));
	}

	protected IModel createLabelModel(IModel embeddedModel)
	{
		return new PropertyModel(embeddedModel, ognlExpression);
	}

	public String getOgnlExpression()
	{
		return ognlExpression;
	}

}
