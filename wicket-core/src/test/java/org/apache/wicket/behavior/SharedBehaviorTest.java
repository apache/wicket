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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * @since 1.5.8
 */
public class SharedBehaviorTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-4570
	 *
	 * A Behavior can be shared between components unless the Behavior itself
	 * deny this by overriding its #bind(Component).
	 *
	 * This test verifies that behavior's #renderHead(Component, IHeaderResponse) is called
	 * for all its bindings
	 *
	 * @throws Exception
	 */
	@Test
	public void sharedBehaviorRenderHead() throws Exception
	{
		TestPage page = new TestPage();
		executeTest(page, "SharedBehaviorTest_renderHead_expected.html");
	}

	/**
	 * A test page for #sharedBehaviorRenderHead()
	 */
	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private TestPage()
		{
			SharedBehavior behavior = new SharedBehavior();
			WebComponent component1 = new WebComponent("comp1");
			component1.add(behavior);
			WebComponent component2 = new WebComponent("comp2");
			component2.add(behavior);
			add(component1, component2);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><div wicket:id='comp1'></div><div wicket:id='comp2'></div></body></html>");
		}
	}

	/**
	 * A test Behavior for #sharedBehaviorRenderHead()
	 */
	private static class SharedBehavior extends Behavior
	{
		@Override
		public void renderHead(Component component, IHeaderResponse response)
		{
			super.renderHead(component, response);
			response.render(StringHeaderItem.forString("\nRendering header contribution for component with id: " + component.getId()));
		}
	}
}
