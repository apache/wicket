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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;

/**
 * A simple text field.
 * 
 * @author Jonathan Locke
 * 
 * @param <T>
 *            The model object type
 */
public class TextField<T> extends AbstractTextComponent<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public TextField(final String id)
	{
		this(id, null, null);
	}

	/**
	 * @param id
	 *            See Component
	 * @param type
	 *            Type for field validation
	 */
	public TextField(final String id, final Class<T> type)
	{
		this(id, null, type);
	}

	/**
	 * @param id
	 * @param model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public TextField(final String id, final IModel<T> model)
	{
		this(id, model, null);
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param type
	 *            The type to use when updating the model for this text field
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public TextField(final String id, IModel<T> model, Class<T> type)
	{
		super(id, model);
		setType(type);

		// don't double encode the value. it is encoded by ComponentTag.writeOutput()
		setEscapeModelStrings(false);
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		// Must be attached to an input tag
		checkComponentTag(tag, "input");

		// check for text type
		String[] inputTypes = getInputTypes();
		if (inputTypes != null)
		{
			checkComponentTagAttribute(tag, "type", inputTypes);
		}
		else
		{
			if (tag.getAttributes().containsKey("type"))
			{
				checkComponentTagAttribute(tag, "type", "text");
			}
		}

		tag.put("value", getValue());

		// Default handling for component tag
		super.onComponentTag(tag);
	}

	/**
	 * Subclass should override this method if this textfield is mapped on a different input type as
	 * text. Like PasswordTextField or HiddenField.
	 * 
	 * @return The input type of this textfield, default is null
	 */
	protected String[] getInputTypes()
	{
		return null;
	}
}
