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
package org.apache.wicket.markup.html.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.wicket.util.tester.DiffUtil;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * @author jcompagner
 */
class HomePageRedirectTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
	void testRenderHomePagePageRedirect() throws Exception
	{
		tester.startPage(HomePagePageRedirect.class);

		assertEquals(RedirectPage.class, tester.getLastRenderedPage().getClass());

		// Validate the document
		String document = tester.getLastResponseAsString();
		DiffUtil.validatePage(document, this.getClass(), "RedirectPage-expected1.html", true);
	}

	/**
	 * @throws Exception
	 */
	@Test
	void testRenderHomePageClassRedirect() throws Exception
	{
		tester.startPage(HomePageClassRedirect.class);

		assertEquals(RedirectPage.class, tester.getLastRenderedPage().getClass());

		String document = tester.getLastResponseAsString();
		DiffUtil.validatePage(document, this.getClass(), "RedirectPage-expected2.html", true);
	}
}
