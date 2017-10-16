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

import java.time.LocalTime;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test for {@link TimeField}.
 */
public class TimeFieldTest extends WicketTestCase
{

	@Test
	public void timeNull()
	{
		TestPage page = new TestPage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.submit();
		tester.assertNoErrorMessage();
		assertNull(page.field.getModelObject());
	}

	@Test
	public void timeEmptyHours()
	{
		TestPage page = new TestPage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field:minutes", "8");
		formTester.submit();
		tester.assertErrorMessages("The value of 'field' is not a valid LocalTime.");
	}

	@Test
	public void timeEmptyMinutes()
	{
		TestPage page = new TestPage(null);
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field:hours", "8");
		formTester.submit();
		tester.assertErrorMessages("The value of 'field' is not a valid LocalTime.");
	}

	@Test
	public void timeNotNull()
	{
		TestPage page = new TestPage(LocalTime.of(6, 15));
		tester.startPage(page);
		FormTester formTester = tester.newFormTester("form", false);
		formTester.setValue("field:hours", "8");
		formTester.submit();
		LocalTime t = page.field.getModelObject();
		assertNotNull(t);
		assertEquals(8, t.getHour());
		assertEquals(15, t.getMinute());
	}

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		public TimeField field;

		TestPage(LocalTime val)
		{
			Form<Void> form = new Form<>("form");
			add(form);

			form.add(field = new TimeField("field", Model.of(val)));
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
