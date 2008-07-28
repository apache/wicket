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
 * An implementation of a textfield with the autoassist ajax behavior
 * 
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
	 * @param id
	 * @param type
	 */
	public AutoCompleteTextField(String id, Class<T> type)
	{
		this(id, (IModel<T>)null, type, new AutoCompleteSettings());
	}

	/**
	 * @param id
	 * @param model
	 * @param type
	 * @param preselect
	 *            the first item
	 * @deprecated use the constructor {@link AutoCompleteTextField}
	 *             {@link #AutoCompleteTextField(String, IModel, Class, AutoCompleteSettings)}
	 */
	@Deprecated
	public AutoCompleteTextField(String id, IModel<T> model, Class<T> type, boolean preselect)
	{
		this(id, model, type, StringAutoCompleteRenderer.INSTANCE,
			new AutoCompleteSettings().setPreselect(preselect));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 * @param type
	 * @param settings
	 */
	public AutoCompleteTextField(String id, IModel<T> model, Class<T> type,
		AutoCompleteSettings settings)
	{
		this(id, model, type, StringAutoCompleteRenderer.INSTANCE, settings);
	}

	/**
	 * @param id
	 * @param object
	 * @param preselect
	 * @deprecated use the constructor {@link AutoCompleteTextField}
	 *             {@link #AutoCompleteTextField(String, IModel, AutoCompleteSettings)}
	 */
	@Deprecated
	public AutoCompleteTextField(String id, IModel<T> object, boolean preselect)
	{
		this(id, object, (Class<T>)null, new AutoCompleteSettings().setPreselect(preselect));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param object
	 * @param settings
	 */
	public AutoCompleteTextField(String id, IModel<T> object, AutoCompleteSettings settings)
	{
		this(id, object, (Class<T>)null, settings);
	}


	/**
	 * @param id
	 * @param object
	 */
	public AutoCompleteTextField(String id, IModel<T> object)
	{
		this(id, object, (Class<T>)null, new AutoCompleteSettings());
	}

	/**
	 * @param id
	 * @param preselect
	 * @deprecated use the constructor {@link AutoCompleteTextField}
	 *             {@link #AutoCompleteTextField(String, AutoCompleteSettings)}
	 */
	@Deprecated
	public AutoCompleteTextField(String id, boolean preselect)
	{
		this(id, (IModel<T>)null, new AutoCompleteSettings().setPreselect(preselect));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param settings
	 */
	public AutoCompleteTextField(String id, AutoCompleteSettings settings)
	{
		this(id, (IModel<T>)null, settings);

	}

	/**
	 * @param id
	 */
	public AutoCompleteTextField(String id)
	{
		this(id, (IModel<T>)null, new AutoCompleteSettings());

	}

	/**
	 * @param id
	 * @param renderer
	 */
	public AutoCompleteTextField(String id, IAutoCompleteRenderer<T> renderer)
	{
		this(id, (IModel<T>)null, renderer);
	}

	/**
	 * @param id
	 * @param type
	 * @param renderer
	 */
	public AutoCompleteTextField(String id, Class<T> type, IAutoCompleteRenderer<T> renderer)
	{
		this(id, null, type, renderer, new AutoCompleteSettings());
	}

	/**
	 * @param id
	 * @param model
	 * @param renderer
	 */
	public AutoCompleteTextField(String id, IModel<T> model, IAutoCompleteRenderer<T> renderer)
	{
		this(id, model, (Class<T>)null, renderer, new AutoCompleteSettings());
	}

	/**
	 * @param id
	 * @param model
	 * @param type
	 * @param renderer
	 * @param preselect
	 * @deprecated use the constructor {@link AutoCompleteTextField}
	 *             {@link #AutoCompleteTextField(String, IModel, Class, IAutoCompleteRenderer, AutoCompleteSettings)}
	 */
	@Deprecated
	public AutoCompleteTextField(String id, IModel<T> model, Class<T> type,
		IAutoCompleteRenderer<T> renderer, boolean preselect)
	{
		this(id, model, type, renderer, new AutoCompleteSettings().setPreselect(preselect));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 * @param type
	 * @param renderer
	 * @param settings
	 */
	public AutoCompleteTextField(String id, IModel<T> model, Class<T> type,
		IAutoCompleteRenderer<T> renderer, AutoCompleteSettings settings)
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
	protected AutoCompleteBehavior<T> newAutoCompleteBehavior(IAutoCompleteRenderer<T> renderer,
		AutoCompleteSettings settings)
	{
		return new AutoCompleteBehavior<T>(renderer, settings)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Iterator<T> getChoices(String input)
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
			// we do this here instad of constructor so we can have an overridable factory method
			add(behavior = newAutoCompleteBehavior(renderer, settings));
		}
		super.onBeforeRender();
	}

	/** {@inheritDoc} */
	@Override
	protected void onComponentTag(ComponentTag tag)
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
