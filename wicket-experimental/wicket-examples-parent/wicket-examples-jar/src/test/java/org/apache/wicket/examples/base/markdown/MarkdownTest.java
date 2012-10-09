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
package org.apache.wicket.examples.base.markdown;

import java.io.IOException;
import java.nio.charset.Charset;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.io.Resources;

public class MarkdownTest
{
	@Test
	public void testAlert()
	{
		String html = Markdown.markdownToHtml("{% alert %}Boo{% endalert %}");
		Assert.assertEquals("<p><div class=\"alert alert-info\">Boo</div>\n</p>\n", html);
	}

	@Test
	public void testAlert2()
	{
		String html = Markdown.markdownToHtml("{% alert alert-info %}\n"
			+ "The constructor of the page you are linking to needs to be either a\n"
			+ "default constructor or a constructor taking `PageParameters` as the\n"
			+ "sole parameter—otherwise Wicket will not be able to create an\n"
			+ "instance of your page.\n" + "{% endalert %}\n");

		Assert.assertEquals("<p><div class=\"alert alert-info\">\n"
			+ "The constructor of the page you are linking to needs to be either a\n"
			+ "default constructor or a constructor taking <code>PageParameters</code> as the\n"
			+ "sole parameter—otherwise Wicket will not be able to create an\n"
			+ "instance of your page.\n" + "</div>\n</p>\n", html);
	}

	@Test
	public void testAlert3() throws IOException
	{
		String markdown = Resources.toString(
			Resources.getResource(MarkdownTest.class, "alert3.md"), Charset.forName("utf-8"));
		String html = Markdown.markdownToHtml(markdown);
		String expected = Resources.toString(
			Resources.getResource(MarkdownTest.class, "expected3.html"), Charset.forName("utf-8"));
		Assert.assertEquals(expected, html);
	}
}
