/*
 * $Id$ $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package session;

import wicket.Component;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Home page of the wizard example.
 * 
 * @author Eelco Hillenius
 */
public class Home extends WebPage
{
	/**
	 * Constructor.
	 */
	public Home()
	{
		add(new MyForm("form"));
	}

	private final class MyForm extends Form
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public MyForm(String id)
		{
			super(id);
			IModel model = new Model()
			{
				public Object getObject(Component component)
				{
					return ((TestSession)getSession()).getName();
				}

				public void setObject(Component component, Object object)
				{
					((TestSession)getSession()).setName(String.valueOf(object));
				}
			};
			add(new TextField("name", model));
		}
	}
}