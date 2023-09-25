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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link NumberTextField}.
 * 
 * @author svenmeier
 */
class NumberTextFieldTest extends WicketTestCase
{

	/**
	 * WICKET-4884, WICKET-3591
	 */
	@Test
	void convertBigDecimal()
	{
		TestPage<BigDecimal> testPage = new TestPage<>();
		testPage.textField.setType(BigDecimal.class);
		testPage.textField.setMinimum(Model.of(new BigDecimal("0.00")));
		testPage.textField.setMaximum(new BigDecimal("100.00"));
		testPage.textField.setModelObject(new BigDecimal("0.00"));
		tester.startPage(testPage);

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("<input wicket:id=\"number\" type=\"number\" value=\"0.00\" name=\"number\" min=\"0.00\" max=\"100.00\"/>"));

		FormTester formTester = tester.newFormTester(testPage.form.getId());
		formTester.setValue(testPage.textField.getId(), "50.50");
		formTester.submit();
		assertEquals(new BigDecimal("50.50"), testPage.textField.getDefaultModelObject());
	}

	/**
	 * WICKET-4884, WICKET-3591
	 */
	@Test
	void convertDouble()
	{
		TestPage<Double> testPage = new TestPage<Double>();
		testPage.textField.setType(Double.class);
		testPage.textField.setMinimum(Double.valueOf("0.0"));
		testPage.textField.setMaximum(Double.valueOf("2000.0"));
		testPage.textField.setModelObject(Double.valueOf("1000.0"));
		tester.startPage(testPage);

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("<input wicket:id=\"number\" type=\"number\" value=\"1000.0\" name=\"number\" min=\"0.0\" max=\"2000.0\"/>"));

		FormTester formTester = tester.newFormTester(testPage.form.getId());
		formTester.setValue(testPage.textField.getId(), "2,000.00");
		formTester.submit();
		assertEquals(Double.valueOf("2000.00"), testPage.textField.getDefaultModelObject());
	}

	/**
	 * WICKET-4884, WICKET-3591
	 */
	@Test
	void convertLong()
	{
		TestPage<Long> testPage = new TestPage<Long>();
		testPage.textField.setType(Long.class);
		testPage.textField.setMinimum(Long.valueOf("0"));
		testPage.textField.setMaximum(Long.valueOf("100"));
		testPage.textField.setModelObject(Long.valueOf("0"));
		tester.startPage(testPage);

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("<input wicket:id=\"number\" type=\"number\" value=\"0\" name=\"number\" min=\"0\" max=\"100\"/>"));

		FormTester formTester = tester.newFormTester(testPage.form.getId());
		formTester.setValue(testPage.textField.getId(), "50");
		formTester.submit();
		assertEquals(Long.valueOf("50"), testPage.textField.getDefaultModelObject());
	}

	/**
	 * WICKET-5467
	 */
	@Test
	void respectStepAny()
	{
		TestPage<Double> testPage = new TestPage<Double>();
		testPage.textField.setType(Double.class);
		testPage.textField.setStep(Model.of(NumberTextField.ANY));
		testPage.textField.setModelObject(Double.valueOf("1000.0"));
		tester.startPage(testPage);

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("<input wicket:id=\"number\" step=\"any\" type=\"number\" value=\"1000.0\" name=\"number\"/>"));
	}

	/**
	 * WICKET-5467
	 */
	@Test
	void respectStepWithNumberValue()
	{
		TestPage<Double> testPage = new TestPage<Double>();
		testPage.textField.setType(Double.class);
		testPage.textField.setStep(Double.valueOf(0.3d));
		testPage.textField.setModelObject(Double.valueOf("1000.0"));
		tester.startPage(testPage);

		String response = tester.getLastResponseAsString();
		assertTrue(response.contains("<input wicket:id=\"number\" step=\"0.3\" type=\"number\" value=\"1000.0\" name=\"number\"/>"));
	}

	/**
	 * @param <N>
	 *            type parameter
	 */
	public static class TestPage<N extends Number & Comparable<N>> extends WebPage
		implements
			IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		Form<Void> form;
		NumberTextField<N> textField;

		/** */
		TestPage()
		{
			add(form = new Form<Void>("form")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public Locale getLocale()
				{
					return Locale.GERMAN;
				}
			});
			form.add(textField = new NumberTextField<N>("number", Model.of((N)null)));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body>"
					+ "<form wicket:id=\"form\"><input wicket:id=\"number\" step=\"any\" type=\"number\" /></form></body></html>");
		}
	}
}
