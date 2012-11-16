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
package org.apache.wicket.examples.niceurl;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * jWebUnit test for Hello World.
 */
public class NiceUrlTest extends Assert
{
	private WicketTester tester;

	/**
	 * 
	 */
	@Before
	public void before()
	{
		tester = new WicketTester(new NiceUrlApplication());
		tester.startPage(Home.class);
	}

	/**
	 * 
	 */
	@After
	public void tearDown()
	{
		tester.destroy();
	}

	/**
	 * Test page.
	 */
	@Test
	public void testHomePage()
	{
		tester.assertContains("Wicket Examples - niceurl");
		tester.assertContains("This example displays how you can work with 'nice' urls for bookmarkable pages.");
	}

	/**
	 * Test page.
	 */
	@Test
	public void testPage1()
	{
		tester.clickLink("page1Link");
		tester.assertRenderedPage(Page1.class);
		tester.clickLink("homeLink");
		tester.assertRenderedPage(Home.class);
	}

	/**
	 * Test page.
	 */
	@Test
	public void testPage2()
	{
		tester.clickLink("page2Link");
		tester.assertRenderedPage(Page2.class);
		tester.clickLink("refreshLink");
		tester.assertRenderedPage(Page2.class);
		tester.clickLink("homeLink");
		tester.assertRenderedPage(Home.class);
	}

	/**
	 * Test page.
	 */
	@Test
	public void testPage2QP()
	{
		tester.clickLink("page2LinkSegments");
		tester.assertRenderedPage(Page2QP.class);
		tester.clickLink("refreshLink");
		tester.assertRenderedPage(Page2QP.class);
		tester.clickLink("homeLink");
		tester.assertRenderedPage(Home.class);
	}

	/**
	 * Test page.
	 */
	@Test
	public void testPage3()
	{
		tester.clickLink("page3Link");
		tester.assertRenderedPage(org.apache.wicket.examples.niceurl.mounted.Page3.class);
		tester.clickLink("homeLink");
		tester.assertRenderedPage(Home.class);
	}

	/**
	 * Test page.
	 */
	@Test
	public void testPage4()
	{
		tester.clickLink("page4Link");
		tester.assertRenderedPage(org.apache.wicket.examples.niceurl.mounted.Page4.class);
		tester.clickLink("homeLink");
		tester.assertRenderedPage(Home.class);
	}

	/**
	 * Test page.
	 */
	@Test
	public void testPage5()
	{
		tester.clickLink("page5Link");
		tester.assertRenderedPage(org.apache.wicket.examples.niceurl.mounted.Page5.class);
		tester.clickLink("homeLink");
		tester.assertRenderedPage(Home.class);
	}
}
