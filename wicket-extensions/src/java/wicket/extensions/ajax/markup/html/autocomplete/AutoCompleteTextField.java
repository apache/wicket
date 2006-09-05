/*
 * $Id$ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.ajax.markup.html.autocomplete;

import java.util.Iterator;

import wicket.MarkupContainer;
import wicket.markup.html.form.TextField;
import wicket.model.IModel;

/**
 * An implementation of a textfield with the autoassist ajax behavior
 * 
 * @see AutoCompleteBehavior
 * @see IAutoCompleteRenderer
 * @param <T>
 *            The type
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AutoCompleteTextField<T> extends TextField<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 */
	public AutoCompleteTextField(MarkupContainer parent, String id)
	{
		this(parent, id, (IModel<T>)null);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param type
	 *            Type for field validation
	 */
	public AutoCompleteTextField(MarkupContainer parent, String id, Class type)
	{
		this(parent, id, (IModel<T>)null, type);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param type
	 *            Type for field validation
	 * @param renderer
	 *            does the rendering of the behavior
	 */
	public AutoCompleteTextField(MarkupContainer parent, String id, Class type,
			IAutoCompleteRenderer renderer)
	{
		this(parent, id, null, type, renderer);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param renderer
	 *            does the rendering of the behavior
	 */
	public AutoCompleteTextField(MarkupContainer parent, String id, IAutoCompleteRenderer renderer)
	{
		this(parent, id, (IModel<T>)null, renderer);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 */
	public AutoCompleteTextField(MarkupContainer parent, String id, IModel<T> model)
	{
		this(parent, id, model, (Class)null);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 * @param type
	 *            Type for field validation
	 */
	public AutoCompleteTextField(MarkupContainer parent, String id, IModel<T> model, Class type)
	{
		this(parent, id, model, type, StringAutoCompleteRenderer.INSTANCE);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 * @param type
	 *            Type for field validation
	 * @param renderer
	 *            does the rendering of the behavior
	 */
	public AutoCompleteTextField(MarkupContainer parent, String id, IModel<T> model, Class type,
			IAutoCompleteRenderer renderer)
	{
		super(parent, id, model, type);

		add(new AutoCompleteBehavior(renderer)
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected Iterator getChoices(String input)
			{
				return AutoCompleteTextField.this.getChoices(input);
			}

		});

	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 * @param renderer
	 *            does the rendering of the behavior
	 */
	public AutoCompleteTextField(MarkupContainer parent, String id, IModel<T> model,
			IAutoCompleteRenderer renderer)
	{
		this(parent, id, model, (Class)null, renderer);
	}

	/**
	 * Callback method that should return an iterator over all possible assist
	 * choice objects. These objects will be passed to the renderer to generate
	 * output. Usually it is enough to return an iterator over strings.
	 * 
	 * @see AutoCompleteBehavior#getChoices(String)
	 * 
	 * @param input
	 *            current input
	 * @return iterator ver all possible choice objects
	 */
	protected abstract Iterator getChoices(String input);
}
