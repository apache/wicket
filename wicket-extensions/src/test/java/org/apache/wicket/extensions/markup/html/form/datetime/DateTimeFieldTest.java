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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.converter.LocalDateConverter;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test for {@link LocalDateTimeField}.
 */
public class DateTimeFieldTest extends WicketTestCase
{
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void dateTimeNull()
	{
		TestPage page = new TestPage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.submit();
		tester.assertNoErrorMessage();
		assertNull(page.field.getModelObject());
	}

	@Test
	public void timeEmpty()
	{
		LocalDate date = LocalDate.of(2017, 02, 13);
		TestPage page = new TestPage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field:date",
			new LocalDateConverter().convertToString(date, Locale.forLanguageTag("en-US")));
		formTester.submit();
		tester.assertNoErrorMessage();
		assertEquals(LocalDateTime.of(date, LocalTime.of(12, 0)), page.field.getModelObject());
	}

	@Test
	public void dateEmpty()
	{
		TestPage page = new TestPage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field:time:hours", "6");
		formTester.setValue("field:time:minutes", "15");
		formTester.select("field:time:amOrPmChoice", 0);
		formTester.submit();
		tester.assertErrorMessages("The value of 'field' is not a valid LocalDateTime.");
	}

	@Test
	public void dateTimeNotEmpty()
	{
		LocalDate date = LocalDate.of(2017, 02, 13);
		TestPage page = new TestPage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field:date",
			new LocalDateConverter().convertToString(date, Locale.forLanguageTag("en-US")));
		formTester.setValue("field:time:hours", "6");
		formTester.setValue("field:time:minutes", "15");
		formTester.select("field:time:amOrPmChoice", 0);
		formTester.submit();
		tester.assertNoErrorMessage();
		assertEquals(LocalDateTime.of(date, LocalTime.of(6, 15)), page.field.getModelObject());
	}

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		public LocalDateTimeField field;

		TestPage(LocalDateTime val)
		{
			Form<Void> form = new Form<>("form");
			add(form);

			form.add(field = new LocalDateTimeField("field", Model.of(val))
			{
				@Override
				protected LocalTime getDefaultTime()
				{
					return LocalTime.NOON;
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>"
				+ "<form wicket:id=\"form\"><span wicket:id=\"field\"/></form></body></html>");
		}
	}
}