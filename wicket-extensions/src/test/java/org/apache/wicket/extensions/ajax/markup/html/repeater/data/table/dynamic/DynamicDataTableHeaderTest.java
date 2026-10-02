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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTable.ToolbarPosition;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ResizableColumnsBehavior;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

class DynamicDataTableHeaderTest extends WicketTestCase
{
	private static final List<Person> PEOPLE = List.of(new Person("2", "Linus"),
		new Person("1", "Ada"), new Person("3", "Grace"));

	private static final String NAME_SORT = "table:headersRow:headers:1:sort";

	@Test
	void sortableHeaderIsALinkWithBothArrows()
	{
		startTable(new SortablePeopleProvider(), 10);

		String page = tester.getLastResponseAsString();
		assertTrue(page.contains("aria-sort=\"none\""), page);
		assertHeaderClass(1, "wicket_orderNone");
		assertTrue(page.contains("<span class=\"dynamic-data-table-sort-asc\">&#9650;</span>" +
			"<span class=\"dynamic-data-table-sort-desc\">&#9660;</span>"), page);
		assertTrue(tester.getComponentFromLastRenderedPage(NAME_SORT).isEnabledInHierarchy());
		assertFalse(tester.getComponentFromLastRenderedPage("table:headersRow:headers:2:sort")
			.isEnabledInHierarchy(), "a column without sort property is not sortable");
		tester.assertInvisible("table:headersRow:headers:2:arrows");
	}

	@Test
	void onlyASortableHeaderLinkHasAMarkupId()
	{
		startTable(new SortablePeopleProvider(), 10);

		assertTrue(tester.getComponentFromLastRenderedPage(NAME_SORT).getOutputMarkupId());
		assertFalse(tester.getComponentFromLastRenderedPage("table:headersRow:headers:2:sort")
			.getOutputMarkupId(), "a header rendering its body only cannot carry an id");
	}

	@Test
	void clickingTheHeaderSortsAscendingThenDescending()
	{
		SortablePeopleProvider provider = new SortablePeopleProvider();
		DynamicDataTable<Person, String> table = startTable(provider, 10);

		tester.clickLink(NAME_SORT);

		assertEquals(new SortParam<>("name", true), provider.getSort());
		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("aria-sort=\"ascending\""), response);
		assertHeaderClass(1, "wicket_orderUp");
		assertTrue(response.contains("dynamic-data-table-sort-desc"), response);
		assertTrue(response.contains(".update({"), response);
		assertEquals(List.of("Ada", "Grace", "Linus"), names(served(table)));

		tester.clickLink(NAME_SORT);

		assertEquals(new SortParam<>("name", false), provider.getSort());
		assertTrue(tester.getLastResponseAsString().contains("aria-sort=\"descending\""));
		assertHeaderClass(1, "wicket_orderDown");
		assertEquals(List.of("Linus", "Grace", "Ada"), names(served(table)));
	}

	@Test
	void sortingShowsTheFirstPage()
	{
		DynamicDataTable<Person, String> table = startTable(new SortablePeopleProvider(), 1);
		table.setCurrentPage(2);

		tester.clickLink(NAME_SORT);

		assertEquals(0, table.getCurrentPage());
	}

	@Test
	void headersAreNotSortableWithoutASortableProvider()
	{
		startTable(new PeopleProvider(), 10);

		assertFalse(tester.getComponentFromLastRenderedPage(NAME_SORT).isEnabledInHierarchy());
		assertFalse(tester.getLastResponseAsString().contains("aria-sort"));
	}

	@Test
	void rowsAreNumberedAcrossPages()
	{
		DynamicDataTable<Person, String> table = startTable(new SortablePeopleProvider(), 2);
		table.setCurrentPage(1);

		JSONArray rows = served(table);

		assertEquals(1, rows.length());
		assertEquals(3, rows.getJSONObject(0).getInt("number"));
	}

	@Test
	void rowNumberColumnShowsTheRowVariable()
	{
		assertEquals("{{@number}}", new RowNumberColumn<Person>(Model.of("#")).getTemplate());
	}

	@Test
	void navigationIsAtTheBottomByDefault()
	{
		startTable(new PeopleProvider(), 1);

		tester.assertVisible("table:bottomNavigation");
		tester.assertInvisible("table:topNavigation");
	}

	@Test
	void navigationCanBeOnTopOrOnBothEnds()
	{
		DynamicDataTable<Person, String> table = table(new PeopleProvider(), 1)
			.setNavigationPosition(ToolbarPosition.TOP);
		tester.startPage(new TablePage(table));
		tester.assertVisible("table:topNavigation");
		tester.assertInvisible("table:bottomNavigation");

		table.setNavigationPosition(ToolbarPosition.BOTH);
		tester.startPage(tester.getLastRenderedPage());
		tester.assertVisible("table:topNavigation");
		tester.assertVisible("table:bottomNavigation");
	}

	@Test
	void navigationIsHiddenOnASinglePageWherever()
	{
		DynamicDataTable<Person, String> table = table(new PeopleProvider(), 10)
			.setNavigationPosition(ToolbarPosition.BOTH);
		tester.startPage(new TablePage(table));

		tester.assertInvisible("table:topNavigation");
		tester.assertInvisible("table:bottomNavigation");
	}

	@Test
	void pagingRerendersBothNavigations()
	{
		DynamicDataTable<Person, String> table = table(new PeopleProvider(), 1)
			.setNavigationPosition(ToolbarPosition.BOTH);
		tester.startPage(new TablePage(table));

		tester.clickLink("table:topNavigation:span:paging:navigator:next");

		assertEquals(1, table.getCurrentPage());
		tester.assertComponentOnAjaxResponse("table:topNavigation");
		tester.assertComponentOnAjaxResponse("table:bottomNavigation");
	}

	@Test
	void sortableColumnWrapperDelegatesToTheColumn()
	{
		IDynamicColumn<Person> column = new DynamicColumn<Person>(Model.of("Name"), "{{name}}")
			.setCssClass("name");
		SortableDynamicColumn<Person, String> sortable = new SortableDynamicColumn<>(column,
			"name");

		assertEquals("name", sortable.getSortProperty());
		assertTrue(sortable.isSortable());
		assertEquals("Name", sortable.getHeader().getObject());
		assertEquals("{{name}}", sortable.getTemplate());
		assertEquals("name", sortable.getCssClass());
	}

	@Test
	void wrappedCompoundColumnRegistersItsActions()
	{
		RecordingAction select = new RecordingAction();
		DynamicDataTable<Person, String> table = new DynamicDataTable<>("table",
			List.of(new SortableDynamicColumn<>(
				new CompoundDynamicColumn<>(Model.of("Actions"), List.of(select)), "actions")),
			new SortablePeopleProvider(), 10);

		assertThrows(IllegalArgumentException.class,
			() -> table.addActionContributor(new RecordingAction()));
	}

	@Test
	void wrappedColumnContributesItsHeaderItems()
	{
		DynamicDataTable<Person, String> table = new DynamicDataTable<>("table",
			List.of(new SortableDynamicColumn<>(
				new ProgressBarColumn<Person>(Model.of("Progress"), "progress"), "progress")),
			new SortablePeopleProvider(), 10);
		tester.startPage(new TablePage(table));

		tester.assertContains("wicket-progress-bar.*\\.css");
	}

	@Test
	void columnsAreNotResizableWithoutTheBehavior()
	{
		tester.startPage(new TablePage(table(new PeopleProvider(), 10)));

		String page = tester.getLastResponseAsString();
		assertFalse(page.contains("\"resize\""), page);
		assertFalse(page.contains("wicket-resizable-columns"), page);
	}

	@Test
	void resizingIsConfiguredForTheBrowser()
	{
		DynamicDataTable<Person, String> table = resizable(table(new PeopleProvider(), 10));
		tester.startPage(new TablePage(table));

		tester.assertContains(Pattern.quote("\"resize\":{}"));
		tester.assertContains(Pattern.quote("Wicket.ResizableColumns.attach("));
		tester.assertContains("wicket-resizable-columns.*\\.js");
		tester.assertContains("wicket-resizable-columns.*\\.css");
		tester.assertContains(Pattern.quote("data-resizable=\"false\""));
	}

	@Test
	void resizedWidthsAreStoredAndRenderedAgain()
	{
		DynamicDataTable<Person, String> table = resizable(table(new PeopleProvider(), 10));
		tester.startPage(new TablePage(table));

		resize(table, "40,120.5,80");

		assertArrayEquals(new double[] { 40, 120.5, 80 }, table.getColumnWidths());
		tester.startPage(tester.getLastRenderedPage());
		tester.assertContains(Pattern.quote("\"widths\":[40,120.5,80]"));
		tester.assertContains(Pattern.quote("options.widths = [40,120.5,80];"));
	}

	@Test
	void invalidWidthsAreIgnored()
	{
		DynamicDataTable<Person, String> table = resizable(table(new PeopleProvider(), 10));
		tester.startPage(new TablePage(table));

		for (String widths : List.of("40,120", "a,b,c", "-1,2,3", "Infinity,1,1", "1,2,1e9"))
		{
			resize(table, widths);
			assertNull(table.getColumnWidths(), widths);
		}
	}

	private static DynamicDataTable<Person, String> resizable(
		DynamicDataTable<Person, String> table)
	{
		table.add(new ResizableColumnsBehavior(ResizableColumnsBehavior.Mode.STRETCH));
		return table;
	}

	private void resize(DynamicDataTable<Person, String> table, String widths)
	{
		tester.getRequest().getPostParameters().setParameterValue("widths", widths);
		tester.executeBehavior(table.getBehaviors(ResizableColumnsBehavior.class).get(0));
	}

	@Test
	void proportionsAndParentWidthAreSentToTheBrowser()
	{
		DynamicDataTable<Person, String> table = table(new PeopleProvider(), 10)
			.setColumnProportions(1, 2.5, 1)
			.setAdjustToParentWidth(true);
		tester.startPage(new TablePage(table));

		tester.assertContains(
			Pattern.quote("\"layout\":{\"proportions\":[1,2.5,1],\"adjustToParent\":true}"));
		assertArrayEquals(new double[] { 1, 2.5, 1 }, table.getColumnProportions());
	}

	@Test
	void withoutProportionsOrParentWidthNoLayoutIsSent()
	{
		tester.startPage(new TablePage(table(new PeopleProvider(), 10)));

		assertFalse(tester.getLastResponseAsString().contains("\"layout\""));
	}

	@Test
	void proportionsHaveToBePositiveAndOnePerColumn()
	{
		DynamicDataTable<Person, String> table = table(new PeopleProvider(), 10);

		assertThrows(IllegalArgumentException.class, () -> table.setColumnProportions(1, 2));
		assertThrows(IllegalArgumentException.class, () -> table.setColumnProportions(1, 0, 1));
		assertThrows(IllegalArgumentException.class,
			() -> table.setColumnProportions(1, Double.NaN, 1));
		assertNull(table.setColumnProportions((double[])null).getColumnProportions());
	}

	@Test
	void actionsWithTheReservedPrefixAreRejected()
	{
		DynamicDataTable<Person, String> table = table(new PeopleProvider(), 10);

		assertThrows(IllegalArgumentException.class,
			() -> table.addActionContributor(action(DynamicDataTable.SELECT_ACTION)));
		assertThrows(IllegalArgumentException.class,
			() -> table.addActionContributor(action("_wicketCustom")));
		assertThrows(IllegalArgumentException.class, () -> table.setColumnWidths(1, 2));
	}


	private static RecordingAction action(String actionId)
	{
		return new RecordingAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getActionId()
			{
				return actionId;
			}
		};
	}

	@Test
	void rowsPerPageCanBeChosen()
	{
		DynamicDataTable<Person, String> table = table(new PeopleProvider(), 10)
			.setItemsPerPageOptions(1L, 2L, 10L);
		tester.startPage(new TablePage(table));
		tester.assertVisible("table:bottomNavigation:span:pageSize");
		tester.assertInvisible("table:bottomNavigation:span:paging:navigator");

		DropDownChoice<?> choice = (DropDownChoice<?>)tester.getComponentFromLastRenderedPage(
			"table:bottomNavigation:span:pageSize:itemsPerPage");
		tester.getRequest().getPostParameters().setParameterValue(choice.getInputName(), "0");
		tester.executeAjaxEvent(choice, "change");

		assertEquals(1, table.getItemsPerPage());
		tester.assertComponentOnAjaxResponse("table:bottomNavigation");
		assertThrows(IllegalArgumentException.class, () -> table.setItemsPerPageOptions(0L));
	}

	@Test
	void navigationIsHiddenWithoutChoiceOnASinglePage()
	{
		startTable(new PeopleProvider(), 10);

		tester.assertInvisible("table:bottomNavigation");
	}

	private void assertHeaderClass(int index, String cssClass)
	{
		String th = tester.getTagsByWicketId("headers").get(index).getAttribute("class");
		assertTrue(th.contains(cssClass), th);
	}

	private void executeAction(DynamicDataTable<Person, String> table, String action, String key)
	{
		tester.getRequest().getPostParameters().setParameterValue("action", action);
		tester.getRequest().getPostParameters().setParameterValue("key", key);
		tester.executeBehavior(table.getBehaviors(AbstractDefaultAjaxBehavior.class).get(0));
	}

	private DynamicDataTable<Person, String> startTable(
		IDynamicDataProvider<Person, String> provider, long rowsPerPage)
	{
		DynamicDataTable<Person, String> table = table(provider, rowsPerPage);
		tester.startPage(new TablePage(table));
		return table;
	}

	private static DynamicDataTable<Person, String> table(
		IDynamicDataProvider<Person, String> provider, long rowsPerPage)
	{
		List<IDynamicColumn<Person>> columns = new ArrayList<>();
		columns.add(new RowNumberColumn<>(Model.of("#")));
		columns.add(new SortableDynamicColumn<>(new DynamicColumn<>(Model.of("Name"), "{{name}}"),
			"name"));
		columns.add(new DynamicColumn<>(Model.of("Id"), "{{id}}"));
		return new DynamicDataTable<>("table", columns, provider, rowsPerPage);
	}

	private JSONArray served(DynamicDataTable<Person, String> table)
	{
		tester.executeBehavior(table.getBehaviors(AbstractAjaxBehavior.class)
			.stream()
			.filter(behavior -> behavior instanceof AbstractDefaultAjaxBehavior == false)
			.findFirst()
			.orElseThrow());
		return new JSONObject(tester.getLastResponseAsString()).getJSONArray("rows");
	}

	private static List<String> names(JSONArray rows)
	{
		List<String> names = new ArrayList<>();
		for (int i = 0; i < rows.length(); i++)
		{
			JSONObject row = rows.getJSONObject(i);
			assertEquals(i + 1, row.getInt("number"));
			names.add(row.getJSONObject("data").getString("name"));
		}
		return names;
	}

	public static class Person implements Serializable, IIdentifiable<String>
	{
		private static final long serialVersionUID = 1L;

		private final String id;

		private final String name;

		Person(String id, String name)
		{
			this.id = id;
			this.name = name;
		}

		@Override
		public String getId()
		{
			return id;
		}

		public String getName()
		{
			return name;
		}
	}

	private static class PeopleProvider implements IDynamicDataProvider<Person, String>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public List<Person> rows(long first, long count)
		{
			return PEOPLE.subList((int)first, (int)(first + count));
		}

		@Override
		public long size()
		{
			return PEOPLE.size();
		}

		@Override
		public Person findByKey(String key)
		{
			return PEOPLE.stream().filter(p -> p.getId().equals(key)).findFirst().orElse(null);
		}

		@Override
		public Class<String> getKeyType()
		{
			return String.class;
		}
	}

	private static class SortablePeopleProvider
		extends
			SortableDynamicDataProvider<Person, String, String>
	{
		private static final long serialVersionUID = 1L;

		private final PeopleProvider people = new PeopleProvider();

		SortablePeopleProvider()
		{
			super(String.class);
		}

		@Override
		public List<Person> rows(long first, long count)
		{
			List<Person> sorted = new ArrayList<>(PEOPLE);
			SortParam<String> sort = getSort();
			if (sort != null)
			{
				Comparator<Person> byName = Comparator.comparing(Person::getName);
				sorted.sort(sort.isAscending() ? byName : byName.reversed());
			}
			return sorted.subList((int)first, (int)(first + count));
		}

		@Override
		public long size()
		{
			return people.size();
		}

		@Override
		public Person findByKey(String key)
		{
			return people.findByKey(key);
		}
	}

	private static class RecordingAction implements IAjaxActionColumnContributor<Person>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public String getActionId()
		{
			return "select";
		}

		@Override
		public String getTemplate()
		{
			return "<a href=\"#\" data-dt-action=\"select\">select</a>";
		}

		@Override
		public void onAction(Person row, AjaxRequestTarget target)
		{
		}
	}

	public static class TablePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TablePage(DynamicDataTable<Person, String> table)
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
