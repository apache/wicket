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
import java.util.List;

/**
 * The rows the user selected in a {@link DynamicDataTable}, with a {@link SelectionColumn}.
 * <p>
 * The selection is either a list of keys, built up by checking rows, or every row the provider
 * has, chosen with the "select all" link the table offers once every row of a page is checked.
 * The second kind is meant for bulk actions and holds no keys at all: {@link #fetch()} goes
 * through the provider's rows instead.
 * <p>
 * The selection is a view of the table's state, kept on the server: it changes as the user
 * selects rows.
 *
 * @param <T>
 *            the type of the rows
 * @param <K>
 *            the type of the keys
 * @see DynamicDataTable#getSelection()
 * @since 11.0.0
 */
public interface ISelection<T, K extends Serializable>
{
	/**
	 * The kinds of selection.
	 */
	enum SelectionType
	{
		/** Every row of the provider, whichever rows it has when it is {@link #fetch() fetched}. */
		ALL,

		/** The rows of a list of keys. */
		RANGE
	}

	/**
	 * @return the kind of selection
	 */
	SelectionType getType();

	/**
	 * @return the selected keys in the order they were selected, empty for
	 *         {@link SelectionType#ALL}
	 */
	List<K> getKeys();

	/**
	 * @param key
	 *            a key
	 * @return whether the row of the key is selected
	 */
	boolean isSelected(K key);

	/**
	 * @return the number of selected rows: the provider's size for {@link SelectionType#ALL}, the
	 *         number of keys otherwise, including keys of rows deleted since they were selected
	 */
	long size();

	/**
	 * @return whether no row is selected
	 */
	default boolean isEmpty()
	{
		return size() == 0;
	}

	/**
	 * Returns the selected rows, read lazily from the provider while iterating: for
	 * {@link SelectionType#ALL} its rows in order, a chunk at a time, otherwise the rows of the
	 * keys, skipping keys whose row no longer exists. Iterate it within the request that
	 * fetched it.
	 *
	 * @return the selected rows
	 */
	Iterable<T> fetch();
}
