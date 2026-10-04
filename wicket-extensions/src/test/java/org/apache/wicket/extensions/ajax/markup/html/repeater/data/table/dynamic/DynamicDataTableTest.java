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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.openjson.JSONObject;

class DynamicDataTableTest extends WicketTestCase
{
	private static final List<Person> PEOPLE = List.of(new Person("1", "Ada"),
		new Person("2", "Linus"));

	private TestTable table;

	private RecordingAction select;

	@BeforeEach
	void startTable()
	{
		select = new RecordingAction("select");

		List<IDynamicColumn<Person>> columns = new ArrayList<>();
		columns.add(new DynamicColumn<>(Model.of("Name"), "<b>{{name}}</b>"));
		columns.add(new CompoundDynamicColumn<>(Model.of("Actions"),
			List.<IDynamicColumnContributor<Person>> of(select, () -> " | {{id}}"))
				.setCssClass("actions"));

		table = new TestTable("table", columns);
		tester.startPage(new TestPage(table));
	}

	@Test
	void rendersHeadersAndInitScript()
	{
		String page = tester.getLastResponseAsString();
		TagTester header = tester.getTagByWicketId("headers");
		assertEquals("Name", tester.getTagByWicketId("header").getValue());
		assertTrue(page.contains(
			"<span wicket:id=\"header\" class=\"dynamic-data-table-sort-title\">Actions</span>"),
			page);
		assertTrue(page.contains("<th wicket:id=\"headers\" class=\"actions\""), page);
		assertFalse(header.hasChildTag("a"), "a header that does not sort is no link");
		assertTrue(page.contains("<tbody></tbody>"), page);
		assertTrue(page.contains("new Wicket.DynamicDataTable({"), page);
		assertTrue(page.contains("\"template\":\"<b>{{name}}<\\/b>\""), page);
	}

	@Test
	void compoundColumnConcatenatesTheContributorTemplates()
	{
		CompoundDynamicColumn<Person> column = new CompoundDynamicColumn<>(Model.of("Actions"),
			List.<IDynamicColumnContributor<Person>> of(select, () -> " | {{id}}"));

		assertEquals("<a href=\"#\" data-dt-action=\"select\">select</a> | {{id}}",
			column.getTemplate());
	}

	@Test
	void servesRowsAsJson()
	{
		tester.executeBehavior(dataBehavior());

		assertEquals(
			"{\"rows\":[{\"key\":\"1\",\"number\":1,\"selected\":false," +
				"\"data\":{\"id\":\"1\",\"name\":\"Ada\"}}," +
				"{\"key\":\"2\",\"number\":2,\"selected\":false," +
				"\"data\":{\"id\":\"2\",\"name\":\"Linus\"}}]," +
				"\"itemCount\":2,\"currentPage\":0}",
			tester.getLastResponseAsString());
	}

	@Test
	void navigationIsHiddenOnASinglePage()
	{
		String page = tester.getLastResponseAsString();

		assertEquals(1, table.getPageCount());
		assertFalse(page.contains("navigatorLabel\">Showing"), page);
	}

	@Test
	void servesTheRowsOfTheCurrentPage()
	{
		table.setItemsPerPage(1);
		table.setCurrentPage(1);

		tester.executeBehavior(dataBehavior());

		assertEquals("{\"rows\":[{\"key\":\"2\",\"number\":2,\"selected\":false," +
			"\"data\":{\"id\":\"2\",\"name\":\"Linus\"}}]," +
			"\"itemCount\":2,\"currentPage\":1}", tester.getLastResponseAsString());
	}

	@Test
	void currentPageIsClampedToTheLastPage()
	{
		table.setItemsPerPage(1);
		table.setCurrentPage(5);

		assertEquals(2, table.getPageCount());
		assertEquals(1, table.getCurrentPage());
	}

	@Test
	void pagingReloadsTheRowsAndUpdatesTheNavigation()
	{
		table.setItemsPerPage(1);
		tester.startPage(tester.getLastRenderedPage());
		tester.assertContains("Showing 1 to 1 of 2");

		tester.clickLink("table:bottomNavigation:span:paging:navigator:next");

		assertEquals(1, table.getCurrentPage());
		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("Showing 2 to 2 of 2"), response);
		assertTrue(response.contains(varName() + ".update({"), response);
		assertTrue(response.contains("\"name\":\"Linus\""), response);
	}

	@Test
	void actionIsDispatchedToItsContributor()
	{
		executeAction("select", "2");

		assertSame(PEOPLE.get(1), select.lastRow);
		assertNull(table.notFoundAction);
	}

	@Test
	void actionOnVanishedRowIsNotFound()
	{
		executeAction("select", "42");

		assertNull(select.lastRow);
		assertEquals("select", table.notFoundAction);
		assertEquals("42", table.notFoundKey);
		assertNull(table.notFoundRow);
	}

	@Test
	void unregisteredActionIsNotFound()
	{
		executeAction("unknown", "1");

		assertNull(select.lastRow);
		assertEquals("unknown", table.notFoundAction);
		assertEquals("1", table.notFoundKey);
		assertSame(PEOPLE.get(0), table.notFoundRow);
	}

	@Test
	void secondContributorForTheSameActionIsRejected()
	{
		assertThrows(IllegalArgumentException.class,
			() -> table.addActionContributor(new RecordingAction("select")));
	}

	@Test
	void refreshRepaintsTheRowsFromTheResponse()
	{
		executeAction("refresh", "1");

		String var = varName();
		assertTrue(tester.getLastResponseAsString()
			.contains("if (" + var + ") { " + var + ".update({\"rows\":[{\"key\":\"1\"," +
				"\"number\":1,\"selected\":false,\"data\":{\"id\":\"1\",\"name\":\"Ada\"}}," +
				"{\"key\":\"2\",\"number\":2,\"selected\":false," +
				"\"data\":{\"id\":\"2\",\"name\":\"Linus\"}}],\"itemCount\":2," +
				"\"currentPage\":0}); }"), tester.getLastResponseAsString());
	}

	@Test
	void navigationCallbackReRendersTheNavigation()
	{
		String navigationId = table.get("bottomNavigation").getMarkupId();

		tester.executeBehavior(table.getBehaviors(AbstractDefaultAjaxBehavior.class).get(1));

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("<component id=\"" + navigationId + "\""), response);
	}

	@Test
	void initScriptCarriesThePagingState()
	{
		String page = tester.getLastResponseAsString();

		assertTrue(page.contains("\"itemCount\":2,\"currentPage\":0"), page);
	}

	@Test
	void removalInAjaxRequestDestroysTheInstance()
	{
		String var = varName();

		tester.executeBehavior(tester.getLastRenderedPage()
			.getBehaviors(RemoveTableBehavior.class)
			.get(0));

		assertTrue(tester.getLastResponseAsString()
			.contains("if (" + var + ") { " + var + ".destroy(); }"));
	}

	@Test
	void updateRowsMessageIsSentAsJsonAddressingTheTable() throws IOException
	{
		JSONObject message = send(table.newUpdateRowsMessage(List.of(PEOPLE.get(0))));

		assertEquals(table.getMarkupId(), message.getString("tableId"));
		assertEquals("updateRows", message.getString("typeId"));
		JSONObject row = message.getJSONArray("rows").getJSONObject(0);
		assertEquals("1", row.getString("key"));
		assertEquals("Ada", row.getJSONObject("data").getString("name"));
	}

	@Test
	void refreshMessageIsSentAsJsonAddressingTheTable() throws IOException
	{
		JSONObject message = send(table.newRefreshMessage());

		assertEquals(table.getMarkupId(), message.getString("tableId"));
		assertEquals("refresh", message.getString("typeId"));
		assertEquals(2, message.length());
	}

	@Test
	void defaultMessageTypesAreHandedToTheInstance()
	{
		tester.assertContains(Pattern.quote(", {\"updateRows\": " +
			UpdateRowsMessageType.INSTANCE.getTypeFunction() + ", \"refresh\": " +
			RefreshMessageType.INSTANCE.getTypeFunction() + ", \"updateRow\": " +
			UpdateRowMessageType.INSTANCE.getTypeFunction() + "});"));
	}

	@Test
	void updateRowMessageIsSentAsJsonAddressingTheRow() throws IOException
	{
		JSONObject message = send(table.newUpdateRowMessage(PEOPLE.get(1)));

		assertEquals(table.getMarkupId(), message.getString("tableId"));
		assertEquals("updateRow", message.getString("typeId"));
		assertEquals("2", message.getString("key"));
		assertEquals("Linus", message.getJSONObject("data").getString("name"));
	}

	@Test
	void messageIsSentOverARegisteredConnectionWithoutARequest() throws IOException
	{
		List<String> sent = new ArrayList<>();
		PageIdKey key = new PageIdKey(7);
		WebSocketSettings.Holder.get(tester.getApplication())
			.getConnectionRegistry()
			.setConnection(tester.getApplication(), "session", key, recordingConnection(sent));
		IWebSocketLightWeightMessage message = UpdateRowMessageType.newMessage("table1", "2",
			PEOPLE.get(1));

		assertTrue(message.send(tester.getApplication(), "session", key));
		assertFalse(message.send(tester.getApplication(), "another session", key));

		assertEquals(1, sent.size());
		assertEquals("2", new JSONObject(sent.get(0)).getString("key"));
	}

	@Test
	void headerContributingColumnRendersItsHeaderItems()
	{
		DynamicDataTable<Person, String> progress = new DynamicDataTable<>("table",
			List.of(new ProgressBarColumn<>(Model.of("Progress"), "progress")),
			new PeopleProvider(), 10);
		tester.startPage(new TestPage(progress));

		tester.assertContains("wicket-progress-bar.*\\.css");
		tester.assertContains(Pattern.quote("value=\\\"{{progress}}\\\""));
	}

	@Test
	void messageTypeIsHandedToTheInstance()
	{
		table.addMessageType(new PriceMessageType());
		tester.startPage(tester.getLastRenderedPage());

		tester.assertContains(Pattern.quote(", \"price\": function (message) { }});"));
	}

	@Test
	void secondMessageTypeWithTheSameIdIsRejected()
	{
		table.addMessageType(new PriceMessageType());

		assertThrows(IllegalArgumentException.class,
			() -> table.addMessageType(new PriceMessageType()));
	}

	@Test
	void refreshViaWebSocketsPushesToTheConnectionOfThePage() throws IOException
	{
		List<String> sent = new ArrayList<>();
		tester.getSession().bind();
		WebSocketSettings.Holder.get(tester.getApplication())
			.getConnectionRegistry()
			.setConnection(tester.getApplication(), tester.getSession().getId(),
				new PageIdKey(table.getPage().getPageId()), recordingConnection(sent));

		assertTrue(table.refreshViaWebSockets());

		assertEquals(1, sent.size());
		JSONObject message = new JSONObject(sent.get(0));
		assertEquals(table.getMarkupId(), message.getString("tableId"));
		assertEquals("refresh", message.getString("typeId"));
	}

	@Test
	void refreshViaWebSocketsWithoutConnectionSendsNothing() throws IOException
	{
		tester.getSession().bind();

		assertFalse(table.refreshViaWebSockets());
	}

	private static JSONObject send(IWebSocketLightWeightMessage message) throws IOException
	{
		List<String> sent = new ArrayList<>();
		message.send(recordingConnection(sent));
		assertEquals(1, sent.size());
		return new JSONObject(sent.get(0));
	}

	private static IWebSocketConnection recordingConnection(List<String> sent)
	{
		return (IWebSocketConnection)Proxy.newProxyInstance(
			DynamicDataTableTest.class.getClassLoader(),
			new Class<?>[] { IWebSocketConnection.class }, (proxy, method, args) -> {
				switch (method.getName())
				{
					case "isOpen" :
						return true;
					case "sendMessage" :
						sent.add((String)args[0]);
						return proxy;
					default :
						throw new UnsupportedOperationException(method.getName());
				}
			});
	}

	private String varName()
	{
		return "Wicket.DynamicDataTable.instances[\"" + table.getMarkupId() + "\"]";
	}

	private void executeAction(String action, String key)
	{
		tester.getRequest().getPostParameters().setParameterValue("action", action);
		tester.getRequest().getPostParameters().setParameterValue("key", key);
		tester.executeBehavior(table.getBehaviors(AbstractDefaultAjaxBehavior.class).get(0));
	}

	private AbstractAjaxBehavior dataBehavior()
	{
		return table.getBehaviors(AbstractAjaxBehavior.class)
			.stream()
			.filter(behavior -> !(behavior instanceof AbstractDefaultAjaxBehavior))
			.findFirst()
			.orElseThrow();
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

	private static class RecordingAction implements IAjaxActionColumnContributor<Person>
	{
		private static final long serialVersionUID = 1L;

		private final String actionId;

		Person lastRow;

		RecordingAction(String actionId)
		{
			this.actionId = actionId;
		}

		@Override
		public String getActionId()
		{
			return actionId;
		}

		@Override
		public String getTemplate()
		{
			return "<a href=\"#\" data-dt-action=\"" + actionId + "\">" + actionId + "</a>";
		}

		@Override
		public void onAction(Person row, AjaxRequestTarget target)
		{
			lastRow = row;
		}
	}

	private static class PriceMessageType implements IWebSocketMessageType
	{
		private static final long serialVersionUID = 1L;

		@Override
		public String getTypeId()
		{
			return "price";
		}

		@Override
		public String getTypeFunction()
		{
			return "function (message) { }";
		}
	}

	private static class TestTable extends DynamicDataTable<Person, String>
	{
		private static final long serialVersionUID = 1L;

		String notFoundAction;

		String notFoundKey;

		Person notFoundRow;

		TestTable(String id, List<IDynamicColumn<Person>> columns)
		{
			super(id, columns, new PeopleProvider(), 10);

			addActionContributor(new IAjaxActionColumnContributor<>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String getActionId()
				{
					return "refresh";
				}

				@Override
				public String getTemplate()
				{
					return "";
				}

				@Override
				public void onAction(Person row, AjaxRequestTarget target)
				{
					refresh(target);
				}
			});
		}

		@Override
		protected void onNotFoundAction(AjaxRequestTarget target, String action, String key,
			Person row)
		{
			notFoundAction = action;
			notFoundKey = key;
			notFoundRow = row;
		}
	}

	private static class RemoveTableBehavior extends AbstractDefaultAjaxBehavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected void respond(AjaxRequestTarget target)
		{
			((MarkupContainer)getComponent()).get("table").remove();
		}
	}

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TestPage(DynamicDataTable<Person, String> table)
		{
			add(table);
			add(new RemoveTableBehavior());
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
