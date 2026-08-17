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
package org.apache.wicket.examples.source;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.examples.helloworld.HelloWorld;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SourcesPage}.
 */
public class SourcesPageTest extends WicketTestCase
{
	private String render(String source)
	{
		PageParameters parameters = SourcesPage.generatePageParameters(HelloWorld.class, source);
		tester.startPage(SourcesPage.class, parameters);
		return tester.getLastResponseAsString();
	}

	/**
	 * A resource of the package of the requested page is displayed.
	 */
	@Test
	public void sourceOfThePackageIsDisplayed()
	{
		assertFalse(render("HelloWorld.html").contains("Unable to read the source for"));
	}

	/**
	 * An absolute class path resource outside of the package of the requested page is refused.
	 */
	@Test
	public void absoluteClassPathResourceIsRefused()
	{
		String response = render("/META-INF/NOTICE");
		assertFalse(response.contains("Apache Software Foundation (http"));
		assertTrue(response.contains("Unable to read the source for"));
	}

	/**
	 * A parent directory reference is refused.
	 */
	@Test
	public void parentDirectoryResourceIsRefused()
	{
		String response = render("../style.css");
		assertTrue(response.contains("Unable to read the source for"));
	}
}
