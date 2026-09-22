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
package org.apache.wicket.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.apache.wicket.examples.cdi.ConversationPage2;
import org.apache.wicket.examples.repeater.BasePage;
import org.apache.wicket.examples.repeater.ExamplePage;
import org.apache.wicket.examples.repeater.Index;
import org.apache.wicket.examples.repeater.RepeaterApplication;
import org.apache.wicket.examples.repeater.SortingPage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the index that lists the examples it finds next to itself, using the repeater example for
 * a package that has an index, a base page and examples that want to be read in a given order.
 */
class ExampleIndexPanelTest
{
	private WicketTester tester;

	@BeforeEach
	void start()
	{
		tester = new WicketTester(new RepeaterApplication());
	}

	@AfterEach
	void stop()
	{
		tester.destroy();
	}

	@Test
	void findsTheBookmarkableExamplePagesOfThePackage()
	{
		List<Class<?>> pages = ExamplePages.inPackage(Index.class.getPackageName(),
			WicketExamplePage.class, false);

		assertTrue(pages.contains(SortingPage.class));
		assertFalse(pages.contains(ExamplePage.class), "abstract page listed");
		assertFalse(pages.stream().anyMatch(page -> page.getEnclosingClass() != null),
			"nested class listed");
	}

	@Test
	void listsTheExamplesInTheOrderTheyAreMeantToBeRead()
	{
		tester.startPage(Index.class);

		assertOrder("OrderedRepeatingView Example", "RefreshingView Example", "Contacts Editor",
			"Simple DataView Example", "Paging DataView Example", "Sorting DataView Example",
			"DataView and optimized item removal", "DataGridView Example", "DataTable Example",
			"DataTable with FilterToolbar Example", "GridView Example",
			"AjaxFallbackDataTable Example");
	}

	@Test
	void readsWhatAnExampleSaysAboutItselfAndNotWhatItsBasePageSays()
	{
		assertEquals("Sorting DataView Example",
			WicketExamplePage.string(SortingPage.class, "title", Locale.ENGLISH, null, null));
		assertNull(WicketExamplePage.string(BasePage.class, "title", Locale.ENGLISH, null, null),
			"the base page borrowed a title");
	}

	@Test
	void leavesOutAnExamplePageThatIsNotOneToStartAt()
	{
		assertEquals("false", WicketExamplePage.string(ConversationPage2.class, "index",
			Locale.ENGLISH, null, null));
	}

	@Test
	void showsTheDescriptionOfEachExample()
	{
		tester.startPage(Index.class);

		tester.assertContains("basic example of a repeater view");
		tester.assertContains("demonstrates a grid view");
	}

	@Test
	void leavesOutTheIndexItselfAndThePagesWithoutATitle()
	{
		tester.startPage(Index.class);

		tester.assertContainsNot("Repeaters</a>");
		tester.assertContainsNot("BasePage");
	}

	private void assertOrder(String... titles)
	{
		String document = tester.getLastResponseAsString();

		int previous = -1;
		for (String title : titles)
		{
			int position = document.indexOf('>' + title + '<');
			assertTrue(position > previous, title + " is not listed after the previous example");
			previous = position;
		}
	}
}
