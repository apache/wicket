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
import java.util.List;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.model.util.ListModel;


/**
 * Palette component example
 * 
 * @author ivaynberg
 */
public class PalettePage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public PalettePage()
	{
		List<Person> persons = ComponentReferenceApplication.getPersons();
		IChoiceRenderer<Person> renderer = new ChoiceRenderer<Person>("fullName", "fullName");

		final Palette<Person> palette = new Palette<Person>("palette", new ListModel<Person>(
			new ArrayList<Person>()), new CollectionModel<Person>(persons), renderer, 10, true);


		Form<Void> form = new Form<Void>("form")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit()
			{
				for (Person person : palette.getModelObject())
				{
					info("selected person: " + person);
				}
			}
		};

		add(form);
		form.add(palette);

		add(new FeedbackPanel("feedback"));
	}

	@Override
	protected void explain()
	{
		String html = "<form wicket:id=\"form\">\n" + "<span wicket:id=\"palette\">\n"
			+ "</span>\n</form>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;Form f=new Form(\"form\");<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;add(f);<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;List persons = ComponentReferenceApplication.getPersons();;<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;IChoiceRenderer renderer = new ChoiceRenderer(\"fullName\", \"fullName\");<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;final Palette palette = new Palette(\"palette\", new ListModel&lt;Person&gt;(new ArrayList&lt;Person&gt;()), new CollectionModel&lt;Person&gt;(<br/>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;persons), renderer, 10, true);<br/>";
		add(new ExplainPanel(html, code));
	}
}
