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


import org.junit.Test;

/**
 * Test the {@link Component#onRemove()}. Test if it gets called and propagated to the Components
 * children.
 */
public class RemoveTest extends WicketTestCase
{

	static final String PATH = RemoveTestPage.COMPONENT + Component.PATH_SEPARATOR +
		RemoveTestPage.LINK;

	/**
	 * The test
	 */
	@Test
	public void onRemovalFromHierarchy()
	{
		final RemoveTestPage page = new RemoveTestPage();
		tester.startPage(page);
		// on initial load of the page no calls should have occurred.
		assertEquals("componentOnRemovalFromHierarchy was called.", 0,
			page.getComponentOnRemovalFromHierarchyCalls());
		assertEquals("linkOnRemovalFromHierarchy was called.", 0,
			page.getLinkOnRemovalFromHierarchyCalls());
		assertEquals("behaviorOnRemovalFromHierarchy was called.", 0,
			page.getBehaviorOnRemovalCalls());

		tester.clickLink(PATH);
		// first click provoked a remove, so one call.
		assertEquals("componentOnRemovalFromHierarchy wasn't called.", 1,
			page.getComponentOnRemovalFromHierarchyCalls());
		// test if it got propagated to the children.
		assertEquals("linkOnRemovalFromHierarchy wasn't called.", 1,
			page.getLinkOnRemovalFromHierarchyCalls());
		assertEquals("behaviorOnRemovalFromHierarchy wasn't called.", 1,
			page.getBehaviorOnRemovalCalls());

		try
		{
			tester.clickLink(PATH);
			fail("Missing Exception");
		}
		catch (WicketRuntimeException wre)
		{
			// do nothing.
			// This exception was expected.
		}
	}
}
