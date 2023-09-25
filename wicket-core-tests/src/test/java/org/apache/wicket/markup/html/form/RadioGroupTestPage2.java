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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

/**
 * Tests rendering of the RadioGroup and Radio components
 * 
 * @author igor
 */
public class RadioGroupTestPage2 extends WebPage
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Constructor
	 */
	public RadioGroupTestPage2()
	{
		Form<Void> form = new Form<Void>("form");
		RadioGroup<String> group = new RadioGroup<String>("group", new Model<String>("radio2"));
		WebMarkupContainer container = new WebMarkupContainer("container");
		Radio<String> radio1 = new Radio<String>("radio1", new Model<String>("radio1"));
		Radio<String> radio2 = new Radio<String>("radio2", new Model<String>("radio2"));


		add(form);
		form.add(group);
		group.add(radio1);
		group.add(container);
		// here by mistake we add the radio component not under the group to
		// test the error when it cannot find its group
		form.add(radio2);
	}
}
