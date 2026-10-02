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
package org.apache.wicket.extensions.markup.html.repeater.data.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ResizableColumnsBehavior.Mode;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class ResizableColumnsBehaviorTest extends WicketTestCase
{
	@Test
	void fitIsTheDefault()
	{
		assertEquals(Mode.FIT, new ResizableColumnsBehavior().getMode());
	}

	@Test
	void attachesTheScriptToTheTable()
	{
		TablePage page = new TablePage("table", new ResizableColumnsBehavior(Mode.STRETCH)
			.setMinColumnWidth(50));
		tester.startPage(page);

		String id = page.get("table").getMarkupId();
		tester.assertContains(Pattern.quote("Wicket.ResizableColumns.attach(\"" + id +
			"\", {\"mode\":\"stretch\",\"minWidth\":50});"));
		tester.assertContains("wicket-resizable-columns.*\\.js");
		tester.assertContains("wicket-resizable-columns.*\\.css");
		assertTrue(tester.getLastResponseAsString().contains("id=\"" + id + "\""));
	}

	@Test
	void fitModeIsWrittenInLowerCase()
	{
		tester.startPage(new TablePage("table", new ResizableColumnsBehavior()));

		tester.assertContains(Pattern.quote("{\"mode\":\"fit\",\"minWidth\":30}"));
	}

	@Test
	void onlyTablesCanBeResized()
	{
		assertThrows(Exception.class,
			() -> tester.startPage(new TablePage("div", new ResizableColumnsBehavior())));
	}

	@Test
	void minimumWidthCannotBeNegative()
	{
		assertThrows(IllegalArgumentException.class,
			() -> new ResizableColumnsBehavior().setMinColumnWidth(-1));
	}

	public static class TablePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TablePage(String id, ResizableColumnsBehavior behavior)
		{
			add(new WebMarkupContainer(id).add(behavior));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			String id = get("table") != null ? "table" : "div";
			String tag = "table".equals(id) ? "table" : "div";
			return new StringResourceStream("<html><head></head><body><" + tag +
				" wicket:id=\"" + id + "\"><thead><tr><th>A</th></tr></thead></" + tag +
				"></body></html>");
		}
	}
}
