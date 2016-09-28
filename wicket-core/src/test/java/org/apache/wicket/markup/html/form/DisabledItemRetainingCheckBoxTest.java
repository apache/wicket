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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test for the retainDisabledSelected flag on ListMultipleChoice.
 */
public class DisabledItemRetainingCheckBoxTest extends WicketTestCase
{
	/**
	 * testRenderMyPage()
	 */
	@Test
	public void renderMyPage()
	{
		TestPage page = tester.startPage(TestPage.class);
		tester.assertRenderedPage(TestPage.class);
		tester.debugComponentTrees();
		assertTrue(page.selection.contains(1));
		assertTrue(page.selection.contains(2));
		assertEquals(2, page.selection.size());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void retainDisabledSelected_On() throws Exception
	{
		TestPage page = tester.startPage(TestPage.class);
		FormTester form = tester.newFormTester("form");
		form.selectMultiple("choices", new int[] { 0 }, true);
		form.submit();
		assertTrue(page.selection.contains(0));
		assertTrue(page.selection.contains(1));
		assertEquals(2, page.selection.size());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void retainDisabledSelected_Off() throws Exception
	{
		TestPage page = tester.startPage(TestPage.class);
		FormTester form = tester.newFormTester("form");
		form.selectMultiple("choices2", new int[] { 0 }, true);
		form.submit();
		assertTrue(page.selection2.contains(0));
		assertFalse(page.selection2.contains(1));
		assertEquals(1, page.selection2.size());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void retainDisabledSelected_NoSelection() throws Exception
	{
		TestPage page = tester.startPage(TestPage.class);
		FormTester form = tester.newFormTester("form");
		form.selectMultiple("choices", new int[] { }, true);
		form.submit();
		assertTrue(page.selection.contains(1));
		assertEquals(1, page.selection.size());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void retainDisabledSelected_Off_NoSelection() throws Exception
	{
		TestPage page = tester.startPage(TestPage.class);
		FormTester form = tester.newFormTester("form");
		form.selectMultiple("choices2", new int[] { }, true);
		form.submit();
		assertEquals(0, page.selection2.size());
	}

	/**
	 */
	public static class TestPage extends WebPage
	{

		private static final long serialVersionUID = 1L;

		Collection<Integer> selection = new ArrayList<Integer>(Arrays.asList(1, 2));
		Collection<Integer> selection2 = new ArrayList<Integer>(Arrays.asList(1, 2));

		/**
		 * Construct.
		 * 
		 * @param parameters
		 */
		public TestPage(final PageParameters parameters)
		{

			Form<TestPage> form = new Form<TestPage>("form");
			add(form);
			form.add(new CheckBoxMultipleChoice<Integer>("choices",
				new PropertyModel<Collection<Integer>>(this, "selection"), Arrays.asList(0, 1, 2))
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean isDisabled(Integer object, int index, String selected)
				{
					return index == 1;
				}

			}.setRetainDisabledSelected(true));
			form.add(new CheckBoxMultipleChoice<Integer>("choices2",
				new PropertyModel<Collection<Integer>>(this, "selection2"), Arrays.asList(0, 1, 2))
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean isDisabled(Integer object, int index, String selected)
				{
					return index == 1;
				}


			});
		}
	}
}
