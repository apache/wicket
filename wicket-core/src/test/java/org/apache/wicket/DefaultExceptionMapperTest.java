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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.settings.def.ExceptionSettings;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link DefaultExceptionMapper}
 */
public class DefaultExceptionMapperTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				getExceptionSettings().setUnexpectedExceptionDisplay(
					ExceptionSettings.SHOW_NO_EXCEPTION_PAGE);
			}
		};
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-4659">WICKET-4659</a>
	 */
	@Test
	public void shouldDisableCaching()
	{
		WebResponse response = mock(WebResponse.class);
		tester.getRequestCycle().setResponse(response);
		new DefaultExceptionMapper().map(mock(Exception.class));
		verify(response).disableCaching();
		tester.destroy();
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3520">WICKET-3520</a>
	 */
	@Test
	public void showNoExceptionPage()
	{
		tester.setExposeExceptions(false);

		ShowNoExceptionPage page = new ShowNoExceptionPage(null);
		tester.startPage(page);

		tester.submitForm("form");

		Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, tester.getLastResponse()
			.getStatus());

		tester.destroy();
	}

	/**
	 * A test page for {@link DefaultExceptionMapperTest#showNoExceptionPage()}
	 */
	public static class ShowNoExceptionPage extends WebPage
		implements
			IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param parameters
		 */
		public ShowNoExceptionPage(final PageParameters parameters)
		{
			super(parameters);

			Form<?> form = new Form<Void>("form")
			{
				private static final long serialVersionUID = 1L;

				/**
				 * Always fails.
				 */
				@Override
				public void onSubmit()
				{
					throw new RuntimeException("test");
				}

			};
			add(form);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><form wicket:id=\"form\"></form></body></html>");
		}

	}


}
