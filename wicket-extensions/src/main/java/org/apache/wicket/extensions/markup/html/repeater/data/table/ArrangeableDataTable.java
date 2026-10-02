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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ResizableColumnsBehavior.Mode;
import org.apache.wicket.markup.repeater.data.IDataProvider;

/**
 * A {@link DataTable} whose columns the user can resize and move: it gets a
 * {@link ResizableColumnsBehavior} and a {@link MovableColumnsBehavior}. The columns marked
 * {@link IMovableColumn} can be moved, the others keep their places.
 * <p>
 * The table keeps the order and the widths of its columns, so they survive the table being
 * rendered again, for example after a move or when the page is reloaded; the list of columns it
 * was given is not changed. After a move the table renders itself again via Ajax and calls
 * {@link #onColumnsMoved(AjaxRequestTarget)}; after a resize it calls
 * {@link #onColumnsResized(AjaxRequestTarget)}.
 *
 * @param <T>
 *            the type of the rows
 * @param <S>
 *            the type of the sort property
 * @since 11.0.0
 */
public class ArrangeableDataTable<T, S> extends DataTable<T, S>
	implements
		IResizableColumnsTable,
		IMovableColumnsTable
{
	private static final long serialVersionUID = 1L;

	private final List<IColumn<T, S>> arranged;

	private double[] widths;

	/**
	 * Constructor.
	 *
	 * @param id
	 *            the component id
	 * @param columns
	 *            the columns, in their initial order
	 * @param dataProvider
	 *            the provider of the rows
	 * @param rowsPerPage
	 *            the number of rows per page
	 */
	public ArrangeableDataTable(String id, List<? extends IColumn<T, S>> columns,
		IDataProvider<T> dataProvider, long rowsPerPage)
	{
		this(id, new ArrayList<>(columns), dataProvider, rowsPerPage);
	}

	private ArrangeableDataTable(String id, ArrayList<IColumn<T, S>> arranged,
		IDataProvider<T> dataProvider, long rowsPerPage)
	{
		super(id, arranged, dataProvider, rowsPerPage);
		this.arranged = arranged;
		setOutputMarkupId(true);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		ResizableColumnsBehavior resizable = newResizableColumnsBehavior();
		if (resizable != null)
		{
			add(resizable);
		}
		MovableColumnsBehavior movable = newMovableColumnsBehavior();
		if (movable != null)
		{
			add(movable);
		}
	}

	/**
	 * Creates the behavior letting the user resize the columns, in {@link Mode#STRETCH} by
	 * default.
	 *
	 * @return the behavior, or {@code null} for columns that cannot be resized
	 */
	protected ResizableColumnsBehavior newResizableColumnsBehavior()
	{
		return new ResizableColumnsBehavior(Mode.STRETCH);
	}

	/**
	 * Creates the behavior letting the user move the columns.
	 *
	 * @return the behavior, or {@code null} for columns that cannot be moved
	 */
	protected MovableColumnsBehavior newMovableColumnsBehavior()
	{
		return new MovableColumnsBehavior();
	}

	/**
	 * @param column
	 *            a column of the table
	 * @return whether the user can move the column: whether it is marked {@link IMovableColumn}
	 */
	protected boolean isMovable(IColumn<T, S> column)
	{
		return column instanceof IMovableColumn;
	}

	@Override
	public boolean[] getMovableColumns()
	{
		boolean[] movable = new boolean[arranged.size()];
		for (int i = 0; i < movable.length; i++)
		{
			movable[i] = isMovable(arranged.get(i));
		}
		return movable;
	}

	/**
	 * Moves the column, with its width, and renders the table again.
	 */
	@Override
	public void moveColumn(AjaxRequestTarget target, int from, int to)
	{
		int index = to > from ? to - 1 : to;
		arranged.add(index, arranged.remove(from));
		if (widths != null)
		{
			List<Double> moved = new ArrayList<>();
			for (double width : widths)
			{
				moved.add(width);
			}
			moved.add(index, moved.remove(from));
			for (int i = 0; i < widths.length; i++)
			{
				widths[i] = moved.get(i);
			}
		}
		target.add(this);
		onColumnsMoved(target);
	}

	@Override
	public double[] getShownColumnWidths()
	{
		return widths != null ? widths.clone() : null;
	}

	/**
	 * Keeps the widths, if there is one for each column.
	 */
	@Override
	public void columnsResized(AjaxRequestTarget target, double[] widths)
	{
		if (widths != null && widths.length == arranged.size())
		{
			this.widths = widths.clone();
			onColumnsResized(target);
		}
	}

	/**
	 * Called after the user moved a column and the table is rendered again. Does nothing by
	 * default.
	 *
	 * @param target
	 *            the handler of the request
	 */
	protected void onColumnsMoved(AjaxRequestTarget target)
	{
	}

	/**
	 * Called after the user resized a column. Does nothing by default.
	 *
	 * @param target
	 *            the handler of the request
	 */
	protected void onColumnsResized(AjaxRequestTarget target)
	{
	}
}
