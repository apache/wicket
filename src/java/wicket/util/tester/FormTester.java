/*
 * $Id$ $Revision:
 * 1.1 $ $Date$
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
package wicket.util.tester;

import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;

/**
 * A helper for testing validaiton and submission of Form component.
 * 
 * @author Ingram Chen
 */
public class FormTester
{
	/** An instance of FormTester can only be used once. Create a new instance of each test */
	private boolean closed = false;

	/** form component to be test */
	private Form workingForm;

	/** wicketTester that create FormTester */
	private final WicketTester wicketTester;

	/** path to form component */
	private final String path;

	/**
	 * @see WicketTester#newFormTester(String)
	 * 
	 * @param path
	 *            path to form component
	 * @param workingForm
	 *            form component to be test
	 * @param wicketTester
	 *            wicketTester that create FormTester
	 * @param fillBlankString
	 *            specify whether fill all child FormComponents with blank
	 *            String
	 */
	FormTester(final String path, final Form workingForm, final WicketTester wicketTester,
			boolean fillBlankString)
	{
		this.path = path;
		this.workingForm = workingForm;
		this.wicketTester = wicketTester;
		this.wicketTester.setupRequestAndResponse();

		if (fillBlankString)
		{
			workingForm.visitFormComponents(new FormComponent.IVisitor()
			{
				public void formComponent(FormComponent formComponent)
				{
					if (formComponent.isEnabled())
					{
						setValue(formComponent.getInputName(), "");
					}
				}
			});
		}

	}

	/**
	 * simulate filling a field of a Form.
	 * 
	 * @param formComponentId
	 *            relative path (from form) to formComponent
	 * @param value
	 *            field value of form.
	 */
	public void setValue(final String formComponentId, final String value)
	{
		checkClosed();

		FormComponent formComponent = (FormComponent)workingForm.get(formComponentId);
		wicketTester.getServletRequest().setParameter(formComponent.getInputName(), value);
	}

	/**
	 * submit the form. note that submit() can be executed only once.
	 */
	public void submit()
	{
		checkClosed();
		try
		{
			wicketTester.getServletRequest().setRequestToComponent(workingForm);
			wicketTester.processRequestCycle();
		}
		finally
		{
			closed = true;
		}
	}

	/**
	 * FormTester must only be used once. Create a new instance of FormTester
	 * for each test.
	 */
	private void checkClosed()
	{
		if (closed)
		{
			throw new IllegalStateException("'" + path
					+ "' already sumbitted. Note that FormTester "
					+ "is allowed to submit only once");
		}
	}
}