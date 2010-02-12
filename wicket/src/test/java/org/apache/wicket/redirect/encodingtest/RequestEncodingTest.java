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
package org.apache.wicket.redirect.encodingtest;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class RequestEncodingTest extends WicketTestCase
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(RequestEncodingTest.class);

	private WicketApplication application;

	/**
	 * @see org.apache.wicket.WicketTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		application = new WicketApplication();
		tester = new WicketTester(application);
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(HomePage.class);
		// String document = tester.getServletResponse().getDocument();
	}

	/**
	 * 
	 */
	public void testDefault()
	{
		// TODO Wicket NG
		if (true)
			return;

		tester.startPage(A.class, new PageParameters("file=umlaut-\u00E4-\u00F6-\u00FC"));
		tester.assertRenderedPage(B.class);

		String url2 = ((B)tester.getLastRenderedPage()).getInterceptContinuationURL();
		assertTrue(url2.contains("umlaut-%C3%A4-%C3%B6-%C3%BC"));

		tester.clickLink("link");
		tester.assertRenderedPage(A.class);

		String file = ((A)tester.getLastRenderedPage()).getFileParameter();
		assertEquals("umlaut-\u00E4-\u00F6-\u00FC", file);

		String document = tester.getServletResponse().getDocument();
		assertTrue(document.contains("\u00E4-\u00F6-\u00FC"));
	}

	/**
	 * 
	 */
	public void testUmlautsInQueryParameter()
	{
		// TODO Wicket NG
		if (true)
			return;

		application.mount(new MixedParamUrlCodingStrategy("Apath", A.class, new String[] { "file" }));
		testDefault();
	}

	/**
	 * 
	 */
	public void testUmlautsInRequestUri()
	{
		// TODO Wicket NG
		if (true)
			return;

		application.mountBookmarkablePage("Aparameter", A.class);
		testDefault();
	}
}
