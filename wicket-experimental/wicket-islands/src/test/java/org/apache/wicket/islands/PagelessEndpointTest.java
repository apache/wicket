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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.islands.IslandTestPage.Add;
import org.apache.wicket.islands.IslandTestPage.Sum;
import org.apache.wicket.mock.MockWebResponse;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.junit.jupiter.api.Test;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

/**
 * Page-less endpoints.
 */
class PagelessEndpointTest extends IslandTestCase
{
	@Override
	protected void configureIslands(IslandSettings settings)
	{
		super.configureIslands(settings);
		settings.getPagelessEndpoints()
			.register("echo", Add.class, (add, call) -> {
				if (add.amount() < 0)
				{
					throw call.reject("negative");
				}
				call.invalidate("totals");
				return new Sum(add.amount() * 2);
			}, IPagelessAuthorization.PERMIT_ALL)
			.register("secret", Void.class, (none, call) -> "secret", call -> false)
			.register("boom", Void.class, (none, call) -> {
				throw new IllegalStateException("broken");
			}, IPagelessAuthorization.PERMIT_ALL);
	}

	@Test
	@SuppressWarnings("unchecked")
	void answersWithTheResultAndInvalidations()
	{
		MockWebResponse response = post("echo", "{\"amount\":21}", "same-origin");

		Map<String, Object> answer = JSON.readValue(response.getBinaryResponse(), Map.class);
		assertEquals(Boolean.TRUE, answer.get("ok"));
		assertEquals(Map.of("total", 42), answer.get("value"));
		assertEquals(List.of("totals"), answer.get("invalidate"));
		assertTrue(response.getContentType().startsWith("application/json"));
	}

	@Test
	void rejectedCallIsInvalid()
	{
		assertEquals("invalid", error(answer(post("echo", "{\"amount\":-1}", null))).get("kind"));
	}

	@Test
	void malformedPayloadIsInvalid()
	{
		assertEquals("invalid", error(answer(post("echo", "{\"amount\":", null))).get("kind"));
	}

	@Test
	void deniedCallIsForbidden()
	{
		assertEquals("forbidden", error(answer(post("secret", "", null))).get("kind"));
	}

	@Test
	void failingEndpointIsAnError()
	{
		assertEquals("error", error(answer(post("boom", "", null))).get("kind"));
	}

	@Test
	void unknownEndpointIsNotFound()
	{
		assertEquals(404, post("nothing", "", null).getStatus());
	}

	@Test
	void crossSiteRequestIsRefused()
	{
		assertEquals(403, post("echo", "{\"amount\":1}", "cross-site").getStatus());
		assertEquals(403, post("echo", "{\"amount\":1}", "same-site").getStatus());
	}

	@Test
	void requestWithoutTheHeaderIsRefused()
	{
		assertEquals(403, send("echo", "POST", "{\"amount\":1}", null, false).getStatus());
	}

	@Test
	void getIsRefused()
	{
		assertEquals(405, send("echo", "GET", "", null, true).getStatus());
	}

	@Test
	@SuppressWarnings("unchecked")
	void islandStateHoldsTheUrlsOfItsPagelessCalls()
	{
		tester.startPage(IslandTestPage.class);

		Matcher matcher = Pattern.compile(
			"data-wicket-island-state>(.*?)</script>", Pattern.DOTALL)
			.matcher(tester.getLastResponseAsString());
		assertTrue(matcher.find());
		Map<String, String> pageless = (Map<String, String>)JSON
			.readValue(matcher.group(1), Map.class)
			.get("pageless");
		assertEquals(List.of("echo", "sum"), List.copyOf(pageless.keySet()));
		assertTrue(pageless.get("echo").endsWith("wicket-islands-call/echo"), pageless.toString());
		assertEquals(pageless.get("echo"), pageless.get("sum"));
	}

	@Test
	void classicAjaxRequestSendsInvalidations()
	{
		tester.startPage(IslandTestPage.class);

		tester.clickLink("invalidate");

		assertTrue(tester.getLastResponseAsString()
			.contains("<data name=\"wicket-islands\"><![CDATA[{\"invalidate\":[\"totals\",\"scores\"]}]]></data>"),
			tester.getLastResponseAsString());
	}

	private MockWebResponse post(String id, String body, String fetchSite)
	{
		return send(id, "POST", body, fetchSite, true);
	}

	private MockWebResponse send(String id, String method, String body, String fetchSite,
		boolean header)
	{
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		MockHttpServletRequest http = new MockHttpServletRequest(tester.getApplication(),
			tester.getHttpSession(), tester.getServletContext())
		{
			@Override
			public ServletInputStream getInputStream()
			{
				ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				return new ServletInputStream()
				{
					@Override
					public int read()
					{
						return in.read();
					}

					@Override
					public boolean isFinished()
					{
						return in.available() == 0;
					}

					@Override
					public boolean isReady()
					{
						return true;
					}

					@Override
					public void setReadListener(ReadListener listener)
					{
					}
				};
			}
		};
		http.setMethod(method);
		if (header)
		{
			http.addHeader(PagelessEndpointResource.HEADER, "1");
		}
		if (fetchSite != null)
		{
			http.addHeader("Sec-Fetch-Site", fetchSite);
		}
		MockWebResponse response = new MockWebResponse();
		new PagelessEndpointResource().respond(new IResource.Attributes(
			new ServletWebRequest(http, ""), response, new PageParameters().set(0, id)));
		return response;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> answer(MockWebResponse response)
	{
		assertTrue(response.getStatus() == null || response.getStatus() == 200,
			"status " + response.getStatus());
		return JSON.readValue(response.getBinaryResponse(), Map.class);
	}
}
