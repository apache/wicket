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
package org.apache.wicket.session;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-5165
 */
public class BindSessionOnRedirectTest extends WicketTestCase
{

	@Test
	public void bindSessionWhenThereAreFeedbackMessages()
	{
		tester.startPage(FirstPage.class);
		assertTrue(tester.getSession().isTemporary());

		tester.clickLink("link");

		tester.assertRenderedPage(SecondPage.class);
		assertFalse(tester.getSession().isTemporary());
	}

	public static class FirstPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public FirstPage()
		{
			add(new StatelessLink<Void>("link") {

				@Override
				public void onClick()
				{
					getSession().info("Session message");
					setResponsePage(SecondPage.class);
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><a wicket:id='link'>Link</a></body></html>");
		}
	}

	public static class SecondPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public SecondPage()
		{
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>second page</body></html>");
		}
	}
}
