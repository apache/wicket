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
package org.apache.wicket.markup.transformer;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author Pedro Santos
 */
public class AbstractTransformerBehaviorTest extends WicketTestCase
{
	/** */
	@Test
	public void responseTransformation()
	{
		TestPage testPage = new TestPage();
		testPage.add(new AbstractTransformerBehavior()
		{
			/** */
			private static final long serialVersionUID = 1L;

			@Override
			public CharSequence transform(Component component, CharSequence output)
				throws Exception
			{
				return output.toString().replace("to be replaced", "replacement");
			}
		});
		tester.startPage(testPage);
		assertTrue(tester.getLastResponseAsString().contains("replacement"));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4105
	 */
	@Test
	public void transformationInAjaxRequest()
	{
		tester.startPage(new AjaxTestPage());
		tester.assertRenderedPage(AjaxTestPage.class);

		tester.assertContains("normal request");
		tester.assertContainsNot("ajax request");

		tester.clickLink("updateLabel", true);
		tester.assertContains("ajax request");
		tester.assertContainsNot("normal request");

	}

	private static class AjaxTestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		/**
		 * Constructor.
		 */
		private AjaxTestPage()
		{
			final Label label = new Label("label", "a label");
			label.setOutputMarkupId(true);
			label.add(new AbstractTransformerBehavior()
			{
				@Override
				public CharSequence transform(Component component, CharSequence output)
					throws Exception
				{
					CharSequence result;
					if (getRequestCycle().find(AjaxRequestTarget.class) != null)
					{
						result = "ajax request";
					}
					else
					{
						result = "normal request";
					}

					return result;
				}
			});

			add(label);

			add(new AjaxLink<Void>("updateLabel")
			{
				@Override
				public void onClick(AjaxRequestTarget target)
				{
					target.add(label);
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><span wicket:id='label'></span><a wicket:id='updateLabel'>Link</a></body></html>");
		}
	}

	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>{to be replaced}</body></html>");
		}

	}
}
