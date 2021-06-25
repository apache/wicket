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
package org.apache.wicket.core.util.tester.apps_5;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 * Contains a form with a textfield on it. Also contains markup for a link, but you must add the
 * link yourself
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
	private Form<MockPojo> form;

	private TextField<String> nameField;


	/**
	 * Construct.
	 * 
	 * @param mockPojo
	 */
	public MockPageWithFormAndLink(MockPojo mockPojo)
	{
		form = new Form<MockPojo>("form", new CompoundPropertyModel<MockPojo>(mockPojo));
		add(form);
		nameField = new TextField<String>("name");
		form.add(nameField);
	}


	/**
	 * @return the form component
	 */
	public Form<MockPojo> getForm()
	{
		return form;
	}


	/**
	 * @return the name text field
	 */
	public TextField<String> getNameField()
	{
		return nameField;
	}


	/**
	 * @param form
	 */
	public void setForm(Form<MockPojo> form)
	{
		this.form = form;
	}


	/**
	 * @param name
	 */
	public void setNameField(TextField<String> name)
	{
		nameField = name;
	}
}
