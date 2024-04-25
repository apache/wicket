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

package org.apache.wicket.csp;


import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.Serial;
import java.util.List;

class CSPRequestCycleListenerTest extends WicketTestCase
{
	public static List<Class<? extends Page>> pageClasses()
	{
		return List.of( DummyHomePage.class, RedirectPage.class, RestartPage.class, StatelessAutoRedirectPage.class, StatefulAutoRedirectPage.class );
	}

	protected WebApplication newApplication()
	{
		return new WebApplication()
		{
			@Override
			public Class<? extends Page> getHomePage()
			{
				return DummyHomePage.class;
			}

			@Override
			protected void init()
			{
				super.init();

				getCspSettings().blocking().strict()
						.add(CSPDirective.STYLE_SRC, CSPDirectiveSrcValue.SELF)
						.add(CSPDirective.STYLE_SRC, "https://fonts.foo.bar/css")
						.add(CSPDirective.FONT_SRC, "https://fonts.foo.bar");

				mountPage("redirect", RedirectPage.class);
			}
		};
	}

	@ParameterizedTest(name="pageClass={arguments}")
	@MethodSource("pageClasses")
	<C extends Page> void strict(final Class<C> pageClass)
	{
		tester.startPage(pageClass);
		tester.assertRenderedPage(DummyHomePage.class);

		final String cspHeaderValue = tester.getLastResponse().getHeader("Content-Security-Policy");
		Assertions.assertEquals("default-src 'none'; script-src 'strict-dynamic' 'NONCE'; style-src 'NONCE' 'self' https://fonts.foo.bar/css; img-src 'self'; connect-src 'self'; font-src 'self' https://fonts.foo.bar; manifest-src 'self'; child-src 'self'; base-uri 'self'; frame-src 'self'", cspHeaderValue.replaceAll("'nonce-[^']+'", "'NONCE'"));
	}

	public static class RestartPage extends WebPage
	{
		@Serial
		private static final long serialVersionUID = 1L;

		public RestartPage()
		{
			throw new RestartResponseException(
					new PageProvider(DummyHomePage.class),
					RedirectPolicy.NEVER_REDIRECT
			);
		}
	}

	public static class RedirectPage extends WebPage
	{
		@Serial
		private static final long serialVersionUID = 1L;

		public RedirectPage()
		{
			throw new RestartResponseException(
					new PageProvider(DummyHomePage.class),
					RedirectPolicy.ALWAYS_REDIRECT
			);
		}
	}

	public static class StatelessAutoRedirectPage extends WebPage
	{
		@Serial
		private static final long serialVersionUID = 1L;

		public StatelessAutoRedirectPage()
		{
			final DummyHomePage page = new DummyHomePage();
			throw new RestartResponseException(
					new PageProvider(page.setStatelessHint(true)),
					RedirectPolicy.AUTO_REDIRECT
			);
		}
	}

	public static class StatefulAutoRedirectPage extends WebPage
	{
		@Serial
		private static final long serialVersionUID = 1L;

		public StatefulAutoRedirectPage()
		{
			final DummyHomePage page = new DummyHomePage();
			throw new RestartResponseException(
					new PageProvider(page.setStatelessHint(false)),
					RedirectPolicy.AUTO_REDIRECT
			);
		}
	}
}