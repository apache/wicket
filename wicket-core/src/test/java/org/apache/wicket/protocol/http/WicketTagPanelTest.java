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

import org.apache.wicket.protocol.http.documentvalidation.HtmlDocumentValidator;
import org.apache.wicket.protocol.http.documentvalidation.Tag;
import org.apache.wicket.protocol.http.documentvalidation.TextContent;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple application that demonstrates the mock http application code (and checks that it is
 * working)
 * 
 * @author Chris Turner
 */
public class WicketTagPanelTest extends WicketTestCase
{
	private static final Logger log = LoggerFactory.getLogger(WicketTagPanelTest.class);

	/**
	 * @throws Exception
	 */
	@Test
	public void renderHomePage() throws Exception
	{
		tester.startPage(WicketPanelPage.class);
		String document = tester.getLastResponseAsString();
		// log.info(document);
		assertTrue(document, validatePage1(document));
	}

	/**
	 * Validate page 2 of the paged table.
	 * 
	 * @param document
	 *            The document
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
	@Test
	public void renderHomePageWicketTagRemoved() throws Exception
	{
		// Remove wicket tags from output
		tester.getApplication().getMarkupSettings().setStripWicketTags(true);
		tester.startPage(WicketPanelPage.class);

		// Validate the document
		String document = tester.getLastResponseAsString();
		// log.info(document);
		assertTrue("Document with Wicket tags stripped did not match", validatePage2(document));
	}

	/**
	 * Validate page 2 of the paged table.
	 * 
	 * @param document
	 *            The document
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
