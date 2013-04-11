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
package org.apache.wicket.extensions.markup.html.tabs;

import java.util.List;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.util.tester.TagTester;
import org.junit.Test;

/**
 * Test for visibility of {@link ITab}s in a {@link TabbedPanel}.
 */
public class TabbedPanelVisibilityTest extends WicketTestCase
{

	/**
	 * WICKET-4658
	 */
	@Test
	public void firstSelected_2Visible()
	{
		final TabbedPanelVisibilityTestPage visibilityTestPage = new TabbedPanelVisibilityTestPage(
			2, 2);
		tester.startPage(visibilityTestPage);

		final List<TagTester> tabsTags = tester.getTagsByWicketId("tabs");
		assertEquals("tab0 selected", tabsTags.get(0).getAttribute("class"));
		assertEquals("tab1 last", tabsTags.get(1).getAttribute("class"));
	}

	/**
	 * WICKET-4658
	 */
	@Test
	public void lastSelected_2Visible()
	{
		final TabbedPanelVisibilityTestPage visibilityTestPage = new TabbedPanelVisibilityTestPage(
			2, 2);
		// selecting the last tab
		visibilityTestPage.tabbedPanel.setSelectedTab(1);
		tester.startPage(visibilityTestPage);

		final List<TagTester> tabsTags = tester.getTagsByWicketId("tabs");
		assertEquals("tab0", tabsTags.get(0).getAttribute("class"));
		assertEquals("tab1 selected last", tabsTags.get(1).getAttribute("class"));
	}

	/**
	 * WICKET-4658
	 */
	@Test
	public void firstSelected_1Visible()
	{
		final TabbedPanelVisibilityTestPage visibilityTestPage = new TabbedPanelVisibilityTestPage(
			2, 1);
		tester.startPage(visibilityTestPage);

		final List<TagTester> tabsTags = tester.getTagsByWicketId("tabs");
		assertEquals("tab0 selected last", tabsTags.get(0).getAttribute("class"));
	}
}