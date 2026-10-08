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
package org.apache.wicket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.settings.MarkupSettings;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests the characters a component id and a markup id may contain.
 */
class ComponentIdCharactersTest extends WicketTestCase
{
	private static final String[] INVALID_IDS = { "a'b", "a\"b", "a`b", "a\\b", "a;b", "a{b",
			"a}b", "a<b", "a>b", "a b", "a\nb", "a b", "a&b",
			"a=b", "a/b", "a+b", "a,b" };

	/**
	 * A component id may contain brackets and parentheses when
	 * {@link MarkupSettings#setPropertyExpressionIdsAllowed(boolean)} says so, a markup id never
	 */
	private static final String[] PROPERTY_EXPRESSION_IDS = { "a(b", "a)b", "a[b", "a]b" };

	@Test
	void componentIdMayContainLettersDigitsAndPunctuation()
	{
		assertEquals("a-b_c.d1", new WebComponent("a-b_c.d1").getId());
		assertEquals("überété", new WebComponent("überété").getId());
	}

	@Test
	void componentIdWithOtherCharactersIsRejected()
	{
		for (String id : INVALID_IDS)
		{
			assertThrows(WicketRuntimeException.class, () -> new WebComponent(id), id);
		}
		for (String id : PROPERTY_EXPRESSION_IDS)
		{
			assertThrows(WicketRuntimeException.class, () -> new WebComponent(id), id);
		}
		assertThrows(WicketRuntimeException.class, () -> new WebComponent("a:b"));
		assertThrows(WicketRuntimeException.class, () -> new WebComponent("a~b"));
	}

	@Test
	void componentIdMayContainBracketsWhenAllowed()
	{
		tester.getApplication().getMarkupSettings().setPropertyExpressionIdsAllowed(true);

		for (String id : PROPERTY_EXPRESSION_IDS)
		{
			assertEquals(id, new WebComponent(id).getId());
		}
		for (String id : INVALID_IDS)
		{
			assertThrows(WicketRuntimeException.class, () -> new WebComponent(id), id);
		}
	}

	/**
	 * A {@link CompoundPropertyModel} uses the component id as its property expression, and
	 * {@code PropertyResolver} supports {@code map[key]} and {@code method()} expressions.
	 */
	@Test
	void componentIdMayBeAPropertyExpression()
	{
		tester.getApplication().getMarkupSettings().setPropertyExpressionIdsAllowed(true);

		Bean bean = new Bean();
		bean.map.put("key", "value");

		tester.startPage(new PropertyExpressionPage(bean));
		tester.assertLabel("form:name()", "Alice");
		tester.assertModelValue("form:map[key]", "value");

		Component field = tester.getComponentFromLastRenderedPage("form:map[key]");
		String markupId = field.getMarkupId();
		assertTrue(markupId.startsWith("map_key_"), markupId);
		assertFalse(markupId.contains("["), markupId);

		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("map[key]", "changed");
		formTester.submit();

		assertEquals("changed", bean.map.get("key"));
	}

	@Test
	void markupIdMayContainLettersDigitsAndPunctuation()
	{
		WebComponent component = new WebComponent("component");
		component.setMarkupId("a-b_c.d1");

		assertEquals("a-b_c.d1", component.getMarkupId());
	}

	@Test
	void markupIdWithOtherCharactersIsRejected()
	{
		WebComponent component = new WebComponent("component");
		for (String id : INVALID_IDS)
		{
			assertThrows(IllegalArgumentException.class, () -> component.setMarkupId(id), id);
			assertThrows(IllegalArgumentException.class, () -> component.setMarkupIdImpl(id), id);
		}
		tester.getApplication().getMarkupSettings().setPropertyExpressionIdsAllowed(true);
		for (String id : PROPERTY_EXPRESSION_IDS)
		{
			assertThrows(IllegalArgumentException.class, () -> component.setMarkupId(id), id);
			assertThrows(IllegalArgumentException.class, () -> component.setMarkupIdImpl(id), id);
		}
	}

	@Test
	void markupIdFromTheMarkupIsChecked()
	{
		tester.startPage(new MarkupIdPage("a-b_c.d1"));
		assertTrue(tester.getLastResponseAsString().contains("id=\"a-b_c.d1\""));

		WicketRuntimeException e = assertThrows(WicketRuntimeException.class,
			() -> tester.startPage(new MarkupIdPage("a&#039;b")));
		assertCauseMentions(e, "The id attribute 'a'b'");
	}

	private static void assertCauseMentions(Throwable e, String text)
	{
		for (Throwable cause = e; cause != null; cause = cause.getCause())
		{
			if (cause.getMessage() != null && cause.getMessage().contains(text))
			{
				return;
			}
		}
		assertTrue(false, "no cause mentions " + text + ": " + e);
	}

	/**
	 * A bean bound with property expressions that are not plain property names.
	 */
	public static class Bean implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final Map<String, String> map = new HashMap<>();

		public Map<String, String> getMap()
		{
			return map;
		}

		public String name()
		{
			return "Alice";
		}
	}

	/**
	 * A page that binds its components with a {@link CompoundPropertyModel}.
	 */
	public static class PropertyExpressionPage extends WebPage
		implements
			IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		PropertyExpressionPage(Bean bean)
		{
			Form<Bean> form = new Form<>("form", new CompoundPropertyModel<>(bean));
			add(form);
			form.add(new Label("name()"));
			form.add(new TextField<String>("map[key]").setOutputMarkupId(true));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><form wicket:id=\"form\">" +
				"<span wicket:id=\"name()\"></span>" +
				"<input type=\"text\" wicket:id=\"map[key]\"/></form></body></html>");
		}
	}

	/**
	 * A page whose markup gives its component an id attribute.
	 */
	public static class MarkupIdPage extends WebPage
		implements
			IMarkupResourceStreamProvider,
			IMarkupCacheKeyProvider
	{
		private static final long serialVersionUID = 1L;

		private final String markupId;

		MarkupIdPage(String markupId)
		{
			this.markupId = markupId;
			add(new WebComponent("component").setOutputMarkupId(true));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><span wicket:id=\"component\" id=\"" + markupId +
					"\"></span></body></html>");
		}

		@Override
		public String getCacheKey(MarkupContainer container, Class<?> containerClass)
		{
			// the markup differs per instance
			return null;
		}
	}
}
