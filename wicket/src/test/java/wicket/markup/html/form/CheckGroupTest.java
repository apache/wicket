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
package wicket.markup.html.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import wicket.WicketRuntimeException;
import wicket.WicketTestCase;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.CompoundPropertyModel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Test for RadioGroup and Radio components
 * 
 * @author igor
 * @author jcompagner
 */
public class CheckGroupTest extends WicketTestCase
{
	/**
	 * @param name
	 */
	public CheckGroupTest(String name)
	{
		super(name);
	}

	/**
	 * mock model object with an embedded property used to test compound
	 * property model
	 * 
	 */
	public static class MockModelObject implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private Set<String> prop1 = new HashSet<String>();
		private String prop2;

		/**
		 * @return prop1
		 */
		public Set<String> getProp1()
		{
			return prop1;
		}

		/**
		 * @param prop1
		 */
		public void setProp1(Set<String> prop1)
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
		// THIS NEEDS TO BE REWRITTEN BASED ON 1_2 VERSION

		MockModelObject modelObject = new MockModelObject();
		MockCheckGroupTestFormPage page = new MockCheckGroupTestFormPage();

		// create component hierarchy

		final Form<MockModelObject> form = new Form<MockModelObject>(page, "form",
				new CompoundPropertyModel<MockModelObject>(modelObject));

		final CheckGroup group = new CheckGroup(form, "prop1");
		final WebMarkupContainer container = new WebMarkupContainer(group, "container");

		// setup some values we will use for testing as well as a test model
		final String check1 = "check1-selection";
		final String check2 = "check2-selection";

		// test model constructors
		List<String> list = new ArrayList<String>();
		IModel<Collection<String>> model = new Model<Collection<String>>(list);
		final CheckGroup group2 = new CheckGroup<String>(group, "group2", model);
		assertTrue(group2.getModelObject() == list);

		final CheckGroup group3 = new CheckGroup<String>(form, "group3", list);
		assertTrue(group3.getModelObject() == list);

		// set up necessary objects to emulate a form submission
		tester.createRequestCycle();

		Check choice1 = new Check<String>(container, "check1", new Model<String>(check1));
		Check choice2 = new Check(group, "prop2");

		modelObject.setProp2(check2);

		// test mock form submissions

		modelObject.getProp1().add(check1);

		form.onFormSubmitted();
		assertTrue("running with nothing selected - model must be empty", modelObject.getProp1()
				.size() == 0);

		tester.getServletRequest().setParameter(group.getInputName(), choice1.getValue());
		form.onFormSubmitted();
		assertTrue("running with choice1 selected - model must only contain value of check1",
				modelObject.getProp1().size() == 1 && modelObject.getProp1().contains(check1));

		tester.getServletRequest().setParameter(group.getInputName(), choice2.getValue());
		form.onFormSubmitted();
		assertTrue("running with choice2 selected - model must only contain value of check2",
				modelObject.getProp1().size() == 1 && modelObject.getProp1().contains(check2));

		// throw in some nulls into the request param to make sure they are
		// ignored
		tester.getServletRequest().getParameterMap().put(group.getInputName(),
				new String[] { null, choice1.getValue(), null, choice2.getValue() });
		form.onFormSubmitted();
		assertTrue(
				"running with choice1 and choice2 selected - model must only contain values of check1 and check2",
				modelObject.getProp1().size() == 2 && modelObject.getProp1().contains(check2)
						&& modelObject.getProp1().contains(check1));

		tester.getServletRequest().getParameterMap().put(group.getInputName(),
				new String[] { "some weird path to test error" });
		try
		{
			form.onFormSubmitted();
			fail("running with an invalid choice value in the request param, should fail");
		}
		catch (WicketRuntimeException e)
		{
			;
		}
	}

	/**
	 * test component rendering
	 * 
	 * @throws Exception
	 */
	public void testRendering() throws Exception
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
			if (e.getMessage().indexOf(
					"Check component [4:form:check2] cannot find its parent CheckGroup") < 0)
			{
				fail("failed with wrong exception");
			}
		}
	}

	/**
	 * @throws Exception
	 */
	public void testDisabledCheckGroup() throws Exception
	{
		executeTest(CheckGroupDisabledTestPage.class, "CheckGroupDisabledTestPage_expected.html");
	}


}
