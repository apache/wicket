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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.WicketTestCase;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.CompoundPropertyModel;
import wicket.model.Model;
import wicket.protocol.http.MockPage;

/**
 * @author jcompagner
 */
/**
 * Test for RadioGroup and Radio components
 * 
 * @author igor
 * 
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
	 * @author igor
	 * 
	 */
	public static class MockModelObject implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private Set prop1 = new HashSet();
		private String prop2;

		/**
		 * @return prop1
		 */
		public Set getProp1()
		{
			return prop1;
		}

		/**
		 * @param prop1
		 */
		public void setProp1(Set prop1)
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
		final String check1 = "check1-selection";
		final String check2 = "check2-selection";

		MockModelObject modelObject = new MockModelObject();
		modelObject.setProp2(check2);

		// test model constructors
		List list = new ArrayList();
		Model model = new Model((Serializable)list);

		final CheckGroup group2 = new CheckGroup("group2", model);
		assertTrue(group2.getModelObject() == list);

		final CheckGroup group3 = new CheckGroup("group3", list);
		assertTrue(group3.getModelObject() == list);


		// set up necessary objects to emulate a form submission

		RequestCycle cycle = application.createRequestCycle();

		MockPage page = new MockPage();

		// create component hierarchy

		final Form form = new Form("form", new CompoundPropertyModel(modelObject));

		final CheckGroup group = new CheckGroup("prop1");

		final WebMarkupContainer container = new WebMarkupContainer("container");

		final Check choice1 = new Check("check1", new Model(check1));
		final Check choice2 = new Check("prop2");

		page.add(form);
		form.add(group);
		group.add(container);
		container.add(choice1);
		group.add(choice2);

		// test mock form submissions

		modelObject.getProp1().add(check1);

		form.onFormSubmitted();
		assertTrue("running with nothing selected - model must be empty", modelObject.getProp1()
				.size() == 0);

		application.getServletRequest().setParameter(group.getInputName(),
				String.valueOf(choice1.getValue()));
		form.onFormSubmitted();
		assertTrue("running with choice1 selected - model must only contain value of check1",
				modelObject.getProp1().size() == 1 && modelObject.getProp1().contains(check1));

		application.getServletRequest().setParameter(group.getInputName(),
				String.valueOf(choice2.getValue()));
		form.onFormSubmitted();
		assertTrue("running with choice2 selected - model must only contain value of check2",
				modelObject.getProp1().size() == 1 && modelObject.getProp1().contains(check2));

		// throw in some nulls into the request param to make sure they are
		// ignored
		application.getServletRequest().getParameterMap().put(
				group.getInputName(),
				new String[] { null, String.valueOf(choice1.getValue()), null,
						String.valueOf(choice2.getValue()) });
		form.onFormSubmitted();
		assertTrue(
				"running with choice1 and choice2 selected - model must only contain values of check1 and check2",
				modelObject.getProp1().size() == 2 && modelObject.getProp1().contains(check2)
						&& modelObject.getProp1().contains(check1));

		application.getServletRequest().getParameterMap().put(group.getInputName(),
				new String[] { "some weird choice uuid to test error" });
		try
		{
			form.onFormSubmitted();
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
