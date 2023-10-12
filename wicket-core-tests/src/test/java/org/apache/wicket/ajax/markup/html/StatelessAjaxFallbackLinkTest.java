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
package org.apache.wicket.ajax.markup.html;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.StatelessPage;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatelessAjaxFallbackLinkTest
{
	private WicketTester tester;

	@BeforeEach
	void setUp()
	{
		tester = new WicketTester(new MockApplication());
	}

	@AfterEach
	void teardown()
	{
		// things must stay stateless
		assertTrue(Session.get().isTemporary());
	}

	@Test
	@SuppressWarnings("unchecked")
	void testGetStatelessHint()
	{
		tester.startPage(StatelessPage.class);

		final StatelessPage page = (StatelessPage)tester.getLastRenderedPage();
		final AjaxFallbackLink<Void> link = (AjaxFallbackLink<Void>)page.get("more");

		assertTrue(link.isStateless());

		link.onClick();

		final List<? extends Behavior> behaviors = link.getBehaviors();
		final AjaxEventBehavior behavior = (AjaxEventBehavior)behaviors.get(0);

		behavior.onRequest();

		assertTrue(link.isStateless());
	}
}
