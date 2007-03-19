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
package wicket;

import java.util.ArrayList;
import java.util.List;

import wicket.markup.html.WebPage;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.model.Model;

public class MockPageWithForm extends WebPage
{
	private boolean selected;
	private boolean submitted;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MockPageWithForm()
	{
		List list = new ArrayList();
		list.add("Select me");
		MyForm form = new MyForm("form");
		DropDownChoice dropDown = new DropDownChoice("dropdown",new Model(), list)
		{
			private static final long serialVersionUID = 1L;

			protected void onSelectionChanged(Object newSelection)
			{
				selected = true;
			}

			/**
			 * @see wicket.markup.html.form.DropDownChoice#wantOnSelectionChangedNotifications()
			 */
			protected boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}
		};


		form.add(dropDown);
		add(form);
	}
	private final class MyForm extends Form
	{
		private static final long serialVersionUID = 1L;

		private MyForm(String id)
		{
			super(id);
		}

		protected void onSubmit()
		{
			submitted = true;
		}
	}
	/**
	 * @return
	 */
	public boolean isSelected()
	{
		return selected;
	}
	/**
	 * @return
	 */
	public boolean isSubmitted()
	{
		return submitted;
	}
}
