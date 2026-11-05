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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.TagTester;
import org.junit.jupiter.api.Test;

/**
 * Rendering of an {@link Island}: its host, its state and its module.
 */
class IslandRenderTest extends IslandTestCase
{
	private static final Pattern STATE = Pattern.compile(
		"<script type=\"application/json\" data-wicket-island-state>(.*?)</script>", Pattern.DOTALL);

	@Test
	void rendersHostWithTypeAndKey()
	{
		IslandTestPage page = tester.startPage(IslandTestPage.class);

		TagTester host = tester.getTagByWicketId("island");
		assertEquals("test-island", host.getAttribute(Island.ATTR_TYPE));
		assertEquals(page.island.getKey(), host.getAttribute(Island.ATTR_KEY));
		assertEquals(page.island.getMarkupId(), host.getAttribute("id"));
		assertTrue(tester.getLastResponseAsString().contains("<div " + Island.ATTR_ROOT + "></div>"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void stateHoldsPropsCallsAndAjaxAttributes()
	{
		tester.startPage(IslandTestPage.class);

		Map<String, Object> state = state();
		Map<String, Object> props = (Map<String, Object>)state.get("props");
		assertEquals(IslandTestPage.TRICKY_NAME, props.get("name"));
		assertEquals(0, props.get("count"));
		assertEquals(List.of("add", "reset", "boom"), state.get("calls"));
		Map<String, Object> ajax = (Map<String, Object>)state.get("ajax");
		assertNotNull(ajax.get("u"));
		assertEquals("POST", ajax.get("m"));
	}

	@Test
	void stateIsEscapedForTheScriptElement()
	{
		tester.startPage(IslandTestPage.class);

		String json = stateJson();
		assertFalse(json.contains("<"), json);
		assertFalse(json.contains(">"), json);
		assertFalse(json.contains("&"), json);
		assertFalse(json.contains("\u2028"), json);
		assertFalse(json.contains("\u2029"), json);
	}

	@Test
	void keyIsStableAcrossRenders()
	{
		IslandTestPage page = tester.startPage(IslandTestPage.class);
		String key = tester.getTagByWicketId("island").getAttribute(Island.ATTR_KEY);

		tester.startPage(page);

		assertEquals(key, tester.getTagByWicketId("island").getAttribute(Island.ATTR_KEY));
	}

	@Test
	void keysDifferBetweenInstances()
	{
		Island<String> one = new Island<>("one", "test-island", Model.of(""));
		Island<String> two = new Island<>("two", "test-island", Model.of(""));

		assertFalse(one.getKey().equals(two.getKey()));
	}

	@Test
	void rejectsInvalidType()
	{
		assertThrows(IllegalArgumentException.class,
			() -> new Island<>("island", "a\"b", Model.of("")));
	}

	@Test
	void rendersEntryModuleAndStyleSheetsFromTheManifest()
	{
		tester.startPage(IslandTestPage.class);

		String response = tester.getLastResponseAsString();
		assertTrue(Pattern.compile(
			"<script type=\"module\" src=\"[^\"]*wicket-islands/assets/test-island-abc123\\.js\"")
			.matcher(response)
			.find(), response);
		int shared = response.indexOf("wicket-islands/assets/shared-aaa111.css");
		int own = response.indexOf("wicket-islands/assets/test-island-def456.css");
		assertTrue(shared > 0 && own > shared, "imported style sheets come first: " + response);
	}

	@Test
	void renderedModuleUrlServesTheModule()
	{
		tester.startPage(IslandTestPage.class);
		Url pageUrl = tester.getLastRequest().getUrl();
		Matcher src = Pattern.compile("<script type=\"module\" src=\"([^\"]+)\"")
			.matcher(tester.getLastResponseAsString());
		assertTrue(src.find());

		pageUrl.resolveRelative(Url.parse(src.group(1)));
		tester.executeUrl(pageUrl.toString());

		assertEquals(200, tester.getLastResponse().getStatus());
		assertEquals("export default {};\n", tester.getLastResponseAsString());
	}

	@Test
	void entryMissingFromTheManifestFails()
	{
		RuntimeException e = assertThrows(RuntimeException.class, () -> tester.startComponentInPage(
			new Island<>("island", "not-built", Model.of(""))));

		Throwable cause = e;
		while (cause.getCause() != null)
		{
			cause = cause.getCause();
		}
		assertTrue(cause.getMessage().contains("no entry for island type 'not-built'"),
			cause.getMessage());
	}

	private String stateJson()
	{
		Matcher matcher = STATE.matcher(tester.getLastResponseAsString());
		assertTrue(matcher.find(), tester.getLastResponseAsString());
		return matcher.group(1);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> state()
	{
		return JSON.readValue(stateJson(), Map.class);
	}
}
