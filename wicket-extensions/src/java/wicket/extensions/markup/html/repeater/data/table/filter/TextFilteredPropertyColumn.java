/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 00:46:21 +0200 (vr, 26 mei 2006) $
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
package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.model.IModel;
import wicket.model.PropertyModel;

/**
 * A filtered property column that creates a textfield filter component. The
 * default model of the created textfield is a property model with the same
 * property expression as the one used to display data. This works well when the
 * filter state object is of the same type as the objects in the data table.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class TextFilteredPropertyColumn extends FilteredPropertyColumn
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param displayModel
	 * @param sortProperty
	 * @param propertyExpression
	 */
	public TextFilteredPropertyColumn(IModel displayModel, String sortProperty,
			String propertyExpression)
	{
		super(displayModel, sortProperty, propertyExpression);
	}

	/**
	 * @param displayModel
	 * @param propertyExpression
	 */
	public TextFilteredPropertyColumn(IModel displayModel, String propertyExpression)
	{
		super(displayModel, propertyExpression);
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.table.filter.IFilteredColumn#getFilter(MarkupContainer, java.lang.String,
	 *      wicket.extensions.markup.html.repeater.data.table.filter.FilterForm)
	 */
	public Component getFilter(MarkupContainer parent, String componentId, FilterForm form)
	{
		return new TextFilter(parent, componentId, getFilterModel(form), form);
	}

	/**
	 * Returns the model that will be passed on to the text filter. Users can
	 * override this method to change the model.
	 * 
	 * @param form
	 *            filter form
	 * @return model passed on to the text filter
	 */
	protected IModel getFilterModel(FilterForm form)
	{
		return new PropertyModel(form.getModel(), getPropertyExpression());
	}


}
