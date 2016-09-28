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
package org.apache.wicket.markup.html.form.border;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * This is not a real DateField. It only servers some testing purposes (derived from
 * FormComponentPanel)
 */
public class MyDateField extends FormComponentPanel<String>
{
	private static final long serialVersionUID = 1L;

	private String date;

	private final TextField<String> dateField;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public MyDateField(String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public MyDateField(String id, IModel<String> model)
	{
		super(id, model);
		setType(String.class);
		PropertyModel<String> dateFieldModel = new PropertyModel<String>(this, "date");
		add(dateField = new TextField<String>("date", dateFieldModel));
	}

	/**
	 * Gets date.
	 * 
	 * @return date
	 */
	public String getDate()
	{
		return date;
	}

	/**
	 * Sets date.
	 * 
	 * @param date
	 *            date
	 */
	public void setDate(String date)
	{
		this.date = date;
		setDefaultModelObject(date);
	}

	/**
	 * Sets the converted input. In this case, we're really just interested in the nested date
	 * field, as that is the element that receives the real user input. So we're just passing that
	 * on.
	 * <p>
	 * Note that overriding this method is a better option than overriding {@link #updateModel()}
	 * like the first versions of this class did. The reason for that is that this method can be
	 * used by form validators without having to depend on the actual model being updated, and this
	 * method is called by the default implementation of {@link #updateModel()} anyway (so we don't
	 * have to override that anymore).
	 * </p>
	 * 
	 * @see org.apache.wicket.markup.html.form.FormComponent#convertInput()
	 */
	@Override
	public void convertInput()
	{
		setConvertedInput(dateField.getConvertedInput() + "-converted");
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		dateField.setRequired(isRequired());

		String d = (String)getDefaultModelObject();
		if (d != null)
		{
			date = d;
		}
		else
		{
			date = null;
		}

		super.onBeforeRender();
	}
}
