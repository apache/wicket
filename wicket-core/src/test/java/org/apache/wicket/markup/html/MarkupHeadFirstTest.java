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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * <a href="https://issues.apache.org/jira/browse/WICKET-3761">WICKET-3761</a>
 */
public class MarkupHeadFirstTest extends WicketTestCase
{

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3761">WICKET-3761</a>
	 * 
	 * <p>
	 * Tests that header contribution provided by <wicket:head> is contributed before the header
	 * contribution from {@link Component#renderHead(IHeaderResponse)}
	 * </p>
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderMyPage() throws Exception
	{
		executeTest(MarkupHeadFirstPage.class, "MarkupHeadFirstPage.html");
	}

	/**
	 * A test page
	 */
	public static class MarkupHeadFirstPage extends WebPage
		implements
			IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public MarkupHeadFirstPage()
		{
			add(new MarkupHeadFirstPanel("panel"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><div wicket:id=\"panel\"></div></body></html>");
		}
	}

	private static class MarkupHeadFirstPanel extends Panel
		implements
			IMarkupResourceStreamProvider
	{

		private static final long serialVersionUID = 1L;

		public MarkupHeadFirstPanel(String id)
		{
			super(id);
		}

		@Override
		public void renderHead(IHeaderResponse response)
		{
			response.render(JavaScriptHeaderItem.forUrl("java.js"));
			super.renderHead(response);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<wicket:head><script type=\"text/javascript\" src=\"markup.js\"></script></wicket:head><wicket:panel>content for MyPanel goes here</wicket:panel>");
		}
	}
}
