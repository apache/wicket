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

import java.util.ArrayList;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;


/**
 * CheckGroup and Check components example page
 * 
 * @author ivaynberg
 */
public class CheckGroupPage2 extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public CheckGroupPage2()
	{
		final CheckGroup<Person> group = new CheckGroup<>("group", new ArrayList<Person>());
		final CheckGroup<Person> group2 = new CheckGroup<>("group2", new ArrayList<Person>());
		Form<?> form = new Form<Void>("form")
		{
			@Override
			protected void onSubmit()
			{
				info("selection 1 person(s): " + group.getDefaultModelObjectAsString());
				info("selection 2 person(s): " + group2.getDefaultModelObjectAsString());
			}
		};

		add(form);
		form.add(group);
		group.add(group2);

		group2.add(new CheckGroupSelector("groupselector", group));
		group2.add(new CheckGroupSelector("groupselector2", group2));
		ListView<Person> persons = new ListView<Person>("persons",
			ComponentReferenceApplication.getPersons())
		{

			@Override
			protected void populateItem(ListItem<Person> item)
			{
				item.add(new Check<>("checkbox", item.getModel(), group));
				item.add(new Check<>("checkbox2", item.getModel(), group2));
				item.add(new Label("name",
					new PropertyModel<>(item.getDefaultModel(), "name")));
				item.add(new Label("lastName", new PropertyModel<String>(item.getDefaultModel(),
					"lastName")));
			}

		};

		group2.add(persons);

		add(new FeedbackPanel("feedback"));
	}
}
