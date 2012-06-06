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
package org.apache.wicket.extensions.markup.html.form.palette;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;

/**
 * Test for {@link Palette}.
 */
public class PaletteTest extends WicketTestCase
{

	/**
	 */
	@Test
	public void standard()
	{
		IModel<List<String>> selected = new ListModel<String>(new ArrayList<String>(Arrays.asList(
			"A", "D")));

		IModel<List<String>> all = new ListModel<String>(new ArrayList<String>(Arrays.asList("A",
			"B", "C", "D")));

		PaletteTestPage testPage = new PaletteTestPage(selected, all);

		tester.startPage(testPage);

		FormTester formTester = tester.newFormTester(testPage.form.getId());
		formTester.submit();

		Collection<String> collection = testPage.palette.getModelCollection();

		assertEquals(2, collection.size());
		Iterator<String> iterator = collection.iterator();
		assertEquals("A", iterator.next());
		assertEquals("D", iterator.next());
	}

	/**
	 * WICKET-4231 palette with choicesModel missing currently selected
	 */
	@Test
	public void choicesModelMissingSelected()
	{
		IModel<List<String>> selected = new ListModel<String>(new ArrayList<String>(
			Arrays.asList("D")));

		IModel<List<String>> all = new ListModel<String>(new ArrayList<String>(Arrays.asList("A",
			"B", "C")));

		PaletteTestPage testPage = new PaletteTestPage(selected, all);

		tester.startPage(testPage);

		FormTester formTester = tester.newFormTester(testPage.form.getId());
		formTester.submit();

		Collection<String> collection = testPage.palette.getModelCollection();

		assertEquals(0, collection.size());
	}

	/**
	 * WICKET-4231 palette with choicesModel accounting for currently selected
	 */
	@Test
	public void choicesModelAccountingForSelected()
	{
		final List<String> list = new ArrayList<String>(Arrays.asList("D"));

		IModel<List<String>> selected = new ListModel<String>(list);

		IModel<List<String>> all = new LoadableDetachableModel<List<String>>()
		{
			@Override
			protected List<String> load()
			{
				List<String> fromDB = Arrays.asList("A", "B", "C"); // normally coming from DB

				List<String> result = new ArrayList<String>();
				result.addAll(fromDB);

				// include already selected
				result.addAll(list);

				return result;
			}
		};

		PaletteTestPage testPage = new PaletteTestPage(selected, all);

		tester.startPage(testPage);

		FormTester formTester = tester.newFormTester(testPage.form.getId());
		formTester.submit();

		Collection<String> collection = testPage.palette.getModelCollection();

		assertEquals(1, collection.size());
		assertEquals("D", collection.iterator().next());
	}

	/**
	 * WICKET-4590 single unselected item
	 */
	@Test
	public void choicesModelSingleNotSelected()
	{
		IModel<List<String>> selected = new ListModel<String>(new ArrayList<String>());

		IModel<List<String>> all = new ListModel<String>(new ArrayList<String>(Arrays.asList("A")));

		PaletteTestPage testPage = new PaletteTestPage(selected, all);

		tester.startPage(testPage);

		tester.assertContains("<option value=\"A\">A</option>");
	}
}
