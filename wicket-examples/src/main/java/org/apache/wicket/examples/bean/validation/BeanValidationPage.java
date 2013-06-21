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
package org.apache.wicket.examples.bean.validation;

import java.util.Date;

import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;

public class BeanValidationPage extends WicketExamplePage
{

	Person person = new Person();

	public BeanValidationPage()
	{
		add(new FeedbackPanel("feedback"));

		Form<?> form = new Form<>("form");
		add(form);

		form.add(new TextField<String>("name", new PropertyModel<String>(this, "person.name")).add(new PropertyValidator<>()));
		form.add(new TextField<String>("phone", new PropertyModel<String>(this, "person.phone")).add(new PropertyValidator<>()));
		form.add(new TextField<String>("email", new PropertyModel<String>(this, "person.email")).add(new PropertyValidator<>()));
		form.add(new DateTextField("birthdate", new PropertyModel<Date>(this, "person.birthdate"),
			new StyleDateConverter("S-", true)).add(new PropertyValidator<>()));

	}
}