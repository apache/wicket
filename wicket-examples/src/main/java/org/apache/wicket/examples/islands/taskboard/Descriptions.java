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
package org.apache.wicket.examples.islands.taskboard;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Sanitizes card descriptions. A description is HTML written in the browser's rich-text editor
 * and sent with the form, so it is untrusted: it goes through an allowlist of formatting
 * elements before it reaches a card, and is only rendered unescaped because of that.
 */
public final class Descriptions
{
	private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
		.allowElements("p", "br", "b", "strong", "i", "em", "u", "s", "code", "pre", "blockquote",
			"ul", "ol", "li", "h3", "h4", "a")
		.allowUrlProtocols("https", "http", "mailto")
		.allowAttributes("href")
		.onElements("a")
		.requireRelsOnLinks("noopener", "noreferrer", "nofollow")
		.toFactory();

	private Descriptions()
	{
	}

	/**
	 * @param html
	 *            HTML from the client, untrusted
	 * @return the HTML with every element, attribute and URL outside the allowlist removed
	 */
	public static String sanitize(String html)
	{
		return html == null ? "" : POLICY.sanitize(html);
	}
}
