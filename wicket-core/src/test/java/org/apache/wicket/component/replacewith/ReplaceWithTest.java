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

import org.apache.wicket.WicketTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for Component#replaceWith() method
 */
public class ReplaceWithTest extends WicketTestCase
{
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5417
	 */
	@Test
	public void replaceWithInOnInitialize()
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
	public void replaceWithInOnBeforeRender()
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
	public void replaceWithInOnConfigure()
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
	public void replaceWithInConstructor()
	{
		HomePage page = new HomePage();

		expectedException.expect(IllegalStateException.class);
		expectedException.
		    expectMessage("This method can only be called on a component that has already been added to its parent.");

		page.add(new ReplaceInConstructorPanel("panel"));
	}
}
