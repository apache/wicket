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

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.model.IModel;


/**
 * A helper implementation for a filtered column.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 * @param <S>
 *            the type of the sort property
 * 
 */
public abstract class FilteredAbstractColumn<T, S> extends AbstractColumn<T, S>
	implements
		IFilteredColumn<T, S>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param displayModel
	 *            model used to display the header text of this column
	 * 
	 * @param sortProperty
	 *            sort property this column represents
	 */
	public FilteredAbstractColumn(final IModel<String> displayModel, final S sortProperty)
	{
		super(displayModel, sortProperty);
	}

	/**
	 * Constructor
	 * 
	 * @param displayModel
	 *            model used to display the header text of this column
	 */
	public FilteredAbstractColumn(final IModel<String> displayModel)
	{
		super(displayModel);
	}


}
