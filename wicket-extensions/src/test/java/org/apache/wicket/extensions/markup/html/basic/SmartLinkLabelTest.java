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

import org.apache.wicket.Component;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for {@link SmartLinkLabel} and {@link SmartLinkMultiLineLabel} as rendered components.
 */
public class SmartLinkLabelTest extends WicketTestCase
{
	private static final String OTHER_URL = "sftp://ftp.test.com/pub";

	@Test
	public void labelLinkifiesWebUrl()
	{
		String body = render(new SmartLinkLabel("component", "see http://www.test.com now"));

		assertEquals("see <a href=\"http://www.test.com\">http://www.test.com</a> now", body);
	}

	@Test
	public void labelDoesNotLinkifyOtherScheme()
	{
		String body = render(new SmartLinkLabel("component", "see " + OTHER_URL + " now"));

		assertEquals("see " + OTHER_URL + " now", body);
	}

	@Test
	public void labelStillEscapesMarkup()
	{
		String body = render(new SmartLinkLabel("component", "<b>bold</b> & http://www.test.com"));

		assertEquals(
			"&lt;b&gt;bold&lt;/b&gt; &amp; <a href=\"http://www.test.com\">http://www.test.com</a>",
			body);
	}

	/**
	 * A quote or an angle bracket in the model text is escaped before the parser runs. The URL
	 * pattern admits '&amp;' but not ';', so an escaped quote right after a URL leaves a truncated
	 * '&amp;quot' inside the href, which a browser decodes into the attribute's value.
	 */
	@Test
	public void labelEscapesMarkupAroundUrl()
	{
		String body = render(new SmartLinkLabel("component",
			"see http://www.test.com/a\" and <b>bold</b> now"));

		assertEquals("see <a href=\"http://www.test.com/a&quot\">http://www.test.com/a&quot</a>"
			+ "; and &lt;b&gt;bold&lt;/b&gt; now", body);
	}

	@Test
	public void multiLineLabelLinkifiesWebUrl()
	{
		String body = render(
			new SmartLinkMultiLineLabel("component", "line 1 http://www.test.com\nline 2"));

		assertEquals("<p>line 1 <a href=\"http://www.test.com\">http://www.test.com</a><br/>line 2</p>",
			body);
	}

	@Test
	public void multiLineLabelDoesNotLinkifyOtherScheme()
	{
		String body = render(
			new SmartLinkMultiLineLabel("component", "line 1 " + OTHER_URL + "\nline 2"));

		assertEquals("<p>line 1 " + OTHER_URL + "<br/>line 2</p>", body);
	}

	private String render(Component component)
	{
		tester.startComponentInPage(component);
		String response = tester.getLastResponseAsString();

		String open = "<span wicket:id=\"component\">";
		int start = response.indexOf(open);
		int end = response.indexOf("</span>", start);
		assertTrue("component not rendered in:\n" + response, start != -1 && end != -1);
		return response.substring(start + open.length(), end);
	}
}
