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
package org.apache.wicket.request.http.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * RedirectRequestHandlerTest
 */
class RedirectRequestHandlerTest
{
	private static final String REDIRECT_URL = "redirectUrl";

	private final IRequestCycle requestCycle = mock(IRequestCycle.class);
	private final WebResponse webResponse = mock(WebResponse.class);
	private final WebRequest webRequest = mock(WebRequest.class);

	@BeforeEach
	void before() {
		when(requestCycle.getResponse()).thenReturn(webResponse);
		when(requestCycle.getRequest()).thenReturn(webRequest);
	}

	@Test
	void permanentlyMovedShouldSetLocationHeader()
	{
		RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL,
			HttpServletResponse.SC_MOVED_PERMANENTLY);

		handler.respond(requestCycle);

		verify(webResponse).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		verify(webResponse).setHeader("Location", REDIRECT_URL);
	}

	/**
	 * tempMovedShouldRedirect()
	 */
	@Test
	void tempMovedShouldRedirect()
	{
		RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL,
			HttpServletResponse.SC_MOVED_TEMPORARILY);

		IRequestCycle requestCycle = mock(IRequestCycle.class);
		WebResponse webResponse = mock(WebResponse.class);

		when(requestCycle.getResponse()).thenReturn(webResponse);

		handler.respond(requestCycle);

		verify(webResponse).sendRedirect(REDIRECT_URL);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5131
	 */
	@Test
	void seeOtherShouldSetLocationHeader()
	{
		RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL,
			HttpServletResponse.SC_SEE_OTHER);

		handler.respond(requestCycle);

		verify(webResponse).setStatus(HttpServletResponse.SC_SEE_OTHER);
		verify(webResponse).setHeader("Location", REDIRECT_URL);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-6638
	 */
	@Test
	public void seeOtherShouldSetAjaxLocationHeaderForAjaxRequests()
	{
		RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL,
				HttpServletResponse.SC_SEE_OTHER);

		when(webRequest.isAjax()).thenReturn(true);

		handler.respond(requestCycle);

		verify(webResponse).setStatus(HttpServletResponse.SC_SEE_OTHER);
		verify(webResponse).setHeader("Ajax-Location", REDIRECT_URL);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-6764
	 */
	@Test
	void movedPermanentlyAndModeRedirect_shouldSendRedirect()
	{
		RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL,
		                                                            HttpServletResponse.SC_MOVED_PERMANENTLY);
		handler.mode(RedirectRequestHandler.Mode.REDIRECT);

		handler.respond(requestCycle);

		verify(webResponse).sendRedirect(REDIRECT_URL);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-6764
	 */
	@Test
	void movedTemporarilyAndModeStatus_shouldSetLocation()
	{
		RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL,
		                                                            HttpServletResponse.SC_MOVED_TEMPORARILY);
		handler.mode(RedirectRequestHandler.Mode.STATUS);

		handler.respond(requestCycle);

		verify(webResponse).setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		verify(webResponse).setHeader("Location", REDIRECT_URL);
	}
}
