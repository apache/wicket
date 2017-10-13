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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.converter.LocalDateConverter;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DateTimeFieldTest extends WicketTestCase {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void timeNullTest() {
		TestTimePage page = new TestTimePage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.submit();
		assertNull(page.field.getModelObject());
	}

	@Test
	public void timeNullHoursTest() {
		TestTimePage page = new TestTimePage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field:minutes", "8");
		formTester.submit();
		assertNull(page.field.getModelObject());
	}

	@Test
	public void timeNullMinutesTest() {
		TestTimePage page = new TestTimePage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field:hours", "8");
		formTester.submit();
		assertNull(page.field.getModelObject());
	}

	@Test
	public void timeNotNullTest() {
		TestTimePage page = new TestTimePage(LocalTime.of(6, 15));
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field:hours", "8");
		formTester.submit();
		LocalTime t = page.field.getModelObject();
		assertNotNull(t);
		assertEquals(8, t.getHour());
		assertEquals(15, t.getMinute());
	}

	@Test
	public void dateNullTest() {
		TestDatePage page = new TestDatePage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.submit();
		assertNull(page.field.getModelObject());
	}

	@Test
	public void dateNotNullTest() {
		LocalDate date = LocalDate.of(2017, 02, 13);
		TestDatePage page = new TestDatePage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field", new LocalDateConverter().convertToString(date, Locale.forLanguageTag("en-US")));
		formTester.submit();
		LocalDate d = page.field.getModelObject();
		assertNotNull(d);
		assertEquals(date, d);
	}

	@Test
	public void dateTimeNullTest() {
		TestDateTimePage page = new TestDateTimePage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.submit();
		assertNull(page.field.getModelObject());
	}

	@Test
	public void dateTimeNullTest1() {
		LocalDate date = LocalDate.of(2017, 02, 13);
		TestDateTimePage page = new TestDateTimePage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field:date", new LocalDateConverter().convertToString(date, Locale.forLanguageTag("en-US")));
		formTester.submit();
		assertNull(page.field.getModelObject());
	}

	@Test
	public void dateTimeNullTest2() {
		TestDateTimePage page = new TestDateTimePage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field:time:hours", "6");
		formTester.setValue("field:time:minutes", "15");
		formTester.select("field:time:amOrPmChoice", 0);
		formTester.submit();
		assertNull(page.field.getModelObject());
	}

	@Test
	public void dateTimeNotNullTest() {
		LocalDate date = LocalDate.of(2017, 02, 13);
		TestDateTimePage page = new TestDateTimePage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field:date", new LocalDateConverter().convertToString(date, Locale.forLanguageTag("en-US")));
		formTester.setValue("field:time:hours", "6");
		formTester.setValue("field:time:minutes", "15");
		formTester.select("field:time:amOrPmChoice", 0);
		formTester.submit();
		assertNotNull(page.field.getModelObject());
		assertEquals(LocalDateTime.of(date, LocalTime.of(6,  15)), page.field.getModelObject());
	}

	public static class TestDateTimePage extends TestPage<LocalDateTime>
	{
		private static final long serialVersionUID = 1L;

		TestDateTimePage(LocalDateTime val)
		{
			super(val);
		}

		@Override
		FormComponent<LocalDateTime> newComponent()
		{
			return new LocalDateTimeField("field", model);
		}
	}

	public static class TestDatePage extends TestPage<LocalDate>
	{
		private static final long serialVersionUID = 1L;

		TestDatePage(LocalDate val)
		{
			super(val);
			tag = "input type=\"text\"";
		}

		@Override
		FormComponent<LocalDate> newComponent()
		{
			return new LocalDateTextField("field", model, FormatStyle.SHORT);
		}
	}

	public static class TestTimePage extends TestPage<LocalTime>
	{
		private static final long serialVersionUID = 1L;

		TestTimePage(LocalTime val)
		{
			super(val);
		}

		@Override
		FormComponent<LocalTime> newComponent()
		{
			return new TimeField("field", model);
		}
	}

	public abstract static class TestPage<T extends Serializable> extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;
		Form<Void> form;
		FormComponent<T> field;
		IModel<T> model = new Model<T>();
		String tag = "span";

		/** */
		public TestPage(T val)
		{
			add(form = new Form<>("form"));
			model.setObject(val);
			form.add(field = newComponent());
		}

		abstract FormComponent<T> newComponent();

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream(String.format("<html><body>"
					+ "<form wicket:id=\"form\"><%s wicket:id=\"field\"/></form></body></html>", tag));
		}

		@Override
		protected void onDetach()
		{
			super.onDetach();
			model.detach();
		}
	}
}
