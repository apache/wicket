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
public class RadioGroupPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public RadioGroupPage()
	{

		final RadioGroup<Person> group = new RadioGroup<>("group", new Model<Person>());
		Form<?> form = new Form("form")
		{
			@Override
			protected void onSubmit()
			{
				info("selected person: " + group.getDefaultModelObjectAsString());
			}
		};

		add(form);
		form.add(group);

		ListView<Person> persons = new ListView<Person>("persons",
			ComponentReferenceApplication.getPersons())
		{

			@Override
			protected void populateItem(ListItem<Person> item)
			{
				item.add(new Radio<>("radio", item.getModel()));
				item.add(new Label("name",
					new PropertyModel<>(item.getDefaultModel(), "name")));
				item.add(new Label("lastName", new PropertyModel<String>(item.getDefaultModel(),
					"lastName")));
			}

		};

		group.add(persons);

		add(new FeedbackPanel("feedback"));
	}

	@Override
	protected void explain()
	{
		String html = "<form wicket:id=\"form\">\n" + "<span wicket:id=\"group\">\n"
			+ "<tr wicket:id=\"persons\">\n"
			+ "<td><input type=\"radio\" wicket:id=\"radio\"/></td>\n"
			+ "<td><span wicket:id=\"name\">[this is where name will be]</span></td>\n"
			+ "<td><span wicket:id=\"lastName\">[this is where lastname will be]</span></td>\n"
			+ "</tr>\n" + "</span>" + "</form>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;Form f=new Form(\"form\");<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;add(f);<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;RadioGroup group=new RadioGroup(\"group\");<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(group);<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;ListView persons=new ListView(\"persons\", getPersons()) {<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;protected void populateItem(ListItem item) {<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;item.add(new Radio(\"radio\", item.getModel()));<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;item.add(new Label(\"name\", new PropertyModel(item.getModel(), \"name\")));<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;item.add(new Label(\"lastName\", new PropertyModel(item.getModel(), \"lastName\")));<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;};<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;group.add(persons);<br/>";
		add(new ExplainPanel(html, code));
	}
}
