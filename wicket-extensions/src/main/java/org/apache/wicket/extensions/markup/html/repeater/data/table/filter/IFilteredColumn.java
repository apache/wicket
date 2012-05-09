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
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;


/**
 * Represents a data table column that can be filtered. The filter is represented by a component
 * returned from the getFilter() method.
 * 
 * @see IColumn
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 * @param <S>
 *     the type of the sorting parameter
 * 
 */
public interface IFilteredColumn<T, S> extends IColumn<T, S>
{
	/**
	 * Returns the component used by user to filter the column. If null is returned, no filter will
	 * be added.
	 * 
	 * @param componentId
	 *            component id for returned filter component
	 * @param form
	 *            FilterForm object for the toolbar. components can use this form's model to access
	 *            properties of the state object (
	 *            <code>PropertyModel(form.getModel(), "property"</code>) or retrieve the
	 *            {@link IFilterStateLocator} object by using {@link FilterForm#getStateLocator()}
	 * @return component that will be used to represent a filter for this column, or null if no such
	 *         component is desired
	 */
	Component getFilter(String componentId, FilterForm<?> form);

}
