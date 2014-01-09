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

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * RedirectRequestHandlerTest
 */
public class RedirectRequestHandlerTest
{

	private static final String REDIRECT_URL = "redirectUrl";

	/**
	 * permenanentlyMovedShouldSetLocationHeader()
	 */
	@Test
	public void permenanentlyMovedShouldSetLocationHeader()
	{
		RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL,
			HttpServletResponse.SC_MOVED_PERMANENTLY);

		IRequestCycle requestCycle = Mockito.mock(IRequestCycle.class);
		WebResponse webResponse = Mockito.mock(WebResponse.class);

		Mockito.when(requestCycle.getResponse()).thenReturn(webResponse);

		handler.respond(requestCycle);

		Mockito.verify(webResponse).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		Mockito.verify(webResponse).setHeader("Location", REDIRECT_URL);
	}

	/**
	 * tempMovedShouldRedirect()
	 */
	@Test
	public void tempMovedShouldRedirect()
	{
		RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL,
			HttpServletResponse.SC_MOVED_TEMPORARILY);

		IRequestCycle requestCycle = Mockito.mock(IRequestCycle.class);
		WebResponse webResponse = Mockito.mock(WebResponse.class);

		Mockito.when(requestCycle.getResponse()).thenReturn(webResponse);

		handler.respond(requestCycle);

		Mockito.verify(webResponse).sendRedirect(REDIRECT_URL);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5131
	 */
	@Test
	public void seeOtherShouldSetLocationHeader()
	{
		RedirectRequestHandler handler = new RedirectRequestHandler(REDIRECT_URL,
				HttpServletResponse.SC_SEE_OTHER);

		IRequestCycle requestCycle = Mockito.mock(IRequestCycle.class);
		WebResponse webResponse = Mockito.mock(WebResponse.class);

		Mockito.when(requestCycle.getResponse()).thenReturn(webResponse);

		handler.respond(requestCycle);

		Mockito.verify(webResponse).setStatus(HttpServletResponse.SC_SEE_OTHER);
		Mockito.verify(webResponse).setHeader("Location", REDIRECT_URL);
	}
}
