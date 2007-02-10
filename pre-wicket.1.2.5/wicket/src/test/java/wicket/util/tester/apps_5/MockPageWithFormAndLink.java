/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.util.tester.apps_5;

import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.model.CompoundPropertyModel;

/**
 * Contains a form with a textfield on it. Also contains markup for a link, but
 * you must add the link yourself
 * 
 * @author Frank Bille
 */
public class MockPageWithFormAndLink extends WebPage
{
	/**
	 * @author Frank Bille
	 */
	public static class MockPojo
	{
		private String name;

		/**
		 * @return name
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * @param name
		 */
		public void setName(String name)
		{
			this.name = name;
		}
	}

	private static final long serialVersionUID = 1L;
	private Form form;

	private TextField nameField;


	/**
	 * Construct.
	 * 
	 * @param mockPojo
	 */
	public MockPageWithFormAndLink(MockPojo mockPojo)
	{
		form = new Form("form", new CompoundPropertyModel(mockPojo));
		add(form);
		nameField = new TextField("name");
		form.add(nameField);
	}


	/**
	 * @return the form component
	 */
	public Form getForm()
	{
		return form;
	}


	/**
	 * @return the name text field
	 */
	public TextField getNameField()
	{
		return nameField;
	}


	/**
	 * @param form
	 */
	public void setForm(Form form)
	{
		this.form = form;
	}


	/**
	 * @param name
	 */
	public void setNameField(TextField name)
	{
		this.nameField = name;
	}
}
