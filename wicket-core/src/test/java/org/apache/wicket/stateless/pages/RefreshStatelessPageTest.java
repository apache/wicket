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
package org.apache.wicket.stateless.pages;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-3965
 */
public class RefreshStatelessPageTest extends WicketTestCase
{

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3965
	 */
	@Test
	public void refreshStatelessPage()
	{
		tester.startPage(new StatefulPage());
		tester.clickLink("link", false);

		tester.assertRenderedPage(StatelessPage.class);
		Page renderedPage = tester.getLastRenderedPage();
		tester.executeUrl("wicket/page?" + renderedPage.getId());
		tester.assertRenderedPage(StatelessPage.class);
	}

	private static class StatefulPage extends WebPage implements IMarkupResourceStreamProvider
	{

		private StatefulPage()
		{
			add(new Link<Void>("link")
			{
				@Override
				public void onClick()
				{
					setResponsePage(new StatelessPage());
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body>Stateful <a wicket:id='link'>Link</a></body></html>");
		}
	}

	private static class StatelessPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public StatelessPage()
		{
			setStatelessHint(true);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>Stateless</body></html>");
		}
	}

}
