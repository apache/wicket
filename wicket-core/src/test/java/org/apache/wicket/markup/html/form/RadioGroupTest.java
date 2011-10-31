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

import java.io.Serializable;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.MockPage;
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
	 * mock model object with an embedded property used to test compound property model
	 * 
	 * @author igor
	 * 
	 */
	public static class MockModelObject implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String prop1;
		private String prop2;

		/**
		 * @return prop1
		 */
		public String getProp1()
		{
			return prop1;
		}

		/**
		 * @param prop1
		 */
		public void setProp1(String prop1)
		{
			this.prop1 = prop1;
		}

		/**
		 * @return prop2
		 */
		public String getProp2()
		{
			return prop2;
		}

		/**
		 * @param prop2
		 */
		public void setProp2(String prop2)
		{
			this.prop2 = prop2;
		}


	}

	/**
	 * test component form processing
	 */
	// TODO (Eelco) This is an awful test. Why is 'mock page' (which isn't a
	// real mock, but just some arbitrary page) used rather than a page with
	// markup that corresponds to the component structure that is build up?
	// Components and markup go together in Wicket, period.
	@Test
	public void formProcessing()
	{
		// setup some values we will use for testing as well as a test model
		final String radio1 = "radio1-selection";
		// object used to test compound property model
		MockModelObject modelObject = new MockModelObject();

		// object used to test regular model
		Model<String> model = new Model<String>();

		// set up necessary objects to emulate a form submission

		// this could have been any page it seems. see comment at method
		MockPage page = new MockPage();

		// create component hierarchy

		final Form<MockModelObject> form = new Form<MockModelObject>("form",
			new CompoundPropertyModel<MockModelObject>(modelObject))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getMarkupId()
			{
				// hack for the fact that this test doesn't relate to any markup
				return "foo";
			}
		};

		final RadioGroup<String> group = new RadioGroup<String>("prop1");

		final WebMarkupContainer container = new WebMarkupContainer("container");

		final Radio<String> choice1 = new Radio<String>("radio1", new Model<String>(radio1));
		final Radio<String> choice2 = new Radio<String>("prop2");

		final RadioGroup<String> group2 = new RadioGroup<String>("group2", model);

		final Radio<String> choice3 = new Radio<String>("radio3", new Model<String>(radio1));

		page.add(form);
		form.add(group);
		group.add(container);
		container.add(choice1);
		group.add(choice2);
		form.add(group2);
		group2.add(choice3);

		// test mock form submissions

		modelObject.setProp1(radio1);

		form.onFormSubmitted();
		assertTrue("group: running with nothing selected - model must be set to null",
			modelObject.getProp1() == null);
		assertTrue("group2: running with nothing selected - model must be set to null",
			model.getObject() == null);

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(group.getInputName(), choice1.getValue());
		tester.getRequest()
			.getPostParameters()
			.setParameterValue(group2.getInputName(), choice3.getValue());

		tester.applyRequest();

		form.onFormSubmitted();
		assertEquals("group: running with choice1 selected - model must be set to value of radio1",
			modelObject.getProp1(), choice1.getDefaultModelObject());
		assertEquals(
			"group2: running with choice3 selected - model must be set to value of radio1",
			model.getObject(), choice3.getDefaultModelObject());

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(group.getInputName(), choice2.getValue());
		tester.applyRequest();
		form.onFormSubmitted();
		assertEquals("group: running with choice2 selected - model must be set to value of radio2",
			modelObject.getProp1(), choice2.getDefaultModelObject());

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(group2.getInputName(), choice1.getValue());
		tester.applyRequest();
		try
		{
			form.onFormSubmitted();
			fail("group2: ran with an invalid choice selected but did not fail");
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
