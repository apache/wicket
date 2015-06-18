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

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;


/**
 * Test for RadioGroup and Radio components
 * 
 * @author igor
 * 
 */
public class RadioGroupTest extends WicketTestCase
{
	/**
	 * test component form processing
	 */
	@Test
	public void formProcessing()
	{
		// object used to test regular model
		Model<String> model = new Model<String>();

		RadioGroupProcessingTestPage page = new RadioGroupProcessingTestPage(model);

		model.setObject("initial");

		tester.startPage(page);

		tester.submitForm(page.form);
		assertTrue("group: running with nothing selected - model must be set to null",
			model.getObject() == null);

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(page.group.getInputName(), page.radio1.getValue());
		tester.submitForm(page.form);

		assertEquals("group: running with choice1 selected - model must be set to value of radio1",
			model.getObject(), "radio1");

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(page.group.getInputName(), page.radio2.getValue());
		tester.submitForm(page.form);

		assertEquals("group: running with choice2 selected - model must be set to value of radio2",
			model.getObject(), "radio2");

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(page.group.getInputName(), "some weird choice uuid to test error");

		try
		{
			tester.submitForm(page.form);
			fail("group: ran with an invalid choice selected but did not fail");
		}
		catch (WicketRuntimeException e)
		{

		}
	}

	/**
	 * test component rendering
	 * 
	 * @throws Exception
	 */
	@Test
	public void rendering() throws Exception
	{
		executeTest(RadioGroupTestPage1.class, "RadioGroupTestPage1_expected.html");
		try
		{
			executeTest(RadioGroupTestPage2.class, "");
			fail("the rendering of page above must fail because radio2 component is not under any group");
		}
		catch (WicketRuntimeException e)
		{
			if (!e.getMessage().contains(
				"Radio component [1:form:radio2] cannot find its parent RadioGroup"))
			{
				fail("failed with wrong exception");
			}
		}
	}

	/**
	 * Regression test for markup parsing of radio buttons. Tests issue #1465676.
	 * 
	 * @throws Exception
	 */
	@Test
	public void radioGroupTestPage3() throws Exception
	{
		// this test fails. You can make the test pass by closing the input tags
		// this was not the case in beta1
		executeTest(RadioGroupTestPage3.class, "RadioGroupTestPage3_expected.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void disabledRadioGroup() throws Exception
	{
		executeTest(RadioGroupDisabledTestPage.class, "RadioGroupDisabledTestPage_expected.html");
	}
}
