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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author Pedro Santos
 */
public class NestedFormsTest extends WicketTestCase
{
	/**
	 *
	 */
	@Test
	public void postOrderSequenceSubmittingRootForm()
	{
		tester.startPage(TestPage.class);
		TestPage testPage = (TestPage)tester.getLastRenderedPage();
		FormTester formTester = tester.newFormTester("outerForm");
		formTester.submit("outerSubmit");
		assertEquals(3, testPage.submitSequence.size());
		assertEquals(0, testPage.submitSequence.indexOf(testPage.outerSubmit));
		assertEquals(1, testPage.submitSequence.indexOf(testPage.innerForm));
		assertEquals(2, testPage.submitSequence.indexOf(testPage.outerForm));
	}

	/**
	 *
	 */
	@Test
	public void postOrderSequenceSubmittingInnerForm()
	{
		tester.startPage(TestPage.class);
		TestPage testPage = (TestPage)tester.getLastRenderedPage();
		FormTester formTester = tester.newFormTester("outerForm");
		formTester.submit("innerForm:innerSubmit");
		assertEquals(2, testPage.submitSequence.size());
		assertEquals(0, testPage.submitSequence.indexOf(testPage.innerSubmit));
		assertEquals(1, testPage.submitSequence.indexOf(testPage.innerForm));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void subitInnerFormWithAjaxBehavior()
	{
		tester.startPage(TestAjaxPage.class);

		FormTester formTester = tester.newFormTester("formA");		
		formTester.submit("formB:submit");
		
		TestAjaxPage testPage = (TestAjaxPage)tester.getLastRenderedPage();
		
		assertFalse(testPage.isFormAsubmitted());
		assertTrue(testPage.isFormBsubmitted());
	}

	public static class TestAjaxPage extends WebPage implements IMarkupResourceStreamProvider
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private boolean formAsubmitted;
		private boolean formBsubmitted;
		
		public TestAjaxPage() 
		{
			final Form<Void> formA = new Form<Void>("formA")
			{
				@Override
				public void onDetach() 
				{
					formAsubmitted = isSubmitted();
					super.onDetach();
				}
			};
			final Form<Void> formB = new Form<Void>("formB")
			{
				@Override
				protected void onDetach() 
				{
					formBsubmitted = isSubmitted();
					super.onDetach();
				}
			};
			
			formB.add(new AjaxButton("submit") 
			{
				private static final long serialVersionUID = 1L;
			});
			
			formA.add(formB);
			add(formA);
		}
		
		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<head></head>" +
				"<form wicket:id=\"formA\">\r\n" + 
				"				Form A\r\n" + 
				"				<form wicket:id=\"formB\">\r\n" + 
				"					Form B\r\n" + 
				"					<button wicket:id=\"submit\">submit</button>\r\n" + 
				"				</form>\r\n" + 
				"			</form>");
		}

		public boolean isFormAsubmitted() {
			return formAsubmitted;
		}

		public boolean isFormBsubmitted() {
			return formBsubmitted;
		}
	}
	
	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		/** */
		private static final long serialVersionUID = 1L;
		private List<Component> submitSequence = new ArrayList<Component>();
		Form<Void> outerForm;
		Button outerSubmit;
		Form<Void> innerForm;
		Button innerSubmit;

		/**
		 * Construct.
		 */
		public TestPage()
		{
			outerForm = new Form<Void>("outerForm")
			{
				/** */
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit()
				{
					submitSequence.add(this);
				}
			};
			add(outerForm);
			outerSubmit = new Button("outerSubmit")
			{
				/** */
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit()
				{
					submitSequence.add(this);
				}
			};
			outerForm.add(outerSubmit);
			innerForm = new Form<Void>("innerForm")
			{
				/** */
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit()
				{
					submitSequence.add(this);
				}
			};
			outerForm.add(innerForm);
			innerSubmit = new Button("innerSubmit")
			{
				/** */
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit()
				{
					submitSequence.add(this);
				}
			};
			innerForm.add(innerSubmit);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body>"
					+ "<form wicket:id=\"outerForm\">"//
					+ "  <input type=\"submit\" wicket:id=\"outerSubmit\"/>"//
					+ "  <form wicket:id=\"innerForm\"><input type=\"submit\" wicket:id=\"innerSubmit\"/></form>"//
					+ "</form>" + //
					"</body></html>");
		}
	}
}
