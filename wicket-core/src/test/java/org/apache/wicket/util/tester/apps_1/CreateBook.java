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
package org.apache.wicket.util.tester.apps_1;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

/**
 * 
 * @author Juergen Donnerstag
 */
public class CreateBook extends WebPage
{
	private static final long serialVersionUID = 1L;

	private final Book book = new Book(null, null);

	/**
	 * 
	 */
	public CreateBook()
	{
		add(new CreateForm("createForm"));
	}

	/**
	 * 
	 * @author Juergen Donnerstag
	 */
	public class CreateForm extends Form<Void>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * 
		 * @param id
		 */
        CreateForm(String id)
		{
			super(id);

			// label model here comes from java
			add(new RequiredTextField<String>("id", new PropertyModel<String>(book, "id")).setLabel(new Model<String>(
				"id")));
			// label model here comes from CreateBook.properties
			add(new RequiredTextField<String>("name", new PropertyModel<String>(book, "name")));
		}

		/**
		 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		public void onSubmit()
		{
			Page page = new SuccessPage();
			page.info(getString("book.save.success", new Model<Book>(book)));
			setResponsePage(page);
		}
	}
}
