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
package org.apache.wicket.component.replacewith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests for Component#replaceWith() method
 */
class ReplaceWithTest extends WicketTestCase
{

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5417
	 */
	@Test
	void replaceWithInOnInitialize()
	{
		HomePage page = new HomePage();
		page.add(new ReplaceInOnInitializePanel("panel"));

		tester.startPage(page);
		tester.assertRenderedPage(HomePage.class);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5417
	 */
	@Test
	void replaceWithInOnBeforeRender()
	{
		HomePage page = new HomePage();
		page.add(new ReplaceInOnBeforeRenderPanel("panel"));

		tester.startPage(page);
		tester.assertRenderedPage(HomePage.class);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5417
	 */
	@Test
	void replaceWithInOnConfigure()
	{
		HomePage page = new HomePage();
		page.add(new ReplaceInOnConfigurePanel("panel"));

		tester.startPage(page);
		tester.assertRenderedPage(HomePage.class);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5417
	 */
	@Test
	void replaceWithInConstructor()
	{
		HomePage page = new HomePage();

		Exception e = assertThrows(IllegalStateException.class, () -> {
			page.add(new ReplaceInConstructorPanel("panel"));
		});

		assertEquals(
			"This method can only be called on a component that has already been added to its parent.",
			e.getMessage());
	}
}
