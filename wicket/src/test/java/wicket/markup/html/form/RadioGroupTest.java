/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form;

import java.io.Serializable;

import wicket.WicketRuntimeException;
import wicket.WicketTestCase;

/**
 * Test for RadioGroup and Radio components
 * 
 * @author igor
 * @author jcompagner
 */
public class RadioGroupTest extends WicketTestCase
{
	/**
	 * @param name
	 */
	public RadioGroupTest(String name)
	{
		super(name);
	}

	/**
	 * mock model object with an embedded property used to test compound
	 * property model
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
	public void testFormProcessing()
	{
		// setup some values we will use for testing as well as a test model
//		 FIXME backported from 1.2.1 needs to be reworked for 2.0
		/*
		final String radio1 = "radio1-selection";
		// object used to test compound property model
		MockModelObject modelObject = new MockModelObject();

		// object used to test regular model
		Model model = new Model();

		// set up necessary objects to emulate a form submission

		application.createRequestCycle();
		MockPage page = new MockPage();

		// create component hierarchy

		final Form form = new Form("form", new CompoundPropertyModel(modelObject));

		final RadioGroup group = new RadioGroup("prop1");

		final WebMarkupContainer container = new WebMarkupContainer("container");

		final Radio choice1 = new Radio("radio1", new Model(radio1));
		final Radio choice2 = new Radio("prop2");

		final RadioGroup group2 = new RadioGroup("group2", model);

		final Radio choice3 = new Radio("radio3", new Model(radio1));

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
		assertTrue("group: running with nothing selected - model must be set to null", modelObject
				.getProp1() == null);
		assertTrue("group2: running with nothing selected - model must be set to null", model
				.getObject(null) == null);

		application.getServletRequest().setParameter(group.getInputName(), choice1.getPath().substring(group.getPath().length() + 1));
		application.getServletRequest().setParameter(group2.getInputName(), choice3.getPath().substring(group2.getPath().length() + 1));
		form.onFormSubmitted();
		assertEquals("group: running with choice1 selected - model must be set to value of radio1",
				modelObject.getProp1(), choice1.getModelObject());
		assertEquals(
				"group2: running with choice3 selected - model must be set to value of radio1",
				model.getObject(null), choice3.getModelObject());

		application.getServletRequest().setParameter(group.getInputName(), choice2.getPath().substring(group.getPath().length() + 1));
		form.onFormSubmitted();
		assertEquals("group: running with choice2 selected - model must be set to value of radio2",
				modelObject.getProp1(), choice2.getModelObject());

		application.getServletRequest().setParameter(group2.getInputName(), choice1.getPath().substring(group.getPath().length() + 1));
		try
		{
			form.onFormSubmitted();
			fail("group2: ran with an invalid choice selected but did not fail");
		}
		catch (WicketRuntimeException e)
		{

		}
*/
	}

	/**
	 * test component rendering
	 * 
	 * @throws Exception
	 */
	public void testRendering() throws Exception
	{
		executeTest(RadioGroupTestPage1.class, "RadioGroupTestPage1_expected.html");
		try
		{
			executeTest(RadioGroupTestPage2.class, "");
			fail("the rendering of page above must fail because radio2 component is not under any group");
		}
		catch (WicketRuntimeException e)
		{
			if (e.getMessage().indexOf(
					"Radio component [1:form:radio2] cannot find its parent RadioGroup") < 0)
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
	public void testRadioGroupTestPage3() throws Exception
	{
		// this test fails. You can make the test pass by closing the input tags
		// this was not the case in beta1
		executeTest(RadioGroupTestPage3.class, "RadioGroupTestPage3_expected.html");
	}	
	
	/**
	 * @throws Exception
	 */
	public void testDisabledRadioGroup()  throws Exception
	{
		executeTest(RadioGroupDisabledTestPage.class, "RadioGroupDisabledTestPage_expected.html");
	}	
}
