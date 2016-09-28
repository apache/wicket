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
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-4251
 *
 * Tests that an intercepted Ajax request is continued in non-Ajax response
 */
public class RestartResponseAtInterceptPageExceptionInAjaxTest extends WicketTestCase
{

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication() {
			@Override
			public Class<? extends Page> getHomePage()
			{
				return HomePage.class;
			}
		};
	}

	public static class HomePage extends WebPage implements IMarkupResourceStreamProvider
	{
		public HomePage() 
		{
	        // set the intercept data
			new RestartResponseAtInterceptPageException(DummyHomePage.class);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body></body></html>");
		}
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4251
	 *
	 * Asserts that the special WebRequest#PARAM_AJAX parameter is no preserved neither in the intercept url nor
	 * in its post parameters
	 */
	@Test
	public void requestAPageInAjaxButReceiveItInNonAjaxResponse()
	{
		// issue ajax request
		tester.executeAjaxUrl(Url.parse("?"+WebRequest.PARAM_AJAX+"=true&"+WebRequest.PARAM_AJAX_BASE_URL+"=/"));

		// verify that ajax request parameters are not saved
		RestartResponseAtInterceptPageException.InterceptData data = RestartResponseAtInterceptPageException.InterceptData.get();
		assertNull(data.getOriginalUrl().getQueryParameter(WebRequest.PARAM_AJAX));
		assertNull(data.getOriginalUrl().getQueryParameter(WebRequest.PARAM_AJAX_BASE_URL));
		assertNull(data.getPostParameters().get(WebRequest.PARAM_AJAX));
		assertNull(data.getPostParameters().get(WebRequest.PARAM_AJAX_BASE_URL));
	}

}
