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

/**
 * @author jcompagner
 */
public class FormDispatchEventTest extends WicketTestCase
{
	private final class MyForm extends Form
	{
		private static final long serialVersionUID = 1L;

		private MyForm(MarkupContainer parent, String id)
		{
			super(parent, id);
		}

		@Override
		protected void onSubmit()
		{
			submit = true;
		}

		/**
		 * @param name
		 * @return The hidden field id of the form
		 */
		public String getHiddenField(String name)
		{
			return getHiddenFieldId(name);
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
		MockPageWithFormAndDropdown page = new MockPageWithFormAndDropdown();
		MyForm form = new MyForm(page, "form");

		DropDownChoice<String> dropDown = new DropDownChoice<String>(form, "dropdown", new Model<String>(), new ArrayList<String>())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSelectionChanged(Object newSelection)
			{
				selection = true;
			}

			/**
			 * @see wicket.markup.html.form.DropDownChoice#wantOnSelectionChangedNotifications()
			 */
			@Override
			protected boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}
		};


		application.setupRequestAndResponse();
		RequestCycle cycle = application.createRequestCycle();

		page.urlFor(IRedirectListener.INTERFACE);
		cycle.getSession().touch(page);
		cycle.getSession().update();

		form.onFormSubmitted();
		assertTrue("form should should set value ", submit);

		application.getServletRequest().setParameter(
				form.getHiddenField(Form.HIDDEN_FIELD_FAKE_SUBMIT),
				dropDown.urlFor(IOnChangeListener.INTERFACE).toString());

		form.onFormSubmitted();
		assertTrue("Selection should be called", selection);
	}
}
