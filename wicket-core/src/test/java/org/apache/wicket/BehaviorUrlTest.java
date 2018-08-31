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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 */
class BehaviorUrlTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-3097
	 */
	@Test
	void urlRemainsStable()
	{
		TestPage page = new TestPage();

		int indexBeforeRender = page.container.getBehaviorId(page.callbackBehavior);

		tester.startPage(page);

		page = (TestPage)tester.getLastRenderedPage();
		int indexAfterRender = page.container.getBehaviorId(page.callbackBehavior);

		assertEquals(indexBeforeRender, indexAfterRender, "index of behavior in the raw list should not have changed");

	}

	/**
	 * Asserting that the component model assigning don't affect the behavior data index
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-3142
	 */
	@Test
	void urlRemainsStableAfterComponentReceiveAnModel()
	{
		TestPage page = new TestPage();

		int indexBeforeRender = page.container.getBehaviorId(page.callbackBehavior);

		page.container.setDefaultModel(Model.of(""));

		int indexAfterRender = page.container.getBehaviorId(page.callbackBehavior);

		assertEquals(indexBeforeRender, indexAfterRender, "index of behavior in the raw list should not have changed");

	}

	/**
	 */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private WebMarkupContainer container;
		private TestCallbackBehavior callbackBehavior;

		/**
		 * Construct.
		 */
		TestPage()
		{
			callbackBehavior = new TestCallbackBehavior();

			container = new WebMarkupContainer("container");
			container.add(new TestTemporaryBehavior());
			container.add(callbackBehavior);
			add(container);

		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><a wicket:id=\"container\">container</a></html>");
		}
	}

	/**
	 */
	private static class TestTemporaryBehavior extends Behavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isTemporary(Component c)
		{
			return true;
		}
	}

	/**
	 */
	private static class TestCallbackBehavior extends Behavior implements IRequestListener
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			super.onComponentTag(component, tag);
			tag.put("href",
				component.urlForListener(this, new PageParameters()));
		}

		@Override
		public void onRequest()
		{
		}
	}


	@Test
	void testBehaviorUrlNotDoubleEscaped()
	{
		tester.startPage(EscapeTestPage.class);

		String response = tester.getLastResponseAsString();
//		System.err.println(response);
		assertTrue(response.contains(EscapeTestPage.TEST_QUERY_STRING));

		tester.executeAjaxEvent("form:textfield", "change");

		EscapeTestPage testPage = (EscapeTestPage)tester.getLastRenderedPage();
		IRequestParameters lastParameters = testPage.getLastQueryParameters();
		assertEquals(StringValue.valueOf("value_1"), lastParameters.getParameterValue("query_p_1"));
	}

	/** */
	public static class EscapeTestPage extends MockPageParametersAware
	{
		private static final long serialVersionUID = 1L;
		/** */
		static final String TEST_QUERY_STRING = "&query_p_1=value_1";

		/** */
		public EscapeTestPage()
		{
			getTextField().add(new AjaxEventBehavior("change")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public CharSequence getCallbackUrl()
				{
					return super.getCallbackUrl() + TEST_QUERY_STRING;
				}

				@Override
				protected void onEvent(AjaxRequestTarget target)
				{
				}
			});
		}
	}
}