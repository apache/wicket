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

import java.io.Serializable;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 */
public class SelectTestPage2 extends WebPage
{

	public Form<Void> form;
	public Select<Foo> select;
	public SelectOption<Foo> option0;

	public SelectTestPage2()
	{
		form = new Form<>("form");
		add(form);

		select = new Select<Foo>("select", new Model<Foo>(new Foo("foo")))
		{
			/**
			 * Selection based custom equality.
			 */
			@Override
			protected boolean isSelected(IModel<?> model)
			{
				return ((Foo)model.getObject()).string == getModelObject().string;
			}
		};
		form.add(select);

		select.add(option0 = new SelectOption<Foo>("option0", new Model<>(new Foo("foo"))));
	}

	/**
	 * Model object without equals implementation.
	 */
	public static class Foo implements Serializable
	{
		public String string;

		public Foo(String string)
		{
			this.string = string;
		}
	}
}