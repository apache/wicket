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
package org.apache.wicket.examples.ajax.builtin;

import org.apache.wicket.examples.AjaxEngineSelector;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests that the ajax examples are listed and titled from the same resource.
 */
class IndexTest extends WicketTestCase
{
	@Test
	void listsExamplesWithTheirTitle()
	{
		tester.startPage(Index.class);

		tester.assertRenderedPage(Index.class);
		tester.assertLabel("exampleTitle", "Ajax Examples");
		tester.assertContains(">Wicket Examples - Ajax Examples</title>");
		tester.assertContains("Drop Down Choice Example");
		tester.assertContains("demonstrates the linked select boxes usecase");
	}

	@Test
	void examplePageShowsTheTitleItIsListedWith()
	{
		tester.startPage(ClockPage.class);

		tester.assertLabel("exampleTitle", "Clock Example");
		tester.assertContains(">Wicket Examples - Clock Example</title>");
	}

	@Test
	void hidesEffectsExampleWithoutJQuery()
	{
		tester.startPage(Index.class);
		tester.assertContains("Effects Example");

		AjaxEngineSelector.setSessionEngine(AjaxEngineSelector.Engine.VANILLA);
		tester.startPage(tester.getLastRenderedPage());

		tester.assertContainsNot("Effects Example");
	}
}
