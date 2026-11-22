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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Page-bound calls of an {@link Island}.
 */
class IslandCallTest extends IslandTestCase
{
	@BeforeEach
	void render()
	{
		tester.startPage(IslandTestPage.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	void callReturnsItsResultAndInvalidations()
	{
		Map<String, Object> answer = call("add", "7", "{\"amount\":5}");

		assertEquals("7", answer.get("call"));
		assertEquals(Boolean.TRUE, answer.get("ok"));
		assertEquals(5, ((Map<String, Object>)answer.get("value")).get("total"));
		assertEquals(List.of("totals"), answer.get("invalidate"));
	}

	@Test
	void componentsAddedByTheHandlerAreUpdated()
	{
		call("add", "1", "{\"amount\":2}");

		tester.assertComponentOnAjaxResponse("label");
		assertTrue(tester.getLastResponseAsString().contains("count 2"));
	}

	@Test
	void rejectedCallStillUpdatesComponents()
	{
		Map<String, Object> error = error(call("add", "1", "{\"amount\":-1}"));

		assertEquals("invalid", error.get("kind"));
		assertEquals(List.of("negative"), error.get("messages"));
		tester.assertComponentOnAjaxResponse("label");
	}

	@Test
	void callWithoutResult()
	{
		call("add", "1", "{\"amount\":3}");

		Map<String, Object> answer = call("reset", "2", null);

		assertEquals(Boolean.TRUE, answer.get("ok"));
		assertNull(answer.get("value"));
		assertEquals(0, ((IslandTestPage)tester.getLastRenderedPage()).island.getModelObject()
			.count());
	}

	@Test
	void unknownCallIsInvalid()
	{
		assertEquals("invalid", error(call("delete", "1", null)).get("kind"));
	}

	@Test
	void missingPayloadIsInvalid()
	{
		assertEquals("invalid", error(call("add", "1", null)).get("kind"));
	}

	@Test
	void malformedPayloadIsInvalid()
	{
		assertEquals("invalid", error(call("add", "1", "{\"amount\":")).get("kind"));
	}

	@Test
	void unknownPropertyIsInvalid()
	{
		assertEquals("invalid",
			error(call("add", "1", "{\"amount\":1,\"admin\":true}")).get("kind"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void failingHandlerIsAnErrorWithDetailInDevelopment()
	{
		Map<String, Object> error = error(call("boom", "1", null));

		assertEquals("error", error.get("kind"));
		assertTrue(((List<String>)error.get("messages")).get(0).contains("secret detail"));
	}

	@Test
	void resultThatCannotBeSerializedIsAnError()
	{
		assertEquals("error", error(call("unwritable", "1", null)).get("kind"));
	}
}
