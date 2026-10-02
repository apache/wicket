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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Filters and switches the icons of {@link IconsPage}.
 */
class IconsPageTest
{
	private WicketTester tester;

	@BeforeEach
	void startPage()
	{
		tester = new WicketTester(new RepeaterApplication());
		tester.startPage(IconsPage.class);
	}

	@AfterEach
	void stop()
	{
		tester.destroy();
	}

	private List<TagTester> tiles()
	{
		return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), "class",
			"icons-tile", false);
	}

	@Test
	void theFirstSvgIconsAreShownAndCopyTheirConstant()
	{
		List<TagTester> tiles = tiles();

		assertEquals(300, tiles.size());
		assertEquals("SvgIcon.DIGIT_0", tiles.get(0).getAttribute("data-wicket-copy"));
		assertTrue(tester.getLastResponseAsString().contains("class=\"wicket-svg-icon\""));
		tester.assertLabel("results:count",
			"2,001 icons, 300 of them shown; type a name to narrow them down.");
	}

	@Test
	void theIconsAreFilteredByName()
	{
		FormTester form = tester.newFormTester("form");
		form.setValue("filter", "user plus");
		tester.executeAjaxEvent("form:filter", "input");

		List<TagTester> tiles = tiles();
		assertEquals(1, tiles.size());
		assertEquals("SvgIcon.USER_PLUS", tiles.get(0).getAttribute("data-wicket-copy"));
	}

	@Test
	void theSetsCanBeSwitched()
	{
		FormTester form = tester.newFormTester("form");
		form.select("set", 2);
		tester.executeAjaxEvent("form:set", "change");

		List<TagTester> tiles = tiles();
		assertEquals(4, tiles.size());
		assertEquals("IconBasedRowAction.Icon.EDIT", tiles.get(0).getAttribute("data-wicket-copy"));
	}
}
