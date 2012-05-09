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

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;


/**
 * An interface that represents a column in the {@link DefaultDataTable}
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * @param <T>
 * @param <S>
 *     the type of the sorting parameter
 */
public interface IColumn<T, S> extends ICellPopulator<T>
{
	/**
	 * Returns the component that will be used as the header for the column.
	 * 
	 * This component will be contained in &lt;span&gt; tags.
	 * 
	 * @param componentId
	 *            component id for the returned Component
	 * 
	 * @return component that will be used as the header for the column
	 */
	Component getHeader(String componentId);

	/**
	 * Returns the name of the property that this header sorts. If null is returned the header will
	 * be unsortable.
	 * 
	 * @return the sort property
	 */
	S getSortProperty();

	/**
	 * Returns true if the header of the column should be sortable
	 * 
	 * @return true if header should be sortable
	 */
	boolean isSortable();
}
