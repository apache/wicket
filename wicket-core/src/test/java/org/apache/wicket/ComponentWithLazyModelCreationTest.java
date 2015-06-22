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

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @see <a href="https://issues.apache.org/jira/browse/WICKET3142">WICKET3142</a>
 */
public class ComponentWithLazyModelCreationTest extends WicketTestCase
{
	private static final String LABEL_VALUE = "some value";
	/** just an property to be reached by the CompoundPropertyModel */
	public String label = LABEL_VALUE;

	/**
	 * Simulates the reported problem at the ticket: an behavior at the position 0 on the component
	 * data has its position incremented after its ULR get encoded.
	 */
	@Test
	public void urlReferingSomeBehavior()
	{
		TestPage page = new TestPage(new CompoundPropertyModel<ComponentWithLazyModelCreationTest>(
			this));
		tester.startPage(page);

		tester.executeUrl(page.mainCallbackBehavior.statefullUrl);

		tester.assertLabel("label", LABEL_VALUE);
		assertTrue("mainCallbackBehavior was called", page.mainCallbackBehavior.requested);
	}

	/**
	 * If the index used to encode the behavior URL is no longer valid, an possible problem is this
	 * URL invoking the wrong component behavior
	 */
	@Test
	public void urlDontCallOtherBehavior()
	{
		TestPage page = new TestPage(new CompoundPropertyModel<ComponentWithLazyModelCreationTest>(
			this));
		tester.startPage(page);

		tester.executeUrl(page.brotherCallbackBehavior.statefullUrl);

		tester.assertLabel("label", LABEL_VALUE);
		assertTrue("brotherCallbackBehavior was requested", page.brotherCallbackBehavior.requested);
		assertFalse("mainCallbackBehavior was not requested", page.mainCallbackBehavior.requested);
	}

	/**
	 */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;
		private TestCallbackBehavior mainCallbackBehavior;
		private TestCallbackBehavior brotherCallbackBehavior;

		/**
		 * Construct.
		 * 
		 * @param model
		 */
		public TestPage(IModel<ComponentWithLazyModelCreationTest> model)
		{
			super(model);
			mainCallbackBehavior = new TestCallbackBehavior();
			brotherCallbackBehavior = new TestCallbackBehavior();
			// returning an component that will force the model initialization
			Label label = new Label("label");
			label.add(mainCallbackBehavior);
			label.add(brotherCallbackBehavior);
			add(label);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><a wicket:id=\"label\"></a></html>");
		}
	}

	private static class TestCallbackBehavior extends Behavior implements IBehaviorListener
	{
		private static final long serialVersionUID = 1L;
		private boolean requested;
		// simulating the callback URL generated for component aware behaviors
		private String statefullUrl;

		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			super.onComponentTag(component, tag);
			int index = component.getBehaviorId(this);
			IRequestHandler handler = new ListenerInterfaceRequestHandler(
				new PageAndComponentProvider(component.getPage(), component),
				IBehaviorListener.INTERFACE, index);
			statefullUrl = component.getRequestCycle().mapUrlFor(handler).toString();
		}

		@Override
		public void onRequest()
		{
			requested = true;
		}
	}
}
