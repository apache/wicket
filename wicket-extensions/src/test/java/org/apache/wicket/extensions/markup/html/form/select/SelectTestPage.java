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
package org.apache.wicket.extensions.markup.html.form.select;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

/**
 */
public class SelectTestPage extends WebPage
{

	public Form<Void> form;
	public Select<String> select;
	public SelectOption<String> option1;

	public SelectTestPage()
	{
		form = new Form<>("form");
		add(form);

		select = new Select<>("select", new Model<String>(null));
		form.add(select);

		select.add(new SelectOption<>("option0", new Model<>("OPTION_0")));
		select.add(option1 = new SelectOption<>("option1", new Model<>("OPTION_1")));
		select.add(new SelectOption<>("option2", new Model<>("OPTION_2")));

		form.add(new TextField<>("text", new Model<>(null)).setRequired(true));
	}
}