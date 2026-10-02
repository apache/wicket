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
package org.apache.wicket.examples.repeater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.CompoundDynamicColumn;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTable;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTable.BodyHeightUnits;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTable.OverlayPlacement;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.IDynamicColumn;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.RowNumberColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.MovableColumnsBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ResizableColumnsBehavior;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Edits and adds contacts of {@link DynamicDataTablePage}, and checks its columns.
 */
class DynamicDataTablePageTest
{
	private static final String TABLE = "table";

	private static final String FORM = TABLE + ":overlay:window:content:body:form";

	private WicketTester tester;

	private DynamicDataTable<Contact, Long> table;

	private Contact contact;

	private String city;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void startPage()
	{
		tester = new WicketTester(new RepeaterApplication());
		tester.startPage(DynamicDataTablePage.class);
		table = (DynamicDataTable<Contact, Long>)tester.getComponentFromLastRenderedPage(TABLE);
		contact = DatabaseLocator.getLargeDatabase().find(0, 1, null).get(0);
		city = contact.getCity();
	}

	@AfterEach
	void stop()
	{
		contact.setCity(city);
		tester.destroy();
	}

	@Test
	void actionsFollowTheRowNumberWithEditBeforeDeleteAndDownload()
	{
		List<IDynamicColumn<Contact>> columns = table.getColumns();
		assertTrue(columns.get(1) instanceof RowNumberColumn);
		IDynamicColumn<Contact> actions = columns.get(2);
		assertTrue(actions instanceof CompoundDynamicColumn);
		String template = actions.getTemplate(table);
		int edit = template.indexOf("data-dt-action=\"edit\"");
		int delete = template.indexOf("data-dt-action=\"delete\"");
		int download = template.indexOf("data-dt-action=\"download\"");
		assertTrue(edit >= 0 && edit < delete && delete < download, template);
		assertEquals(1, columns.stream()
			.filter(column -> column.getTemplate(table).contains("data-dt-action=\"edit\""))
			.count());
	}

	@Test
	void selectionNumberActionsAndIdCannotBeResized()
	{
		List<Boolean> resizable = table.getColumns()
			.stream()
			.map(IDynamicColumn::isResizable)
			.toList();

		assertEquals(List.of(false, false, false, false), resizable.subList(0, 4));
		assertTrue(resizable.subList(4, resizable.size()).stream().allMatch(Boolean::booleanValue));
		assertEquals(4, TagTester.createTagsByAttribute(tester.getLastResponseAsString(),
			"data-resizable", "false", false).size());
	}

	@Test
	void theExtraColumnsAreHiddenAtFirstAndOfferedByTheColumnChooser()
	{
		List<String> hidden = table.getColumns()
			.stream()
			.filter(column -> table.isColumnVisible(column) == false)
			.map(column -> column.getTemplate(table))
			.toList();

		assertEquals(List.of("{{homePhone}}", "{{cellPhone}}", "{{city}}", "{{countryName}}"),
			hidden);
		String page = tester.getLastResponseAsString();
		assertEquals(2 * 4, page.split("aria-pressed=\"false\"", -1).length - 1, page);
	}

	@Test
	void theSelectionRowNumberIdAndActionsAreFixedAndTheOthersMovable()
	{
		List<IDynamicColumn<Contact>> columns = table.getColumns();
		for (int i = 0; i < columns.size(); i++)
		{
			assertEquals(i > 3, table.isMovable(columns.get(i)), String.valueOf(i));
		}
	}

	@Test
	void anAddedContactIsSavedAndSelected()
	{
		ContactsDatabase database = DatabaseLocator.getLargeDatabase();
		int count = database.getCount();
		Component add = ((MarkupContainer)table.get("topNavigation")).visitChildren(Component.class,
			(child, visit) -> {
				if ("button".equals(child.getId()))
				{
					visit.stop(child);
				}
			});
		tester.clickLink(add);

		assertTrue(table.isOverlayShown());
		FormTester form = tester.newFormTester(FORM);
		form.setValue("firstName", "Zoe");
		form.setValue("lastName", "Quintana");
		form.setValue("country", "PA");
		form.setValue("city", "Panama");
		form.setValue("address", "1 Via Espana");
		tester.executeAjaxEvent(FORM + ":save", "click");

		Contact added = ((DynamicDataTablePage)tester.getLastRenderedPage()).getSelected();
		try
		{
			assertEquals(count + 1, database.getCount());
			assertEquals("Quintana", added.getLastName());
			assertEquals(added, database.get(added.getId()));
			assertFalse(table.isOverlayShown());
		}
		finally
		{
			database.delete(added);
		}
	}

	@Test
	void editShowsTheContactInAFormInTheOverlay()
	{
		edit();

		assertTrue(table.isOverlayShown());
		tester.assertComponentOnAjaxResponse(TABLE + ":overlay");
		tester.assertComponent(FORM.substring(0, FORM.lastIndexOf(':')), ContactEditPanel.class);
		tester.assertModelValue(FORM + ":city", city);
		tester.assertModelValue(FORM + ":firstName", contact.getFirstName());
	}

	@Test
	void savingUpdatesTheContactAndClosesTheOverlay()
	{
		edit();

		FormTester form = tester.newFormTester(FORM);
		form.setValue("city", "Paris");
		tester.executeAjaxEvent(FORM + ":save", "click");

		assertEquals("Paris", DatabaseLocator.getLargeDatabase().get(contact.getId()).getCity());
		assertFalse(table.isOverlayShown());
		tester.assertComponentOnAjaxResponse(TABLE + ":overlay");
	}

	@Test
	void anEmptyRequiredFieldKeepsTheOverlayOpen()
	{
		edit();

		FormTester form = tester.newFormTester(FORM);
		form.setValue("city", "");
		tester.executeAjaxEvent(FORM + ":save", "click");

		assertEquals(city, contact.getCity());
		assertTrue(table.isOverlayShown());
		tester.assertComponentOnAjaxResponse(FORM);
		assertTrue(tester.getLastResponseAsString().contains("data-dt-changed=\"true\""));
	}

	@Test
	void cancellingClosesTheOverlayWithoutSaving()
	{
		edit();

		FormTester form = tester.newFormTester(FORM);
		form.setValue("city", "Paris");
		tester.clickLink(FORM + ":cancel", true);

		assertEquals(city, contact.getCity());
		assertFalse(table.isOverlayShown());
	}

	@Test
	void escapeClosesTheOverlay()
	{
		edit();

		executeAction(DynamicDataTable.CLOSE_OVERLAY_ACTION, null);

		assertFalse(table.isOverlayShown());
		assertEquals(city, contact.getCity());
	}

	@Test
	@SuppressWarnings("unchecked")
	void theColumnsAreRememberedAndCanBeReset()
	{
		List<String> initial = table.getColumnState().getOrder();
		int shown = table.getVisibleColumns().size();
		tester.getRequest().getPostParameters().setParameterValue("from", "5");
		tester.getRequest().getPostParameters().setParameterValue("to", String.valueOf(shown));
		tester.executeBehavior(table.getBehaviors(MovableColumnsBehavior.class).get(0));
		tester.getRequest().getPostParameters().setParameterValue("widths",
			String.join(",", Collections.nCopies(shown, "50")));
		tester.executeBehavior(table.getBehaviors(ResizableColumnsBehavior.class).get(0));
		List<String> moved = table.getColumnState().getOrder();

		tester.startPage(DynamicDataTablePage.class);
		table = (DynamicDataTable<Contact, Long>)tester.getComponentFromLastRenderedPage(TABLE);
		assertEquals(moved, table.getColumnState().getOrder(), "a new page keeps the order");
		assertEquals(50d, table.getColumnWidths()[0]);

		tester.clickLink("options:resetColumns", true);

		assertEquals(initial, table.getColumnState().getOrder());
		assertEquals(shown, table.getVisibleColumns().size());
		assertNull(table.getColumnWidths());
	}

	@Test
	void theSelectionBarOptionIsTheBulkSelectionToolbarPosition()
	{
		tester.assertContains("Bulk selection toolbar position");
	}

	@Test
	void theEditFormIsShownOverThePage()
	{
		assertEquals(OverlayPlacement.BODY, table.getOverlayPlacement());

		edit();

		TagTester overlay = tester.getTagById(table.get("overlay").getMarkupId());
		assertTrue(overlay.getAttribute("class").contains("dynamic-data-table-overlay-body"));
	}

	@Test
	void theBodyIs800PixelsHighAndTheOptionsChangeIt()
	{
		assertEquals(800, table.getBodyHeight());
		assertEquals(BodyHeightUnits.PIXELS, table.getBodyHeightUnits());
		tester.assertContains(Pattern.quote("\"bodyHeight\":{\"value\":800,\"unit\":\"px\"}"));

		tester.getRequest().getPostParameters().setParameterValue("options:bodyHeightUnits",
			String.valueOf(BodyHeightUnits.VIEWPORT_HEIGHT.ordinal()));
		tester.executeAjaxEvent("options:bodyHeightUnits", "change");
		tester.getRequest().getPostParameters().setParameterValue("options:bodyHeight", "60");
		tester.executeAjaxEvent("options:bodyHeight", "change");

		assertEquals(60, table.getBodyHeight());
		assertEquals(BodyHeightUnits.VIEWPORT_HEIGHT, table.getBodyHeightUnits());
		tester.assertComponentOnAjaxResponse(table);

		tester.getRequest().getPostParameters().setParameterValue("options:bodyHeight", "0");
		tester.executeAjaxEvent("options:bodyHeight", "change");

		assertEquals(-1, table.getBodyHeight(), "0 lets the body take the height of its rows");
		tester.assertComponentOnAjaxResponse(table);
		tester.assertContainsNot(Pattern.quote("\"bodyHeight\":{"));
	}

	private void edit()
	{
		executeAction("edit", String.valueOf(contact.getId()));
	}

	private void executeAction(String action, String key)
	{
		tester.getRequest().getPostParameters().setParameterValue("action", action);
		if (key != null)
		{
			tester.getRequest().getPostParameters().setParameterValue("key", key);
		}
		tester.executeBehavior(table.getBehaviors(AbstractDefaultAjaxBehavior.class)
			.stream()
			.filter(behavior -> behavior.getClass().getSimpleName().equals("ActionBehavior"))
			.findFirst()
			.orElseThrow());
	}
}
