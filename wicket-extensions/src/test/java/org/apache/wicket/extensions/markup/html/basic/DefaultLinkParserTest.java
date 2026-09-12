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
package org.apache.wicket.extensions.markup.html.basic;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link DefaultLinkParser}.
 * 
 * @author Juergen Donnerstag
 */
public class DefaultLinkParserTest extends Assert
{
	/**
	 * 
	 */
	@Test
	public void test1()
	{
		ILinkParser parser = new DefaultLinkParser();
		assertNull(parser.parse(null));
		assertEquals("", parser.parse(""));
		assertEquals("test", parser.parse("test"));

		assertEquals("<a href=\"mailto:test@email.com\">test@email.com</a>",
			parser.parse("test@email.com"));
		assertEquals("text (<a href=\"mailto:test@email.com\">test@email.com</a>) text",
			parser.parse("text (test@email.com) text"));
		assertEquals("text <a href=\"mailto:test@email.com\">test@email.com</a> text",
			parser.parse("text test@email.com text"));

		assertEquals("<a href=\"http://www.test.com\">http://www.test.com</a>",
			parser.parse("http://www.test.com"));
		assertEquals("text (<a href=\"http://www.test.com\">http://www.test.com</a>) text",
			parser.parse("text (http://www.test.com) text"));
		assertEquals("text <a href=\"http://www.test.com\">http://www.test.com</a> text",
			parser.parse("text http://www.test.com text"));
		assertEquals("text <a href=\"http://www.test.com:8080\">http://www.test.com:8080</a> text",
			parser.parse("text http://www.test.com:8080 text"));
		assertEquals(
			"text <a href=\"http://www.test.com/test/murx.jsp\">http://www.test.com/test/murx.jsp</a> text",
			parser.parse("text http://www.test.com/test/murx.jsp text"));
		assertEquals(
			"text <a href=\"http://www.test.com/test/murx.jsp?query=test&q2=murx\">http://www.test.com/test/murx.jsp</a> text",
			parser.parse("text http://www.test.com/test/murx.jsp?query=test&q2=murx text"));

		assertEquals(
			"line 1 <a href=\"http://www.test.com/test/murx.jsp\">http://www.test.com/test/murx.jsp</a> \nline2 <a href=\"mailto:murx@email.de\">murx@email.de</a> \r\nline3",
			parser.parse("line 1 http://www.test.com/test/murx.jsp \nline2 murx@email.de \r\nline3"));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4099
	 */
	@Test
	public void dontParseMarkup()
	{
		String text = "<a href=\"http://some.url\">label</a>";
		ILinkParser parser = new DefaultLinkParser();

		String parsed = parser.parse(text);
		assertEquals(text, parsed);
	}

	/**
	 * WICKET-3174
	 */
	@Test
	public void testEmailWithPlusChar()
	{
		final String testEmailAddress = "my+test@example.com";
		final String testExpectedLink = "<a href=\"mailto:my+test@example.com\">my+test@example.com</a>";

		ILinkParser parser = new DefaultLinkParser();
		final String result = parser.parse(testEmailAddress);
		assertEquals("Expected chars to left of + to be included in the link.", testExpectedLink,
			result);
	}

	/**
	 * WICKET-4477
	 */
	@Test
	public void testEmailWithMinusChar()
	{
		final String testEmailAddress = "my-test@example.com";
		final String testExpectedLink = "<a href=\"mailto:my-test@example.com\">my-test@example.com</a>";

		ILinkParser parser = new DefaultLinkParser();
		final String result = parser.parse(testEmailAddress);
		assertEquals("Expected chars to left of - to be included in the link.", testExpectedLink,
			result);
	}

	/**
	 * The schemes the javadoc promises are linkified, case-insensitively.
	 */
	@Test
	public void allowedSchemesAreLinkified()
	{
		ILinkParser parser = new DefaultLinkParser();
		for (String url : new String[] { "http://www.test.com", "https://www.test.com",
				"ftp://ftp.test.com/pub", "HTTPS://www.test.com", "Http://www.test.com" })
		{
			assertEquals("<a href=\"" + url + "\">" + url + "</a>", parser.parse(url));
			assertEquals("text <a href=\"" + url + "\">" + url + "</a> text",
				parser.parse("text " + url + " text"));
		}
	}

	/**
	 * A URL with any other scheme is left as it is, alone and inside other text.
	 */
	@Test
	public void otherSchemesAreNotLinkified()
	{
		ILinkParser parser = new DefaultLinkParser();
		for (String url : new String[] { "sftp://ftp.test.com/pub", "ssh://www.test.com",
				"file://etc/hosts", "gopher://www.test.com/1", "httpx://www.test.com",
				"Ftps://ftp.test.com", "news://www.test.com?x=1" })
		{
			assertEquals(url, parser.parse(url));
			assertEquals("Look at " + url + " please", parser.parse("Look at " + url + " please"));
		}
	}

	/**
	 * A URL that is not linkified does not affect an allowed one next to it, and the other way
	 * round.
	 */
	@Test
	public void mixedSchemesInOneText()
	{
		ILinkParser parser = new DefaultLinkParser();
		assertEquals(
			"sftp://ftp.test.com and <a href=\"http://www.test.com\">http://www.test.com</a>",
			parser.parse("sftp://ftp.test.com and http://www.test.com"));
	}

	/**
	 * The render strategy only builds links for http, https and ftp on its own, whatever the pattern
	 * it is paired with matched.
	 */
	@Test
	public void urlRenderStrategyOnlyLinksAllowedSchemes()
	{
		ILinkRenderStrategy strategy = DefaultLinkParser.URL_RENDER_STRATEGY;
		assertEquals("<a href=\"http://www.test.com\">http://www.test.com</a>",
			strategy.buildLink("http://www.test.com"));
		assertEquals("sftp://ftp.test.com/pub", strategy.buildLink("sftp://ftp.test.com/pub"));
		assertEquals("Ssh://www.test.com", strategy.buildLink("Ssh://www.test.com"));

		LinkParser wide = new LinkParser();
		wide.addLinkRenderStrategy("[a-zA-Z]+://[\\w\\.\\/]+", strategy);
		assertEquals("sftp://ftp.test.com/pub", wide.parse("sftp://ftp.test.com/pub"));
		assertEquals("<a href=\"http://test\">http://test</a>", wide.parse("http://test"));
	}
}
