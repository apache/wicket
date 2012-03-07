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
package org.apache.wicket.extensions.markup.html.repeater.data.grid;

import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.io.IClusterable;

/**
 * Represents an object that is capable of populating an {@link Item} container representing a cell
 * in a {@link DataGridView} with components.
 * <p>
 * Example
 * <p>
 * 
 * <pre>
 * class NamePopulator implements ICellPopulator
 * {
 * 	void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
 *       User user=(User)rowModel.getObject(cellItem);
 *       String name=user.getFirstName()+&quot; &quot;+user.getLastName();
 *       cellItem.add(new Label(componentId, name);
 *     }}
 * </pre>
 * 
 * In this example the IDataProvider assigned to the DataGridView retrieves User objects from the
 * database. The cell populator adds a label to the cell that will display the full name of the
 * user.
 * 
 * @see DataGridView
 * @see Item
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @param <T>
 *            Model object type
 */
public interface ICellPopulator<T> extends IClusterable, IDetachable
{
	/**
	 * Method used to populate a cell in the {@link DataGridView}
	 * 
	 * <b>Implementation MUST add a component to the cellItem using the component id provided by
	 * componentId argument, otherwise a WicketRuntimeException will be thrown</b>
	 * 
	 * @param cellItem
	 *            the item representing the current table cell being rendered
	 * @param componentId
	 *            the id of the component used to render the cell (only one component should be
	 *            added to the cell)
	 * @param rowModel
	 *            the model of the row item being rendered. this model usually contains the model
	 *            provided by the data provider.
	 * 
	 * @see Item
	 */
	void populateItem(final Item<ICellPopulator<T>> cellItem, final String componentId,
		final IModel<T> rowModel);
}
