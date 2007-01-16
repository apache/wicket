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
package wicket;

import wicket.ajax.AjaxEventBehavior;

/**
 * Test for ajax handler.
 * 
 * @author Juergen Donnerstag
 */
public class ComponentTest extends WicketTestCase
{
	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public ComponentTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_1() throws Exception
	{
		executeTest(TestPage_1.class, "TestPageExpectedResult_1.html");
	}

	/**
	 * Tests the number of detach calls on a Page, Component, Behavior and Model
	 * during a normal request.
	 * 
	 * @throws Exception
	 */
	public void testDetachPage() throws Exception
	{
		executeTest(TestDetachPage.class, "TestDetachPageExpectedResult.html");
		TestDetachPage page = (TestDetachPage)application.getLastRenderedPage();
		assertEquals(1, page.getNrComponentDetachModelCalls());
		assertEquals(1, page.getNrComponentDetachModelsCalls());
		assertEquals(1, page.getNrComponentInternalDetachCalls());
		assertEquals(1, page.getNrComponentOnDetachCalls());
		assertEquals(1, page.getNrPageDetachModelCalls());
		assertEquals(1, page.getNrPageDetachModelsCalls());
		assertEquals(1, page.getNrPageInternalDetachCalls());
		assertEquals(1, page.getNrPageOnDetachCalls());
		assertEquals(1, page.getNrModelDetachCalls());
		assertEquals(1, page.getNrAjaxBehaviorDetachModelCalls());
	}

	/**
	 * Tests the number of detach calls on a Page, Component, Behavior and Model
	 * during an Ajax request.
	 * 
	 * @throws Exception
	 */
	public void testDetachPageAjaxRequest() throws Exception
	{
		executeTest(TestDetachPage.class, "TestDetachPageExpectedResult.html");
		TestDetachPage page = (TestDetachPage)application.getLastRenderedPage();

		assertEquals(1, page.getNrComponentDetachModelCalls());
		assertEquals(1, page.getNrComponentDetachModelsCalls());
		assertEquals(1, page.getNrComponentInternalDetachCalls());
		assertEquals(1, page.getNrComponentOnDetachCalls());
		assertEquals(1, page.getNrPageDetachModelCalls());
		assertEquals(1, page.getNrPageDetachModelsCalls());
		assertEquals(1, page.getNrPageInternalDetachCalls());
		assertEquals(1, page.getNrPageOnDetachCalls());
		assertEquals(1, page.getNrModelDetachCalls());
		assertEquals(1, page.getNrAjaxBehaviorDetachModelCalls());

		AjaxEventBehavior behavior = page.getAjaxBehavior();
		executedBehavior(TestDetachPage.class, behavior, "TestDetachPageAjaxResult.html");
		assertTrue(1<page.getNrComponentDetachModelCalls());
		assertTrue(1<page.getNrComponentDetachModelsCalls());
		assertTrue(1<page.getNrComponentInternalDetachCalls());
		assertTrue(1<page.getNrComponentOnDetachCalls());
		assertTrue(1<page.getNrPageDetachModelCalls());
		assertTrue(1<page.getNrPageDetachModelsCalls());
		assertTrue(1<page.getNrPageInternalDetachCalls());
		assertTrue(1<page.getNrPageOnDetachCalls());
		assertTrue(1<page.getNrModelDetachCalls());
		assertTrue(1<page.getNrAjaxBehaviorDetachModelCalls());
	}
}
