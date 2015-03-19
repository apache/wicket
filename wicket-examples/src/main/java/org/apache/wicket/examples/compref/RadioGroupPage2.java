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

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;


/**
 * RadioGroup and Radio components example page
 * 
 * @author ivaynberg
 */
public class RadioGroupPage2 extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public RadioGroupPage2()
	{

		final RadioGroup<Person> group = new RadioGroup<>("group", new Model<Person>());
		final RadioGroup<Person> group2 = new RadioGroup<>("group2", new Model<Person>());
		Form<?> form = new Form<Void>("form")
		{
			@Override
			protected void onSubmit()
			{
				info("selection group1: " + group.getDefaultModelObjectAsString());
				info("selection group2: " + group2.getDefaultModelObjectAsString());
			}
		};

		add(form);
		form.add(group);
		group.add(group2);

		ListView<Person> persons = new ListView<Person>("persons",
			ComponentReferenceApplication.getPersons())
		{

			@Override
			protected void populateItem(ListItem<Person> item)
			{
				item.add(new Radio<>("radio", item.getModel(), group));
				item.add(new Radio<>("radio2", item.getModel(), group2));
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
