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
package org.apache.wicket.markup.html.image;

import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test for WICKET-1382
 */
public class AjaxUpdatedImageTest extends WicketTestCase
{
	/**
	 * Tests that Image re-rendered in Ajax request have 'wicket:antiCache' parameter in its 'src'
	 * attribute value
	 */
	@Test
	public void wicket1382()
	{
		AjaxyImagesPage page = tester.startPage(AjaxyImagesPage.class);

		TagTester tagTester = tester.getTagById(page.image.getMarkupId());
		final String srcAttr = tagTester.getAttribute("src");
		assertFalse(
			"Image has not be rendered in Ajax request so it has no wicket:antiCache' parameter",
			srcAttr.contains("antiCache"));

		// make an ajax call
		tester.clickLink("link", true);
		page = (AjaxyImagesPage)tester.getLastRenderedPage();
		tagTester = tester.getTagById(page.image.getMarkupId());
		final String srcAttr1 = tagTester.getAttribute("src");
		assertTrue(
			"Image has not be rendered in Ajax request so it has no wicket:antiCache' parameter",
				srcAttr1.contains("antiCache"));
	}
}
