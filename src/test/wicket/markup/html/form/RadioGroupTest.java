/*
 * $Id$
 * $Revision$ $Date$
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

import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.WicketTestCase;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.CompoundPropertyModel;
import wicket.model.Model;
import wicket.protocol.http.MockPage;

/**
 * Test for RadioGroup and Radio components
 * @author igor
 *
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


	}

	/**
	 * test component form processing
	 */
	public void testFormProcessing()
	{
		// setup some values we will use for testing as well as a test model
		final String radio1 = "radio1-selection";
		final String radio2 = "radio2-selection";

		MockModelObject modelObject = new MockModelObject();
		
		// set up necessary objects to emulate a form submission
		
		RequestCycle cycle = application.createRequestCycle();

		MockPage page = new MockPage(new PageParameters());

		page.getSession().setRequestCycle(cycle);

		// create component hierarchy
		
		final Form form = new Form("form", new CompoundPropertyModel(modelObject));
		page.add(form);

		final RadioGroup group = new RadioGroup("prop1");
		form.add(group);

		final WebMarkupContainer container = new WebMarkupContainer("container");
		group.add(container);

		final Radio choice1 = new Radio("radio1", new Model(radio1));
		final Radio choice2 = new Radio("radio2", new Model(radio2));

		container.add(choice1);
		group.add(choice2);

		// test mock form submissions

		modelObject.setProp1(radio1);
		
		form.onFormSubmitted();
		assertTrue("running with nothing selected - model must be set to null", modelObject
				.getProp1() == null);

		application.getServletRequest().setParameter(group.getInputName(), choice1.getPath());
		form.onFormSubmitted();
		assertEquals("running with choice1 selected - model must be set to value of radio1", modelObject
				.getProp1(), choice1.getModelObject());

		application.getServletRequest().setParameter(group.getInputName(), choice2.getPath());
		form.onFormSubmitted();
		assertEquals("running with choice2 selected - model must be set to value of radio2", modelObject
				.getProp1(), choice2.getModelObject());
	}

	/**
	 * test component rendering
	 * @throws Exception
	 */
	public void testRendering() throws Exception
	{
		executeTest(RadioGroupTestPage1.class, "RadioGroupTestPage1_expected.html");
	}


}
