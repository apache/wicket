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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTableHeaderTest.Person;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTableHeaderTest.TablePage;
import org.apache.wicket.extensions.markup.html.icon.FontAwesomeIcon;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class DynamicDataTableColumnsTest extends WicketTestCase
{
	private static final List<Person> PEOPLE = List.of(new Person("1", "Ada"),
		new Person("2", "Linus"));

	private TestTable table;

	private IDynamicColumn<Person> number;

	private IDynamicColumn<Person> name;

	private IDynamicColumn<Person> id;

	private IDynamicColumn<Person> fixed;

	@Test
	void everyColumnIsShownByDefault()
	{
		start();

		assertEquals(table.getColumns(), table.getVisibleColumns());
		assertEquals(4, tester.getTagsByWicketId("headers").size());
	}

	@Test
	void aHiddenColumnIsNeitherInTheHeaderNorInTheTemplates()
	{
		create();
		table.setColumnVisible(id, false);
		tester.startPage(new TablePage(table));

		assertEquals(List.of(number, name, fixed), table.getVisibleColumns());
		assertEquals(4, table.getColumns().size());
		assertEquals(3, tester.getTagsByWicketId("headers").size());
		assertFalse(tester.getLastResponseAsString().contains("{{id}}"));
	}

	@Test
	void aColumnNotOfTheTableIsRejected()
	{
		create();

		assertThrows(IllegalArgumentException.class,
			() -> table.setColumnVisible(new DynamicColumn<>(Model.of("Other"), "x"), false));
	}

	@Test
	void movableColumnsHaveAHandle()
	{
		start();

		List<TagTester> headers = tester.getTagsByWicketId("headers");
		assertFalse(headers.get(0).hasAttribute("data-dt-movable"));
		assertEquals("true", headers.get(1).getAttribute("data-dt-movable"));
		assertEquals("true", headers.get(2).getAttribute("data-dt-movable"));
		assertFalse(headers.get(3).hasAttribute("data-dt-movable"));
		assertEquals(2, tester.getTagsByWicketId("move").size());
		tester.assertContains(Pattern.quote("\"move\":\"_wicket-move\""));
		tester.assertContainsNot("drop-end");
	}

	@Test
	void withoutMovableColumnsNothingCanBeMoved()
	{
		create();
		table.setColumnVisible(name, false).setColumnVisible(id, false);
		tester.startPage(new TablePage(table));

		assertFalse(tester.getLastResponseAsString().contains("\"move\""));
	}

	@Test
	void aMovedColumnIsPutInFrontOfTheTargetAndTheTableRendered()
	{
		start();

		executeAction(DynamicDataTable.MOVE_ACTION, "1,3");

		assertEquals(List.of(number, id, name, fixed), table.getColumns());
		assertEquals(1, table.changes);
		tester.assertComponentOnAjaxResponse(table);
	}

	@Test
	void aColumnCanBeMovedToTheEnd()
	{
		create();
		table.setColumnVisible(fixed, false);
		tester.startPage(new TablePage(table));

		executeAction(DynamicDataTable.MOVE_ACTION, "1,3");

		assertEquals(List.of(number, id, name), table.getVisibleColumns());
	}

	@Test
	void fixedColumnsKeepTheirPlaces()
	{
		start();

		for (String move : List.of("3,1", "1,0", "1,4", "0,2", "1,1", "1,2", "1,9", "-1,2", "a,b",
			"1", ""))
		{
			executeAction(DynamicDataTable.MOVE_ACTION, move);
			assertEquals(List.of(number, name, id, fixed), table.getColumns(), move);
		}
		assertEquals(0, table.changes);
	}

	@Test
	void widthsAndProportionsMoveWithTheirColumn()
	{
		create();
		table.setColumnWidths(10, 20, 30, 40).setColumnProportions(1, 2, 3, 4);

		table.moveColumn(name, 2);

		assertArrayEquals(new double[] { 10, 30, 20, 40 }, table.getColumnWidths());
		assertArrayEquals(new double[] { 1, 3, 2, 4 }, table.getColumnProportions());
	}

	@Test
	void onlyTheWidthsAndProportionsOfShownColumnsAreSent()
	{
		create();
		table.setColumnWidths(10, 20, 30, 40).setColumnProportions(1, 2, 3, 4);
		table.setColumnVisible(id, false);
		tester.startPage(new TablePage(table));

		tester.assertContains(Pattern.quote("\"proportions\":[1,2,4]"));
		tester.assertContains(Pattern.quote("\"widths\":[10,20,40]"));
	}

	@Test
	void resizingKeepsTheWidthOfAHiddenColumn()
	{
		create();
		table.setColumnVisible(id, false);
		tester.startPage(new TablePage(table));

		executeAction(DynamicDataTable.RESIZE_ACTION, "11,22,44");

		assertArrayEquals(new double[] { 11, 22, 0, 44 }, table.getColumnWidths());
		table.setColumnVisible(id, true);
		tester.startPage(new TablePage(table));
		assertFalse(tester.getLastResponseAsString().contains("\"widths\""),
			"a column of unknown width lets the browser lay the columns out");
	}

	@Test
	void showColumnsShowsTheGivenHideableColumnsOnly()
	{
		start();

		executeAction(DynamicDataTable.SHOW_COLUMNS_ACTION, "0,2");

		assertEquals(List.of(number, id), table.getVisibleColumns());
		assertEquals(1, table.changes);
		tester.assertComponentOnAjaxResponse(table);
	}

	@Test
	void aColumnIsHideableWhenItOrTheColumnItWrapsIsMarked()
	{
		create();

		assertFalse(table.isHideable(number));
		assertTrue(table.isHideable(name));
		assertTrue(table.isHideable(fixed));
		assertFalse(table.isMovable(fixed));
	}

	@Test
	void showColumnsLeavesAColumnThatIsNotHideable()
	{
		List<IDynamicColumn<Person>> columns = new ArrayList<>();
		SelectionColumn<Person> selection = new SelectionColumn<>();
		columns.add(selection);
		columns.add(new HideableColumn("Name", "{{name}}"));
		table = new TestTable(columns);
		tester.startPage(new TablePage(table));

		executeAction(DynamicDataTable.SHOW_COLUMNS_ACTION, "");

		assertEquals(List.of(selection), table.getVisibleColumns());
	}

	@Test
	void showColumnsIgnoresBadIndexes()
	{
		start();

		for (String shown : List.of("0,9", "x"))
		{
			executeAction(DynamicDataTable.SHOW_COLUMNS_ACTION, shown);
			assertEquals(table.getColumns(), table.getVisibleColumns(), shown);
		}
		assertEquals(0, table.changes);
	}

	@Test
	void showColumnsIgnoresHidingEveryColumn()
	{
		table = new TestTable(
			List.of(new HideableColumn("Name", "{{name}}"), new HideableColumn("Id", "{{id}}")));
		tester.startPage(new TablePage(table));

		executeAction(DynamicDataTable.SHOW_COLUMNS_ACTION, "");

		assertEquals(table.getColumns(), table.getVisibleColumns());
		assertEquals(0, table.changes);
	}

	@Test
	void theColumnChooserListsTheHideableColumnsWithTheirState()
	{
		create();
		table.addToolbarAction(new ColumnChooserToolbarAction<>());
		table.setColumnVisible(id, false);
		tester.startPage(new TablePage(table));

		List<TagTester> buttons = tester.getTagsByWicketId("column");
		assertEquals(3, buttons.size());
		assertEquals("1", buttons.get(0).getAttribute("data-dt-column"));
		assertEquals("true", buttons.get(0).getAttribute("aria-pressed"));
		assertEquals("false", buttons.get(1).getAttribute("aria-pressed"));
		assertEquals("Id", tester.getTagsByWicketId("label").get(1).getValue());
		TagTester toggle = tester.getTagByWicketId("toggle");
		assertEquals("Columns", toggle.getAttribute("title"));
		tester.assertContains(Pattern.quote("data-dt-apply=\"true\""));
	}

	@Test
	void anIconToolbarActionIsAButtonCallingTheAction()
	{
		create();
		List<String> clicks = new ArrayList<>();
		table.addToolbarAction(new IconBasedToolbarAction<>(FontAwesomeIcon.PLUS, Model.of("Add <"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onClick(DynamicDataTable<Person, String> table,
				AjaxRequestTarget target)
			{
				clicks.add(table.getId());
			}
		});
		tester.startPage(new TablePage(table));

		TagTester button = tester.getTagByWicketId("button");
		assertEquals("button", button.getName());
		assertEquals("Add <", button.getAttribute("aria-label"));
		assertTrue(button.getValue().contains("fa-plus"), button.getValue());
		IconToolbarButton<?, ?> component = table.visitChildren(IconToolbarButton.class,
			(child, visit) -> visit.stop((IconToolbarButton<?, ?>)child));
		tester.clickLink(component.get("button"));
		assertEquals(List.of("table"), clicks);
	}

	@Test
	void aMoveIsSavedInTheSessionByDefault()
	{
		start();

		executeAction(DynamicDataTable.MOVE_ACTION, "1,3");

		ColumnState state = SessionColumnStateStore.INSTANCE.load(table.getColumnStateKey());
		assertEquals(List.of("0", "2", "1", "3"), state.getOrder());
		assertEquals(state, table.getColumnState());
	}

	@Test
	void aResizeIsSaved()
	{
		start();

		executeAction(DynamicDataTable.RESIZE_ACTION, "10,20,30,40");

		ColumnState state = SessionColumnStateStore.INSTANCE.load(table.getColumnStateKey());
		assertEquals(Map.of("0", 10d, "1", 20d, "2", 30d, "3", 40d), state.getWidths());
	}

	@Test
	void aNewTableShowsItsColumnsAsTheUserLeftThem()
	{
		start();
		executeAction(DynamicDataTable.MOVE_ACTION, "1,3");
		executeAction(DynamicDataTable.SHOW_COLUMNS_ACTION, "0,1,2");
		executeAction(DynamicDataTable.RESIZE_ACTION, "10,20,30");

		create();
		tester.startPage(new TablePage(table));

		assertEquals(List.of(number, id, name, fixed), table.getColumns());
		assertEquals(List.of(number, id, name), table.getVisibleColumns());
		assertArrayEquals(new double[] { 10, 20, 30, 0 }, table.getColumnWidths());
	}

	@Test
	void aStateMovesOnlyMovableColumnsAndHidesOnlyHideableOnes()
	{
		create();

		table.setColumnState(new ColumnState(List.of("3", "2", "9", "1", "0"), Set.of("0", "3"),
			Map.of("1", 50d, "9", 70d, "3", -1d)));

		assertEquals(List.of(number, id, name, fixed), table.getColumns());
		assertEquals(List.of(number, id, name), table.getVisibleColumns());
		assertArrayEquals(new double[] { 0, 0, 50, 0 }, table.getColumnWidths());
	}

	@Test
	void aStateHidingEveryColumnShowsThemAll()
	{
		table = new TestTable(
			List.of(new HideableColumn("Name", "{{name}}"), new HideableColumn("Id", "{{id}}")));

		table.setColumnState(new ColumnState(List.of(), Set.of("0", "1"), Map.of()));

		assertEquals(table.getColumns(), table.getVisibleColumns());
	}

	@Test
	void withoutAStoreNothingIsSaved()
	{
		create();
		table.setColumnStateStore(null);
		tester.startPage(new TablePage(table));

		executeAction(DynamicDataTable.MOVE_ACTION, "1,3");

		assertNull(SessionColumnStateStore.INSTANCE.load(table.getColumnStateKey()));
	}

	@Test
	void aStoreOfTheApplicationIsUsed()
	{
		Map<String, ColumnState> saved = new HashMap<>();
		create();
		table.setColumnStateStore(new IColumnStateStore()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public ColumnState load(String key)
			{
				return new ColumnState(List.of("0", "2", "1", "3"), Set.of(), Map.of());
			}

			@Override
			public void save(String key, ColumnState state)
			{
				saved.put(key, state);
			}
		});
		tester.startPage(new TablePage(table));
		assertEquals(List.of(number, id, name, fixed), table.getColumns());

		executeAction(DynamicDataTable.MOVE_ACTION, "2,1");

		assertEquals(List.of("0", "1", "2", "3"), saved.get(table.getColumnStateKey()).getOrder());
	}

	@Test
	void aTableRenderedAgainViaAjaxGetsItsRowsAlongEscaped()
	{
		create();
		table = new TestTable(List.of(number, name, id, fixed),
			List.of(new Person("1", "</script><b>&")));
		tester.startPage(new TablePage(table));
		assertFalse(tester.getLastResponseAsString().contains("\"rows\""),
			"a page fetches its rows");

		executeAction(DynamicDataTable.MOVE_ACTION, "1,3");

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("\"rows\":[{\"key\":\"1\""), response);
		assertTrue(response.contains("\\u003c/script\\u003e\\u003cb\\u003e\\u0026"), response);
		assertFalse(response.contains("</script><b>"), response);
	}

	private void start()
	{
		create();
		tester.startPage(new TablePage(table));
	}

	private void create()
	{
		number = new RowNumberColumn<>(Model.of("#"));
		name = new SortableDynamicColumn<>(new MovableColumn("Name", "{{name}}"), "name");
		id = new MovableColumn("Id", "{{id}}");
		fixed = new HideableColumn("Fixed", "fixed");
		table = new TestTable(List.of(number, name, id, fixed));
	}

	private void executeAction(String action, String key)
	{
		tester.getRequest().getPostParameters().setParameterValue("action", action);
		tester.getRequest().getPostParameters().setParameterValue("key", key);
		tester.executeBehavior(table.getBehaviors(AbstractDefaultAjaxBehavior.class).get(0));
	}

	private static class HideableColumn extends DynamicColumn<Person> implements IHideableColumn
	{
		private static final long serialVersionUID = 1L;

		HideableColumn(String header, String template)
		{
			super(Model.of(header), template);
		}
	}

	private static class MovableColumn extends HideableColumn implements IMovableColumn
	{
		private static final long serialVersionUID = 1L;

		MovableColumn(String header, String template)
		{
			super(header, template);
		}
	}

	private static class TestTable extends DynamicDataTable<Person, String>
	{
		private static final long serialVersionUID = 1L;

		int changes;

		TestTable(List<IDynamicColumn<Person>> columns)
		{
			this(columns, PEOPLE);
		}

		TestTable(List<IDynamicColumn<Person>> columns, List<Person> people)
		{
			super("table", columns, new PeopleProvider(people), 10);
			setNavigationPosition(ToolbarPosition.TOP);
		}

		@Override
		protected void onColumnsChanged(AjaxRequestTarget target)
		{
			changes++;
		}
	}

	private static class PeopleProvider implements IDynamicDataProvider<Person, String>
	{
		private static final long serialVersionUID = 1L;

		private final List<Person> people;

		PeopleProvider(List<Person> people)
		{
			this.people = people;
		}

		@Override
		public List<Person> rows(long first, long count)
		{
			return people.subList((int)first, (int)Math.min(people.size(), first + count));
		}

		@Override
		public long size()
		{
			return people.size();
		}

		@Override
		public Person findByKey(String key)
		{
			return people.stream().filter(p -> p.getId().equals(key)).findFirst().orElse(null);
		}

		@Override
		public Class<String> getKeyType()
		{
			return String.class;
		}
	}
}
