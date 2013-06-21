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
package org.apache.wicket.extensions.markup.html.repeater.data.table.filter;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * Filter that can be represented by a drop down list of choices
 * 
 * @see DropDownChoice
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            The model object type
 */
public class ChoiceFilter<T> extends AbstractFilter
{
	private static final long serialVersionUID = 1L;

	private final DropDownChoice<T> choice;

	/**
	 * @param id
	 * @param model
	 * @param form
	 * @param choices
	 * @param autoSubmit
	 */
	public ChoiceFilter(final String id, final IModel<T> model, final FilterForm<?> form,
		final IModel<List<? extends T>> choices, final boolean autoSubmit)
	{
		this(id, model, form, choices, new ChoiceRenderer<T>(), autoSubmit);
	}

	/**
	 * @param id
	 * @param model
	 * @param form
	 * @param choices
	 * @param autoSubmit
	 */
	public ChoiceFilter(final String id, final IModel<T> model, final FilterForm<?> form,
		final List<? extends T> choices, final boolean autoSubmit)
	{
		this(id, model, form, Model.ofList(choices), new ChoiceRenderer<T>(), autoSubmit);
	}

	/**
	 * @param id
	 * @param model
	 * @param form
	 * @param choices
	 * @param renderer
	 * @param autoSubmit
	 */
	public ChoiceFilter(final String id, final IModel<T> model, final FilterForm<?> form,
		final List<? extends T> choices, final IChoiceRenderer<T> renderer, final boolean autoSubmit)
	{
		this(id, model, form, Model.ofList(choices), renderer, autoSubmit);
	}

	/**
	 * @param id
	 *            component id
	 * @param model
	 *            model for the drop down choice component
	 * @param form
	 *            filter form this component will be attached to
	 * @param choices
	 *            list of choices, see {@link DropDownChoice}
	 * @param renderer
	 *            choice renderer, see {@link DropDownChoice}
	 * @param autoSubmit
	 *            if true this filter will submit the form on selection change
	 * @see DropDownChoice
	 */
	public ChoiceFilter(final String id, final IModel<T> model, final FilterForm<?> form,
		final IModel<List<? extends T>> choices, final IChoiceRenderer<T> renderer,
		final boolean autoSubmit)
	{
		super(id, form);

		choice = newDropDownChoice("filter", model, choices, renderer);

		if (autoSubmit)
		{
			choice.add(AttributeModifier.replace("onchange", "this.form.submit();"));
		}
		enableFocusTracking(choice);

		add(choice);
	}

	/**
	 * Factory method for the drop down choice component
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            component model
	 * @param choices
	 *            choices model
	 * @param renderer
	 *            choice renderer
	 * @return created drop down component
	 */
	protected DropDownChoice<T> newDropDownChoice(final String id, final IModel<T> model,
		final IModel<List<? extends T>> choices, final IChoiceRenderer<T> renderer)
	{
		DropDownChoice<T> dropDownChoice = new DropDownChoice<>(id, model, choices, renderer);
		dropDownChoice.setNullValid(true);
		return dropDownChoice;
	}

	/**
	 * @return the DropDownChoice form component created to represent this filter
	 */
	public final DropDownChoice<T> getChoice()
	{
		return choice;
	}

}
