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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

/**
 * A helper implementation for the IColumn interface
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * @param <T>
 *            the type of the object that will be rendered in this column's cells
 * @param <S>
 *            the type of the sort property
 */
public abstract class AbstractColumn<T, S> implements IStyledColumn<T, S>
{
	private static final long serialVersionUID = 1L;

	private final IModel<String> displayModel;

	private final S sortProperty;

	/**
	 * @param displayModel
	 *            model used to generate header text
	 * @param sortProperty
	 *            sort property this column represents
	 */
	public AbstractColumn(final IModel<String> displayModel, final S sortProperty)
	{
		this.displayModel = displayModel;
		this.sortProperty = sortProperty;
	}

	/**
	 * @param displayModel
	 *            model used to generate header text
	 */
	public AbstractColumn(final IModel<String> displayModel)
	{
		this(displayModel, null);
	}

	/**
	 * @return returns display model to be used for the header component
	 */
	public IModel<String> getDisplayModel()
	{
		return displayModel;
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn#getSortProperty()
	 */
	@Override
	public S getSortProperty()
	{
		return sortProperty;
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn#isSortable()
	 */
	@Override
	public boolean isSortable()
	{
		return getSortProperty() != null;
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn#getHeader(java.lang.String)
	 */
	@Override
	public Component getHeader(final String componentId)
	{
		return new Label(componentId, getDisplayModel());
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	@Override
	public void detach()
	{
		if (displayModel != null)
		{
			displayModel.detach();
		}
	}

	@Override
	public String getCssClass()
	{
		return null;
	}
}
