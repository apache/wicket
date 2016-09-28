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
package org.apache.wicket.markup.resolver;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestHomePage extends WicketTestCase
{

	@Test
	public void testRenderMyPage_1()
	{
		// start and render the test page
		tester.startPage(HomePage_1.class);

		// assert rendered page class
		tester.assertRenderedPage(HomePage_1.class);

		// assert rendered label component
		tester.assertLabel("message",
			"If you see this message wicket is properly configured and running");

		String href = "href=\"../resource/org.apache.wicket.markup.resolver.HomePage_1/main.css\"";

//		String doc = tester.getLastResponseAsString();
		tester.assertContains(href);

		// When rendered the 2nd time, the result should be same. The href must not contain the
		// locale
		tester.startPage(HomePage_1.class);
		tester.assertContains(href);
	}

	@Test
	public void testRenderMyPage_2()
	{
		// start and render the test page
		tester.startPage(HomePage_2.class);

		String href = "href=\"../resource/org.apache.wicket.markup.resolver.HomePage_2/main.css\"";

//		String doc = tester.getLastResponseAsString();
		tester.assertContains(href);

		// When rendered the 2nd time, the result should be same. The href must not contain the
		// locale
		tester.startPage(HomePage_2.class);
		tester.assertContains(href);
	}
}
