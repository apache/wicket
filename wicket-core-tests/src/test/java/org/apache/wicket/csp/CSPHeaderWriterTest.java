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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.wicket.csp.CSPDirective.STYLE_SRC;
import static org.apache.wicket.csp.CSPDirectiveSrcValue.SELF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CSPHeaderWriterTest extends WicketTestCase
{

	@BeforeEach
	void setup()
	{
		tester.getApplication().getCspSettings().blocking().strict().add(STYLE_SRC, SELF);
	}

	@Test
	void addCspDirectiveToBufferedPage()
	{
		tester.startPage(Page.class);
		tester.clickLink("link_to_page_instance");

		assertThat(tester.getLastResponse().getHeader("Content-Security-Policy")).contains(
			STYLE_SRC.getValue());
	}

	@Test
	void dontAddCSPHeaderToRedirectResponses()
	{
		tester.setFollowRedirects(false);
		tester.getApplication().getCspSettings().blocking().add(STYLE_SRC, SELF);
		tester.startPage(Page.class);

		var requestCycle = tester.getRequestCycle();

		tester.clickLink("link_to_page_instance");

		var response = ((MockHttpServletResponse)requestCycle.getResponse().getContainerResponse());
		assertEquals(302, response.getStatus());
		assertFalse(response.containsHeader(CSPHeaderMode.BLOCKING.getHeader()));
	}

	@Test
	void addCspDirectiveToBufferedPageAfterRedirect()
	{
		tester.startPage(AutoRedirectPage.class);

		assertThat(tester.getLastRenderedPage()).isInstanceOf(Page.class);
		assertThat(tester.getLastResponse().getHeader("Content-Security-Policy")).contains(
			STYLE_SRC.getValue());
	}

	@Test
	void addCspDirectiveToStatelessPageAfterRedirect()
	{
		tester.startPage(AlwaysRedirectPage.class);

		assertThat(tester.getLastRenderedPage()).isInstanceOf(Page.class);
		assertThat(tester.getLastResponse().getHeader("Content-Security-Policy")).contains(
			STYLE_SRC.getValue());
	}

	@Test
	void addCspDirectiveToStatelessPageAfterNoRedirect()
	{
		tester.startPage(NeverRedirectPage.class);

		assertThat(tester.getLastRenderedPage()).isInstanceOf(Page.class);
		assertThat(tester.getLastResponse().getHeader("Content-Security-Policy")).contains(
			STYLE_SRC.getValue());
	}

	public static class Page extends WebPage implements IMarkupResourceStreamProvider
	{
		@Override
		protected void onInitialize()
		{
			super.onInitialize();
			add(new StatelessLink<Void>("link_to_page_instance")
			{
				@Override
				public void onClick()
				{
					setResponsePage(new Page());
				}
			});
		}

		@Override
		public void renderHead(IHeaderResponse response)
		{
			response.render(CssHeaderItem.forReference(
				new CssResourceReference(CSPHeaderWriterTest.class, "style.css"), "screen"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><head></head><body><a wicket:id=\"link_to_page_instance\">link</a></body></html>");
		}
	}

	public static class AutoRedirectPage extends Page
	{
		public AutoRedirectPage()
		{
			throw new RestartResponseException(new Page());
		}
	}

	public static class AlwaysRedirectPage extends Page
	{
		public AlwaysRedirectPage()
		{
			throw new RestartResponseException(Page.class, new PageParameters());
		}
	}

	public static class NeverRedirectPage extends Page
	{
		public NeverRedirectPage()
		{
			throw new RestartResponseException(new PageProvider(Page.class),
				RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT);
		}
	}

}
