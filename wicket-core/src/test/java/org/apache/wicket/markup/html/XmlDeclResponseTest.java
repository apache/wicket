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
package org.apache.wicket.markup.html;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupType;
import org.apache.wicket.protocol.http.WebApplication;
import org.junit.Test;

/**
 * Test xml decl in WebResponse
 */
public class XmlDeclResponseTest extends WicketTestCase
{
	private static final String markupWith = "<?xml version='1.0' encoding='UTF-8' ?><html><body></body></html>";
	private static final String markupWithout = "<html><body></body></html>";

	private static final String acceptString = "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
	public static final String ACCEPT = "Accept";

	private int insertXmlDecl = 0;

	@Override
	protected WebApplication newApplication()
	{
		return new WebApplication()
		{
			@Override
			public Class<? extends Page> getHomePage()
			{
				return null;
			}

			@Override
			public void renderXmlDecl(WebPage page, boolean insert)
			{
				// < 0 : never render xml decl
				// == 0: apply rules
				// > 0 : always render xml decl (ignore rules)
				if (insertXmlDecl >= 0)
				{
					super.renderXmlDecl(page, (insertXmlDecl > 0 ? true : false));
				}
			}
		};
	}

	/** */
	@Test
	public void insertAlways()
	{
		insertXmlDecl = 1;

		tester.startPage(new SimplePage(MarkupType.HTML_MIME));
		String doc = tester.getLastResponseAsString();
		assertEquals(markupWith, doc);

		tester.startPage(new SimplePage(MarkupType.XML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWith, doc);

		tester.addRequestHeader(ACCEPT, acceptString);
		tester.startPage(new SimplePage(MarkupType.HTML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWith, doc);

		tester.addRequestHeader(ACCEPT, acceptString);
		tester.startPage(new SimplePage(MarkupType.XML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWith, doc);

		tester.addRequestHeader(ACCEPT, "xx");
		tester.startPage(new SimplePage(MarkupType.HTML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWith, doc);

		tester.addRequestHeader(ACCEPT, "xx");
		tester.startPage(new SimplePage(MarkupType.XML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWith, doc);
	}

	/** */
	@Test
	public void insertNever()
	{
		insertXmlDecl = -1;

		tester.startPage(new SimplePage(MarkupType.HTML_MIME));
		String doc = tester.getLastResponseAsString();
		assertEquals(markupWithout, doc);

		tester.startPage(new SimplePage(MarkupType.XML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWithout, doc);

		tester.addRequestHeader(ACCEPT, acceptString);
		tester.startPage(new SimplePage(MarkupType.HTML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWithout, doc);

		tester.addRequestHeader(ACCEPT, acceptString);
		tester.startPage(new SimplePage(MarkupType.XML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWithout, doc);

		tester.addRequestHeader(ACCEPT, "xx");
		tester.startPage(new SimplePage(MarkupType.HTML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWithout, doc);

		tester.addRequestHeader(ACCEPT, "xx");
		tester.startPage(new SimplePage(MarkupType.XML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWithout, doc);
	}

	/** */
	@Test
	public void insertWithRules()
	{
		insertXmlDecl = 0;

		tester.startPage(new SimplePage(MarkupType.HTML_MIME));
		String doc = tester.getLastResponseAsString();
		assertEquals(markupWithout, doc);

		tester.startPage(new SimplePage(MarkupType.XML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWith, doc);

		tester.addRequestHeader(ACCEPT, acceptString);
		tester.startPage(new SimplePage(MarkupType.HTML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWithout, doc);

		tester.addRequestHeader(ACCEPT, acceptString);
		tester.startPage(new SimplePage(MarkupType.XML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWith, doc);

		tester.addRequestHeader(ACCEPT, "xx");
		tester.startPage(new SimplePage(MarkupType.HTML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWithout, doc);

		tester.addRequestHeader(ACCEPT, "xx");
		tester.startPage(new SimplePage(MarkupType.XML_MIME));
		doc = tester.getLastResponseAsString();
		assertEquals(markupWithout, doc);
	}

	/**
	 * 
	 */
	public static class SimplePage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		private final String mime;

		/**
		 * @param mime
		 */
		public SimplePage(String mime)
		{
			this.mime = mime;
		}

		@Override
		public MarkupType getMarkupType()
		{
			return new MarkupType("html", mime);
		}

		@Override
		public IMarkupFragment getMarkup()
		{
			return Markup.of(markupWithout);
		}
	}
}
