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

import java.util.function.Function;

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
		TestPage	testPage	= new TestPage();
		String		replacement	= "replacement";

		testPage.add(new Label(TestPage.LABEL_ID, TestPage.LABEL_VAL));
		testPage.add(new AbstractTransformerBehavior()
		{
			/** */
			private static final long serialVersionUID = 1L;

			@Override
			public CharSequence transform(Component component, CharSequence output)
				throws Exception
			{
				return output.toString().replace(TestPage.LABEL_VAL, replacement);
			}
		});

		tester.startPage(testPage);
		assertTrue(tester.getLastResponseAsString().contains(replacement));
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

	/**
	 * Test how multiple different transformers applied to the same component behave.
	 * <p>
	 * The current implementation of {@link AbstractTransformerBehavior} doesn't support multiple
	 * instances on the same component, a container needs to be used explicitly instead. So make
	 * sure the implementation is as expected, as otherwise the container might not be necessary at
	 * all anymore, and that the container really works around the problem.
	 * </p>
	 * @see <a href="https://issues.apache.org/jira/projects/WICKET/issues/WICKET-6823">JIRA issue</a>
	 */
	@Test
	public void multiTransesSameComp()
	{
		TestPage	testPage	= new TestPage();
		Label		label		= new Label(TestPage.LABEL_ID, TestPage.LABEL_VAL);

		Function<String, AbstractTransformerBehavior> transProd = (val) ->
		{
			return new AbstractTransformerBehavior()
			{
				/** */
				private static final long serialVersionUID = 1L;

				@Override
				public CharSequence transform(Component component, CharSequence output)
					throws Exception
				{
					String outStr = output.toString();
					if (outStr.contains(TestPage.LABEL_VAL))
					{
						return outStr.replace(TestPage.LABEL_VAL, val);
					}

					// Make somewhat sure to recognize that BOTH transformers have been applied.
					return outStr.replaceAll
					(
						">(.+)</span>",
						String.format(">$1%s</span>", val)
					);
				}
			};
		};

		label.add(transProd.apply("foo"));
		label.add(transProd.apply("bar"));

		// Make sure the expected limited implementation is still available, which makes a container
		// necessary only at all. If that has changed, the container might be removed as well.
		testPage.add(label);
		tester.startPage(testPage);
		tester.isVisible(TestPage.LABEL_ID);
		tester.assertContains(">foo</span>");
		tester.assertContainsNot("</html>");

		testPage	= new TestPage();
		label		= new Label(TestPage.LABEL_ID, TestPage.LABEL_VAL);

		label.add(AbstractTransformerBehavior.Multi.newFor
		(
			transProd.apply("foo"),
			transProd.apply("bar")
		));

		// Maike sure that the container provided as workaround really fixes the problem.
		testPage.add(label);
		tester.startPage(testPage);
		tester.isVisible(TestPage.LABEL_ID);
		tester.assertContains(">foobar</span>");
		tester.assertContains("</html>");
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
					if (getRequestCycle().find(AjaxRequestTarget.class).isPresent())
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

		private static final String LABEL_ID	= "label";
		private static final String LABEL_VAL	= "{to be replaced}";

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><span wicket:id='label'></span></body></html>");
		}
	}
}
