/*
 * $Id$ $Revision$ $Date$
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

import wicket.IRedirectListener;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.WicketTestCase;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.protocol.http.MockPage;

/**
 * @author jcompagner
 */
public class FormDispatchEventTest extends WicketTestCase
{
	private boolean selection;

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public FormDispatchEventTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testDropDownEvent() throws Exception
	{
		Form form = new Form("form")
		{
			private static final long serialVersionUID = 1L;

			protected void onSubmit()
			{
				assertTrue("on submit shouldn't be reached", true);
			}
		};

		DropDownChoice dropDown = new DropDownChoice("dropdown")
		{
			private static final long serialVersionUID = 1L;

			protected void onSelectionChanged(Object newSelection)
			{
				selection = true;
			}

			/**
			 * @see wicket.markup.html.form.DropDownChoice#wantOnSelectionChangedNotifications()
			 */
			protected boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}
		};


		form.add(dropDown);

		MockPage page = new MockPage();
		page.add(form);

		application.setupRequestAndResponse();
		RequestCycle cycle = application.createRequestCycle();

		page.urlFor(IRedirectListener.INTERFACE);
		cycle.getSession().touch(page);

		try
		{
			form.onFormSubmitted();
			assertTrue("form should throw an error", true);
		}
		catch (WicketRuntimeException ex)
		{
			// nothing to do
		}

		application.getServletRequest().setParameter(form.getHiddenFieldId(),
				dropDown.urlFor(IOnChangeListener.INTERFACE));

		form.onFormSubmitted();
		assertTrue("Selection should be called", selection);
	}
}
