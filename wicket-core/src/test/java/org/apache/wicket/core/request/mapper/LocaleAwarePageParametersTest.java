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
package org.apache.wicket.core.request.mapper;

import static org.hamcrest.Matchers.is;

import java.util.Locale;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-6419
 */
public class LocaleAwarePageParametersTest extends WicketTestCase
{
	private Locale defaultLocale = null;

	@Before
	public void before()
	{
		defaultLocale = Locale.getDefault(Locale.Category.DISPLAY);
		Locale.setDefault(Locale.Category.DISPLAY, Locale.ENGLISH);
	}

	@After
	public void after()
	{
		if (defaultLocale != null)
		{
			Locale.setDefault(Locale.Category.DISPLAY, defaultLocale);
		}
	}

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();

				mountPage("unaware", LocaleUnawarePageParametersPage.class);
				mount(new MountedMapper("aware", LocaleAwarePageParametersPage.class)
				      {
					      @Override
					      protected Locale resolveLocale()
					      {
						      return resolveUserLocale();
					      }
				      }
				);
			}

			@Override
			public Session newSession(Request request, Response response)
			{
				final Session session = super.newSession(request, response);
				session.setLocale(Locale.GERMANY);
				return session;
			}
		};
	}

	@Test
	public void localeUnaware()
	{
		tester.executeUrl("unaware?number=1.234,0");
		final Page page = tester.getLastRenderedPage();

		assertThat(page.getPageParameters().get("number").toDouble(), is(1.234));
	}

	@Test
	public void localeAware()
	{
		tester.executeUrl("aware?number=1.234,0");
		final Page page = tester.getLastRenderedPage();

		assertThat(page.getPageParameters().get("number").toDouble(), is(1234d));
	}

	private static class BasePage extends WebPage implements IMarkupResourceStreamProvider
	{
		protected BasePage(PageParameters parameters)
		{
			super(parameters);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>content</body></html>");
		}
	}

	public static class LocaleUnawarePageParametersPage extends BasePage
	{
		public LocaleUnawarePageParametersPage(PageParameters parameters)
		{
			super(parameters);
		}
	}

	public static class LocaleAwarePageParametersPage extends BasePage
	{
		public LocaleAwarePageParametersPage(PageParameters parameters)
		{
			super(parameters);
		}
	}
}
