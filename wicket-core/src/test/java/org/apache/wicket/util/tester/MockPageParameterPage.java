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
package org.apache.wicket.util.tester;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Mock page for testing PageParameters handling in WicketTester.
 * 
 * @author Frank Bille Jensen (frankbille)
 */
public class MockPageParameterPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param pageParameters
	 */
	public MockPageParameterPage(PageParameters pageParameters)
	{
		add(new BookmarkablePageLink<Void>("link", MockPageParameterPage.class,
			new PageParameters().set("id", 1)));
		add(new Label("label", pageParameters.get("id").toString()));
	}

	/**
	 * Mock Page for testing WebPages implements as inner pages.
	 */
	public static final class MockInnerClassPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public MockInnerClassPage()
		{
			add(new Label("title", "Hello world!"));
		}
	}
}
