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

import java.io.Serializable;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;

/**
 * A provider of the rows of a {@link DynamicDataTable} that can sort them, the counterpart of
 * {@link org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider} for
 * a {@code DataTable}.
 * <p>
 * The table changes the {@link #getSortState() sort state} when a
 * {@link ISortableDynamicColumn sortable column}'s header is clicked, and then fetches the rows
 * again; {@link #rows(long, long)} has to return them in the order the sort state describes.
 *
 * @param <T>
 *            the type of the rows
 * @param <K>
 *            the type of the keys
 * @param <S>
 *            the type of the sort properties
 * @see SortableDynamicDataProvider
 * @since 11.0.0
 */
public interface ISortableDynamicDataProvider<T, K extends Serializable, S>
	extends
		IDynamicDataProvider<T, K>,
		ISortStateLocator<S>
{
}
