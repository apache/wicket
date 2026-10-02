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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * Makes any column of a {@link DynamicDataTable} sortable: the header, template, CSS class,
 * export value and header items are those of the wrapped column, and the rows are sorted by the
 * given property. The actions of a wrapped {@link CompoundDynamicColumn} are registered with the table as if it
 * were given directly.
 *
 * @param <T>
 *            the type of the rows
 * @param <S>
 *            the type of the sort property
 * @since 11.0.0
 */
public class SortableDynamicColumn<T, S>
	implements
		ISortableDynamicColumn<T, S>,
		IHeaderContributor
{
	private static final long serialVersionUID = 1L;

	private final IDynamicColumn<T> column;

	private final S sortProperty;

	/**
	 * @param column
	 *            the column to sort by
	 * @param sortProperty
	 *            the property the rows are sorted by
	 */
	public SortableDynamicColumn(IDynamicColumn<T> column, S sortProperty)
	{
		this.column = Args.notNull(column, "column");
		this.sortProperty = Args.notNull(sortProperty, "sortProperty");
	}

	/**
	 * @return the wrapped column
	 */
	public IDynamicColumn<T> getColumn()
	{
		return column;
	}

	@Override
	public S getSortProperty()
	{
		return sortProperty;
	}

	@Override
	public IModel<String> getHeader()
	{
		return column.getHeader();
	}

	@Override
	public String getTemplate()
	{
		return column.getTemplate();
	}

	@Override
	public String getTemplate(DynamicDataTable<T, ?> table)
	{
		return column.getTemplate(table);
	}

	@Override
	public String getId()
	{
		return column.getId();
	}

	@Override
	public String getCssClass()
	{
		return column.getCssClass();
	}

	@Override
	public Component newHeader(String id)
	{
		return column.newHeader(id);
	}

	@Override
	public boolean isResizable()
	{
		return column.isResizable();
	}

	@Override
	public boolean isExportable()
	{
		return column.isExportable();
	}

	@Override
	public Object getExportValue(T row)
	{
		return column.getExportValue(row);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		if (column instanceof IHeaderContributor contributor)
		{
			contributor.renderHead(response);
		}
	}

	@Override
	public void detach()
	{
		column.detach();
	}
}
