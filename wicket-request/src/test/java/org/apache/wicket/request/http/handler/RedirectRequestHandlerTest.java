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

import static jakarta.servlet.http.HttpServletResponse.SC_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;
import static jakarta.servlet.http.HttpServletResponse.SC_SEE_OTHER;
import static jakarta.servlet.http.HttpServletResponse.SC_TEMPORARY_REDIRECT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * RedirectRequestHandlerTest
 *
 * @see <a href="https://issues.apache.org/jira/browse/WICKET-5131">WICKET-5131: Support for 303</a>
 * @see <a href="https://issues.apache.org/jira/browse/WICKET-6764">WICKET-6c764: RedirectToUrlException change the second question mark in URL from "?" to "%3F"</a>
 */
class RedirectRequestHandlerTest
{
	private static final String REDIRECT_URL = "redirectUrl";
	private static final int SC_PERMANENT_REDIRECT = 308; // Replace with static import of HttpServletResponse.SC_PERMANENT_REDIRECT, requires Jakarta servlet-api 6.1

	private final IRequestCycle requestCycle = mock(IRequestCycle.class);
	private final WebResponse webResponse = mock(WebResponse.class);
	private final WebRequest webRequest = mock(WebRequest.class);

	@BeforeEach
	void before() {
		when(requestCycle.getResponse()).thenReturn(webResponse);
		when(requestCycle.getRequest()).thenReturn(webRequest);
	}

	@ParameterizedTest
	@ValueSource(ints = { SC_MOVED_PERMANENTLY, SC_FOUND, SC_SEE_OTHER, SC_TEMPORARY_REDIRECT, SC_PERMANENT_REDIRECT })
	void modeRedirectAlwaysSendsRedirect(int status)
	{
		final RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL, status).mode(RedirectRequestHandler.Mode.REDIRECT);
		handler.respond(requestCycle);

		verify(webResponse).sendRedirect(REDIRECT_URL);
	}

	@ParameterizedTest
	@ValueSource(ints = { SC_MOVED_PERMANENTLY, SC_FOUND, SC_SEE_OTHER, SC_TEMPORARY_REDIRECT, SC_PERMANENT_REDIRECT })
	void modeStatusSetsLocationHeaderAndStatus(int status)
	{
		when(webRequest.isAjax()).thenReturn(false);
		final RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL, status).mode(RedirectRequestHandler.Mode.STATUS);
		handler.respond(requestCycle);

		verify(webResponse).setStatus(status);
		verify(webResponse).setHeader("Location", REDIRECT_URL);
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-6638">WICKET-6638: Ajax-Support for RedirectRequestHandler</a>
	 */
	@ParameterizedTest
	@ValueSource(ints = { SC_MOVED_PERMANENTLY, SC_FOUND, SC_SEE_OTHER, SC_TEMPORARY_REDIRECT, SC_PERMANENT_REDIRECT })
	void modeStatusSetsAjaxLocationHeaderAndStatusDuringAjaxRequest(int status)
	{
		when(webRequest.isAjax()).thenReturn(true);
		final RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL, status).mode(RedirectRequestHandler.Mode.STATUS);
		handler.respond(requestCycle);

		verify(webResponse).setStatus(status);
		verify(webResponse).setHeader("Ajax-Location", REDIRECT_URL);
	}

	@ParameterizedTest
	@ValueSource(ints = {  SC_MOVED_PERMANENTLY, SC_SEE_OTHER, SC_TEMPORARY_REDIRECT, SC_PERMANENT_REDIRECT })
	void modeAutoSetsLocationHeaderAndStatusExceptFor302(int status)
	{
		when(webRequest.isAjax()).thenReturn(false);
		final RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL, status).mode(RedirectRequestHandler.Mode.AUTO);
		handler.respond(requestCycle);

		verify(webResponse).setStatus(status);
		verify(webResponse).setHeader("Location", REDIRECT_URL);
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-6638">WICKET-6638: Ajax-Support for RedirectRequestHandler</a>
	 */
	@ParameterizedTest
	@ValueSource(ints = {  SC_MOVED_PERMANENTLY, SC_SEE_OTHER, SC_TEMPORARY_REDIRECT, SC_PERMANENT_REDIRECT })
	void modeAutoSetsAjaxLocationHeaderAndStatusExceptFor302DuringAjaxRequest(int status)
	{
		when(webRequest.isAjax()).thenReturn(true);
		final RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL, status).mode(RedirectRequestHandler.Mode.AUTO);
		handler.respond(requestCycle);

		verify(webResponse).setStatus(status);
		verify(webResponse).setHeader("Ajax-Location", REDIRECT_URL);
	}

	@Test
	void modeAutoSendsRedirectFor302()
	{
		final RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL, SC_FOUND).mode(RedirectRequestHandler.Mode.AUTO);
		handler.respond(requestCycle);

		verify(webResponse).sendRedirect(REDIRECT_URL);
	}
}
