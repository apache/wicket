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

import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

/**
 * Filter component that generates a 'go' and 'clear' buttons.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class GoAndClearFilter extends GoFilter
{
	private static final long serialVersionUID = 1L;

	private static final IModel<String> DEFAULT_CLEAR_MODEL = new ResourceModel("datatable.clear",
		"clear");

	private final Button clear;

	private final Object originalState;

	/**
	 * Constructor
	 * 
	 * This constructor will use default models for the 'clear' and 'go' button labels.
	 * Uses the form's model object as an original state
	 * 
	 * @param id
	 *            component id
	 * @param form
	 *            filter form of the filter toolbar
	 */
	public GoAndClearFilter(final String id, final FilterForm<?> form)
	{
		this(id, form, DEFAULT_GO_MODEL, DEFAULT_CLEAR_MODEL);
	}

	/**
	 * Constructor.
	 * Uses the form's model object as an original state
	 * 
	 * @param id
	 *            component id
	 * @param form
	 *            filter form of the filter toolbar
	 * @param goModel
	 *            model for the label of the 'go' button
	 * @param clearModel
	 *            model for the label of the 'clear' button
	 */
	public GoAndClearFilter(final String id, final FilterForm<?> form,
		final IModel<String> goModel, final IModel<String> clearModel)
	{
		this(id, goModel, clearModel, WicketObjects.cloneModel(form.getDefaultModelObject()));
	}

	/**
	 * Constructor
	 *
	 * @param id
	 *            component id
	 * @param goModel
	 *            model for the label of the 'go' button
	 * @param clearModel
	 *            model for the label of the 'clear' button
	 * @param originalState
	 *            the object to use as original state
	 */
	public GoAndClearFilter(final String id, final IModel<String> goModel,
			final IModel<String> clearModel, Object originalState)
	{
		super(id, goModel);

		this.originalState = originalState;

		clear = new Button("clear", clearModel)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit()
			{
				onClearSubmit(this);
			}
		};

		clear.setDefaultFormProcessing(true);

		add(clear);
	}

	/**
	 * @return button component representing the clear button
	 */
	protected Button getClearButton()
	{
		return clear;
	}

	/**
	 * This method should be implemented by subclasses to provide behavior for the clear button.
	 * 
	 * @param button
	 *            the 'clear' button
	 * 
	 */
	@SuppressWarnings("unchecked")
	protected void onClearSubmit(final Button button)
	{
		Form<Object> form = (Form<Object>)button.getForm();
		form.setDefaultModelObject(WicketObjects.cloneModel(originalState));
	}

}
