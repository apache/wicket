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

import junit.framework.TestCase;

import org.apache.wicket.util.tester.WicketTester;

/**
 * jWebUnit test for Hello World.
 */
public class NiceUrlTest extends TestCase
{
	private WicketTester tester;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		tester = new WicketTester(new NiceUrlApplication());
		tester.startPage(Home.class);
	}

	/**
	 * Test page.
	 * 
	 * @throws Exception
	 */
	public void testHomePage() throws Exception
	{
		tester.assertContains("Wicket Examples - niceurl");
		tester.assertContains("This example displays how you can work with 'nice' urls for bookmarkable pages.");
	}

	/**
	 * Test page.
	 * 
	 * @throws Exception
	 */
	public void testPage1() throws Exception
	{
		tester.clickLink("page1Link");
		tester.assertRenderedPage(Page1.class);
		tester.clickLink("homeLink");
		tester.assertRenderedPage(Home.class);
	}

	/**
	 * Test page.
	 * 
	 * @throws Exception
	 */
	public void testPage2() throws Exception
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
	 * 
	 * @throws Exception
	 */
	public void testPage2QP() throws Exception
	{
		tester.clickLink("page2LinkQP");
		tester.assertRenderedPage(Page2QP.class);
		tester.clickLink("refreshLink");
		tester.assertRenderedPage(Page2QP.class);
		tester.clickLink("homeLink");
		tester.assertRenderedPage(Home.class);
	}

	/**
	 * Test page.
	 * 
	 * @throws Exception
	 */
	public void testPage3() throws Exception
	{
		tester.clickLink("page3Link");
		tester.assertRenderedPage(org.apache.wicket.examples.niceurl.mounted.Page3.class);
		tester.clickLink("homeLink");
		tester.assertRenderedPage(Home.class);
	}

	/**
	 * Test page.
	 * 
	 * @throws Exception
	 */
	public void testPage4() throws Exception
	{
		tester.clickLink("page4Link");
		tester.assertRenderedPage(org.apache.wicket.examples.niceurl.mounted.Page4.class);
		tester.clickLink("homeLink");
		tester.assertRenderedPage(Home.class);
	}

	/**
	 * Test page.
	 * 
	 * @throws Exception
	 */
	public void testPage5() throws Exception
	{
		tester.clickLink("page5Link");
		tester.assertRenderedPage(org.apache.wicket.examples.niceurl.mounted.Page5.class);
		tester.clickLink("homeLink");
		tester.assertRenderedPage(Home.class);
	}
}
