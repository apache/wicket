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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests the characters a component id and a markup id may contain.
 */
class ComponentIdCharactersTest extends WicketTestCase
{
	private static final String[] INVALID_IDS = { "a'b", "a\"b", "a`b", "a\\b", "a;b", "a(b",
			"a)b", "a[b", "a]b", "a{b", "a}b", "a<b", "a>b", "a\nb", "a\rb", "a\tb", "a\u0000b",
			"a\u2028b", "a\u2029b" };

	/** Characters that cannot end a JavaScript string literal are still allowed */
	private static final String VALID_ID = "a b&c=d/e+f,g.h-i_j*k!l\u00fcber1";

	@Test
	void componentIdMayContainOtherCharacters()
	{
		assertEquals(VALID_ID, new WebComponent(VALID_ID).getId());
	}

	@Test
	void componentIdWithDisallowedCharactersIsRejected()
	{
		for (String id : INVALID_IDS)
		{
			assertThrows(WicketRuntimeException.class, () -> new WebComponent(id), id);
		}
		assertThrows(WicketRuntimeException.class, () -> new WebComponent("a:b"));
		assertThrows(WicketRuntimeException.class, () -> new WebComponent("a~b"));
	}

	@Test
	void markupIdMayContainOtherCharacters()
	{
		WebComponent component = new WebComponent("component");
		component.setMarkupId(VALID_ID);

		assertEquals(VALID_ID, component.getMarkupId());
	}

	@Test
	void markupIdWithDisallowedCharactersIsRejected()
	{
		WebComponent component = new WebComponent("component");
		for (String id : INVALID_IDS)
		{
			assertThrows(IllegalArgumentException.class, () -> component.setMarkupId(id), id);
			assertThrows(IllegalArgumentException.class, () -> component.setMarkupIdImpl(id), id);
		}
	}

	@Test
	void markupIdFromTheMarkupIsChecked()
	{
		tester.startPage(new MarkupIdPage("a-b_c.d1 e&amp;f"));
		assertTrue(tester.getLastResponseAsString().contains("id=\"a-b_c.d1 e&amp;f\""));

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
