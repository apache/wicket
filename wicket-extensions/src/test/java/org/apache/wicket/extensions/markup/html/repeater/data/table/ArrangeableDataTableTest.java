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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * An {@link ArrangeableDataTable} lets the user resize and move its columns and keeps their order
 * and widths.
 */
class ArrangeableDataTableTest
{
	private WicketTester tester;

	private IColumn<Contact, String> id;

	private IColumn<Contact, String> first;

	private IColumn<Contact, String> last;

	private List<IColumn<Contact, String>> columns;

	private ArrangeableDataTable<Contact, String> table;

	@BeforeEach
	void start()
	{
		tester = new WicketTester(new RepeaterApplication());
		id = new PropertyColumn<>(Model.of("Id"), "id");
		first = new MovableColumn("First", "firstName");
		last = new MovableColumn("Last", "lastName");
		columns = List.of(id, first, last);
		table = new ArrangeableDataTable<>("table", columns, new SortableContactDataProvider(), 5);
		table.addTopToolbar(new HeadersToolbar<>(table, null));
		tester.startPage(new TablePage(table));
	}

	@AfterEach
	void stop()
	{
		tester.destroy();
	}

	@Test
	void theColumnsCanBeResizedAndTheMarkedOnesMoved()
	{
		assertEquals(1, table.getBehaviors(ResizableColumnsBehavior.class).size());
		assertEquals(1, table.getBehaviors(MovableColumnsBehavior.class).size());
		tester.assertContains(Pattern.quote("Wicket.ResizableColumns.attach("));
		tester.assertContains(Pattern.quote("\"movable\":[1,2]"));
		tester.assertContains(Pattern.quote("Wicket.MovableColumns.attach("));
		assertArrayEquals(new boolean[] { false, true, true }, table.getMovableColumns());
	}

	@Test
	void aMovedColumnIsRenderedInItsNewPlace()
	{
		move(2, 1);

		assertEquals(List.of(id, last, first), table.getColumns());
		assertEquals(List.of(id, first, last), columns, "the given columns are left alone");
		tester.assertComponentOnAjaxResponse(table);
		String response = tester.getLastResponseAsString();
		assertTrue(response.indexOf(">Last<") < response.indexOf(">First<"), response);
	}

	@Test
	void aColumnThatIsNotMovableKeepsItsPlace()
	{
		move(0, 3);
		move(1, 0);

		assertEquals(List.of(id, first, last), table.getColumns());
	}

	@Test
	void theWidthsAreKeptAndMoveWithTheirColumns()
	{
		assertNull(table.getShownColumnWidths());

		resize("10,20,30");
		assertArrayEquals(new double[] { 10, 20, 30 }, table.getShownColumnWidths());

		move(2, 1);
		assertArrayEquals(new double[] { 10, 30, 20 }, table.getShownColumnWidths());

		tester.startPage(tester.getLastRenderedPage());
		tester.assertContains(Pattern.quote("options.widths = [10,30,20];"));
	}

	@Test
	void widthsNotFittingTheColumnsAreIgnored()
	{
		resize("10,20");
		resize("10,a,30");

		assertNull(table.getShownColumnWidths());
	}

	@Test
	void theBehaviorsCanBeLeftOut()
	{
		ArrangeableDataTable<Contact, String> plain = new ArrangeableDataTable<>("table",
			columns, new SortableContactDataProvider(), 5)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected ResizableColumnsBehavior newResizableColumnsBehavior()
			{
				return null;
			}

			@Override
			protected MovableColumnsBehavior newMovableColumnsBehavior()
			{
				return null;
			}
		};
		tester.startPage(new TablePage(plain));

		assertTrue(plain.getBehaviors(ResizableColumnsBehavior.class).isEmpty());
		assertTrue(plain.getBehaviors(MovableColumnsBehavior.class).isEmpty());
	}

	private void move(int from, int to)
	{
		tester.getRequest().getPostParameters().setParameterValue("from", String.valueOf(from));
		tester.getRequest().getPostParameters().setParameterValue("to", String.valueOf(to));
		tester.executeBehavior(table.getBehaviors(MovableColumnsBehavior.class).get(0));
	}

	private void resize(String widths)
	{
		tester.getRequest().getPostParameters().setParameterValue("widths", widths);
		tester.executeBehavior(table.getBehaviors(ResizableColumnsBehavior.class).get(0));
	}

	private static class MovableColumn extends PropertyColumn<Contact, String>
		implements
			IMovableColumn
	{
		private static final long serialVersionUID = 1L;

		MovableColumn(String header, String property)
		{
			super(Model.of(header), property);
		}
	}

	public static class TablePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TablePage(DataTable<Contact, String> table)
		{
			add(table);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><head></head><body><table wicket:id=\"table\"></table></body></html>");
		}
	}
}
