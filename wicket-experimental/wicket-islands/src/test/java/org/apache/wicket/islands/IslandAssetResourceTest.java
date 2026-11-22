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

import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

/**
 * Serving the files of the build.
 */
class IslandAssetResourceTest extends IslandTestCase
{
	@Test
	void servesFilesListedInTheManifest()
	{
		tester.executeUrl("wicket-islands/assets/test-island-abc123.js");

		assertEquals(200, tester.getLastResponse().getStatus());
		assertTrue(tester.getLastResponse().getContentType().startsWith("text/javascript"));
		assertEquals("export default {};\n", tester.getLastResponseAsString());
	}

	@Test
	void refusesFilesNotListedInTheManifest()
	{
		tester.executeUrl("wicket-islands/manifest.json");

		assertEquals(404, tester.getLastResponse().getStatus());
	}

	@Test
	void refusesClasspathEntriesOutsideTheBuild()
	{
		tester.executeUrl("wicket-islands/..%2FIslandTestPage.html");

		assertEquals(404, tester.getLastResponse().getStatus());
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
