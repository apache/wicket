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
package org.apache.wicket.extensions.markup.html.repeater.data.table;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.export.IExportableColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;


/**
 * A convenience implementation of column that adds a label to the cell whose model is determined by
 * the provided wicket property expression (same as used by {@link PropertyModel}) that is evaluated
 * against the current row's model object
 * <p>
 * Example
 * 
 * <pre>
 * columns[0] = new PropertyColumn(new Model&lt;String&gt;(&quot;First Name&quot;), &quot;name.first&quot;);
 * </pre>
 * 
 * The above will attach a label to the cell with a property model for the expression
 * &quot;name.first&quot;
 * 
 * @see PropertyModel
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * @param <T>
 *            The Model object type
 * @param <S>
 *            the type of the sort property
 */
public class PropertyColumn<T, S> extends AbstractColumn<T, S> implements IExportableColumn<T, S, Object>
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
	public PropertyColumn(final IModel<String> displayModel, final S sortProperty,
		final String propertyExpression)
	{
		super(displayModel, sortProperty);
		this.propertyExpression = propertyExpression;
	}

	/**
	 * Creates a non sortable property column
	 * 
	 * @param displayModel
	 *            display model
	 * @param propertyExpression
	 *            wicket property expression
	 * @see PropertyModel
	 */
	public PropertyColumn(final IModel<String> displayModel, final String propertyExpression)
	{
		super(displayModel, null);
		this.propertyExpression = propertyExpression;
	}

	/**
	 * Implementation of populateItem which adds a label to the cell whose model is the provided
	 * property expression evaluated against rowModelObject
	 * 
	 * @see ICellPopulator#populateItem(Item, String, IModel)
	 */
	@Override
	public void populateItem(final Item<ICellPopulator<T>> item, final String componentId,
		final IModel<T> rowModel)
	{
		item.add(new Label(componentId, createLabelModel(rowModel)));
	}

	/**
	 * Factory method for generating a model that will generated the displayed value. Typically the
	 * model is a property model using the {@link #propertyExpression} specified in the constructor.
	 * 
	 * @param rowModel
	 * @return model
	 * @deprecated
	 *	since 6.2.0, scheduled for removal in 7.0.0. Please use {@link #getDataModel(org.apache.wicket.model.IModel)}
	 *	instead.
	 */
	protected IModel<?> createLabelModel(final IModel<T> rowModel)
	{
		return getDataModel(rowModel);
	}

	/**
	 * @return wicket property expression
	 */
	public String getPropertyExpression()
	{
		return propertyExpression;
	}

	/**
	 * Factory method for generating a model that will generated the displayed value. Typically the
	 * model is a property model using the {@link #propertyExpression} specified in the constructor.
	 *
	 * @param rowModel
	 * @return model
	 */
	@Override
	public IModel<Object> getDataModel(IModel<T> rowModel)
	{
		PropertyModel<Object> propertyModel = new PropertyModel<Object>(rowModel, propertyExpression);
		return propertyModel;
	}
}
