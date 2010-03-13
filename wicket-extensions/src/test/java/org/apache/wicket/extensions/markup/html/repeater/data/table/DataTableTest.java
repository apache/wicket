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
package org.apache.wicket.extensions.markup.html.repeater.data.table;

import junit.framework.TestCase;

import org.apache.wicket.util.diff.DiffUtil;
import org.apache.wicket.util.tester.WicketTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DataTableTest extends TestCase
{
	private WicketTester tester;

	@Override
	protected void setUp() throws Exception
	{
		tester = new WicketTester(new RepeaterApplication());
	}

	@Override
	protected void tearDown() throws Exception
	{
		tester.destroy();
	}

	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(DataTableTest.class);

	/**
	 * @throws Exception
	 */
	public void test_1() throws Exception
	{
		tester.startPage(DataTablePage.class);
		tester.assertRenderedPage(DataTablePage.class);

		String document = tester.getLastResponseAsString();
		int index = document.indexOf("<thead");
		assertTrue("Expected at least on <thead>", index != -1);
		index = document.indexOf("<thead", index + 1);
		assertTrue("There must be only one <thead>", index == -1);

		index = document.indexOf("<tbody");
		assertTrue("Expected at least on <tbody>", index != -1);
		index = document.indexOf("<tbody", index + 1);
		assertTrue("There must be only one <tbody>", index == -1);

		log.error(document);
		log.error("==============================================");
		log.error("==============================================");
		log.error(removeFillers(document));

		String doc = removeFillers(document);
		DiffUtil.validatePage(doc, getClass(), "DataTablePage_ExpectedResult.html", true);
	}

	private String removeFillers(String doc)
	{
		doc = doc.replaceAll("(?s)<span .*?>.*?</span>", "<x/>");
		doc = doc.replaceAll("(?s)<div .*?>.*?</div>", "<x/>");
		doc = doc.replaceAll("(?s)<a .*?>.*?</a>", "<x/>");
		doc = doc.replaceAll("(?s)>\\s*?[\\n\\r]+\\s*?</", "><x/></");
		doc = doc.replaceAll("(?s)[\\n\\r]+\\s*?([\\n\\r]+)", "\r\n");
		doc = doc.replaceAll("(<x/>)+", "<x/>");

		return doc;
	}
}
