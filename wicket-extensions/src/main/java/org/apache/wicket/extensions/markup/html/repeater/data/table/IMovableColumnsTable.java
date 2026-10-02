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

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * A table whose columns the user can move to other places, with a {@link MovableColumnsBehavior}
 * added to it. The table decides which columns can move and moves them, rendering itself again.
 *
 * @since 11.0.0
 */
public interface IMovableColumnsTable
{
	/**
	 * @return for each column the table shows, in display order, whether the user can move it;
	 *         columns that cannot move keep their places when others move
	 */
	boolean[] getMovableColumns();

	/**
	 * Called when the user moved a column. The indexes are checked to be within the shown
	 * columns, the moved column to be movable and every column that is not to keep its place.
	 *
	 * @param target
	 *            the handler of the request reporting the move, to render the table again with
	 * @param from
	 *            the index of the moved column among the shown columns
	 * @param to
	 *            the index of the column it goes in front of, or the number of shown columns to
	 *            put it at the end
	 */
	void moveColumn(AjaxRequestTarget target, int from, int to);
}
