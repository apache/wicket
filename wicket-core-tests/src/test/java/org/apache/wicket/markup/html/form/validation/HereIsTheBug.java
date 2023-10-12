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
package org.apache.wicket.markup.html.form.validation;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 * 
 */
public class HereIsTheBug extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public HereIsTheBug(String id)
	{
		super(id);

		Form<FormData> form = new Form<FormData>("form", new CompoundPropertyModel<FormData>(
			new FormData()));
		FormComponentFeedbackBorder border = new FormComponentFeedbackBorder("border");
		TextField<String> textField = new TextField<String>("name");
		textField.setRequired(true);
		border.add(textField);
		border.add(new FeedbackPanel("feedback"));
		form.add(border);
		add(form);
	}

	static class FormData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		String _name;

		public String getName()
		{
			return _name;
		}

		public void setName(String name)
		{
			_name = name;
		}
	}
}
