/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.extensions.markup.html.repeater.data.table.filter;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * A filtered property column that creates a textfield filter component. The default model of the
 * created textfield is a property model with the same property expression as the one used to
 * display data. This works well when the filter state object is of the same type as the objects in
 * the data table.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            The column's model object type
 * @param <F>
 *            Filter's model object type
 * @param <S>
 *            the type of the sort property
 * 
 */
public class TextFilteredPropertyColumn<T, F, S> extends FilteredPropertyColumn<T, S>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param displayModel
	 * @param sortProperty
	 * @param propertyExpression
	 */
	public TextFilteredPropertyColumn(final IModel<String> displayModel, final S sortProperty,
		final String propertyExpression)
	{
		super(displayModel, sortProperty, propertyExpression);
	}

	/**
	 * @param displayModel
	 * @param propertyExpression
	 */
	public TextFilteredPropertyColumn(final IModel<String> displayModel,
		final String propertyExpression)
	{
		super(displayModel, propertyExpression);
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilteredColumn#getFilter(java.lang.String,
	 *      org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm)
	 */
	public Component getFilter(final String componentId, final FilterForm<?> form)
	{
		return new TextFilter<F>(componentId, getFilterModel(form), form);
	}

	/**
	 * Returns the model that will be passed on to the text filter. Users can override this method
	 * to change the model.
	 * 
	 * @param form
	 *            filter form
	 * @return model passed on to the text filter
	 */
	protected IModel<F> getFilterModel(final FilterForm<?> form)
	{
		return new PropertyModel<F>(form.getDefaultModel(), getPropertyExpression());
	}


}
