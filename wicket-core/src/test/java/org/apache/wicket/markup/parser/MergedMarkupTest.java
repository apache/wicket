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
package org.apache.wicket.markup.parser;

import junit.framework.TestCase;

import org.apache.wicket.Page;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupParser;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.util.tester.WicketTester;

/**
 * 
 */
public class MergedMarkupTest extends TestCase
{
	/**
	 * test1()
	 */
	public void test1()
	{
		WicketTester tester = new WicketTester();

		Page page = new SubPageWithoutMarkup();
		Markup markup = page.getAssociatedMarkup();
		MarkupResourceStream stream = markup.getMarkupResourceStream();
		assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>", stream.getXmlDeclaration());
		assertEquals("utf-8", stream.getEncoding());
		assertEquals(MarkupParser.WICKET, stream.getWicketNamespace());
	}

	/**
	 * test2()
	 */
	public void test2()
	{
		WicketTester tester = new WicketTester();

		Page page = new SubPageWithMarkup();
		MarkupStream markup = page.getAssociatedMarkupStream(true);
		assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>", markup.getXmlDeclaration());
		assertEquals("utf-8", markup.getEncoding());
		assertEquals(MarkupParser.WICKET, markup.getWicketNamespace());
	}
}
