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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 */
public class WicketTesterSessionInvalidateTest extends WicketTestCase
{

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3212
	 */
	@Test
	public void sessionInvalidate()
	{
		tester.startPage(MyPage.class);

		assertNull(tester.getSession().getStyle());

		tester.getSession().setStyle("style1");

		assertEquals("style1", tester.getSession().getStyle());

		// invalidate the session
		tester.clickLink("link");

		assertNull(tester.getSession().getStyle());
	}

	/**
	 */
	public static class MyPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param pageParameters
		 */
		public MyPage(PageParameters pageParameters)
		{
			super(pageParameters);

			add(new Link<Void>("link")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					getSession().invalidate();
				}

			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id='link'>link</a></body></html>");
		}
	}
}
