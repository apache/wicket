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
package wicket.util.tester;

import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;

/**
 * 
 * @author ingram
 */
public class FormTester
{
	private boolean closed = false;

	private Form workingForm;

	private final WicketTester mockWebApp;

	private final String path;

	/**
	 * @see WicketTester#newFormTester(String)
	 * 
	 * @param path
	 * @param workingForm
	 * @param mockWebApp
	 */
	FormTester(final String path, final Form workingForm, final WicketTester mockWebApp)
	{
		this.path = path;
		this.workingForm = workingForm;
		this.mockWebApp = mockWebApp;
		this.mockWebApp.setupRequestAndResponse();
	}

	/**
	 * 
	 * @param formComponentId
	 * @param value
	 */
	public void setValue(final String formComponentId, final String value)
	{
		checkClosed();
		
		FormComponent formComponent = (FormComponent)workingForm.get(formComponentId);
		mockWebApp.getServletRequest().setParameter(formComponent.getInputName(), value);
	}

	/**
	 * 
	 */
	public void submit()
	{
		checkClosed();
		try
		{
			mockWebApp.getServletRequest().setRequestToComponent(workingForm);
			mockWebApp.processRequestCycle();
		}
		finally
		{
			closed = true;
		}
	}

	/**
	 * 
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