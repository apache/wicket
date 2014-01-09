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

import static org.junit.Assert.*;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTesterScope;
import org.junit.Rule;
import org.junit.Test;

/**
 * Form validation related tests
 * 
 * @author igor
 */
public class FormValidationTest
{
	@Rule
	public WicketTesterScope scope = new WicketTesterScope();

	/**
	 * Tests validation of form components when all errors are rendered using a feedback panel.
	 * 
	 * Validation status depends on whether or not a form component has error messages, here we test
	 * submission roundtrip in a usecase where all error messages are rendered and cleared at the
	 * end of the request.
	 */
	@Test
	public void renderedFeedbackMessages()
	{
		// start the page

		TestPage page = new TestPage();
		scope.getTester().startPage(page);

		// submit the form without filling out any values

		FormTester formTester = scope.getTester().newFormTester(page.form.getPageRelativePath());
		formTester.setClearFeedbackMessagesBeforeSubmit(true);
		formTester.submit();

		// the first required form component should fail and so should the form

		assertTrue(page.form.hasError());
		assertFalse(page.field1.isValid());

		// fill out a value and submit again

		formTester = scope.getTester().newFormTester(page.form.getPageRelativePath());
		formTester.setValue(page.field1, "hi");
		formTester.setClearFeedbackMessagesBeforeSubmit(true);
		formTester.submit();

		// now the form and the form component should be valid

		assertFalse(page.form.hasError());
		assertTrue(page.field1.isValid());
	}

	/**
	 * Tests validation of form components when not all errors are rendered (maybe a missing
	 * feedback panel, maybe a feedback panel with a filter).
	 * 
	 * Validation status depends on whether or not a form component has error messages, here we test
	 * submission roundtrip in a usecase where not all error messages are rendered and cleared at
	 * the end of the request.
	 * 
	 * Even though a form component has error messages not rendered from the previous submission
	 * they should not block the component from re-validating.
	 */
	@Test
	public void unrenderedFeedbackMessages()
	{
		// start the page

		TestPage page = new TestPage();
		scope.getTester().startPage(page);

		// submit the form without filling out any values

		FormTester formTester = scope.getTester().newFormTester(page.form.getPageRelativePath());
		formTester.setClearFeedbackMessagesBeforeSubmit(false);
		formTester.submit();

		// the first required form component should fail and so should the form

		assertTrue(page.form.hasError());
		assertFalse(page.field1.isValid());

		// fill out a value and submit again

		formTester = scope.getTester().newFormTester(page.form.getPageRelativePath());
		formTester.setValue(page.field1, "hi");
		formTester.setClearFeedbackMessagesBeforeSubmit(false);
		formTester.submit();

		// now the form and the form component should be valid

		assertFalse(page.form.hasError());
		assertTrue(page.field1.isValid());
	}


	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		public final TextField field1;
		public final Form form;

		public TestPage()
		{
			form = new Form("form");
			add(form);
			form.add(field1 = new TextField("field1", Model.of("")));
			field1.setRequired(true);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>"//
				+ "<form wicket:id='form'><input wicket:id='field1' type='text'/></form>" //
				+ "</body></html>");
		}
	}

}
