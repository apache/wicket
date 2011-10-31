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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.protocol.http.MockPage;
import org.junit.Test;


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
	 * mock model object with an embedded property used to test compound property model
	 * 
	 * @author igor
	 * 
	 */
	public static class MockModelObject implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private Set<Object> prop1 = new HashSet<Object>();
		private String prop2;

		/**
		 * @return prop1
		 */
		public Set<Object> getProp1()
		{
			return prop1;
		}

		/**
		 * @param prop1
		 */
		public void setProp1(Set<Object> prop1)
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
		final String check1 = "check1-selection";
		final String check2 = "check2-selection";

		MockModelObject modelObject = new MockModelObject();
		modelObject.setProp2(check2);

		// test model constructors
		List<Object> list = new ArrayList<Object>();
		CollectionModel<Object> model = new CollectionModel<Object>(list);

		final CheckGroup<Object> group2 = new CheckGroup<Object>("group2", model);
		assertTrue(group2.getDefaultModelObject() == list);

		final CheckGroup<Object> group3 = new CheckGroup<Object>("group3", list);
		assertTrue(group3.getDefaultModelObject() == list);


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

		final CheckGroup<Object> group = new CheckGroup<Object>("prop1");

		final WebMarkupContainer container = new WebMarkupContainer("container");

		final Check<Serializable> choice1 = new Check<Serializable>("check1",
			new Model<Serializable>(check1));
		final Check<String> choice2 = new Check<String>("prop2");

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

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(group.getInputName(), String.valueOf(choice1.getValue()));
		tester.applyRequest();
		form.onFormSubmitted();
		assertTrue("running with choice1 selected - model must only contain value of check1",
			modelObject.getProp1().size() == 1 && modelObject.getProp1().contains(check1));

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(group.getInputName(), String.valueOf(choice2.getValue()));
		tester.applyRequest();
		form.onFormSubmitted();
		assertTrue("running with choice2 selected - model must only contain value of check2",
			modelObject.getProp1().size() == 1 && modelObject.getProp1().contains(check2));

		// throw in some nulls into the request param to make sure they are
		// ignored
		tester.getRequest().getPostParameters().addParameterValue(group.getInputName(), null);
		tester.getRequest()
			.getPostParameters()
			.addParameterValue(group.getInputName(), String.valueOf(choice1.getValue()));
		tester.getRequest()
			.getPostParameters()
			.addParameterValue(group.getInputName(), String.valueOf(choice2.getValue()));
		tester.applyRequest();
		form.onFormSubmitted();

		assertTrue(
			"running with choice1 and choice2 selected - model must only contain values of check1 and check2",
			modelObject.getProp1().size() == 2 && modelObject.getProp1().contains(check2) &&
				modelObject.getProp1().contains(check1));

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(group.getInputName(), "some weird choice uuid to test error");
		tester.applyRequest();
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
	@Test
	public void rendering() throws Exception
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
			if (!e.getMessage().contains(
				"Check component [4:form:check2] cannot find its parent CheckGroup"))
			{
				fail("failed with wrong exception");
			}

		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void disabledCheckGroup() throws Exception
	{
		executeTest(CheckGroupDisabledTestPage.class, "CheckGroupDisabledTestPage_expected.html");
	}
}
