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
package org.apache.wicket.examples.islands.twins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.islands.Island;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TwinIslandsPageTest
{
	private WicketTester tester;

	private TwinIslandsPage page;

	@BeforeEach
	void setUp()
	{
		tester = new WicketTester(new TwinIslandsApplication());
		page = tester.startPage(TwinIslandsPage.class);
	}

	@AfterEach
	void tearDown()
	{
		tester.destroy();
	}

	@Test
	void thePollIsRenderedByBothFrameworks()
	{
		assertEquals("poll-react", ((Island<?>)page.get("reactPoll")).getType());
		assertEquals("poll-svelte", ((Island<?>)page.get("sveltePoll")).getType());
		tester.assertLabel("summary", "No votes yet.");
	}

	@Test
	void aVoteInOneIslandUpdatesBothAndTheSummary()
	{
		call("reactPoll", "vote", "{\"id\":2}");

		tester.assertComponentOnAjaxResponse(page.get("reactPoll"));
		tester.assertComponentOnAjaxResponse(page.get("sveltePoll"));
		tester.assertComponentOnAjaxResponse(page.get("summary"));
		assertEquals("1 vote, Svelte leads.", page.get("summary").getDefaultModelObjectAsString());
	}

	@Test
	void anOptionAddedInOneIslandIsAnOptionOfBoth()
	{
		call("sveltePoll", "add", "{\"label\":\"  Vue  \"}");

		tester.assertComponentOnAjaxResponse(page.get("reactPoll"));
		assertEquals(List.of("React", "Svelte", "Plain Wicket", "Vue"), labels());
	}

	@Test
	void aDuplicateOptionIsRejected()
	{
		String response = call("reactPoll", "add", "{\"label\":\"svelte\"}");

		assertTrue(response.contains("\"kind\":\"invalid\""), response);
		assertTrue(response.contains("'svelte' is an option already."), response);
		assertEquals(3, labels().size());
	}

	@Test
	void aVoteForAnUnknownOptionIsRejected()
	{
		String response = call("sveltePoll", "vote", "{\"id\":99}");

		assertTrue(response.contains("This option no longer exists."), response);
		assertEquals(0, props().total());
	}

	@Test
	void resetTakesTheVotesBack()
	{
		call("reactPoll", "vote", "{\"id\":1}");
		call("sveltePoll", "vote", "{\"id\":1}");

		tester.clickLink("reset");

		tester.assertComponentOnAjaxResponse(page.get("reactPoll"));
		tester.assertComponentOnAjaxResponse(page.get("sveltePoll"));
		assertEquals(0, props().total());
		tester.assertLabel("summary", "No votes yet.");
	}

	private String call(String islandId, String name, String payload)
	{
		Island<?> island = (Island<?>)page.get(islandId);
		Url url = Url.parse(
			island.getBehaviors(AbstractAjaxBehavior.class).get(0).getCallbackUrl());
		tester.getRequest().setMethod("POST");
		tester.getRequest().getPostParameters().setParameterValue("island-call", name);
		tester.getRequest().getPostParameters().setParameterValue("island-call-id", "1");
		tester.getRequest().getPostParameters().setParameterValue("island-key", island.getKey());
		tester.getRequest().getPostParameters().setParameterValue("island-payload", payload);
		tester.executeAjaxUrl(url);
		return tester.getLastResponseAsString();
	}

	private PollIsland.PollProps props()
	{
		return (PollIsland.PollProps)page.get("sveltePoll").getDefaultModelObject();
	}

	private List<String> labels()
	{
		return props().options().stream().map(PollIsland.OptionProps::label).toList();
	}
}
