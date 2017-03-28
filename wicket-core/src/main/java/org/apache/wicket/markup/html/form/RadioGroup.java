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

import org.apache.wicket.IRequestListener;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * Component used to connect instances of Radio components into a group. Instances of Radio have to
 * be in the component hierarchy somewhere below the group component. The model object of the group
 * is set to the model object of the selected radio component or null if none selected.
 * 
 * ie
 * 
 * <pre>
 *    &lt;span wicket:id=&quot;radiochoicegroup&quot;&gt;
 *      ...
 *      &lt;input type=&quot;radio&quot; wicket:id=&quot;singleradiochoice1&quot;/&gt;choice 1
 *      ...
 *      &lt;input type=&quot;radio&quot; wicket:id=&quot;singleradiochoice2&quot;/&gt;choice 2
 *      ...
 *    &lt;/span&gt;
 * </pre>
 * 
 * @author Igor Vaynberg
 * @author Sven Meier (svenmeier)
 * 
 * @param <T>
 *            The model object type
 */
public class RadioGroup<T> extends FormComponent<T> implements IRequestListener
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(String)
	 */
	public RadioGroup(String id)
	{
		super(id);
		setRenderBodyOnly(true);
	}

	/**
	 * @param id
	 * @param model
	 * @see WebMarkupContainer#WebMarkupContainer(String, IModel)
	 */
	public RadioGroup(String id, IModel<T> model)
	{
		super(id, model);
		setRenderBodyOnly(true);
	}

	/**
	 * Whether a request should be generated with each selection change, resulting in the
	 * model being updated (of just this component) and {@link #onSelectionChanged(Object)}
	 * being called. This method returns false by default.
	 * <p>
	 * Use an {@link AjaxFormChoiceComponentUpdatingBehavior} with <tt>change</tt> event,
	 * if you want to use Ajax instead.
	 * 
	 * @return returns {@value false} by default, i.e. selection changes do not result in a request
	 */
	protected boolean wantOnSelectionChangedNotifications()
	{
		return false;
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#getStatelessHint()
	 */
	@Override
	protected boolean getStatelessHint()
	{
		if (wantOnSelectionChangedNotifications())
		{
			return false;
		}
		return super.getStatelessHint();
	}

	@Override
	protected String getModelValue()
	{
		String radioValue = visitChildren(Radio.class, new IVisitor<Radio<T>, String>()
		{
			@Override
			public void component(Radio<T> radio, IVisit<String> visit)
			{
				if (getModelComparator().compare(RadioGroup.this, radio.getDefaultModelObject()))
				{
					visit.stop(radio.getValue());
				}
			}
		});

		return radioValue;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#convertValue(String[])
	 */
	@Override
	protected T convertValue(String[] input) throws ConversionException
	{
		if (input != null && input.length > 0)
		{
			final String value = input[0];

			// retrieve the selected single radio choice component
			Radio<T> choice = visitChildren(Radio.class,
				new org.apache.wicket.util.visit.IVisitor<Radio<T>, Radio<T>>()
				{

					@Override
					public void component(final Radio<T> radio, final IVisit<Radio<T>> visit)
					{
						if (radio.getValue().equals(value))
						{
							visit.stop(radio);
						}
					}

				});

			if (choice == null)
			{
				throw new WicketRuntimeException(
					"submitted http post value [" +
						value +
						"] for RadioGroup component [" +
						getPath() +
						"] is illegal because it does not point to a Radio component. " +
						"Due to this the RadioGroup component cannot resolve the selected Radio component pointed to by the illegal value. A possible reason is that component hierarchy changed between rendering and form submission.");
			}


			// assign the value of the group's model
			return choice.getModelObject();
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		// No longer applicable, breaks XHTML validation.
		tag.remove("disabled");
		tag.remove("name");
	}

	/**
	 * Called when a selection changes.
	 */
	@Override
	public final void onRequest()
	{
		Form<?> form = getForm();
		if (form == null) {
			convertInput();
			updateModel();
			onSelectionChanged(getModelObject());
		} else {
			form.onFormSubmitted(new IFormSubmitter()
			{
				@Override
				public void onSubmit()
				{
					convertInput();
					updateModel();
					onSelectionChanged(getModelObject());
				}
				
				@Override
				public void onError()
				{
				}
				
				@Override
				public void onAfterSubmit()
				{
				}
				
				@Override
				public Form<?> getForm()
				{
					return RadioGroup.this.getForm();
				}
				
				@Override
				public boolean getDefaultFormProcessing()
				{
					return false;
				}
			});
		}
	}

	/**
	 * Template method that can be overridden to be notified by value changes.
	 * {@link #wantOnSelectionChangedNotifications()} has to be overriden to return {@value true} for
	 * this method to being called.
	 * <p>
	 * This method does nothing by default.
	 * 
	 * @param newSelection
	 *            The newly selected object of the backing model NOTE this is the same as you would
	 *            get by calling getModelObject() if the new selection were current
	 */
	protected void onSelectionChanged(final T newSelection)
	{
	}
}
