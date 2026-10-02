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

import org.apache.wicket.util.io.IClusterable;

/**
 * Keeps the {@link ColumnState} of {@link DynamicDataTable}s, so the order, visibility and widths
 * of their columns survive the table: a table loads its state when it is initialized and saves it
 * whenever the user moves, shows, hides or resizes a column. An implementation can keep the states
 * anywhere, for example in a database by user, to have them survive the session and restarts of
 * the server.
 * <p>
 * A table has no store by default; {@link SessionColumnStateStore} keeps the states in the
 * session.
 *
 * @see DynamicDataTable#setColumnStateStore(IColumnStateStore)
 * @since 11.0.0
 */
public interface IColumnStateStore extends IClusterable
{
	/**
	 * @param key
	 *            the {@link DynamicDataTable#getColumnStateKey() key} of the table
	 * @return the state saved for the table, or {@code null} if there is none
	 */
	ColumnState load(String key);

	/**
	 * Saves the state of a table, replacing the one saved before.
	 *
	 * @param key
	 *            the {@link DynamicDataTable#getColumnStateKey() key} of the table
	 * @param state
	 *            the state of its columns
	 */
	void save(String key, ColumnState state);
}
