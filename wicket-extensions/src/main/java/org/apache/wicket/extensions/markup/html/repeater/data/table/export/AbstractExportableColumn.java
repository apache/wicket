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
package org.apache.wicket.extensions.markup.html.repeater.data.table.export;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

/**
 * A helper implementation of {@link IExportableColumn}. This implementation requires you to only
 * implement {@link #getDataModel(org.apache.wicket.model.IModel)}.
 * @author Jesse Long
 * @param <T> The type of each row in the table.
 * @param <S> The type of the sort property of the table.
 * @param <D> The type of the data displayed in this column.
 */
public abstract class AbstractExportableColumn<T, S, D>
        extends AbstractColumn<T, S>
        implements IExportableColumn<T, S, D>
{
	/**
	 * Creates a new {@link AbstractExportableColumn} with the provided display model, and without a sort property.
	 * @param displayModel The {@link IModel} of the text to be used in the column header.
	 */
	public AbstractExportableColumn(IModel<String> displayModel)
	{
		super(displayModel);
	}

	/**
	 * Creates a new {@link AbstractExportableColumn} with the provided display model, and sort property.
	 * @param displayModel The {@link IModel} of the text to be used in the column header.
	 * @param sortProperty The sort property used by this column.
	 */
	public AbstractExportableColumn(IModel<String> displayModel, S sortProperty)
	{
		super(displayModel, sortProperty);
	}

	/**
	 * Creates a {@link Component} which will be used to display the content of the column in this row.
	 * The default implementation simply creates a label with the data model provided.
	 * @param componentId
	 *	    The component id of the display component.
	 * @param dataModel
	 *	    The model of the data for this column in the row. This should usually be passed as the model
	 *	    of the display component.
	 * @return a {@link Component} which will be used to display the content of the column in this row.
	 */
	protected Component createDisplayComponent(String componentId, IModel<D> dataModel)
	{
		return new Label(componentId, dataModel);
	}

	/**
	 * Populated the data for this column in the row into the {@code cellItem}.
	 * <p>
	 * This implementation adds the {@link Component} returned by {@link #createDisplayComponent(java.lang.String, org.apache.wicket.model.IModel) }
	 * to the cell.
	 * @param cellItem The cell to be populated.
	 * @param componentId The component id to be used for the component that will be added to the cell.
	 * @param rowModel A model of the row data.
	 */
	@Override
	public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel)
	{
		cellItem.add(createDisplayComponent(componentId, getDataModel(rowModel)));
	}
}
