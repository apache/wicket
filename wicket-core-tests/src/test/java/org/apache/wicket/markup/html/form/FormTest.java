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
package org.apache.wicket.markup.html.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.MockPageParametersAware;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.visit.IVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Pekka Enberg
 * @author Martijn Dashorst
 */
public class FormTest extends WicketTestCase
{
	private IVisitor<?, ?> visitor;

	/**
	 * 
	 */
	@BeforeEach
	void before()
	{
		visitor = new Form.ValidationVisitor()
		{
			@Override
			public void validate(FormComponent<?> formComponent)
			{
			}
		};
	}

	/**
	 * WICKET-6540
	 */
	@Test
	void defaultButton() throws Exception
	{
		executeTest(FormDefaultButtonTestPage.class, "FormDefaultButtonTestPage_expected.html");
	}

	/**
	 * WICKET-6525 / WICKET-6348
	 */
	@Test
	void formMethods() throws Exception
	{
		executeTest(FormMethodTestPage.class, "FormMethodTestPage_expected.html");
		
		FormMethodTestPage page = (FormMethodTestPage)tester.getLastRenderedPage();
	
		// form action contains path and query 
		assertEquals("var f = document.getElementById('formpost1');f.action='path?param1&param2=val%20ue';Wicket.Event.fire(f, 'submit');",
			page.postForm.getJsForListenerUrl("path?param1&param2=val%20ue").toString());
		
		// form action must not contain query (since ignored by browser), put into hidden form parameters instead 
		assertEquals("document.getElementById('formget2_hf_0').innerHTML = '<input type=\"hidden\" name=\"param1\" value=\"\" /><input type=\"hidden\" name=\"param2\" value=\"val ue\" />';var f = document.getElementById('formget2');f.action='path';Wicket.Event.fire(f, 'submit');",
			page.getForm.getJsForListenerUrl("path?param1&param2=val%20ue").toString());
	}

	/**
	 * WICKET-3488
	 */
	@Test
	void formReplacement()
	{
		tester.startPage(TestPage.class);
		tester.newFormTester("form").submit();
		tester.assertRenderedPage(TestPage.class);
	}

	/**
	 * 
	 */
	@Test
	void actionUrlNotDoubleEscaped()
	{
		tester.startPage(TestPage.class);
		String response = tester.getLastResponseAsString();
		assertTrue(response.contains(Strings.escapeMarkup(TestPage.TEST_QUERY_STRING)));
	}

	@Test
	void onValidateModelObjects()
	{

		class TestPage extends WebPage implements IMarkupResourceStreamProvider
		{
			boolean shouldFail, submit, error;

			TestPage()
			{
				add(new Form<Void>("form")
				{
					@Override
					protected void onValidateModelObjects()
					{
						if (shouldFail)
							error("failed");
					}

					@Override
					protected void onSubmit()
					{
						submit = true;
					}

					@Override
					protected void onError()
					{
						error = true;
					}
				});
			}

			@Override
			public IResourceStream getMarkupResourceStream(final MarkupContainer container,
				Class<?> containerClass)
			{
				return new StringResourceStream("<form wicket:id='form'></form>");
			}
		}

		TestPage page = new TestPage();
		tester.startPage(page);
		tester.submitForm("form");
		assertTrue(page.submit);
		assertFalse(page.error);

		page = new TestPage();
		page.shouldFail = true;
		tester.startPage(page);
		tester.submitForm("form");
		assertFalse(page.submit);
		assertTrue(page.error);
	}

	/** */
	public static class TestPage extends MockPageParametersAware
	{
		private static final long serialVersionUID = 1L;
		/** */
		static final String TEST_QUERY_STRING = "&query_p_1=value_1";

		@Override
		protected Form<Void> newForm(String id)
		{
			return new Form<Void>(id)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected CharSequence getActionUrl()
				{
					return super.getActionUrl() + TEST_QUERY_STRING;
				}
			};
		}

	}

	/**
	 * Tests if a stateful form does not render the pageparameters from the page
	 * 
	 * @throws Exception
	 */
	@Test
	void pageWithParameters() throws Exception
	{
		PageParameters parameters = new PageParameters();
		parameters.add("first", "foo");
		parameters.add("second", "bar");
		executeTest(FormMethodTestPage.class, parameters, "pageWithParameters_expected.html");
	}
}
