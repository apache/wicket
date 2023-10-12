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
package org.apache.wicket.core.util.tester;

import java.io.Serializable;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 * Mock page for testing basic FormTester functionality.
 * 
 * @author frankbille
 */
public class MockFormPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Domain object
	 */
	public class MockDomainObject implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String text;
		private boolean checkbox;
		private String textarea;

		/**
		 * @return checkbox
		 */
		public boolean isCheckbox()
		{
			return checkbox;
		}

		/**
		 * @param checkbox
		 */
		public void setCheckbox(boolean checkbox)
		{
			this.checkbox = checkbox;
		}

		/**
		 * @return text
		 */
		public String getText()
		{
			return text;
		}

		/**
		 * @param text
		 */
		public void setText(String text)
		{
			this.text = text;
		}

		/**
		 * @return textarea
		 */
		public String getTextarea()
		{
			return textarea;
		}

		/**
		 * @param textarea
		 */
		public void setTextarea(String textarea)
		{
			this.textarea = textarea;
		}
	}

	private final MockDomainObject domainObject;

	/**
	 * Construct.
	 */
	public MockFormPage()
	{
		domainObject = new MockDomainObject();
		Form<MockDomainObject> form = new Form<MockDomainObject>("form",
			new CompoundPropertyModel<MockDomainObject>(domainObject));
		add(form);

		form.add(new TextField<String>("text"));
		form.add(new CheckBox("checkbox"));
		form.add(new TextArea<String>("textarea"));
		form.add(new Button("submit"));
	}

	/**
	 * @return domainObject
	 */
	public MockDomainObject getDomainObject()
	{
		return domainObject;
	}
}
