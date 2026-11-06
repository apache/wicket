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
package org.apache.wicket.examples.islands.treasurehunt;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.islands.Island;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TreasureHuntPageTest
{
	private WicketTester tester;

	@BeforeEach
	void setUp()
	{
		tester = new WicketTester(new TreasureHuntApplication());
	}

	@AfterEach
	void tearDown()
	{
		tester.destroy();
	}

	@Test
	void probeOnAStalePageIsExpired()
	{
		TreasureHuntPage page = tester.startPage(TreasureHuntPage.class);
		Island<?> board = (Island<?>)page.get("game:board");
		Url url = Url.parse(board.getBehaviors(AbstractAjaxBehavior.class).get(0).getCallbackUrl());
		tester.startPage(page);

		tester.getRequest().setMethod("POST");
		tester.getRequest().getPostParameters().setParameterValue("island-call", "probe");
		tester.getRequest().getPostParameters().setParameterValue("island-call-id", "1");
		tester.getRequest().getPostParameters().setParameterValue("island-key", board.getKey());
		tester.getRequest().getPostParameters().setParameterValue("island-payload", "{\"x\":1,\"y\":1}");
		tester.executeAjaxUrl(url);

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("\"kind\":\"expired\""), response);
	}
}
