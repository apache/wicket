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
package org.apache.wicket.markup.parser;

import java.util.Arrays;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.value.ValueMap;

/**
 * @author dashorst
 */
public class SignInPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public SignInPanel(String id)
	{
		super(id);
		Form<ValueMap> form = new Form<ValueMap>("signInForm", new CompoundPropertyModel<ValueMap>(
			new ValueMap()));
		form.add(new TextField<String>("username"));
		form.add(new PasswordTextField("password"));
		form.add(new DropDownChoice<String>("domain", Arrays.asList("Wicket", "Tapestry", "JSF",
			".Net")));
		form.add(new CheckBox("rememberMe"));
		form.add(new WebMarkupContainer("loginZonderTokenContainer").add(new CheckBox(
			"loginZonderToken")));
		form.add(new SubmitLink("aanmelden"));
		add(form);
	}

}
