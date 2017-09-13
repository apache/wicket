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
package org.apache.wicket.examples.compref;


import java.util.List;

import org.apache.wicket.markup.MarkupType;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


/**
 * Look ma, you can use plain XML too with Wicket.
 * 
 * @author Eelco Hillenius
 */
public class XmlPage extends WebPage
{
	/**
	 * Constructor
	 */
	public XmlPage()
	{
		add(new PersonsListView("persons", ComponentReferenceApplication.getPersons()));
	}

	@Override
	public MarkupType getMarkupType()
	{
		return new MarkupType("xml", "text/xml");
	}

	/** list view for rendering person objects. */
	private static final class PersonsListView extends ListView<Person>
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 * @param list
		 *            the model
		 */
		public PersonsListView(String id, List<Person> list)
		{
			super(id, list);
		}

		@Override
		protected void populateItem(ListItem<Person> item)
		{
			Person person = item.getModelObject();
			item.add(new Label("firstName", person.getName()));
			item.add(new Label("lastName", person.getLastName()));
		}
	}
}
