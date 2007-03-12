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
package wicket.extensions.markup.html.repeater.data.table.filter;

import java.io.Serializable;
import java.util.List;

import wicket.AttributeModifier;
import wicket.markup.html.form.ChoiceRenderer;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.IChoiceRenderer;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Filter that can be represented by a drop down list of choices
 * 
 * @see DropDownChoice
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class ChoiceFilter extends AbstractFilter
{
	private static final long serialVersionUID = 1L;
	private static final IChoiceRenderer defaultRenderer = new ChoiceRenderer();

	private final DropDownChoice choice;

	/**
	 * @param id
	 * @param model
	 * @param form
	 * @param choices
	 * @param autoSubmit
	 */
	public ChoiceFilter(String id, IModel model, FilterForm form, IModel choices,
			boolean autoSubmit)
	{
		this(id, model, form, choices, defaultRenderer, autoSubmit);
	}

	/**
	 * @param id
	 * @param model
	 * @param form
	 * @param choices
	 * @param autoSubmit
	 */
	public ChoiceFilter(String id, IModel model, FilterForm form, List choices,
			boolean autoSubmit)
	{
		this(id, model, form, new Model((Serializable)choices), defaultRenderer, autoSubmit);
	}

	/**
	 * @param id
	 * @param model
	 * @param form
	 * @param choices
	 * @param renderer
	 * @param autoSubmit
	 */
	public ChoiceFilter(String id, IModel model, FilterForm form, List choices,
			IChoiceRenderer renderer, boolean autoSubmit)
	{
		this(id, model, form, new Model((Serializable)choices), renderer, autoSubmit);
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
	public ChoiceFilter(String id, IModel model, FilterForm form, IModel choices,
			IChoiceRenderer renderer, boolean autoSubmit)
	{
		super(id, form);

		choice = new DropDownChoice("filter", model, choices, renderer);
		choice.setNullValid(true);
		
		if (autoSubmit)
		{
			choice.add(new AttributeModifier("onchange", true, new Model("this.form.submit();")));
		}
		enableFocusTracking(choice);
		
		add(choice);
	}

	/**
	 * @return the DropDownChoice form component created to represent this
	 *         filter
	 */
	public DropDownChoice getChoice()
	{
		return choice;
	}
	
}
