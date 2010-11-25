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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.CollectionModel;


/**
 * tests exception when check is outside any group
 * 
 * @author igor
 * 
 */
public class CheckGroupTestPage5 extends WebPage
{
	/**
	 * Constructor
	 */
	public CheckGroupTestPage5()
	{
		List<String> list = new ArrayList<String>();
		Form<Void> form = new Form<Void>("form");
		CheckGroup<String> group = new CheckGroup<String>("group",
			new CollectionModel<String>(list));
		WebMarkupContainer container = new WebMarkupContainer("container");
		Check<String> check1 = new Check<String>("check1", new Model<String>("check1"));
		Check<String> check2 = new Check<String>("check2", new Model<String>("check2"));


		add(form);
		form.add(group);
		group.add(check1);
		group.add(container);
		// here we add check2 to the form so it is outside the group - it should
		// throw an exception when rendering
		form.add(check2);
	}

	private static final long serialVersionUID = 1L;


}
