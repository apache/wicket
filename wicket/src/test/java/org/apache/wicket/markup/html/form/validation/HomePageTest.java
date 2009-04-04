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
public class HomePageTest extends TestCase
{
	private WicketTester tester;

	@Override
	public void setUp()
	{
		tester = new WicketTester();
		tester.startPage(HomePage.class);
		tester.assertRenderedPage(HomePage.class);
	}

	/**
	 * 
	 */
	public void testWithoutBorder()
	{
		tester.executeAjaxEvent("form:submit", "onclick");
		assertEquals("Expected one error message",
			tester.getMessages(FeedbackMessage.ERROR).size(), 2);
		assertTrue(((HomePage)tester.getLastRenderedPage()).hitOnError);
		assertFalse(((HomePage)tester.getLastRenderedPage()).hitOnSubmit);
	}

	/**
	 * 
	 */
	public void testWithoutBorder2()
	{
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("textfield1", "testxxx");
		tester.executeAjaxEvent("form:submit", "onclick");
		tester.assertNoErrorMessage();
		assertFalse(((HomePage)tester.getLastRenderedPage()).hitOnError);
		assertTrue(((HomePage)tester.getLastRenderedPage()).hitOnSubmit);
	}

	/**
	 * 
	 */
	public void testWithBorder()
	{
		tester.executeAjaxEvent("border:form2:submit", "onclick");
		assertEquals("Expected one error message",
			tester.getMessages(FeedbackMessage.ERROR).size(), 2);
		assertTrue(((MyBorder)tester.getLastRenderedPage().get("border")).hitOnError);
		assertFalse(((MyBorder)tester.getLastRenderedPage().get("border")).hitOnSubmit);
	}

	/**
	 * 
	 */
	public void testWithBorder2()
	{
		TextField textfield = (TextField)tester.getLastRenderedPage().get("border:textfield1");
		tester.getServletRequest().setParameter(textfield.getInputName(), "abcde");
		tester.executeAjaxEvent("border:form2:submit", "onclick");
		tester.assertNoErrorMessage();
		assertFalse(((MyBorder)tester.getLastRenderedPage().get("border")).hitOnError);
		assertTrue(((MyBorder)tester.getLastRenderedPage().get("border")).hitOnSubmit);
	}

	public void testWithPanelAjax()
	{
		tester.executeAjaxEvent("form3:submit", "onclick");

		HomePage page = (HomePage)tester.getLastRenderedPage();
		assertTrue((page.getFormSubmitted() & HomePage.AJAX) == HomePage.AJAX);
	}

	/**
	   * 
	   */
	public void testWithPanelForm()
	{
		tester.clickLink("form3:submit2");
		HomePage page = (HomePage)tester.getLastRenderedPage();
		assertTrue((page.getFormSubmitted() & HomePage.NORMAL) == HomePage.NORMAL);
	}
}
