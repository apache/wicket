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
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.extensions.markup.html.icon.FontAwesomeIcon;
import org.apache.wicket.extensions.markup.html.icon.IIcon;
import org.apache.wicket.extensions.markup.html.icon.SvgIcon;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class IconBasedRowActionTest extends WicketTestCase
{
	private static final String CONFIRM = "table:overlay:window:content:body:confirm";

	private static final String CANCEL = "table:overlay:window:content:body:cancel";

	private ItemsProvider provider;

	private TestTable table;

	private RecordingAction action;

	@Test
	void templateIsAnIconButtonWithTheEscapedTooltip()
	{
		RecordingAction delete = new RecordingAction("delete", IconBasedRowAction.Icon.DELETE,
			Model.of("Delete <it>"));

		String template = delete.getTemplate();

		assertTrue(template.startsWith("<button type=\"button\" class=\"dynamic-data-table-action " +
			"dynamic-data-table-icon-action\" data-dt-action=\"delete\" " +
			"title=\"Delete &lt;it&gt;\" aria-label=\"Delete &lt;it&gt;\"><svg"), template);
		assertTrue(template.contains(" aria-hidden=\"true\""), template);
		assertTrue(template.endsWith("</svg></button>"), template);
	}

	@Test
	void anyIconCanBeUsed()
	{
		String svg = new RecordingAction("edit", SvgIcon.PEN, Model.of("Edit")).getTemplate();
		String font = new RecordingAction("edit", FontAwesomeIcon.PEN, Model.of("Edit"))
			.getTemplate();
		String own = new RecordingAction("edit", () -> "<b>E</b>", Model.of("Edit")).getTemplate();

		assertTrue(svg.contains("aria-label=\"Edit\"><svg class=\"wicket-svg-icon\""), svg);
		assertTrue(font.endsWith("<i class=\"fas fa-pen\" aria-hidden=\"true\"></i></button>"),
			font);
		assertTrue(own.endsWith("<b>E</b></button>"), own);
	}

	@Test
	void theIconCanBeChosenWhenTheTemplateIsRendered()
	{
		RecordingAction edit = new RecordingAction("edit", IconBasedRowAction.Icon.EDIT,
			Model.of("Edit"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public IIcon getIcon()
			{
				return FontAwesomeIcon.PEN;
			}
		};

		assertTrue(edit.getTemplate().contains("fa-pen"), edit.getTemplate());
	}

	@Test
	void withoutConfirmationTheActionIsCarriedOutRightAway()
	{
		start(null);

		executeAction("act", "2");

		assertEquals(List.of("Linus"), action.done);
		assertFalse(table.isOverlayShown());
	}

	@Test
	void theConfirmationIsShownInTheOverlay()
	{
		start(Model.of("Really?"));

		executeAction("act", "2");

		assertTrue(table.isOverlayShown());
		assertTrue(action.done.isEmpty());
		tester.assertComponentOnAjaxResponse("table:overlay");
		tester.assertLabel("table:overlay:window:content:body:question", "Really?");
		tester.assertLabel("table:overlay:window:content:title", "Act");
	}

	@Test
	void confirmingCarriesOutTheActionAndClosesTheOverlay()
	{
		start(Model.of("Really?"));
		executeAction("act", "2");

		tester.clickLink(CONFIRM);

		assertEquals(List.of("Linus"), action.done);
		assertFalse(table.isOverlayShown());
		assertEquals(1, table.closed);
	}

	@Test
	void cancellingClosesTheOverlayWithoutActing()
	{
		start(Model.of("Really?"));
		executeAction("act", "2");

		tester.clickLink(CANCEL);

		assertTrue(action.done.isEmpty());
		assertFalse(table.isOverlayShown());
	}

	@Test
	void aRowGoneBeforeTheConfirmationIsNotActedOn()
	{
		start(Model.of("Really?"));
		executeAction("act", "2");
		provider.removed = "2";

		tester.clickLink(CONFIRM);

		assertTrue(action.done.isEmpty());
		assertFalse(table.isOverlayShown());
	}

	@Test
	void escapeClosesTheOverlay()
	{
		start(Model.of("Really?"));
		executeAction("act", "2");

		executeAction(DynamicDataTable.CLOSE_OVERLAY_ACTION, null);

		assertFalse(table.isOverlayShown());
		assertEquals(1, table.closed);
		tester.assertComponentOnAjaxResponse("table:overlay");
	}

	@Test
	void theOverlayIsAPlaceholderUntilShown()
	{
		start(null);

		tester.assertInvisible("table:overlay");
		assertFalse(tester.getLastResponseAsString().contains("dynamic-data-table-overlay-window"));
	}

	@Test
	void theOverlayContentNeedsItsId()
	{
		start(null);

		assertThrows(IllegalArgumentException.class,
			() -> table.showOverlay(new Label("other"), null));
	}

	@Test
	void closingAClosedOverlayDoesNothing()
	{
		start(null);

		table.closeOverlay(null);

		assertEquals(0, table.closed);
	}

	private void start(IModel<String> confirmation)
	{
		provider = new ItemsProvider();
		action = new RecordingAction("act", IconBasedRowAction.Icon.EDIT, Model.of("Act"));
		action.setConfirmation(confirmation);
		table = new TestTable(action, provider);
		tester.startPage(new TablePage(table));
	}

	private void executeAction(String actionId, String key)
	{
		tester.getRequest().getPostParameters().setParameterValue("action", actionId);
		if (key != null)
		{
			tester.getRequest().getPostParameters().setParameterValue("key", key);
		}
		tester.executeBehavior(table.getBehaviors(AbstractDefaultAjaxBehavior.class).get(0));
	}

	private static class RecordingAction extends IconBasedRowAction<Item>
	{
		private static final long serialVersionUID = 1L;

		final List<String> done = new ArrayList<>();

		RecordingAction(String actionId, IIcon icon, IModel<String> tooltip)
		{
			super(actionId, icon, tooltip);
		}

		@Override
		protected void onRowAction(Item row, AjaxRequestTarget target)
		{
			done.add(row.getName());
		}
	}

	private static class TestTable extends DynamicDataTable<Item, String>
	{
		private static final long serialVersionUID = 1L;

		int closed;

		TestTable(RecordingAction action, ItemsProvider provider)
		{
			super("table", List.of(new CompoundDynamicColumn<>(Model.of("Actions"),
				List.of(action))), provider, 10);
		}

		@Override
		protected void onOverlayClosed(IPartialPageRequestHandler target)
		{
			closed++;
		}
	}

	public static class Item implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String id;

		private final String name;

		Item(String id, String name)
		{
			this.id = id;
			this.name = name;
		}

		public String getId()
		{
			return id;
		}

		public String getName()
		{
			return name;
		}
	}

	private static class ItemsProvider implements IDynamicDataProvider<Item, String>
	{
		private static final long serialVersionUID = 1L;

		private static final List<Item> ITEMS = List.of(new Item("1", "Ada"),
			new Item("2", "Linus"));

		String removed;

		@Override
		public List<Item> rows(long first, long count)
		{
			return ITEMS.subList((int)first, (int)(first + count));
		}

		@Override
		public long size()
		{
			return ITEMS.size();
		}

		@Override
		public String keyOf(Item row)
		{
			return row.getId();
		}

		@Override
		public Item findByKey(String key)
		{
			return ITEMS.stream()
				.filter(item -> item.getId().equals(key) && !key.equals(removed))
				.findFirst()
				.orElse(null);
		}

		@Override
		public Class<String> getKeyType()
		{
			return String.class;
		}
	}

	public static class TablePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TablePage(DynamicDataTable<Item, String> table)
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
