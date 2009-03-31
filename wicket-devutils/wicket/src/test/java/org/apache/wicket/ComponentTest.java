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

import org.apache.wicket.ajax.AjaxEventBehavior;

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
	 * Tests the number of detach calls on a Page, Component, Behavior and Model during a normal
	 * request.
	 * 
	 * @throws Exception
	 */
	public void testDetachPage() throws Exception
	{
		executeTest(TestDetachPage.class, "TestDetachPageExpectedResult.html");
		TestDetachPage page = (TestDetachPage)tester.getLastRenderedPage();
		assertTrue(page.getNrComponentDetachModelCalls() > 0);
		assertTrue(page.getNrComponentDetachModelsCalls() > 0);
		assertTrue(page.getNrComponentOnDetachCalls() > 0);
		assertTrue(page.getNrPageDetachModelCalls() > 0);
		assertTrue(page.getNrPageDetachModelsCalls() > 0);
		assertTrue(page.getNrPageOnDetachCalls() > 0);
		assertTrue(page.getNrModelDetachCalls() > 0);
		assertTrue(page.getNrAjaxBehaviorDetachModelCalls() > 0);
	}

	/**
	 * Tests the number of detach calls on a Page, Component, Behavior and Model during an Ajax
	 * request.
	 * 
	 * @throws Exception
	 */
	public void testDetachPageAjaxRequest() throws Exception
	{
		executeTest(TestDetachPage.class, "TestDetachPageExpectedResult.html");
		TestDetachPage page = (TestDetachPage)tester.getLastRenderedPage();

		assertTrue(page.getNrComponentDetachModelCalls() > 0);
		assertTrue(page.getNrComponentDetachModelsCalls() > 0);
		assertTrue(page.getNrComponentOnDetachCalls() > 0);
		assertTrue(page.getNrPageDetachModelCalls() > 0);
		assertTrue(page.getNrPageDetachModelsCalls() > 0);
		assertTrue(page.getNrPageOnDetachCalls() > 0);
		assertTrue(page.getNrModelDetachCalls() > 0);
		assertTrue(page.getNrAjaxBehaviorDetachModelCalls() > 0);

		AjaxEventBehavior behavior = page.getAjaxBehavior();
		executedBehavior(TestDetachPage.class, behavior, "TestDetachPageAjaxResult.html");
		assertTrue(1 <= page.getNrComponentDetachModelCalls());
		assertTrue(1 <= page.getNrComponentDetachModelsCalls());
		assertTrue(1 <= page.getNrComponentOnDetachCalls());
		assertTrue(1 <= page.getNrPageDetachModelCalls());
		assertTrue(1 <= page.getNrPageDetachModelsCalls());
		assertTrue(1 <= page.getNrPageOnDetachCalls());
		assertTrue(1 <= page.getNrModelDetachCalls());
		assertTrue(1 <= page.getNrAjaxBehaviorDetachModelCalls());
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_1() throws Exception
	{
		executeTest(TestPage_1.class, "TestPageExpectedResult_1.html");
	}
}
