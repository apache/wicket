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

import java.util.Collection;
import java.util.Iterator;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.model.IDetachable;
import wicket.model.IModel;
import wicket.util.string.Strings;
import wicket.version.undo.Change;

/**
 * @author jcompagner
 *
 */
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
	private Collection choices;

	/** The renderer used to generate display/id values for the objects. */
	private IChoiceRenderer renderer;

	/**
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
	public AbstractChoice(final String id, final Collection choices)
	{
		this(id, new ChoiceRenderer(),choices);
	}

	/**
	 * @param id
	 *            See Component
	 * @param choices
	 *            The collection of choices in the dropdown
	 * @see wicket.Component#Component(String)
	 */
	public AbstractChoice(final String id, final IChoiceRenderer renderer,final Collection choices)
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
	public AbstractChoice(final String id, IModel model, final Collection choices)
	{
		this(id, model, new ChoiceRenderer(), choices);
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param choices
	 *            The drop down choices
	 * @see wicket.Component#Component(String, IModel)
	 */
	public AbstractChoice(final String id, IModel model, final IChoiceRenderer renderer, final Collection choices)
	{
		super(id, model);
		this.choices = choices;
		this.renderer = renderer;
	}
	
	/**
	 * @return The collection of object that this choice has
	 */
	public Collection getChoices()
	{
		return choices;
	}


	/**
	 * Sets the list of choices.
	 *
	 * @param choices the list of choices
	 */
	public final void setChoices(Collection choices)
	{
		if (this.choices != null && (this.choices != choices))
		{
			if (isVersioned())
			{
				addStateChange(new Change()
				{
					final Collection oldList = AbstractChoice.this.choices;
	
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
	public IChoiceRenderer getChoiceRenderer()
	{
		return renderer;
	}

	/**
	 * @see wicket.Component#detachModel()
	 */
	protected void detachModel()
	{
		super.detachModel();
		if (choices instanceof IDetachable) ((IDetachable)choices).detach();
	}

	/**
	 * @param selected The object that's currently selected
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
	 * 			  The index in the choices collection this object is in.
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
	protected void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag)
	{
		final StringBuffer buffer = new StringBuffer();
		final Object selected = getModelObject();

		// Append default option
		buffer.append(getDefaultChoice(selected));

		Iterator it = getChoices().iterator();
		int index = 0;
		while(it.hasNext())
		{
			final Object object = it.next();
			if (object != null)
			{
				final String displayValue = renderer.getDisplayValue(object);
				buffer.append("\n<option ");
				if (isSelected(object, index))
				{
					buffer.append("selected=\"selected\"");
				}
				buffer.append("value=\"");
				buffer.append(renderer.getIdValue(object, index));
				buffer.append("\">");
				String display = getLocalizer().getString(
						getId() + "." + displayValue, this, displayValue);
				String escaped = Strings.escapeMarkup(display, false, true);
				buffer.append(escaped);
				buffer.append("</option>");
				index++;
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