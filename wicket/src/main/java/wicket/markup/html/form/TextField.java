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
package wicket.markup.html.form;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.model.IModel;

/**
 * A simple text field.
 * 
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Jonathan Locke
 */
public class TextField<T> extends AbstractTextComponent<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public TextField(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * @param parent
	 *            The parent of this component
	 * @param id
	 *            See Component
	 * @param type
	 *            Type for field validation
	 */
	public TextField(MarkupContainer parent, final String id, final Class<? extends T> type)
	{
		super(parent, id);
		setType(type);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public TextField(MarkupContainer parent, final String id, final IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param type
	 *            The type to use when updating the model for this text field
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public TextField(MarkupContainer parent, final String id, IModel<T> model, Class type)
	{
		super(parent, id, model);
		setType(type);
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		// Must be attached to an input tag
		checkComponentTag(tag, "input");

		// check for text type
		String inputType = getInputType();
		if (inputType != null)
		{
			checkComponentTagAttribute(tag, "type", inputType);
		}
		else
		{
			if (tag.getAttributes().containsKey("type"))
			{
				checkComponentTagAttribute(tag, "type", "text");
			}
		}

		// No validation errors
		tag.put("value", getValue());

		// Default handling for component tag
		super.onComponentTag(tag);
	}

	/**
	 * Subclass should override this method if this textfields mappes on a
	 * different input type as text. Like PasswordField or HiddenField.
	 * 
	 * @return The input type of this textfield, default is 'text'
	 */
	protected String getInputType()
	{
		return null;
	}
}
