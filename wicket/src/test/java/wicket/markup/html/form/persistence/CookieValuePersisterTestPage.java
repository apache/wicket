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
package wicket.markup.html.form.persistence;

import wicket.MarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.Model;


/**
 * Mock page for testing.
 * 
 * @author Chris Turner
 */
public class CookieValuePersisterTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * 
	 */
	public CookieValuePersisterTestPage()
	{
		// Create and add feedback panel to page
		new FeedbackPanel(this, "feedback");
		new TestForm(this, "form");
	}

	/**
	 * 
	 * @author Juergen Donnerstag
	 */
	public final class TestForm extends Form
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 * 
		 * @param parent
		 * @param id
		 */
		public TestForm(MarkupContainer parent, final String id)
		{
			super(parent, id);

			new TextField<String>(this, "input", new Model<String>("test �������"));
		}

		/**
		 * Dummy
		 */
		@Override
		public final void onSubmit()
		{
		}
	}
}