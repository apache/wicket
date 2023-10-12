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
package org.apache.wicket.markup.html.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class ListMultipleChoiceTest extends WicketTestCase
{
	class TestPage extends WebPage
	{
		IModel<List<String>> selectedValues = new ListModel<String>(new ArrayList<String>());
		List<String> choices = Arrays.asList("a", "b", "c", "d", "e", "f");

		TestPage()
		{
			Form<Void> form = new Form<>("form");
			this.add(form);
			form.add(newListMultipleChoice("list", selectedValues, choices));
		}
	}

	@Test
	void testSelectionWorks() throws Exception
	{
		TestPage page = tester.startPage(new TestPage());
		FormTester form = tester.newFormTester("form");
		form.select("list", 1);
		form.select("list", 3);
		form.select("list", 5);
		form.submit();
		assertEquals(3, page.selectedValues.getObject().size());
		assertTrue(page.selectedValues.getObject().contains("b"));
		assertTrue(page.selectedValues.getObject().contains("d"));
		assertTrue(page.selectedValues.getObject().contains("f"));
	}

	@Test
	void testSelectionAccumulates() throws Exception
	{
		final TestPage page = new TestPage();
		page.selectedValues.getObject().add("a");
		tester.startPage(page);
		FormTester form = tester.newFormTester("form");
		form.select("list", 1);
		form.select("list", 3);
		form.select("list", 5);
		form.submit();
		assertEquals(4, page.selectedValues.getObject().size());
		assertTrue(page.selectedValues.getObject().contains("a"));
		assertTrue(page.selectedValues.getObject().contains("b"));
		assertTrue(page.selectedValues.getObject().contains("d"));
		assertTrue(page.selectedValues.getObject().contains("f"));
	}

	private ListMultipleChoice<String> newListMultipleChoice(String id,
															 IModel<List<String>> selectedValues, List<String> choices)
	{
		return new ListMultipleChoice<>(id, selectedValues, choices);
	}
}
