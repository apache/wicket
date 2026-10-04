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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTable.ToolbarPosition;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.visit.IVisit;
import org.junit.jupiter.api.Test;

class DynamicDataTableToolbarTest extends WicketTestCase
{
	private static final String CSV = "table:topNavigation:span:actionsContainer:actions:1";

	private RowsProvider provider;

	private TestTable table;

	private void start(int rows, boolean withPrefix)
	{
		tester.getSession().setLocale(Locale.US);
		provider = new RowsProvider(rows);
		table = new TestTable(provider, withPrefix);
		table.setNavigationPosition(ToolbarPosition.TOP);
		tester.startPage(new TablePage(table));
	}

	@Test
	void theTableCarriesItsCssClass()
	{
		start(5, false);

		assertTrue(tester.getTagByWicketId("table").getAttribute("class").contains(
			DynamicDataTable.CSS_CLASS));
	}

	@Test
	void customToolbarsAreRenderedAboveTheHeadersAndBelowTheRows()
	{
		provider = new RowsProvider(5);
		table = new TestTable(provider, false);
		table.addTopToolbar(new TextToolbar(table, "top toolbar"));
		table.addBottomToolbar(new TextToolbar(table, "bottom toolbar"));
		tester.startPage(new TablePage(table));

		String page = tester.getLastResponseAsString();
		int top = page.indexOf("top toolbar");
		int headers = page.indexOf("wicket:id=\"headersRow\"");
		int body = page.indexOf("<tbody>");
		int bottom = page.indexOf("bottom toolbar");
		assertTrue(top > 0 && top < headers, page);
		assertTrue(bottom > body, page);
	}

	@Test
	void customToolbarsAreRefreshedWithTheRows()
	{
		provider = new RowsProvider(5);
		table = new TestTable(provider, false);
		TextToolbar toolbar = new TextToolbar(table, "custom");
		table.addTopToolbar(toolbar);
		tester.startPage(new TablePage(table));

		tester.executeAjaxEvent("table:bottomNavigation:span:paging:navigator:next", "click");

		tester.assertComponentOnAjaxResponse(toolbar);
	}

	@Test
	void aToolbarMustBeATableRow()
	{
		provider = new RowsProvider(5);
		table = new TestTable(provider, false)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected AbstractDynamicToolbar newNavigationToolbar(String id,
				ToolbarPosition position)
			{
				return new TextToolbar(this, "wrong id");
			}
		};

		assertThrows(RuntimeException.class, () -> tester.startPage(new TablePage(table)));
	}

	@Test
	void theSelectionBarIsShownAtTheTopByDefault()
	{
		start(5, false);
		selectPage();

		tester.assertVisible("table:topSelection");
		tester.assertInvisible("table:bottomSelection");
	}

	@Test
	void theSelectionBarCanBeShownAtTheTopAndTheBottom()
	{
		start(5, false);
		table.setSelectionToolbarPosition(ToolbarPosition.BOTH);
		selectPage();

		tester.assertVisible("table:topSelection");
		tester.assertVisible("table:bottomSelection");
		tester.assertComponentOnAjaxResponse("table:bottomSelection");
	}

	@Test
	void aVisiblePrefixKeepsTheNavigationShown()
	{
		start(2, true);

		assertEquals(1, table.getPageCount());
		tester.assertVisible("table:topNavigation");
		tester.assertLabel("table:topNavigation:span:prefix", "filter");
	}

	@Test
	void aVisiblePrefixIsNotRepaintedWithTheRows()
	{
		start(5, true);
		String toolbarId = tester.getComponentFromLastRenderedPage("table:topNavigation")
			.getMarkupId();

		tester.clickLink("table:topNavigation:span:paging:navigator:next");

		tester.assertComponentOnAjaxResponse("table:topNavigation:span:paging");
		assertFalse(tester.getLastResponseAsString().contains("<component id=\"" + toolbarId +
			"\""));
	}

	@Test
	void theNavigationIsHiddenOnASinglePageWithoutPrefixOrActions()
	{
		DynamicDataTable<Row, Long> plain = new DynamicDataTable<>("table", columns(),
			new RowsProvider(2), 2);
		tester.startPage(new TablePage(plain));

		tester.assertInvisible("table:bottomNavigation");
	}

	@Test
	void toolbarActionsAreShownAfterThePagingLinks()
	{
		start(5, false);

		tester.assertVisible(CSV);
		String page = tester.getLastResponseAsString();
		assertTrue(page.indexOf("dynamic-data-table-export-csv") > page.indexOf(
			"wicket:id=\"navigator\""), page);
		assertTrue(page.contains("aria-label=\"Export to CSV\""), page);
		assertTrue(page.contains("<svg aria-hidden=\"true\""), page);
	}

	@Test
	void csvExportWritesTheExportableColumnsOfAllRows()
	{
		start(3, false);

		assertEquals("\"Id\",\"Name\",\"Born\"\r\n" + "\"1\",\"Row \"\"1\"\"\",\"2001-01-01\"\r\n" +
			"\"2\",\"Row \"\"2\"\"\",\"2001-01-02\"\r\n" + "\"3\",\"Row \"\"3\"\"\",\"2001-01-03\"\r\n",
			exportCsv());
		assertTrue(tester.getLastResponse().getContentType().startsWith("text/csv"));
		assertTrue(tester.getLastResponse().getHeader("Content-Disposition").contains(
			"rows.csv"));
	}

	@Test
	void csvExportWritesTheSelectedRowsIfAny()
	{
		start(3, false);
		executeAction(DynamicDataTable.SELECT_ACTION, "2");
		tester.startPage(tester.getLastRenderedPage());

		assertEquals("\"Id\",\"Name\",\"Born\"\r\n\"2\",\"Row \"\"2\"\"\",\"2001-01-02\"\r\n",
			exportCsv());
	}

	@Test
	void csvExportWritesSelectedRowsOfEveryPage()
	{
		start(5, false);
		executeAction(DynamicDataTable.SELECT_ACTION, "1");
		tester.clickLink("table:topNavigation:span:paging:navigator:next");
		executeAction(DynamicDataTable.SELECT_ACTION, "4");
		tester.startPage(tester.getLastRenderedPage());

		assertEquals(csv(1, 4), exportCsv());
	}

	@Test
	void csvExportWritesEveryRowOnceAllAreSelected()
	{
		start(5, false);
		selectPage();
		clickSelectAll();
		tester.startPage(tester.getLastRenderedPage());

		assertEquals(csv(1, 2, 3, 4, 5), exportCsv());
	}

	@Test
	void csvExportFollowsTheSelectionNarrowedAfterSelectingAll()
	{
		start(5, false);
		selectPage();
		clickSelectAll();
		executeAction(DynamicDataTable.DESELECT_ACTION, "1");
		tester.startPage(tester.getLastRenderedPage());

		assertEquals(csv(2), exportCsv());
	}

	@Test
	void csvExportWritesAllRowsAgainOnceNothingIsSelected()
	{
		start(3, false);
		executeAction(DynamicDataTable.SELECT_ACTION, "2");
		executeAction(DynamicDataTable.DESELECT_ACTION, "2");
		tester.startPage(tester.getLastRenderedPage());

		assertEquals(csv(1, 2, 3), exportCsv());
	}

	@Test
	void csvExportSkipsSelectedRowsThatAreGone()
	{
		start(3, false);
		executeAction(DynamicDataTable.SELECT_ACTION, "1");
		executeAction(DynamicDataTable.SELECT_ACTION, "2");
		provider.rows.removeIf(row -> row.getId() == 2);
		tester.startPage(tester.getLastRenderedPage());

		assertEquals(csv(1), exportCsv());
	}

	private static String csv(int... ids)
	{
		StringBuilder csv = new StringBuilder("\"Id\",\"Name\",\"Born\"\r\n");
		for (int id : ids)
		{
			csv.append("\"").append(id).append("\",\"Row \"\"").append(id).append(
				"\"\"\",\"").append(LocalDate.of(2001, 1, id)).append("\"\r\n");
		}
		return csv.toString();
	}

	private void clickSelectAll()
	{
		Component link = table.visitChildren(AjaxLink.class,
			(AjaxLink<?> candidate, IVisit<Component> visit) -> {
				if ("selectAll".equals(candidate.getId()))
				{
					visit.stop(candidate);
				}
			});
		tester.clickLink(link);
		assertTrue(table.isAllSelected());
	}

	@Test
	void sortingClearsTheSelectionAndRepaintsTheRows()
	{
		start(5, false);
		selectPage();
		table.selectionChanges = 0;

		tester.clickLink("table:headersRow:headers:2:sort");

		assertTrue(table.getSelection().isEmpty());
		assertEquals(1, table.selectionChanges);
		assertEquals(new SortParam<>("name", true), provider.getSort());
		String response = tester.getLastResponseAsString();
		assertTrue(response.contains(".update({\"rows\":"), response);
		assertTrue(response.contains("\"selected\":false"), response);
		assertFalse(response.contains("\"selected\":true"), response);
	}

	@Test
	void sentRowsAreReported()
	{
		start(5, false);

		tester.clickLink("table:topNavigation:span:paging:navigator:next");

		assertEquals(List.of(3L, 4L), table.sent);
	}

	@Test
	void sortableColumnsDelegateTheirExportValue()
	{
		SortableDynamicColumn<Row, String> sortable = new SortableDynamicColumn<>(
			new DynamicColumn<Row>(Model.of("Name"), "{{name}}").setExportValue(Row::getName),
			"name");

		assertTrue(sortable.isExportable());
		assertEquals("Row \"1\"", sortable.getExportValue(new Row(1)));
	}

	private String exportCsv()
	{
		tester.clickLink(CSV + ":link");
		assertTrue(tester.getLastResponseAsString().contains("Wicket.AjaxDownload.initiate("));
		Component button = tester.getComponentFromLastRenderedPage(CSV);
		tester.invokeListener(button, resourceBehavior(button));
		return tester.getLastResponseAsString();
	}

	private static Behavior resourceBehavior(Component button)
	{
		return button.getBehaviors()
			.stream()
			.filter(behavior -> behavior.getClass().getName().endsWith("$ResourceBehavior"))
			.findFirst()
			.orElseThrow();
	}

	private void selectPage()
	{
		executeAction(DynamicDataTable.SELECT_PAGE_ACTION, null);
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

	private static List<IDynamicColumn<Row>> columns()
	{
		List<IDynamicColumn<Row>> columns = new ArrayList<>();
		columns.add(new SelectionColumn<>());
		columns.add(new DynamicColumn<Row>(Model.of("Id"), "{{id}}").setExportValue(Row::getId));
		columns.add(new SortableDynamicColumn<>(
			new DynamicColumn<Row>(Model.of("Name"), "{{name}}").setExportValue(Row::getName),
			"name"));
		columns.add(new DynamicColumn<Row>(Model.of("Born"), "{{born}}").setExportValue(
			Row::getBorn));
		columns.add(new DynamicColumn<>(Model.of("Not exported"), "-"));
		return columns;
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

		public String getName()
		{
			return "Row \"" + id + "\"";
		}

		public LocalDate getBorn()
		{
			return LocalDate.of(2001, 1, (int)id);
		}
	}

	private static class RowsProvider extends SortableDynamicDataProvider<Row, Long, String>
	{
		private static final long serialVersionUID = 1L;

		final List<Row> rows = new ArrayList<>();

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

		private final boolean withPrefix;

		int selectionChanges;

		List<Long> sent;

		TestTable(RowsProvider provider, boolean withPrefix)
		{
			super("table", columns(), provider, 2);
			this.withPrefix = withPrefix;
			addToolbarAction(new CsvExportToolbarAction<Row, Long>().setFileName("rows.csv"));
		}

		@Override
		protected AbstractDynamicToolbar newNavigationToolbar(String id, ToolbarPosition position)
		{
			return new DynamicNavigationToolbar<>(id, this, position)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected Component newPrefix(String id)
				{
					return withPrefix ? new Label(id, "filter") : super.newPrefix(id);
				}
			};
		}

		@Override
		protected void onSelectionChanged(AjaxRequestTarget target)
		{
			selectionChanges++;
		}

		@Override
		protected void onRowsSent(List<Long> keys)
		{
			sent = keys;
		}
	}

	private static class TextToolbar extends AbstractDynamicToolbar
		implements
			IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TextToolbar(DynamicDataTable<?, ?> table, String text)
		{
			super(table);
			add(new Label("text", text));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<wicket:panel><td><span wicket:id=\"text\"></span></td></wicket:panel>");
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
