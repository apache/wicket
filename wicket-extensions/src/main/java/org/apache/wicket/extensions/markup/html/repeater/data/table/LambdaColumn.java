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
import org.apache.wicket.lambda.WicketFunction;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

/**
 * An implementation of column that adds a label to the cell whose model is determined by
 * the provided {@link WicketFunction} that is evaluated against the current row's model object
 * <p>
 * Example
 * 
 * <pre>
 * columns[0] = new LambdaColumn(new Model&lt;String&gt;(&quot;First Name&quot;), this::getFirstName);
 * </pre>
 * 
 * The above will attach a label to the cell which calls {@code #getFirstName()} for each displayed
 * row.
 * 
 * @param <T>
 *            The Model object type
 * @param <S>
 *            the type of the sort property
 */
public class LambdaColumn<T, S> extends AbstractColumn<T, S> implements IExportableColumn<T, S>
{
	private static final long serialVersionUID = 1L;
	
	private WicketFunction<T, ?> function;

	/**
	 * Creates a column that is not sortable.
	 * 
	 * @param displayModel
	 *            display model
	 * @param function
	 *            Wicket function to be applied to each row
	 */
	public LambdaColumn(final IModel<String> displayModel, final WicketFunction<T, ?> function)
	{
		this(displayModel, null, function);
	}
	
	/**
	 * Creates a property column that is also sortable.
	 * 
	 * @param displayModel
	 *            display model
	 * @param sortProperty
	 *            sort property
	 * @param function
	 *            Wicket function to be applied to each row
	 */
	public LambdaColumn(final IModel<String> displayModel, final S sortProperty, final WicketFunction<T, ?> function)
	{
		super(displayModel, sortProperty);
		
		this.function = function;
	}
	
	@Override
	public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel)
	{
		item.add(new Label(componentId, getDataModel(rowModel)));
	}
	
	/**
	 * Factory method for generating a model that will generated the displayed value.
	 *
	 * @param rowModel
	 * @return model
	 */
	@Override
	public IModel<?> getDataModel(IModel<T> rowModel)
	{
		IModel<Object> dataModel = new IModel<Object>()
		{
			@Override
			public Object getObject()
			{
				T before = rowModel.getObject();
				
				if (before == null) {
					return null;
				} else {
					return function.apply(before);
				}
			}
			
			@Override
			public void detach()
			{
				rowModel.detach();
			}
		};
		return dataModel;
	}
}