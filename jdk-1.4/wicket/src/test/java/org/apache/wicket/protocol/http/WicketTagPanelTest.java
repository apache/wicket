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
package org.apache.wicket.protocol.http;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.protocol.http.documentvalidation.HtmlDocumentValidator;
import org.apache.wicket.protocol.http.documentvalidation.Tag;
import org.apache.wicket.protocol.http.documentvalidation.TextContent;
import org.apache.wicket.util.tester.WicketTester;


/**
 * Simple application that demonstrates the mock http application code (and
 * checks that it is working)
 * 
 * @author Chris Turner
 */
public class WicketTagPanelTest extends TestCase
{
	private static final Log log = LogFactory.getLog(WicketTagPanelTest.class);

	private WicketTester application;

	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public WicketTagPanelTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		application = new WicketTester();
	}
	protected void tearDown() throws Exception
	{
		application.destroy();
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage() throws Exception
	{
		application.startPage(WicketPanelPage.class);
		// Validate the document
		String document = application.getServletResponse().getDocument();
		log.info(document);
		assertTrue(validatePage1(document));
	}

	/**
	 * Validate page 2 of the paged table.
	 * @param document The document
	 * @return The validation result
	 */
	private boolean validatePage1(String document)
	{
		HtmlDocumentValidator validator = new HtmlDocumentValidator();
		Tag html = new Tag("html");
		Tag body = new Tag("body");
		html.addExpectedChild(body);
		body.addExpectedChild(new TextContent("\\s+"));
		Tag span = new Tag("span");
		body.addExpectedChild(span);
		Tag wicket = new Tag("wicket:panel");
		span.addExpectedChild(wicket);

		wicket.addExpectedChild(new TextContent("\\s*Panel Content"));

		validator.addRootElement(html);
		return validator.isDocumentValid(document);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePageWicketTagRemoved() throws Exception
	{
		// Remove wicket tags from output
		application.getApplication().getMarkupSettings().setStripWicketTags(true);
		application.startPage(WicketPanelPage.class);

		// Validate the document
		String document = application.getServletResponse().getDocument();
		log.info(document);
		assertTrue("Document with Wicket tags stripped did not match", validatePage2(document));
	}

	/**
	 * Validate page 2 of the paged table.
	 * @param document The document
	 * @return The validation result
	 */
	private boolean validatePage2(String document)
	{
		HtmlDocumentValidator validator = new HtmlDocumentValidator();
		Tag html = new Tag("html");
		Tag body = new Tag("body");
		body.addExpectedChild(new TextContent("\\s+"));
		html.addExpectedChild(body);
		Tag span = new Tag("span");
		body.addExpectedChild(span);

		span.addExpectedChild(new TextContent("\\s*Panel Content"));

		validator.addRootElement(html);
		return validator.isDocumentValid(document);
	}
}
