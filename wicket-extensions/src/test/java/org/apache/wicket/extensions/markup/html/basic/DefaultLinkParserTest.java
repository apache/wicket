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
}
