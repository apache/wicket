/*
 * $Id$ $Revision:
 * 1.51 $ $Date$
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
package wicket.util.tester.apps_1;

import wicket.Page;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.RequiredTextField;
import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * 
 * @author Juergen Donnerstag
 */
public class CreateBook extends WebPage
{
	private static final long serialVersionUID = 1L;

	private Book book = new Book(null, null);

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
	public class CreateForm extends Form
	{
		private static final long serialVersionUID = 1L;

		/**
		 * 
		 * @param id
		 */
		public CreateForm(String id)
		{
			super(id);

			add(new RequiredTextField("id", new PropertyModel(book, "id")));
			add(new RequiredTextField("name", new PropertyModel(book, "name")));
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		public void onSubmit()
		{
			try
			{
				Page page = new SuccessPage();
				page.info(getString("book.save.success", new Model(book)));
				setResponsePage(page);
			}
			finally
			{
				getSession().remove(getPage());
			}
		}
	}
}
