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
package org.apache.wicket.markup.renderStrategy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.Page;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author juergen donnerstag
 */
class ChildFirstHeaderRenderStrategyTest extends WicketTestCase
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(ChildFirstHeaderRenderStrategyTest.class);

	/**
	 * @throws Exception
	 */
	@Test
	void test1() throws Exception
	{
		executeCombinedTest(SimplePage1.class, "SimplePage1_ExpectedResult.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	void test2() throws Exception
	{
		executeCombinedTest(SimplePage2.class, "SimplePage2_ExpectedResult.html");
	}

	/**
	 * Tests that when a controller of an enclosure is added to the ajax target, its header
	 * contributions reach the response
	 *
	 * WICKET-6459
	 *
	 */
	@Test
	void testAjaxAndEnclosures() throws Exception
	{

		tester.startPage(EnclosureAjaxRenderPage.class);
		tester.assertRenderedPage(EnclosureAjaxRenderPage.class);
		tester.clickLink("ajaxLink", true);

		String lastResponse = tester.getLastResponseAsString();

		String headerContribution = lastResponse.split("<header-contribution")[1].split("</header-contribution")[0];

		Pattern headerStylesheetLinkExtractor = Pattern.compile("<link.*/>");
		Matcher headerStyleSheetLinkMatcher = headerStylesheetLinkExtractor.matcher(headerContribution);

		List<String> headerStylesheetLinks = new ArrayList<>();

		while (headerStyleSheetLinkMatcher.find()){
			headerStylesheetLinks.add(headerStyleSheetLinkMatcher.group());
		}

		assertTrue(headerStylesheetLinks.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"../../enclosedInInline.css\" />"));
		assertTrue(headerStylesheetLinks.contains("<link rel=\"stylesheet\" type=\"text/css\" href=\"../../enclosed.css\" />"));

	}

	/**
	 * 
	 * @param <T>
	 * @param pageClass
	 * @param filename
	 * @throws Exception
	 */
	private <T extends Page> void executeCombinedTest(final Class<T> pageClass,
		final String filename) throws Exception
	{
		tester.startPage(pageClass);
		tester.assertRenderedPage(pageClass);
		tester.assertResultPage(getClass(), filename + "_2");
		System.setProperty("Wicket_HeaderRenderStrategy", "");
	}

	/**
	 * 
	 * @param <T>
	 * @param pageClass
	 * @param filename
	 * @throws Exception
	 */
	<T extends Page> void executeCombinedTestPre1_5(final Class<T> pageClass,
													final String filename) throws Exception
	{
		// Default Config: parent first header render strategy
		log.error("=== PARENT first header render strategy ===");
		tester.startPage(pageClass);
		tester.assertRenderedPage(pageClass);
		tester.assertResultPage(getClass(), filename);

		// child first header render strategy
		log.error("=== CHILD first header render strategy ===");
		System.setProperty("Wicket_HeaderRenderStrategy",
			ChildFirstHeaderRenderStrategy.class.getName());
		tester = new WicketTester();
		tester.startPage(pageClass);
		tester.assertRenderedPage(pageClass);
		tester.assertResultPage(getClass(), filename + "_2");
		System.setProperty("Wicket_HeaderRenderStrategy", "");
	}
}
