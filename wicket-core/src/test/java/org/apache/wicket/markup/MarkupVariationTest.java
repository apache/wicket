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
package org.apache.wicket.markup;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests that changing component's variation will use the correct markup
 */
public class MarkupVariationTest extends WicketTestCase
{

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3931
	 */
	@Test
	public void changeVariation()
	{
		tester.startPage(new VariationPage());
		tester.assertContainsNot("Two");
		tester.assertMarkupVariation(getVariationPanel(), "one");
		tester.assertMarkupVariation(tester.getLastRenderedPage(), null);
		tester.clickLink("p:l");

		tester.assertContainsNot("One");
		tester.assertMarkupVariation(getVariationPanel(), "two");
		tester.assertMarkupVariation(tester.getLastRenderedPage(), null);
		tester.clickLink("p:l");

		tester.assertContainsNot("Two");
		tester.assertMarkupVariation(getVariationPanel(), "one");
		tester.assertMarkupVariation(tester.getLastRenderedPage(), null);
		tester.clickLink("p:l");
	}

	private MarkupContainer getVariationPanel()
	{
		return (MarkupContainer) tester.getComponentFromLastRenderedPage("p");
	}

	private static class VariationPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private VariationPage()
		{
			add(new VariationPanel("p"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{

			return new StringResourceStream("<html><body><div wicket:id='p'></div></body></html>");
		}
	}

	private static class VariationPanel extends Panel
	{
		private String variation;

		public VariationPanel(String id)
		{
			super(id);

			setOutputMarkupId(true);
			variation = "one";

			add(new AjaxLink<Void>("l")
			{

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					variation = "one".equals(variation) ? "two" : "one";
					target.add(VariationPanel.this);
				}
			});

			add(new Label("simpleLabel", "Label"));

			add(new Form<Void>("a_form"));

			add(new Label("child", "Inline Enclosure child text"));
			add(new Label("nestedChild", "Nested Inline Enclosure child text"));

		}

		@Override
		public String getVariation()
		{
			return variation;
		}
	}
}
