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

import static org.hamcrest.CoreMatchers.instanceOf;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * @author Pedro Santos
 */
public class WebPageTest extends WicketTestCase
{

	/**
	 * Asserting newly created pages get a new page id in order to be identified latter.
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3892">WICKET-3892</a>
	 */
	@Test
	public void increaseNewlyCreatedPageId()
	{
		tester.startPage(MainPage.class);
		int mainPageId = tester.getLastRenderedPage().getPageId();

		tester.clickLink("mainLink");
		tester.assertRenderedPage(TargetPage.class);
		int targetPageId = tester.getLastRenderedPage().getPageId();

		assertTrue(mainPageId != targetPageId);

		IManageablePage mainPage = tester.getSession().getPageManager().getPage(mainPageId);
		IManageablePage targetPage = tester.getSession().getPageManager().getPage(targetPageId);

		assertThat(mainPage, instanceOf(MainPage.class));
		assertThat(targetPage, instanceOf(TargetPage.class));
	}

	/** */
	public static class MainPage extends WebPage implements IMarkupResourceStreamProvider
	{
		/** */
		public MainPage()
		{
			AjaxLink<Void> mainLink = new AjaxLink<Void>("mainLink")
			{
				@Override
				public void onClick(AjaxRequestTarget target)
				{
					TargetPage targetPage = new TargetPage();
					setResponsePage(targetPage);
				}
			};
			add(mainLink);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id=\"mainLink\"></a></body></html>");
		}
	}

	/** */
	public static class TargetPage extends WebPage implements IMarkupResourceStreamProvider
	{
		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body></body></html>");
		}
	}

}
