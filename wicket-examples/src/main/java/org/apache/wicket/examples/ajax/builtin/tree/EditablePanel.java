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
package org.apache.wicket.examples.ajax.builtin.tree;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Panel that contains an text field that submits automatically after it loses focus.
 * 
 * @author Matej Knopp
 */
public class EditablePanel extends Panel
{

	/**
	 * Panel constructor.
	 * 
	 * @param id
	 *            Markup id
	 * 
	 * @param inputModel
	 *            Model of the text field
	 */
	public EditablePanel(String id, IModel<?> inputModel)
	{
		super(id);

		TextField<?> field = new TextField("textfield", inputModel);
		add(field);

		field.add(new AjaxFormComponentUpdatingBehavior("blur")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
			}
		});
	}

}
