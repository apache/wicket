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

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
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
 *            The model object type
 * @param <Y>
 *            The column model object type
 * @param <S>
 *            the type of the sort property
 */
public class ChoiceFilteredPropertyColumn<T, Y, S> extends FilteredPropertyColumn<T, S>
{
	private static final long serialVersionUID = 1L;
	private final IModel<List<? extends Y>> filterChoices;

	/**
	 * @param displayModel
	 * @param sortProperty
	 * @param propertyExpression
	 * @param filterChoices
	 *            collection choices used in the choice filter
	 */
	public ChoiceFilteredPropertyColumn(final IModel<String> displayModel,
		final S sortProperty, final String propertyExpression,
		final IModel<List<? extends Y>> filterChoices)
	{
		super(displayModel, sortProperty, propertyExpression);
		this.filterChoices = filterChoices;
	}

	/**
	 * @param displayModel
	 * @param propertyExpression
	 * @param filterChoices
	 *            collection of choices used in the choice filter
	 */
	public ChoiceFilteredPropertyColumn(final IModel<String> displayModel,
		final String propertyExpression, final IModel<List<? extends Y>> filterChoices)
	{
		super(displayModel, propertyExpression);
		this.filterChoices = filterChoices;
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	@Override
	public void detach()
	{
		super.detach();
		if (filterChoices != null)
		{
			filterChoices.detach();
		}
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilteredColumn#getFilter(java.lang.String,
	 *      org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm)
	 */
	@Override
	public Component getFilter(final String componentId, final FilterForm<?> form)
	{
		ChoiceFilter<Y> filter = new ChoiceFilter<Y>(componentId, getFilterModel(form), form,
			filterChoices, enableAutoSubmit());

		IChoiceRenderer<Y> renderer = getChoiceRenderer();
		if (renderer != null)
		{
			filter.getChoice().setChoiceRenderer(renderer);
		}
		return filter;
	}

	/**
	 * Returns the model that will be passed on to the text filter. Users can override this method
	 * to change the model.
	 * 
	 * @param form
	 *            filter form
	 * @return model passed on to the text filter
	 */
	protected IModel<Y> getFilterModel(final FilterForm<?> form)
	{
		return new PropertyModel<Y>(form.getDefaultModel(), getPropertyExpression());
	}

	/**
	 * Returns true if the constructed choice filter should autosubmit the form when its value is
	 * changed.
	 * 
	 * @return true to make choice filter autosubmit, false otherwise
	 */
	protected boolean enableAutoSubmit()
	{
		return true;
	}

	/**
	 * Returns choice renderer that will be used to create the choice filter
	 * 
	 * @return choice renderer that will be used to create the choice filter
	 */
	protected IChoiceRenderer<Y> getChoiceRenderer()
	{
		return null;
	}

	/**
	 * @return filter choices model
	 */
	protected final IModel<List<? extends Y>> getFilterChoices()
	{
		return filterChoices;
	}


}
