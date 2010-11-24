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
package org.apache.wicket;

import junit.framework.TestCase;

import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;


public class BehaviorUrlTest extends TestCase
{
	/**
	 * @see https://issues.apache.org/jira/browse/WICKET-3097
	 */
	public void testUrlRemainsStable()
	{
		WicketTester tester = new WicketTester();

		TestPage page = new TestPage();

		int indexBeforeRender = page.container.getBehaviorId(page.callbackBehavior);

		tester.startPage(page);

		page = (TestPage)tester.getLastRenderedPage();
		int indexAfterRender = page.container.getBehaviorId(page.callbackBehavior);

		assertEquals("index of behavior in the raw list should not have changed",
			indexBeforeRender, indexAfterRender);

	}

	/**
	 * Asserting that the component model assigning don't affect the behavior data index
	 * 
	 * @see https://issues.apache.org/jira/browse/WICKET-3142
	 */
	public void testUrlRemainsStableAfterComponentReceiveAnModel()
	{
		TestPage page = new TestPage();

		int indexBeforeRender = page.container.getBehaviorId(page.callbackBehavior);

		page.container.setDefaultModel(Model.of(""));

		int indexAfterRender = page.container.getBehaviorId(page.callbackBehavior);

		assertEquals("index of behavior in the raw list should not have changed",
			indexBeforeRender, indexAfterRender);

	}

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private WebMarkupContainer container;
		private TestCallbackBehavior callbackBehavior;

		public TestPage()
		{
			callbackBehavior = new TestCallbackBehavior();

			container = new WebMarkupContainer("container");
			container.add(new TestTemporaryBehavior());
			container.add(callbackBehavior);
			add(container);

		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><a wicket:id=\"container\">container</a></html>");
		}
	}

	private static class TestTemporaryBehavior extends AbstractBehavior
	{
		@Override
		public boolean isTemporary(Component c)
		{
			return true;
		}
	}

	private static class TestCallbackBehavior extends AbstractBehavior implements IBehaviorListener
	{
		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			super.onComponentTag(component, tag);
			tag.put("href", component.urlFor(this, IBehaviorListener.INTERFACE));
		}

		public void onRequest()
		{
		}
	}
}