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
package org.apache.wicket.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidatorAdapter;
import org.apache.wicket.validation.validator.PatternValidator;

/**
 * Behavior which renders HTML5 attributes.
 * 
 * @see #onInput(AbstractTextComponent, ComponentTag)
 * @see #onButton(Button, ComponentTag)
 */
public class HTML5Attributes extends Behavior
{
	private static final long serialVersionUID = 1L;

	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		if (component instanceof AbstractTextComponent)
		{
			onInput((AbstractTextComponent<?>)component, tag);
		}
		else if (component instanceof Button)
		{
			onButton((Button)component, tag);
		}
	}

	/**
	 * Writes HTML5 attributes for {@link AbstractTextComponent} inputs:
	 * 
	 * <ul>
	 * <li>{@code required} if component {@link AbstractTextComponent#isRequired()}</li>
	 * <li>{@code placeholder} for {@link AbstractTextComponent#getLabel()}</li>
	 * <li>{@code pattern} for {@link AbstractTextComponent}s with a {@link PatternValidator}</li>
	 * <ul>
	 * 
	 * @param input
	 *            input component
	 * @param tag
	 *            component tag
	 */
	protected void onInput(AbstractTextComponent<?> input, ComponentTag tag)
	{
		if (input.isRequired())
		{
			tag.put("required", "required");
		}

		IModel<String> label = input.getLabel();
		if (label != null && label.getObject() != null)
		{
			tag.put("placeholder", label.getObject());
		}

		for (IValidator<?> validator : input.getValidators())
		{
			if (validator instanceof ValidatorAdapter)
			{
				validator = ((ValidatorAdapter<?>)validator).getValidator();
			}

			if (validator instanceof PatternValidator)
			{
				tag.put("pattern", ((PatternValidator)validator).getPattern().toString());
			}
		}
	}

	/**
	 * Writes HTML5 attributes for {@link Button}s:
	 * 
	 * <ul>
	 * <li>{@code formnovalidate} if {@link Button#getDefaultFormProcessing()} returns {@code false}
	 * </li>
	 * <ul>
	 * 
	 * @param button
	 *            button component
	 * @param tag
	 *            component tag
	 */
	protected void onButton(Button button, ComponentTag tag)
	{
		if (!button.getDefaultFormProcessing())
		{
			tag.put("formnovalidate", "formnovalidate");
		}
	}

	/**
	 * A listener to instantiations of {@link FormComponent}s to add HTML5 attributes.
	 */
	public static class InstantiationListener implements IComponentInstantiationListener
	{
		/**
		 * Adds {@link HTML5Attributes} to all {@link FormComponent}s.
		 */
		@Override
		public void onInstantiation(Component component)
		{
			if (component instanceof FormComponent)
			{
				component.add(new HTML5Attributes());
			}
		}
	}
}