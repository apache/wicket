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
package org.apache.wicket.islands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebResponse;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

/**
 * The URLs of island files on the first page of a session, before the browser sends the session
 * cookie and the container encodes the session id into URLs.
 */
class IslandSessionIdTest extends IslandTestCase
{
	private static final String SESSION_ID = ";jsessionid=first-page";

	private static final Pattern ISLAND_FILE_WITH_SESSION_ID = Pattern.compile(
		"wicket-islands(-library/[^/\"]+)?/assets/[^\"]*" + Pattern.quote(SESSION_ID));

	@Override
	protected void configureIslands(IslandSettings settings)
	{
		super.configureIslands(settings);
		settings.addLibrary("charts", "org/apache/wicket/islands/testlibrary/", "manifest.json");
	}

	@Override
	protected WicketTester newWicketTester(WebApplication app)
	{
		return new WicketTester(app)
		{
			@Override
			protected Response newServletWebResponse(ServletWebRequest servletWebRequest)
			{
				return new ServletWebResponse(servletWebRequest, getResponse())
				{
					@Override
					public String encodeURL(CharSequence url)
					{
						return super.encodeURL(url) + SESSION_ID;
					}

					@Override
					public void sendRedirect(String url)
					{
						super.sendRedirect(url);
						try
						{
							getContainerResponse().sendRedirect(url);
						}
						catch (IOException e)
						{
							throw new RuntimeException(e);
						}
					}
				};
			}
		};
	}

	@Test
	void rendersTheFilesOfIslandsWithoutTheSessionId()
	{
		tester.startPage(LibraryIslandPage.class);

		assertTrue(tester.getRequestCycle()
			.urlFor(LibraryIslandPage.class, null)
			.toString()
			.endsWith(SESSION_ID), "the container encodes the session id into page URLs");
		String response = tester.getLastResponseAsString();
		assertFalse(ISLAND_FILE_WITH_SESSION_ID.matcher(response).find(), response);
	}
}
