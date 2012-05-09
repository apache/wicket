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

import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;


/**
 * Like {@link PropertyColumn} but with support for filters.
 * 
 * @see PropertyColumn
 * @see IFilteredColumn
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 * @param <S>
 *            the type of the sort property
 * 
 */
public abstract class FilteredPropertyColumn<T, S> extends PropertyColumn<T, S>
	implements
		IFilteredColumn<T, S>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param displayModel
	 *            model used to construct header text
	 * @param sortProperty
	 *            sort property this column represents, if null this column will not be sortable
	 * @param propertyExpression
	 *            wicket property expression for the column, see {@link PropertyModel} for details
	 */
	public FilteredPropertyColumn(final IModel<String> displayModel, final S sortProperty,
		final String propertyExpression)
	{
		super(displayModel, sortProperty, propertyExpression);
	}

	/**
	 * @param displayModel
	 *            model used to construct header text
	 * @param propertyExpression
	 *            wicket property expression for the column, see {@link PropertyModel} for details
	 */
	public FilteredPropertyColumn(final IModel<String> displayModel, final String propertyExpression)
	{
		super(displayModel, propertyExpression);
	}

}
