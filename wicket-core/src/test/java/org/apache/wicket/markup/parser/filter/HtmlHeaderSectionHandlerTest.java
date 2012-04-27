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
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

public class HtmlHeaderSectionHandlerTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-4511
	 *
	 * Asserts that HtmlHeaderSectionHandler throws a MarkupException if a &lt;BODY&gt; tag is found
	 * inside &lt;HEAD&gt;
	 *
	 * @throws Exception
	 */
	@Test(expected = MarkupException.class)
	public void loadMarkupWithBodyInsideHead() throws Exception
	{
		CustomMarkupPage customMarkupPage = new CustomMarkupPage();
		tester.startPage(customMarkupPage);
	}

	private static class CustomMarkupPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			// <head> is not closed before <body>
			return new StringResourceStream("<html><head><body>bad markup!</body></head></html>");
		}
	}
}
