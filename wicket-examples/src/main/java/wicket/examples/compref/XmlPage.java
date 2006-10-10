/*
 * $Id: XmlPage.java 5896 2006-05-26 18:33:19 +0000 (Fri, 26 May 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-05-26 18:33:19 +0000 (Fri, 26 May
 * 2006) $
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
package wicket.examples.compref;


import java.util.List;

import wicket.MarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;

/**
 * Look ma, you can use plain XML too with Wicket.
 * 
 * @author Eelco Hillenius
 */
public class XmlPage extends WebPage
{
	/** list view for rendering person objects. */
	private static final class PersonsListView extends ListView<Person>
	{
		/**
		 * Construct.
		 * 
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 *            component id
		 * @param list
		 *            the model
		 */
		public PersonsListView(MarkupContainer parent, final String id, List<Person> list)
		{
			super(parent, id, list);
		}

		@Override
		protected void populateItem(ListItem<Person> item)
		{
			Person person = item.getModelObject();
			new Label(item, "firstName", person.getName());
			new Label(item, "lastName", person.getLastName());
		}
	}

	/**
	 * Constructor
	 */
	public XmlPage()
	{
		new PersonsListView(this, "persons", ComponentReferenceApplication.getPersons());
	}

	/**
	 * @see wicket.MarkupContainer#getMarkupType()
	 */
	@Override
	public String getMarkupType()
	{
		return "xml";
	}
}
