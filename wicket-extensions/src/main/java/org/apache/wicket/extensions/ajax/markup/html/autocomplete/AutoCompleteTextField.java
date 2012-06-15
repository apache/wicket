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
package org.apache.wicket.extensions.ajax.markup.html.autocomplete;

import java.util.Iterator;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;


/**
 * An implementation of a textfield with the autoassist ajax behavior {@link AutoCompleteBehavior}.
 * 
 * Note that you must add your own CSS to make the suggestion display properly, see
 * {@link DefaultCssAutoCompleteTextField} for an example.
 * 
 * @see DefaultCssAutoCompleteTextField
 * @see AutoCompleteBehavior
 * @see IAutoCompleteRenderer
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @param <T>
 *            The model object type
 */
public abstract class AutoCompleteTextField<T> extends TextField<T>
{
	private static final long serialVersionUID = 1L;

	/** auto complete behavior attached to this textfield */
	private AutoCompleteBehavior<T> behavior;

	/** renderer */
	private final IAutoCompleteRenderer<T> renderer;

	/** settings */
	private final AutoCompleteSettings settings;

	/**
	 * Constructor for the given type with default settings.
	 * 
	 * @param id
	 *            component id
	 * @param type
	 *            model objec type
	 */
	public AutoCompleteTextField(final String id, final Class<T> type)
	{
		this(id, null, type, new AutoCompleteSettings());
	}

	/**
	 * Constructor for the given model and type.
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            model
	 * @param type
	 *            model object type
	 * @param settings
	 *            settings for autocomplete
	 */
	@SuppressWarnings("unchecked")
	public AutoCompleteTextField(final String id, final IModel<T> model, final Class<T> type,
		final AutoCompleteSettings settings)
	{
		this(id, model, type, StringAutoCompleteRenderer.INSTANCE, settings);
	}

	/**
	 * Constructor for given model.
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            model
	 * @param settings
	 *            settings for autocomplete
	 */
	public AutoCompleteTextField(final String id, final IModel<T> model,
		final AutoCompleteSettings settings)
	{
		this(id, model, null, settings);
	}

	/**
	 * Constructor for the given model.
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            model
	 */
	public AutoCompleteTextField(final String id, final IModel<T> model)
	{
		this(id, model, null, new AutoCompleteSettings());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            component id
	 * @param settings
	 *            settings for autocomplete
	 */
	public AutoCompleteTextField(final String id, final AutoCompleteSettings settings)
	{
		this(id, null, settings);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            component id
	 */
	public AutoCompleteTextField(final String id)
	{
		this(id, null, new AutoCompleteSettings());
	}

	/**
	 * Constructor using the given renderer.
	 * 
	 * @param id
	 *            component id
	 * @param renderer
	 *            renderer for autocomplete
	 */
	public AutoCompleteTextField(final String id, final IAutoCompleteRenderer<T> renderer)
	{
		this(id, (IModel<T>)null, renderer);
	}

	/**
	 * Constructor for the given type using the given renderer
	 * 
	 * @param id
	 *            component id
	 * @param type
	 *            model object type
	 * @param renderer
	 *            renderer for autocomplete
	 */
	public AutoCompleteTextField(final String id, final Class<T> type,
		final IAutoCompleteRenderer<T> renderer)
	{
		this(id, null, type, renderer, new AutoCompleteSettings());
	}

	/**
	 * Constructor for the given model using the given renderer.
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            model
	 * @param renderer
	 *            renderer for autocomplete
	 */
	public AutoCompleteTextField(final String id, final IModel<T> model,
		final IAutoCompleteRenderer<T> renderer)
	{
		this(id, model, null, renderer, new AutoCompleteSettings());
	}

	/**
	 * Constructor for the given model using the given renderer.
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            model
	 * @param type
	 *            model object type
	 * @param renderer
	 *            renderer for autocomplete
	 * @param settings
	 *            settings for autocomplete
	 */
	public AutoCompleteTextField(final String id, final IModel<T> model, final Class<T> type,
		final IAutoCompleteRenderer<T> renderer, final AutoCompleteSettings settings)
	{
		super(id, model, type);
		this.renderer = renderer;
		this.settings = settings;
	}

	/**
	 * Factory method for autocomplete behavior that will be added to this textfield
	 * 
	 * @param renderer
	 *            auto complete renderer
	 * @param settings
	 *            auto complete settings
	 * @return auto complete behavior
	 */
	protected AutoCompleteBehavior<T> newAutoCompleteBehavior(
		final IAutoCompleteRenderer<T> renderer, final AutoCompleteSettings settings)
	{
		return new AutoCompleteBehavior<T>(renderer, settings)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Iterator<T> getChoices(final String input)
			{
				return AutoCompleteTextField.this.getChoices(input);
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	protected void onBeforeRender()
	{
		// add auto complete behavior to this component if its not already there
		if (behavior == null)
		{
			// we do this here instead of constructor so we can have an overridable factory method
			add(behavior = newAutoCompleteBehavior(renderer, settings));
		}
		super.onBeforeRender();
	}

	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);

		// disable browser's autocomplete
		tag.put("autocomplete", "off");
	}

	/**
	 * Callback method that should return an iterator over all possible assist choice objects. These
	 * objects will be passed to the renderer to generate output. Usually it is enough to return an
	 * iterator over strings.
	 * 
	 * @see AutoCompleteBehavior#getChoices(String)
	 * 
	 * @param input
	 *            current input
	 * @return iterator over all possible choice objects
	 */
	protected abstract Iterator<T> getChoices(String input);
}
