/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.markup.html.form;

import java.util.List;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.model.IDetachable;
import wicket.model.IModel;
import wicket.util.string.Strings;
import wicket.version.undo.Change;

/**
 * Abstract base class for all choice (html select) options.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
abstract class AbstractChoice extends FormComponent
{
	/** The list of objects. */
	private List choices;

	/** The renderer used to generate display/id values for the objects. */
	private IChoiceRenderer renderer;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @see wicket.Component#Component(String)
	 */
	public AbstractChoice(final String id)
	{
		super(id);
	}

	/**
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @see wicket.Component#Component(String)
	 */
	public AbstractChoice(final String id, final List choices)
	{
		this(id, choices,new ChoiceRenderer());
	}

	/**
	 * @param id
	 *            See Component
	 * @param renderer
	 *            The rendering engine
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @see wicket.Component#Component(String)
	 */
	public AbstractChoice(final String id, final List choices,final IChoiceRenderer renderer)
	{
		super(id);
		this.choices = choices;
		this.renderer = renderer;
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @see wicket.Component#Component(String, IModel)
	 */
	public AbstractChoice(final String id, IModel model, final List choices)
	{
		this(id, model, choices, new ChoiceRenderer());
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param renderer
	 *            The rendering engine
	 * @param choices
	 *            The drop down choices
	 * @see wicket.Component#Component(String, IModel)
	 */
	public AbstractChoice(final String id, IModel model, final List choices, final IChoiceRenderer renderer)
	{
		super(id, model);
		this.choices = choices;
		this.renderer = renderer;
	}

	/**
	 * @return The collection of object that this choice has
	 */
	public List getChoices()
	{
		return choices;
	}

	/**
	 * Sets the list of choices.
	 * 
	 * @param choices
	 *            the list of choices
	 */
	public final void setChoices(List choices)
	{
		if ((this.choices != null) && (this.choices != choices))
		{
			if (isVersioned())
			{
				addStateChange(new Change()
				{
					final List oldList = AbstractChoice.this.choices;
					public void undo()
					{
						AbstractChoice.this.choices = oldList;
					}
				});
			}
		}
		this.choices = choices;
	}

	/**
	 * @return The IChoiceRenderer used for rendering the data objects
	 */
	public final IChoiceRenderer getChoiceRenderer()
	{
		return renderer;
	}

	/**
	 * Set the choice renderer to be used.
	 *  
	 * @param renderer
	 */
	public final void setChoiceRenderer(IChoiceRenderer renderer)
	{
	    this.renderer = renderer;
	}
	
	/**
	 * @see wicket.Component#detachModel()
	 */
	protected void detachModel()
	{
		super.detachModel();
		
		if (choices instanceof IDetachable)
		{
			((IDetachable)choices).detach();
		}
	}

	/**
	 * @param selected
	 *            The object that's currently selected
	 * @return Any default choice, such as "Choose One", depending on the
	 *         subclass
	 */
	protected String getDefaultChoice(final Object selected)
	{
		return "";
	}

	/**
	 * Gets whether the given value represents the current selection.
	 * 
	 * @param object
	 *            The object to check
	 * @param index
	 *            The index in the choices collection this object is in.
	 * @return Whether the given value represents the current selection
	 */
	protected abstract boolean isSelected(final Object object, int index);

	/**
	 * Handle the container's body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		final StringBuffer buffer = new StringBuffer();
		final Object selected = getModelObject();

		// Append default option
		buffer.append(getDefaultChoice(selected));

		List choices = getChoices();
		for(int index=0;index<choices.size();index++)
		{
			// Get next choice
			final Object choice = choices.get(index);
			if (choice != null)
			{
				final String displayValue = renderer.getDisplayValue(choice);
				buffer.append("\n<option ");
				if (isSelected(choice, index))
				{
					buffer.append("selected=\"selected\"");
				}
				buffer.append("value=\"");
				buffer.append(renderer.getIdValue(choice, index));
				buffer.append("\">");
				String display = getLocalizer().getString(getId() + "." + displayValue, this,
						displayValue);
				String escaped = Strings.escapeMarkup(display, false, true);
				buffer.append(escaped);
				buffer.append("</option>");
			}
			else
			{
				throw new IllegalArgumentException("Choice list has null value at index " + index);
			}
		}

		buffer.append("\n");
		replaceComponentTagBody(markupStream, openTag, buffer.toString());
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#supportsPersistence()
	 */
	protected boolean supportsPersistence()
	{
		return true;
	}

	/**
	 * Updates the model of this component from the request.
	 * 
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	protected abstract void updateModel();

}