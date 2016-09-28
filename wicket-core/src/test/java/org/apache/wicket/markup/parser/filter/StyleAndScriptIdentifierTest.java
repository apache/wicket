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
package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @since 6.0
 */
public class StyleAndScriptIdentifierTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-4425
	 *
	 * Verifies that the content of <script id="script1" type="text/x-jquery-tmpl">
	 * wont be wrapped in CDATA, while all <script type="text/javascript"> will
	 * be wrapped unless they have their body already wrapped
	 *
	 * @throws Exception
	 */
	@Test
	public void doNotWrapScriptTemplates() throws Exception
	{
		executeTest(PageWithScriptTemplate.class, "PageWithScriptTemplate_expected.html");
	}

	@Test
	public void showWrapInCdata()
	{
		StyleAndScriptIdentifier filter = new StyleAndScriptIdentifier();

		String elementBody = "<!-- someJS() ";
		assertFalse(filter.shouldWrapInCdata(elementBody));

		elementBody = "\n<!-- someJS() ";
		assertFalse(filter.shouldWrapInCdata(elementBody));

		elementBody = "  <!-- someJS() ";
		assertFalse(filter.shouldWrapInCdata(elementBody));


		elementBody = "<![CDATA[ someJS() ";
		assertFalse(filter.shouldWrapInCdata(elementBody));

		elementBody = "\n<![CDATA[ someJS() ";
		assertFalse(filter.shouldWrapInCdata(elementBody));

		elementBody = "  <![CDATA[ someJS() ";
		assertFalse(filter.shouldWrapInCdata(elementBody));


		elementBody = "/*<![CDATA[*/ someJS() ";
		assertFalse(filter.shouldWrapInCdata(elementBody));

		elementBody = "\n/*<![CDATA[ */ someJS() ";
		assertFalse(filter.shouldWrapInCdata(elementBody));

		elementBody = "  /* <![CDATA[ */ \n someJS() ";
		assertFalse(filter.shouldWrapInCdata(elementBody));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4453
	 *
	 * This test wraps rawMarkup in org.apache.wicket.util.string.JavaScriptUtils#SCRIPT_CONTENT_PREFIX
	 * twice - once in Markup.of() and second in the explicit call to StyleAndScriptIdentifier.postProcess().
	 * The second time it realizes that the element body is already wrapped and skips it.
	 */
	@Test
	public void postProcess()
	{
		String rawMarkup = "<script>someJS()</script>";
		Markup createMarkupElementsMarkup = Markup.of(rawMarkup);
		Markup markup = new Markup(createMarkupElementsMarkup.getMarkupResourceStream());
		for (MarkupElement markupElement : createMarkupElementsMarkup)
		{
			markup.addMarkupElement(markupElement);
		}
		StyleAndScriptIdentifier filter = new StyleAndScriptIdentifier();
		filter.postProcess(markup);
		assertEquals("<script>\n/*<![CDATA[*/\nsomeJS()\n/*]]>*/\n</script>", markup.toString(true));
	}
}
