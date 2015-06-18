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

import org.junit.Assert;
import org.apache.wicket.protocol.http.documentvalidation.HtmlDocumentValidator;
import org.apache.wicket.protocol.http.documentvalidation.Tag;
import org.apache.wicket.protocol.http.documentvalidation.TextContent;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This set of tests builds a sample tester for testing the dynamic modicication of attributes in
 * both component tags and also in raw markup. The tests contained here represent testing on one
 * specific area of functionality of the Component class. It is expected that separate test cases
 * will be added to test other facets of Components.
 * 
 * @author Chris Turner
 */
public class AttributeModifierComponentTest extends WicketTestCase
{
	private static final Logger log = LoggerFactory.getLogger(AttributeModifierComponentTest.class);

	/**
	 * 
	 */
	@Before
	public void before()
	{
		tester.startPage(AttributeModifierComponentPage.class);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void componentTagAttributeModification() throws Exception
	{
		String document = tester.getLastResponseAsString();
		// log.info(document);
		Assert.assertTrue(document, validateDocument(document));
	}

	/**
	 * Helper method to validate the returned XML document.
	 * 
	 * @param document
	 *            The document
	 * @return The validation result
	 */
	private boolean validateDocument(String document)
	{
		HtmlDocumentValidator validator = new HtmlDocumentValidator();
		Tag html = new Tag("html");
		Tag head = new Tag("head");
		html.addExpectedChild(head);
		Tag title = new Tag("title");
		head.addExpectedChild(title);
		title.addExpectedChild(new TextContent("Attribute Modifier Test Page"));
		Tag body = new Tag("body");
		html.addExpectedChild(body);

		Tag label1 = new Tag("span");
		label1.addExpectedAttribute("class", "label");
		label1.addExpectedChild(new TextContent("Label 1"));
		body.addExpectedChild(label1);

		Tag label2 = new Tag("span");
		label2.addExpectedAttribute("class", "overrideLabel");
		label2.addExpectedChild(new TextContent("Label 2"));
		body.addExpectedChild(label2);

		Tag label3 = new Tag("span");
		label3.addExpectedAttribute("class", "insertLabel");
		label3.addExpectedChild(new TextContent("Label 3"));
		body.addExpectedChild(label3);

		validator.addRootElement(html);
		return validator.isDocumentValid(document);
	}
}
