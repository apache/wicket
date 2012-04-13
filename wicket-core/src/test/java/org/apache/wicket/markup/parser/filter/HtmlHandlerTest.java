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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * @since 1.5.6
 */
public class HtmlHandlerTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-4494
	 *
	 * Asserts that a page which markup is loaded dynamically with IMarkupResourceStreamProvider
	 * and the the loaded markup has HtmlHandler#doesNotRequireCloseTag tags loads OK
	 *
	 * @throws Exception
	 */
	@Test
	public void loadMarkupWithNonClosedTagsDynamically() throws Exception
	{
		CustomMarkupPage page = new CustomMarkupPage();
		tester.executeTest(HtmlHandlerTest.class, page, "DynamicMarkupPageWithNonClosedTags_expected.html");
	}

	/**
	 * The test page for #loadMarkupWithNonClosedTagsDynamically()
	 */
	private static class CustomMarkupPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private CustomMarkupPage()
		{
			add(new CustomMarkupLabel("label"));
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><span wicket:id='label'></span></body></html>");
		}
	}
}
