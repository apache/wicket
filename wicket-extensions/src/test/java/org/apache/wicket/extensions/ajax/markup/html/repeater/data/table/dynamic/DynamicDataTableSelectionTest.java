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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.ISelection.SelectionType;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

class DynamicDataTableSelectionTest extends WicketTestCase
{
	private static final String TOOLBAR = "table:topSelection";

	private static final String SELECT_ALL = TOOLBAR + ":span:page:selectAll";

	private static final String CLEAR = TOOLBAR + ":span:all:clear";

	private RowsProvider provider;

	private TestTable table;

	private void start(int rows)
	{
		provider = new RowsProvider(rows);
		table = new TestTable(provider);
		tester.startPage(new TablePage(table));
	}

	@Test
	void selectionColumnRendersCheckboxesWithLabels()
	{
		start(5);

		String page = tester.getLastResponseAsString();
		assertTrue(page.contains("<input type=\"checkbox\" " +
			"class=\"dynamic-data-table-select-page\" data-dt-action=\"_wicket-select-page\" " +
			"data-dt-action-unchecked=\"_wicket-deselect-page\" " +
			"aria-label=\"Select all rows on this page\">"), page);
		assertTrue(page.contains("class=\\\"dynamic-data-table-select\\\" data-dt-action=" +
			"\\\"_wicket-select\\\" data-dt-action-unchecked=\\\"_wicket-deselect\\\" " +
			"aria-label=\\\"Select row\\\">"), page);
		assertTrue(page.contains("data-resizable=\"false\""), page);
		tester.assertInvisible(TOOLBAR);
	}

	@Test
	void selectingARowSelectsItsKey()
	{
		start(5);

		executeAction(DynamicDataTable.SELECT_ACTION, "2");

		ISelection<Row, Long> selection = table.getSelection();
		assertEquals(SelectionType.RANGE, selection.getType());
		assertEquals(List.of(2L), selection.getKeys());
		assertTrue(selection.isSelected(2L));
		assertFalse(selection.isSelected(1L));
		assertEquals(1, table.changes);
		tester.assertInvisible(TOOLBAR);
	}

	@Test
	void deselectingARowRemovesItsKey()
	{
		start(5);

		executeAction(DynamicDataTable.SELECT_ACTION, "2");
		executeAction(DynamicDataTable.SELECT_ACTION, "1");
		executeAction(DynamicDataTable.DESELECT_ACTION, "2");

		assertEquals(List.of(1L), table.getSelection().getKeys());
		assertEquals(3, table.changes);
	}

	@Test
	void keysOfNoRowAreIgnored()
	{
		start(5);

		executeAction(DynamicDataTable.SELECT_ACTION, "abc");
		executeAction(DynamicDataTable.SELECT_ACTION, "42");
		executeAction(DynamicDataTable.SELECT_ACTION, null);

		assertTrue(table.getSelection().isEmpty());
		assertEquals(0, table.changes);
	}

	@Test
	void servedRowsCarryTheirSelection()
	{
		start(5);
		executeAction(DynamicDataTable.SELECT_ACTION, "2");

		JSONArray rows = served();

		assertEquals("1", rows.getJSONObject(0).getString("key"));
		assertFalse(rows.getJSONObject(0).getBoolean("selected"));
		assertTrue(rows.getJSONObject(1).getBoolean("selected"));
	}

	@Test
	void selectingThePageOffersToSelectAllRows()
	{
		start(5);

		executeAction(DynamicDataTable.SELECT_PAGE_ACTION, null);

		assertEquals(List.of(1L, 2L), table.getSelection().getKeys());
		tester.assertVisible(TOOLBAR);
		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("All 2 rows on this page are selected."), response);
		assertTrue(response.contains("Select all 5 rows"), response);
	}

	@Test
	void selectingEveryRowOfThePageOneByOneOffersToSelectAllRows()
	{
		start(5);

		executeAction(DynamicDataTable.SELECT_ACTION, "1");
		tester.assertInvisible(TOOLBAR);
		executeAction(DynamicDataTable.SELECT_ACTION, "2");

		tester.assertVisible(TOOLBAR);
	}

	@Test
	void selectingAllRowsIsNotOfferedOnASinglePage()
	{
		start(2);

		executeAction(DynamicDataTable.SELECT_PAGE_ACTION, null);

		tester.assertInvisible(TOOLBAR);
	}

	@Test
	void deselectingThePageRemovesItsKeys()
	{
		start(5);
		executeAction(DynamicDataTable.SELECT_ACTION, "4");
		executeAction(DynamicDataTable.SELECT_PAGE_ACTION, null);

		executeAction(DynamicDataTable.DESELECT_PAGE_ACTION, null);

		assertEquals(List.of(4L), table.getSelection().getKeys());
		tester.assertInvisible(TOOLBAR);
	}

	@Test
	void selectAllSelectsEveryRowOfTheProvider()
	{
		start(5);
		executeAction(DynamicDataTable.SELECT_PAGE_ACTION, null);

		tester.clickLink(SELECT_ALL);

		ISelection<Row, Long> selection = table.getSelection();
		assertEquals(SelectionType.ALL, selection.getType());
		assertEquals(List.of(), selection.getKeys());
		assertEquals(5, selection.size());
		assertTrue(selection.isSelected(5L));
		assertEquals(List.of(1L, 2L, 3L, 4L, 5L), ids(selection.fetch()));
		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("All 5 rows are selected."), response);
		assertTrue(response.contains("Clear selection"), response);
		assertTrue(response.contains(".selectRows(true);"), response);
		assertEquals(2, table.changes);
	}

	@Test
	void allRowsAreFetchedInChunks()
	{
		start(250);
		executeAction(DynamicDataTable.SELECT_PAGE_ACTION, null);
		tester.clickLink(SELECT_ALL);
		provider.counts.clear();

		assertEquals(250, ids(table.getSelection().fetch()).size());
		assertEquals(List.of(100L, 100L, 50L), provider.counts);
	}

	@Test
	void clearingTheSelectionOfAllRows()
	{
		start(5);
		executeAction(DynamicDataTable.SELECT_PAGE_ACTION, null);
		tester.clickLink(SELECT_ALL);

		tester.clickLink(CLEAR);

		assertEquals(SelectionType.RANGE, table.getSelection().getType());
		assertTrue(table.getSelection().isEmpty());
		assertTrue(tester.getLastResponseAsString().contains(".selectRows(false);"));
		tester.assertInvisible(TOOLBAR);
	}

	@Test
	void deselectingARowLeavesTheSelectionOfAllRows()
	{
		start(5);
		executeAction(DynamicDataTable.SELECT_PAGE_ACTION, null);
		tester.clickLink(SELECT_ALL);

		executeAction(DynamicDataTable.DESELECT_ACTION, "1");

		assertEquals(SelectionType.RANGE, table.getSelection().getType());
		assertEquals(List.of(2L), table.getSelection().getKeys());
		tester.assertInvisible(TOOLBAR);
	}

	@Test
	void theSelectionSurvivesPaging()
	{
		start(5);
		executeAction(DynamicDataTable.SELECT_ACTION, "2");

		table.setCurrentPage(1);
		assertFalse(served().getJSONObject(0).getBoolean("selected"));
		table.setCurrentPage(0);

		assertTrue(served().getJSONObject(1).getBoolean("selected"));
		assertEquals(List.of(2L), table.getSelection().getKeys());
	}

	@Test
	void keysOfDeletedRowsAreSkippedWhenFetching()
	{
		start(5);
		executeAction(DynamicDataTable.SELECT_ACTION, "3");
		executeAction(DynamicDataTable.SELECT_ACTION, "1");

		provider.rows.removeIf(row -> row.getId() == 3);

		assertEquals(2, table.getSelection().size());
		assertEquals(List.of(1L), ids(table.getSelection().fetch()));
	}

	@Test
	void clearSelectionDeselectsEveryRow()
	{
		start(5);
		executeAction(DynamicDataTable.SELECT_PAGE_ACTION, null);

		table.clearSelection();

		assertTrue(table.getSelection().isEmpty());
	}

	@Test
	void rowKeysAreFormattedAndParsedByTheProvider()
	{
		start(5);

		assertEquals("3", table.getRowKey(provider.rows.get(2)));
		assertEquals(3, table.findRow("3").getId());
		assertNull(table.findRow("x"));
	}

	private void executeAction(String action, String key)
	{
		tester.getRequest().getPostParameters().setParameterValue("action", action);
		if (key != null)
		{
			tester.getRequest().getPostParameters().setParameterValue("key", key);
		}
		tester.executeBehavior(table.getBehaviors(AbstractDefaultAjaxBehavior.class).get(0));
	}

	private JSONArray served()
	{
		tester.executeBehavior(table.getBehaviors(AbstractAjaxBehavior.class)
			.stream()
			.filter(behavior -> behavior instanceof AbstractDefaultAjaxBehavior == false)
			.findFirst()
			.orElseThrow());
		return new JSONObject(tester.getLastResponseAsString()).getJSONArray("rows");
	}

	private static List<Long> ids(Iterable<Row> rows)
	{
		List<Long> ids = new ArrayList<>();
		rows.forEach(row -> ids.add(row.getId()));
		return ids;
	}

	public static class Row implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final long id;

		Row(long id)
		{
			this.id = id;
		}

		public long getId()
		{
			return id;
		}
	}

	private static class RowsProvider extends SortableDynamicDataProvider<Row, Long, String>
	{
		private static final long serialVersionUID = 1L;

		final List<Row> rows = new ArrayList<>();

		final List<Long> counts = new ArrayList<>();

		RowsProvider(int size)
		{
			super(Long.class, Row::getId);
			for (long id = 1; id <= size; id++)
			{
				rows.add(new Row(id));
			}
		}

		@Override
		public List<Row> rows(long first, long count)
		{
			counts.add(count);
			return new ArrayList<>(rows.subList((int)first, (int)(first + count)));
		}

		@Override
		public long size()
		{
			return rows.size();
		}

		@Override
		public Row findByKey(Long key)
		{
			return rows.stream().filter(row -> row.getId() == key).findFirst().orElse(null);
		}
	}

	private static class TestTable extends DynamicDataTable<Row, Long>
	{
		private static final long serialVersionUID = 1L;

		int changes;

		TestTable(RowsProvider provider)
		{
			super("table", List.of(new SelectionColumn<>(),
				new DynamicColumn<>(Model.of("Id"), "{{id}}")), provider, 2);
		}

		@Override
		protected void onSelectionChanged(AjaxRequestTarget target)
		{
			changes++;
		}
	}

	public static class TablePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TablePage(DynamicDataTable<Row, Long> table)
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
