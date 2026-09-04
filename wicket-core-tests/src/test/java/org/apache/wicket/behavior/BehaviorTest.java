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
package org.apache.wicket.behavior;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.wicket.Component;
import org.apache.wicket.MockPageWithOneComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Behavior}
 */
class BehaviorTest extends WicketTestCase
{
	@Test
	void onTagTest()
	{
		WebMarkupContainer component = new WebMarkupContainer(
			MockPageWithOneComponent.COMPONENT_ID);
		MockPageWithOneComponent page = new MockPageWithOneComponent();
		page.add(component);
		String value = "value";
		String key = "key";
		component.add(Behavior.onTag((c, tag) -> tag.put(key, value)));
		component.add(Behavior.onAttribute("class", oldValue -> "zzz"));

		tester.startPage(page);

		TagTester tagTester = tester.getTagByWicketId(MockPageWithOneComponent.COMPONENT_ID);
		assertEquals(value, tagTester.getAttribute(key));
		assertEquals("zzz", tagTester.getAttribute("class"));
	}

	@Test
	void onAttributeTest()
	{
		WebMarkupContainer component = new WebMarkupContainer(
			MockPageWithOneComponent.COMPONENT_ID);
		MockPageWithOneComponent page = new MockPageWithOneComponent();
		page.add(component);
		String value = "value";
		String key = "key";
		component.add(Behavior.onAttribute(key, oldValue -> value));

		tester.startPage(page);

		TagTester tagTester = tester.getTagByWicketId(MockPageWithOneComponent.COMPONENT_ID);
		assertEquals(value, tagTester.getAttribute(key));
	}

	@Test
	public void temporaryBehaviorsAreRemoved() {
		WebMarkupContainer container = new WebMarkupContainer("test");
		TestTemporaryBehavior temp = new TestTemporaryBehavior();
		container.add(temp);
		assertTrue(container.getBehaviors().contains(temp));
		container.detach();
		assertFalse(container.getBehaviors().contains(temp));
	}

	@Test
	public void consecutiveTemporaryBehaviorsAreRemoved() {
		WebMarkupContainer container = new WebMarkupContainer("test");
		TestTemporaryBehavior temp1 = new TestTemporaryBehavior();
		TestTemporaryBehavior temp2 = new TestTemporaryBehavior();
		container.add(temp1, temp2);
		assertTrue(container.getBehaviors().contains(temp1));
		assertTrue(container.getBehaviors().contains(temp2));
		container.detach();
		assertFalse(container.getBehaviors().contains(temp1));
		assertFalse(container.getBehaviors().contains(temp2));
	}

	public static class TestTemporaryBehavior extends Behavior {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isTemporary(Component c) {
			return true;
		}
	}

}
