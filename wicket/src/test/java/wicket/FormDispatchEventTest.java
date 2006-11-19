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
package wicket;

import java.util.ArrayList;

import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.IOnChangeListener;
import wicket.model.Model;
import wicket.protocol.http.MockPage;

/**
 * @author jcompagner
 */
public class FormDispatchEventTest extends WicketTestCase
{
	private final class MyForm extends Form
	{
		private static final long serialVersionUID = 1L;

		private MyForm(String id)
		{
			super(id);
		}

		protected void onSubmit()
		{
			submit= true;
		}
		
		/**
		 * @return The hidden field id of the form
		 */
		public String getHiddenField()
		{
			return getHiddenFieldId();
		}
	}

	private boolean selection;
	private boolean submit;

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
		MyForm form = new MyForm("form");

		DropDownChoice dropDown = new DropDownChoice("dropdown",new Model(), new ArrayList())
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
		cycle.getSession().update();

		form.onFormSubmitted();
		assertTrue("form should should set value ", submit);

		application.getServletRequest().setParameter(form.getHiddenField(),
				dropDown.urlFor(IOnChangeListener.INTERFACE).toString());

		form.onFormSubmitted();
		assertTrue("Selection should be called", selection);
	}
}
