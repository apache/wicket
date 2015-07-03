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
package org.apache.wicket.examples.repeater;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for Repeater application
 */
public class RepeaterTest extends Assert
{
	/**
	 * Test page.
	 */
	@Test
	public void allRepeaterPages()
	{
		WicketTester tester = new WicketTester(new RepeaterApplication());
		try
		{
			tester.startPage(Index.class);
			tester.assertContains("Wicket Examples - repeater views");

			// AjaxDataTablePage below RepeatingPage will fail this test.
			checkPage(tester, AjaxDataTablePage.class);
			checkPage(tester, RepeatingPage.class);
			checkPage(tester, RefreshingPage.class);
			checkPage(tester, FormPage.class);
			checkPage(tester, SimplePage.class);
			checkPage(tester, PagingPage.class);
			checkPage(tester, SortingPage.class);
			checkPage(tester, OIRPage.class);
			checkPage(tester, DataGridPage.class);
			checkPage(tester, GridViewPage.class);
		}
		finally
		{
			tester.destroy();
		}
	}
	
	@Ignore("WICKET-5942")
	@Test
	public void breakingRepeaterPages()
	{
		WicketTester tester = new WicketTester(new RepeaterApplication());
		try
		{
			tester.startPage(Index.class);
			tester.assertContains("Wicket Examples - repeater views");

			checkPage(tester, RepeatingPage.class);
			checkPage(tester, AjaxDataTablePage.class);
		}
		finally
		{
			tester.destroy();
		}
	}

	private void checkPage(WicketTester tester, Class<? extends WebPage> page)
	{
		tester.startPage(page);
		tester.assertContains("Wicket Examples - repeater views");
		tester.assertContains("Selected Contact: ");
		tester.assertContains("No Contact Selected");
	}
}
