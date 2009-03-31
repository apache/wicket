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
package org.apache.wicket.markup.html.form.validation;

import junit.framework.TestCase;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;

/**
 * Simple test using the WicketTester
 */
public class TestHomePage extends TestCase
{
	private WicketTester tester;

	private FormTester formTester;

	@Override
	public void setUp()
	{
		tester = new WicketTester();

		// Start and render the test page
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(HomePage.class);
	}

	/**
	 * 
	 */
	public void testWithoutBorder()
	{
		formTester = tester.newFormTester("form");
		formTester.submit();
		assertEquals("Expected one error message",
			tester.getMessages(FeedbackMessage.ERROR).size(), 1);
	}

	/**
	 * 
	 */
	public void testWithoutBorder2()
	{
		formTester = tester.newFormTester("form");
		formTester.setValue("textfield1", "testxxx");
		formTester.submit();
		tester.assertNoErrorMessage();
	}

	/**
	 * 
	 */
	public void testWithBorder()
	{
		formTester = tester.newFormTester("border:form2");
		formTester.submit();
		assertEquals("Expected one error message",
			tester.getMessages(FeedbackMessage.ERROR).size(), 1);
	}

	/**
	 * 
	 */
	public void testWithBorder2()
	{
		formTester = tester.newFormTester("border:form2");
		// formTester.setValue("..:textfield1", "testxxx");
		TextField<String> textfield = (TextField<String>)tester.getLastRenderedPage().get(
			"border:textfield1");
		tester.getServletRequest().setParameter(textfield.getInputName(), "abcde");
		formTester.submit();
		tester.assertNoErrorMessage();
	}
}
