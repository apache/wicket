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
package org.apache.wicket;

import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.AbstractStringResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for the usages of {@link RestartResponseException}
 */
public class RestartResponseExceptionTest extends WicketTestCase
{
	/**
	 * Tests that following several {@link RestartResponseException}s will actually leave you at the
	 * final page.
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-2634
	 */
	@Test
	public void doubleRedirect()
	{
		tester.startPage(RestartPage1.class);
		tester.assertRenderedPage(MyDummyPage.class);
	}

	/**
	 * 
	 */
	public static class RestartPage1 extends WebPage
	{
		/**
		 * Constructor.
		 */
		public RestartPage1()
		{
			throw new RestartResponseAtInterceptPageException(RestartPage2.class);
		}
	}

	/**
	 * The final destination
	 */
	public static class MyDummyPage extends WebPage implements IMarkupResourceStreamProvider
	{
		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new AbstractStringResourceStream()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected String getString()
				{
					return "<html></html>";
				}
			};
		}
	}

	/**
	 * 
	 */
	public static class RestartPage2 extends WebPage
	{
		/**
		 * Constructor.
		 */
		public RestartPage2()
		{
			throw new RestartResponseException(MyDummyPage.class);
		}
	}
}
