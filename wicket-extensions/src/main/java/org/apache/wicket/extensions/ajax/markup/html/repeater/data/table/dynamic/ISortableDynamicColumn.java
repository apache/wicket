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

/**
 * A column of a {@link DynamicDataTable} the rows can be sorted by, like a sortable
 * {@link org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn} of a
 * {@code DataTable}.
 * <p>
 * If the table's provider is an {@link ISortableDynamicDataProvider}, the column's header becomes
 * a link showing an arrow for the current order: a click sorts the rows by
 * {@link #getSortProperty()}, ascending first and then alternating, and shows the first page.
 * Any column can be made sortable with {@link SortableDynamicColumn}.
 *
 * @param <T>
 *            the type of the rows
 * @param <S>
 *            the type of the sort property
 * @since 11.0.0
 */
public interface ISortableDynamicColumn<T, S> extends IDynamicColumn<T>
{
	/**
	 * @return the property the rows are sorted by when the header is clicked, or {@code null} if
	 *         the column is not sortable
	 */
	S getSortProperty();

	/**
	 * @return whether the header sorts the rows, {@code true} if there is a
	 *         {@link #getSortProperty() sort property}
	 */
	default boolean isSortable()
	{
		return getSortProperty() != null;
	}
}
