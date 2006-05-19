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

import wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import wicket.extensions.markup.html.repeater.refreshing.Item;
import wicket.markup.html.basic.Label;
import wicket.model.IModel;
import wicket.model.PropertyModel;

/**
 * A convinience implementation of column that adds a label to the cell whose
 * model is determined by the provided wicket property expression (same as used
 * by {@link PropertyModel}) that is evaluated against the current row's model
 * object
 * <p>
 * Example
 * 
 * <pre>
 * columns[0] = new PropertyColumn(new Model(&quot;First Name&quot;), &quot;name.first&quot;);
 * </pre>
 * 
 * The above will attach a label to the cell with a property model for the
 * expression &quot;name.first&quot;
 * 
 * @see PropertyModel
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * 
 */
public class PropertyColumn extends AbstractColumn
{
	private static final long serialVersionUID = 1L;

	private final String propertyExpression;

	/**
	 * Creates a property column that is also sortable
	 * 
	 * @param displayModel
	 *            display model
	 * @param sortProperty
	 *            sort property
	 * @param propertyExpression
	 *            wicket property expression used by PropertyModel
	 */
	public PropertyColumn(IModel displayModel, String sortProperty, String propertyExpression)
	{
		super(displayModel, sortProperty);
		this.propertyExpression = propertyExpression;
	}

	/**
	 * Creates a non sortable property column
	 * 
	 * @param displayModel
	 *            display model
	 * @param propertyExpressions
	 *            wicket property expression
	 * @see PropertyModel
	 */
	public PropertyColumn(IModel displayModel, String propertyExpressions)
	{
		super(displayModel, null);
		this.propertyExpression = propertyExpressions;
	}

	/**
	 * Implementation of populateItem which adds a label to the cell whose model
	 * is the provided property expression evaluated agains rowModelObject
	 * 
	 * @see ICellPopulator#populateItem(Item, String, IModel)
	 */
	public void populateItem(Item item, String componentId, IModel model)
	{
		item.add(new Label(componentId, createLabelModel(model)));
	}

	//TODO Post 1.3: rename embeddedModel to itemModel
	protected IModel createLabelModel(IModel embeddedModel)
	{
		return new PropertyModel(embeddedModel, propertyExpression);
	}

	/**
	 * @return wicket property expression
	 */
	public String getPropertyExpression()
	{
		return propertyExpression;
	}

}
