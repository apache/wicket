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

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * Loading island modules from a Vite dev server.
 */
class IslandDevServerTest extends IslandTestCase
{
	@Override
	protected void configureIslands(IslandSettings settings)
	{
		super.configureIslands(settings);
		settings.setDevServerUrl("http://localhost:5173/");
	}

	@Test
	void rendersModulesFromTheDevServer()
	{
		tester.startPage(IslandTestPage.class);

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains(
			"<script type=\"module\" src=\"http://localhost:5173/@vite/client\""), response);
		assertTrue(response.contains(
			"<script type=\"module\" src=\"http://localhost:5173/@wicket-islands/test-island\""),
			response);
		assertFalse(response.contains("test-island-abc123.js"), response);
	}

	@Test
	void rendersTheCspNonceForVite()
	{
		tester.getApplication().getCspSettings().blocking().strict();

		tester.startPage(IslandTestPage.class);

		String response = tester.getLastResponseAsString();
		assertTrue(Pattern.compile("<meta property=\"csp-nonce\" nonce=\"[^\"]+\"")
			.matcher(response)
			.find(), response);
	}
}
