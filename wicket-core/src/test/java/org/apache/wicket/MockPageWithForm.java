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
package org.apache.wicket;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentUpdatingBehavior;
import org.apache.wicket.model.Model;


/**
 * Tests drop down choice.
 */
public class MockPageWithForm extends WebPage
{
	private final class MyForm extends Form<Void>
	{
		private static final long serialVersionUID = 1L;

		private MyForm(String id)
		{
			super(id);
		}

		@Override
		protected void onSubmit()
		{
			submitted = true;
		}
	}

	private static final long serialVersionUID = 1L;

	private boolean selected;

	private boolean submitted;

	/**
	 * Construct.
	 */
	public MockPageWithForm()
	{
		List<String> list = new ArrayList<String>();
		list.add("Select me");
		MyForm form = new MyForm("form");
		DropDownChoice<String> dropDown = new DropDownChoice<String>("dropdown", new Model<String>(), list);
		dropDown.add(new FormComponentUpdatingBehavior() {
			@Override
			protected void onUpdate()
			{
				selected = true;
			}
		});


		form.add(dropDown);
		add(form);
	}

	/**
	 * @return selected
	 */
	public boolean isSelected()
	{
		return selected;
	}

	/**
	 * @return submitted
	 */
	public boolean isSubmitted()
	{
		return submitted;
	}
}
