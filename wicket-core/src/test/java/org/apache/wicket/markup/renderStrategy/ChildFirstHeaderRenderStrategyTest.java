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

import org.apache.wicket.Page;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author juergen donnerstag
 */
public class ChildFirstHeaderRenderStrategyTest extends WicketTestCase
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(ChildFirstHeaderRenderStrategyTest.class);

	/**
	 * @throws Exception
	 */
	@Test
	public void test1() throws Exception
	{
		executeCombinedTest(SimplePage1.class, "SimplePage1_ExpectedResult.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test2() throws Exception
	{
		executeCombinedTest(SimplePage2.class, "SimplePage2_ExpectedResult.html");
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
	private <T extends Page> void executeCombinedTestPre1_5(final Class<T> pageClass,
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
