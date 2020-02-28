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
package org.apache.wicket.extensions.markup.html.form.datetime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link LocalDateTextField}.
 */
public class LocalDateTextFieldTest extends WicketTestCase
{

	@Test
	public void dateNullTest()
	{
		TestPage page = new TestPage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.submit();
		tester.assertNoErrorMessage();
		assertNull(page.field.getModelObject());
	}

	@Test
	public void dateNotNullTest()
	{
		LocalDate date = LocalDate.of(2017, 02, 03);
		TestPage page = new TestPage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field", "03-02-2017");
		formTester.submit();
		tester.assertNoErrorMessage();
		LocalDate d = page.field.getModelObject();
		assertEquals(date, d);
	}

	@Test
	public void dateParsePatternTest()
	{
		LocalDate date = LocalDate.of(2017, 02, 03);
		TestPage page = new TestPage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field", "3-2-2017");
		formTester.submit();
		tester.assertNoErrorMessage();
		LocalDate d = page.field.getModelObject();
		assertEquals(date, d);
	}

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		public LocalDateTextField field;

		TestPage(LocalDate val)
		{
			Form<Void> form = new Form<>("form");
			add(form);

			form.add(field = new LocalDateTextField("field", Model.of(val), "dd-MM-yyyy", "d-M-yyyy"));
		}
		
		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>"
				+ "<form wicket:id=\"form\"><input wicket:id=\"field\"/></form></body></html>");
		}
	}
}