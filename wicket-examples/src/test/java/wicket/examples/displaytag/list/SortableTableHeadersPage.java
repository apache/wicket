/*
 * $Id: SortableTableHeadersPage.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24
 * May 2006) joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed,
 * 24 May 2006) $
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
package wicket.examples.displaytag.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import wicket.PageParameters;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.PropertyModel;


/**
 * Dummy page used for resource testing.
 */
public class SortableTableHeadersPage extends WebPage
{

	/**
	 * Construct.
	 * 
	 * @param parameters
	 *            page parameters.
	 */
	public SortableTableHeadersPage(final PageParameters parameters)
	{
		super();

		List <User>list = new ArrayList<User>();
		addUser(list, 1, "Name-aaa", "mail-1");
		addUser(list, 2, "Name-bbb", "mail-2");
		addUser(list, 3, "Name-ccc", "mail-3");
		addUser(list, 4, "Name-ddd", "mail-4");
		addUser(list, 5, "Name-eee", "mail-5");
		addUser(list, 6, "Name-aba", "mail-6");
		addUser(list, 7, "Name-bab", "mail-7");
		addUser(list, 8, "Name-dca", "mail-8");
		addUser(list, 9, "Name-eaa", "mail-9");

		ListView<User> table = new ListView<User>(this, "table", list)
		{
			@Override
			protected void populateItem(ListItem listItem)
			{
				User user = (User)listItem.getModelObject();
				new Label(listItem, "id", new PropertyModel(user, "id"));
				new Label(listItem, "name", new PropertyModel(user, "name"));
				new Label(listItem, "email", new PropertyModel(user, "email"));
			}
		};

		new SortableListViewHeaders<User>(this, "header", table)
		{
			/*
			 * If object does not support equals()
			 */
			@Override
			protected int compareTo(SortableListViewHeader header, Object o1, Object o2)
			{
				if (header.getId().equals("id"))
				{
					return ((User)o1).id - ((User)o2).id;
				}

				return super.compareTo(header, o1, o2);
			}

			/**
			 * Define how to do sorting
			 * 
			 * @see SortableListViewHeaders#getObjectToCompare(SortableListViewHeader,
			 *      java.lang.Object)
			 */
			@Override
			protected Comparable getObjectToCompare(final SortableListViewHeader header,
					final Object object)
			{
				final String name = header.getId();
				if (name.equals("name"))
				{
					return ((User)object).name;
				}
				if (name.equals("email"))
				{
					return ((User)object).email;
				}

				return "";
			}
		};

	}

	private void addUser(List<User> data, int id, String name, String email)
	{
		User user = new User();
		user.setId(id);
		user.setName(name);
		user.setEmail(email);
		data.add(user);
	}

	private class User implements Serializable
	{
		private int id;
		private String name;
		private String email;

		/**
		 * Gets email.
		 * 
		 * @return email
		 */
		public final String getEmail()
		{
			return email;
		}

		/**
		 * Sets email.
		 * 
		 * @param email
		 *            email
		 */
		public final void setEmail(String email)
		{
			this.email = email;
		}

		/**
		 * Gets id.
		 * 
		 * @return id
		 */
		public final int getId()
		{
			return id;
		}

		/**
		 * Sets id.
		 * 
		 * @param id
		 *            id
		 */
		public final void setId(int id)
		{
			this.id = id;
		}

		/**
		 * Gets name.
		 * 
		 * @return name
		 */
		public final String getName()
		{
			return name;
		}

		/**
		 * Sets name.
		 * 
		 * @param name
		 *            name
		 */
		public final void setName(String name)
		{
			this.name = name;
		}
	}
}
