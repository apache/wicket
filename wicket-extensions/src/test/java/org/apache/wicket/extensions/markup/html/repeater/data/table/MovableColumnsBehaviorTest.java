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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class MovableColumnsBehaviorTest extends WicketTestCase
{
	@Test
	void aMoveHasToMoveAMovableColumnAndLeaveTheOthersInPlace()
	{
		boolean[] movable = { false, true, true, false, true };

		assertTrue(MovableColumnsBehavior.isValidMove(movable, 2, 1));
		assertTrue(MovableColumnsBehavior.isValidMove(movable, 1, 3));
		assertFalse(MovableColumnsBehavior.isValidMove(movable, 0, 2), "not movable");
		assertFalse(MovableColumnsBehavior.isValidMove(movable, 1, 0), "past a fixed column");
		assertFalse(MovableColumnsBehavior.isValidMove(movable, 1, 5), "moves the fixed column");
		assertFalse(MovableColumnsBehavior.isValidMove(movable, 1, 1), "in its place");
		assertFalse(MovableColumnsBehavior.isValidMove(movable, 1, 2), "in its place");
		assertFalse(MovableColumnsBehavior.isValidMove(movable, -1, 2));
		assertFalse(MovableColumnsBehavior.isValidMove(movable, 1, 6));
	}

	@Test
	void theMovableColumnsAndTheLabelAreHandedToTheScript()
	{
		TestTable table = new TestTable();
		tester.startPage(new TablePage(table));

		tester.assertContains(Pattern.quote("\"movable\":[1,2]"));
		tester.assertContains(Pattern.quote("\"label\":\"Move the column: drag it, or use the " +
			"arrow keys\""));
		tester.assertContains(Pattern.quote("Wicket.MovableColumns.attach(\"" +
			table.getMarkupId() + "\", options);"));
		tester.assertContains("wicket-movable-columns.*\\.js");
		tester.assertContains("wicket-movable-columns.*\\.css");
	}

	@Test
	void validMovesAreHandedToTheTableAndOthersIgnored()
	{
		TestTable table = new TestTable();
		tester.startPage(new TablePage(table));

		for (String[] move : List.of(new String[] { "2", "1" }, new String[] { "0", "2" },
			new String[] { "a", "1" }, new String[] { "1" }))
		{
			tester.getRequest().getPostParameters().setParameterValue("from", move[0]);
			if (move.length > 1)
			{
				tester.getRequest().getPostParameters().setParameterValue("to", move[1]);
			}
			tester.executeBehavior(table.getBehaviors(MovableColumnsBehavior.class).get(0));
		}

		assertEquals(List.of("2,1"), table.moves);
	}

	@Test
	void onlyMovableColumnsTablesCanHaveIt()
	{
		assertThrows(IllegalArgumentException.class,
			() -> new WebMarkupContainer("table").add(new MovableColumnsBehavior()));
	}

	private static class TestTable extends WebMarkupContainer implements IMovableColumnsTable
	{
		private static final long serialVersionUID = 1L;

		final List<String> moves = new ArrayList<>();

		TestTable()
		{
			super("table");
			add(new MovableColumnsBehavior());
		}

		@Override
		public boolean[] getMovableColumns()
		{
			return new boolean[] { false, true, true };
		}

		@Override
		public void moveColumn(AjaxRequestTarget target, int from, int to)
		{
			moves.add(from + "," + to);
		}
	}

	public static class TablePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TablePage(TestTable table)
		{
			add(table);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><head></head><body><table wicket:id=\"table\">" +
				"<thead><tr><th>A</th><th>B</th><th>C</th></tr></thead></table></body></html>");
		}
	}
}
