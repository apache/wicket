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
 * @param The
 *            model object type
 */
public abstract class AutoCompleteTextField extends TextField
{

	private static final long serialVersionUID = 1L;

	/** auto complete behavior attached to this textfield */
	private AutoCompleteBehavior behavior;

	/** renderer */
	private final IAutoCompleteRenderer renderer;

	/** settings */
	private final AutoCompleteSettings settings;

	/**
	 * @param id
	 * @param type
	 */
	public AutoCompleteTextField(String id, Class type)
	{
		this(id, (IModel)null, type, false);
	}

	/**
	 * @param id
	 * @param model
	 * @param type
	 * @param preselect
	 *            the first item
	 */
	public AutoCompleteTextField(String id, IModel model, Class type, boolean preselect)
	{
		this(id, model, type, StringAutoCompleteRenderer.INSTANCE, preselect);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 * @param type
	 * @param settings
	 */
	public AutoCompleteTextField(String id, IModel model, Class type, AutoCompleteSettings settings)
	{
		this(id, model, type, StringAutoCompleteRenderer.INSTANCE, settings);
	}

	/**
	 * @param id
	 * @param object
	 * @param preselect
	 */
	public AutoCompleteTextField(String id, IModel object, boolean preselect)
	{
		this(id, object, (Class)null, preselect);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param object
	 * @param settings
	 */
	public AutoCompleteTextField(String id, IModel object, AutoCompleteSettings settings)
	{
		this(id, object, (Class)null, settings);
	}


	/**
	 * @param id
	 * @param object
	 */
	public AutoCompleteTextField(String id, IModel object)
	{
		this(id, object, (Class)null, false);
	}

	/**
	 * @param id
	 * @param preselect
	 */
	public AutoCompleteTextField(String id, boolean preselect)
	{
		this(id, (IModel)null, preselect);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param settings
	 */
	public AutoCompleteTextField(String id, AutoCompleteSettings settings)
	{
		this(id, (IModel)null, settings);

	}

	/**
	 * @param id
	 */
	public AutoCompleteTextField(String id)
	{
		this(id, (IModel)null, false);

	}

	/**
	 * @param id
	 * @param renderer
	 */
	public AutoCompleteTextField(String id, IAutoCompleteRenderer renderer)
	{
		this(id, (IModel)null, renderer);
	}

	/**
	 * @param id
	 * @param type
	 * @param renderer
	 */
	public AutoCompleteTextField(String id, Class type, IAutoCompleteRenderer renderer)
	{
		this(id, null, type, renderer, false);
	}

	/**
	 * @param id
	 * @param model
	 * @param renderer
	 */
	public AutoCompleteTextField(String id, IModel model, IAutoCompleteRenderer renderer)
	{
		this(id, model, (Class)null, renderer, false);
	}

	/**
	 * @param id
	 * @param model
	 * @param type
	 * @param renderer
	 * @param preselect
	 */
	public AutoCompleteTextField(String id, IModel model, Class type,
		IAutoCompleteRenderer renderer, boolean preselect)
	{
		this(id, model, type, renderer, new AutoCompleteSettings().setPreselect(preselect));
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 * @param type
	 * @param renderer
	 * @param settings
	 */
	public AutoCompleteTextField(String id, IModel model, Class type,
		IAutoCompleteRenderer renderer, AutoCompleteSettings settings)
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
	protected AutoCompleteBehavior newAutoCompleteBehavior(IAutoCompleteRenderer renderer,
		AutoCompleteSettings settings)
	{
		return new AutoCompleteBehavior(renderer, settings)
		{

			private static final long serialVersionUID = 1L;

			protected Iterator getChoices(String input)
			{
				return AutoCompleteTextField.this.getChoices(input);
			}

		};
	}

	/**
	 * @see org.apache.wicket.markup.html.form.AbstractTextComponent#onBeforeRender()
	 */
	protected void onBeforeRender()
	{
		// add auto complete behavior to this component if its not already there
		if (behavior == null)
		{
			// we do this here instad of constructor so we can have an overridable factory method
			behavior = newAutoCompleteBehavior(renderer, settings);
			add(behavior);
		}
		super.onBeforeRender();
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.html.form.TextField#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		// disable browser autocomplete
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
	protected abstract Iterator getChoices(String input);

	protected int getMaxHeightInPx()
	{
		return 50;
	}


}
