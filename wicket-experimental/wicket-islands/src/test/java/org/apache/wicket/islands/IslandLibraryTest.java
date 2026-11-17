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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

/**
 * Islands from a library's prebuilt build next to the application's own.
 */
class IslandLibraryTest extends IslandTestCase
{
	private static final String LIBRARY = "org/apache/wicket/islands/testlibrary/";

	@Override
	protected void configureIslands(IslandSettings settings)
	{
		super.configureIslands(settings);
		settings.addLibrary("charts", LIBRARY, "manifest.json");
	}

	@Test
	void loadsLibraryTypesFromTheLibraryAndOtherTypesFromTheApplication()
	{
		tester.startPage(LibraryIslandPage.class);

		String response = tester.getLastResponseAsString();
		assertTrue(module("wicket-islands-library/charts/assets/charts-bar-111aaa.js")
			.matcher(response)
			.find(), response);
		assertTrue(response.contains("/wicket-islands-library/charts/assets/charts-bar-333ccc.css"),
			response);
		assertTrue(module("wicket-islands/assets/test-island-abc123.js").matcher(response).find(),
			response);
	}

	private static Pattern module(String file)
	{
		return Pattern.compile(
			"<script type=\"module\" src=\"[^\"]*/" + Pattern.quote(file) + "\"");
	}

	@Test
	void loadsLibraryTypesFromTheLibraryWhenTheApplicationUsesTheDevServer()
	{
		IslandSettings.get().setDevServerUrl("http://localhost:5173/");

		tester.startPage(LibraryIslandPage.class);

		String response = tester.getLastResponseAsString();
		assertTrue(module("wicket-islands-library/charts/assets/charts-bar-111aaa.js")
			.matcher(response)
			.find(), response);
		assertTrue(response.contains("http://localhost:5173/@wicket-islands/test-island"),
			response);
		assertFalse(response.contains("@wicket-islands/charts-bar"), response);
	}

	@Test
	void routesEveryEntryOfTheLibraryToIt()
	{
		IslandSettings settings = IslandSettings.get();

		assertSame(settings.getModuleLoader("charts-bar"), settings.getModuleLoader("charts-pie"));
		assertSame(settings.getModuleLoader(), settings.getModuleLoader("test-island"));
	}

	@Test
	void servesTheFilesOfTheLibrary()
	{
		tester.executeUrl("wicket-islands-library/charts/assets/vendor-222bbb.js");

		assertEquals(200, tester.getLastResponse().getStatus());
		assertEquals("export const vendor = 1;\n", tester.getLastResponseAsString());
	}

	@Test
	void servesOnlyTheFilesOfTheLibraryUnderItsPath()
	{
		tester.executeUrl("wicket-islands-library/charts/assets/test-island-abc123.js");

		assertEquals(404, tester.getLastResponse().getStatus());
	}

	@Test
	void refusesASecondLibraryWithTheSameName()
	{
		IslandSettings settings = IslandSettings.get();

		assertThrows(IllegalArgumentException.class,
			() -> settings.addLibrary("charts", "org/apache/wicket/islands/testbuild/",
				"manifest.json"));
	}

	@Test
	void refusesALibraryProvidingATypeOfAnotherLibrary()
	{
		IslandSettings settings = IslandSettings.get();

		IllegalStateException e = assertThrows(IllegalStateException.class,
			() -> settings.addLibrary("graphs", LIBRARY, "manifest.json"));

		assertTrue(e.getMessage().contains("'charts-bar'"), e.getMessage());
		settings.addLibrary("graphs", "org/apache/wicket/islands/testbuild/", "manifest.json");
	}

	@Test
	void refusesALibraryWithoutAManifest()
	{
		IslandSettings settings = IslandSettings.get();

		assertThrows(IllegalStateException.class,
			() -> settings.addLibrary("missing", "org/apache/wicket/islands/nowhere/"));
	}

	@Test
	void refusesAnInvalidName()
	{
		IslandSettings settings = IslandSettings.get();

		assertThrows(IllegalArgumentException.class,
			() -> settings.addLibrary("../charts", LIBRARY, "manifest.json"));
	}

	/**
	 * Keeps {@link WicketTester} from following an error to an error page.
	 */
	@Override
	protected WicketTester newWicketTester(org.apache.wicket.protocol.http.WebApplication app)
	{
		WicketTester tester = super.newWicketTester(app);
		tester.setExposeExceptions(false);
		return tester;
	}
}
