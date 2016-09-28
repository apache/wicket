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

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class RequestEncodingTest extends Assert
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(RequestEncodingTest.class);

	private RedirectApplication application;
	private WicketTester tester;

	/**
	 * @see WicketTestCase#commonBefore()
	 */
	@Before
	public void setUp()
	{
		application = new RedirectApplication();
		tester = new WicketTester(application);
		tester.startPage(RedirectHomePage.class);
		tester.assertRenderedPage(RedirectHomePage.class);
	}

	/**
	 * 
	 */
	@After
	public void tearDown()
	{
		tester.destroy();
	}

	/**
	 * 
	 */
	@Test
	public void defaultTest()
	{
		tester.startPage(RedirectA.class,
			new PageParameters().set("file", "umlaut-\u00E4-\u00F6-\u00FC"));
		tester.assertRenderedPage(RedirectB.class);

		String url2 = ((RedirectB)tester.getLastRenderedPage()).getInterceptContinuationURL();
		assertTrue(url2.contains("umlaut-%C3%A4-%C3%B6-%C3%BC"));

		tester.clickLink("link");
		tester.assertRenderedPage(RedirectA.class);

		String file = ((RedirectA)tester.getLastRenderedPage()).getFileParameter();
		assertEquals("umlaut-\u00E4-\u00F6-\u00FC", file);

		String document = tester.getLastResponseAsString();
		assertTrue(document.contains("umlaut-\u00E4-\u00F6-\u00FC"));
	}


	/**
	 * 
	 */
	@Test
	public void umlautsInRequestUri()
	{
		application.mountPage("Aparameter", RedirectA.class);
		defaultTest();
	}
}
