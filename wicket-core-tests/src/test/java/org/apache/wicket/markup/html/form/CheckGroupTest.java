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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * @author jcompagner
 */
/**
 * Test for RadioGroup and Radio components
 * 
 * @author igor
 * 
 */
class CheckGroupTest extends WicketTestCase
{
	/**
	 * test component form processing
	 */
	@Test
	void formProcessing()
	{

		// test model constructors
		List<String> list = new ArrayList<String>();
		list.add("check2");

		CheckGroupProcessingTestPage page = new CheckGroupProcessingTestPage(list);

		tester.startPage(page);

		tester.submitForm(page.form);

		assertTrue(list.size() == 0, "running with nothing selected - model must be empty");

		tester.getRequest().getPostParameters().setParameterValue(page.group.getInputName(),
			page.check1.getValue());
		tester.submitForm(page.form);

		assertTrue(list.size() == 1 && list.contains("check1"),
			"running with choice1 selected - model must only contain value of check1");

		tester.getRequest().getPostParameters().setParameterValue(page.group.getInputName(),
			page.check2.getValue());
		tester.submitForm(page.form);

		assertTrue(list.size() == 1 && list.contains("check2"),
			"running with choice2 selected - model must only contain value of check2");

		// throw in some nulls into the request param to make sure they are
		// ignored
		tester.getRequest().getPostParameters().addParameterValue(page.group.getInputName(), null);
		tester.getRequest().getPostParameters().addParameterValue(page.group.getInputName(),
			page.check1.getValue());
		tester.getRequest().getPostParameters().addParameterValue(page.group.getInputName(),
			page.check2.getValue());
		tester.submitForm(page.form);

		assertTrue(list.size() == 2 && list.contains("check1") && list.contains("check2"),
			"running with choice1 and choice2 selected - model must only contain values of check1 and check2");

		tester.getRequest().getPostParameters().setParameterValue(page.group.getInputName(),
			"some weird choice uuid to test error");
		try
		{
			tester.submitForm(page.form);
			fail("running with an invalid choice value in the request param, should fail");
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
	void rendering() throws Exception
	{
		executeTest(CheckGroupTestPage1.class, "CheckGroupTestPage1_expected.html");
		executeTest(CheckGroupTestPage2.class, "CheckGroupTestPage2_expected.html");
		executeTest(CheckGroupTestPage3.class, "CheckGroupTestPage3_expected.html");
		executeTest(CheckGroupTestPage4.class, "CheckGroupTestPage4_expected.html");
		try
		{
			executeTest(CheckGroupTestPage5.class, "");
			fail("this will always fail");
		}
		catch (WicketRuntimeException e)
		{
			if (!e.getMessage()
				.contains("Check component [4:form:check2] cannot find its parent CheckGroup"))
			{
				fail("failed with wrong exception");
			}

		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	void disabledCheckGroup() throws Exception
	{
		executeTest(CheckGroupDisabledTestPage.class, "CheckGroupDisabledTestPage_expected.html");
	}
}
