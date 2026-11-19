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
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;

import org.apache.wicket.request.Url;
import org.junit.jupiter.api.Test;

/**
 * Island calls that cannot reach their island are answered, not redirected.
 */
class IslandCallFailureTest extends IslandTestCase
{
	@Test
	void callWithAnotherKeyIsExpired()
	{
		IslandTestPage page = tester.startPage(IslandTestPage.class);

		prepareCall("add", "1", "{\"amount\":1}", "not-the-key");
		tester.executeBehavior(page.island.getBehaviors(IslandEndpointBehavior.class).get(0));

		assertEquals("expired", error(answer()).get("kind"));
		assertEquals("count 0", page.label.getDefaultModelObjectAsString());
	}

	@Test
	void callOnARecreatedPageIsExpired()
	{
		IslandTestPage page = tester.startPage(IslandTestPage.class);
		Url url = callbackUrl(page);
		String key = page.island.getKey();
		tester.getSession().getPageManager().clear();

		prepareCall("add", "1", "{\"amount\":1}", key);
		tester.executeAjaxUrl(url);

		assertEquals("expired", error(answer()).get("kind"));
	}

	@Test
	void callOnAnExpiredPageIsExpired()
	{
		IslandTestPage page = tester.startPage(new NonBookmarkableIslandPage("x"));
		Url url = callbackUrl(page);
		String key = page.island.getKey();
		tester.getSession().getPageManager().clear();

		prepareCall("add", "1", "{\"amount\":1}", key);
		tester.executeAjaxUrl(url);

		assertEquals("expired", error(answer()).get("kind"));
	}

	@Test
	void callOnAStalePageIsExpired()
	{
		IslandTestPage page = tester.startPage(IslandTestPage.class);
		Url url = callbackUrl(page);
		tester.startPage(page);

		prepareCall("add", "1", "{\"amount\":1}", page.island.getKey());
		tester.executeAjaxUrl(url);

		assertEquals("expired", error(answer()).get("kind"));
	}

	@Test
	void callOnAHiddenIslandIsForbidden()
	{
		IslandTestPage page = tester.startPage(IslandTestPage.class);
		Url url = callbackUrl(page);
		page.island.setVisible(false);

		prepareCall("add", "1", "{\"amount\":1}", page.island.getKey());
		tester.executeAjaxUrl(url);

		Map<String, Object> error = error(answer());
		assertEquals("forbidden", error.get("kind"));
		assertFalse(tester.getLastResponseAsString().contains("count 1"));
	}

	private static Url callbackUrl(IslandTestPage page)
	{
		return Url.parse(
			page.island.getBehaviors(IslandEndpointBehavior.class).get(0).getCallbackUrl());
	}
}
