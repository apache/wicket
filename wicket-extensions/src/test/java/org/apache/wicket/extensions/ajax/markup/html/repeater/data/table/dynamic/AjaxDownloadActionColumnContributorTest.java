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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.AjaxDownloadBehavior;
import org.apache.wicket.extensions.ajax.AjaxDownloadBehavior.Location;
import org.apache.wicket.extensions.markup.html.icon.FontAwesomeIcon;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class AjaxDownloadActionColumnContributorTest extends WicketTestCase
{
	private static final List<Item> ITEMS = List.of(new Item("1", "Ada"), new Item("2", "Linus"));

	@Test
	void templateIsALinkWithTheEscapedLabel()
	{
		AjaxDownloadActionColumnContributor<Item> download = new AjaxDownloadActionColumnContributor<>(
			"download", Model.of("<JSON>"));

		assertEquals("<a href=\"#\" data-dt-action=\"download\">&lt;JSON&gt;</a>",
			download.getTemplate());
	}

	@Test
	void withAnIconTheTemplateIsAnIconButtonWithTheLabelAsTooltip()
	{
		AjaxDownloadActionColumnContributor<Item> download = new AjaxDownloadActionColumnContributor<Item>(
			"download", Model.of("<JSON>")).setIcon(IconBasedRowAction.Icon.DOWNLOAD);

		String template = download.getTemplate();

		assertTrue(template.startsWith("<button type=\"button\""), template);
		assertTrue(template.contains("data-dt-action=\"download\" title=\"&lt;JSON&gt;\""),
			template);
	}

	@Test
	void theIconCanBeAnyIcon()
	{
		AjaxDownloadActionColumnContributor<Item> download = new AjaxDownloadActionColumnContributor<Item>(
			"download", Model.of("JSON")).setIcon(FontAwesomeIcon.DOWNLOAD);

		assertTrue(download.getTemplate().contains("<i class=\"fas fa-download\""),
			download.getTemplate());
	}

	@Test
	void contentAndContentTypeCanBeReplaced()
	{
		DynamicDataTable<Item, String> table = startTable(
			new AjaxDownloadActionColumnContributor<Item>("download", Model.of("vCard"))
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected String getContent(Item row)
				{
					return "BEGIN:VCARD " + row.getName();
				}

				@Override
				protected String getContentType()
				{
					return "text/vcard";
				}
			});

		executeAction(table, "download", "2");
		tester.invokeListener(table, resourceBehavior(table));

		assertEquals("BEGIN:VCARD Linus", tester.getLastResponseAsString());
		assertTrue(tester.getLastResponse().getContentType().startsWith("text/vcard"));
	}

	@Test
	void actionInitiatesTheDownloadWithTheConfiguredLocation()
	{
		DynamicDataTable<Item, String> table = startTable(
			new AjaxDownloadActionColumnContributor<Item>("download", Model.of("JSON"))
				.setLocation(Location.IFrame));

		executeAction(table, "download", "2");

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("Wicket.AjaxDownload.initiate("), response);
		assertTrue(response.contains("\"method\":\"iframe\""), response);
	}

	@Test
	void downloadServesTheClickedRowAsAJsonAttachment()
	{
		DynamicDataTable<Item, String> table = startTable(
			new AjaxDownloadActionColumnContributor<>("download", Model.of("JSON")));
		executeAction(table, "download", "2");

		tester.invokeListener(table, resourceBehavior(table));

		assertEquals("{\"id\":\"2\",\"name\":\"Linus\"}", tester.getLastResponseAsString());
		assertTrue(tester.getLastResponse().getContentType().startsWith("application/json"));
		String disposition = tester.getLastResponse().getHeader("Content-Disposition");
		assertTrue(disposition.startsWith("attachment"), disposition);
		assertTrue(disposition.contains("2.json"), disposition);
	}

	@Test
	void downloadInANewWindowIsInline()
	{
		DynamicDataTable<Item, String> table = startTable(
			new AjaxDownloadActionColumnContributor<Item>("download", Model.of("JSON"))
				.setLocation(Location.NewWindow));
		executeAction(table, "download", "1");

		tester.invokeListener(table, resourceBehavior(table));

		assertTrue(tester.getLastResponse().getHeader("Content-Disposition").startsWith("inline"));
	}

	@Test
	void downloadOfAVanishedRowFails()
	{
		DynamicDataTable<Item, String> table = startTable(
			new AjaxDownloadActionColumnContributor<>("download", Model.of("JSON")));
		executeAction(table, "download", "2");
		((ItemsProvider)table.getProvider()).removed = "2";

		tester.invokeListener(table, resourceBehavior(table));

		assertEquals(404, tester.getLastResponse().getStatus());
	}

	private DynamicDataTable<Item, String> startTable(
		AjaxDownloadActionColumnContributor<Item> download)
	{
		DynamicDataTable<Item, String> table = new DynamicDataTable<>("table",
			List.of(new CompoundDynamicColumn<>(Model.of("Actions"), List.of(download))),
			new ItemsProvider(), 10);
		tester.startPage(new TablePage(table));
		return table;
	}

	private void executeAction(DynamicDataTable<Item, String> table, String action, String key)
	{
		tester.getRequest().getPostParameters().setParameterValue("action", action);
		tester.getRequest().getPostParameters().setParameterValue("key", key);
		tester.executeBehavior(table.getBehaviors(AbstractDefaultAjaxBehavior.class)
			.stream()
			.filter(behavior -> behavior instanceof AjaxDownloadBehavior == false)
			.findFirst()
			.orElseThrow());
	}

	private static Behavior resourceBehavior(DynamicDataTable<Item, String> table)
	{
		return table.getBehaviors()
			.stream()
			.filter(behavior -> behavior.getClass().getName().endsWith("$ResourceBehavior"))
			.findFirst()
			.orElseThrow();
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
