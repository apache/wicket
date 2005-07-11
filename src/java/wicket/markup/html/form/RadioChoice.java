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
import wicket.model.IModel;
import wicket.util.string.Strings;

/**
 * A choice subclass that shows choices in radio style.
 * TODO elaborate with an example
 * 
 * <p>
 * You can can extend this class and override method wantOnSelectionChangedNotifications()
 * to force server roundtrips on each selection change.
 * </p>
 * 
 * @author Jonathan Locke
 */
public class RadioChoice extends AbstractSingleSelectChoice implements IOnChangeListener
{
	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String)
	 */
	public RadioChoice(final String id)
	{
		super(id);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, Collection)
	 */
	public RadioChoice(final String id, final Collection choices)
	{
		super(id, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IChoiceRenderer,Collection)
	 */
	public RadioChoice(final String id, final IChoiceRenderer renderer, final Collection choices)
	{
		super(id,renderer,choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel, Collection)
	 */
	public RadioChoice(final String id, IModel model, final Collection choices)
	{
		super(id, model, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel, IChoiceRenderer,Collection)
	 */
	public RadioChoice(final String id, IModel model, final IChoiceRenderer renderer, final Collection choices)
	{
		super(id, model, renderer,choices);
	}

	/**
	 * @see wicket.markup.html.form.IOnChangeListener#onSelectionChanged()
	 */
	public void onSelectionChanged()
	{
		updateModel();
		onSelectionChanged(getModelObject());
	}

	/**
	 * Template method that can be overriden by clients that implement
	 * IOnChangeListener to be notified by onChange events of a select element.
	 * This method does nothing by default.
	 * <p>
	 * Called when a option is selected of a dropdown list that wants to be
	 * notified of this event. This method is to be implemented by clients that
	 * want to be notified of selection events.
	 * 
	 * @param newSelection
	 *			  The newly selected object of the backing model NOTE this is
	 *			  the same as you would get by calling getModelObject() if the
	 *			  new selection were current
	 */
	protected void onSelectionChanged(Object newSelection)
	{
	}

	/**
	 * Whether this component's onSelectionChanged event handler should called using
	 * javascript if the selection changes. If true, a roundtrip will be generated with
	 * each selection change, resulting in the model being updated (of just this component)
	 * and onSelectionChanged being called. This method returns false by default.
	 * @return True if this component's onSelectionChanged event handler should
	 *			called using javascript if the selection changes
	 */
	protected boolean wantOnSelectionChangedNotifications()
	{
		return false;
	}

	/**
	 * @return Prefix to use before choice
	 */
	protected String getPrefix()
	{
		return "";
	}

	/**
	 * @return Separator to use between radio options
	 */
	protected String getSuffix()
	{
		return "<br>\n";
	}

	/**
	 * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	protected final void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag)
	{
		// Buffer to hold generated body
		final StringBuffer buffer = new StringBuffer();

		// Iterate through choices
		final Collection choices = getChoices();

		// Loop through choices
		int index = -1;
		Iterator it = choices.iterator();
		while(it.hasNext())
		{
			index++;
			// Get next choice
			final Object choice = it.next();

			// Get label for choice
			final String label = getChoiceRenderer().getDisplayValue(choice);

			// If there is a display value for the choice, then we know that the
			// choice is automatic in some way. If label is /null/ then we know
			// that the choice is a manually created radio tag at some random
			// location in the page markup!
			if (label != null)
			{
				// Append option suffix
				buffer.append(getPrefix());

				String id = getChoiceRenderer().getIdValue(choice, index);
				// Add radio tag
				buffer.append("<input name=\"" + getPath() + "\"" + " type=\"radio\""
						+ (isSelected(choice,index) ? " checked" : "") + " value=\"" + id
						+ "\"");
				
				// Should a roundtrip be made (have onSelectionChanged called) when the option is clicked?
				if (wantOnSelectionChangedNotifications())
				{
					final String url = urlFor(IOnChangeListener.class);

					// NOTE: do not encode the url as that would give invalid JavaScript
					buffer.append(" onclick=\"location.href='" + url + "&" + getPath()
							+ "=" + id + "';\"");
				}

				buffer.append(">");

				// Add label for radio button
				String display = getLocalizer().getString(getId() + "." + label, this, label);
				String escaped = Strings.escapeMarkup(display, false, true);
				buffer.append(escaped);

				// Append option suffix
				buffer.append(getSuffix());
			}
		}

		// Replace body
		replaceComponentTagBody(markupStream, openTag, buffer.toString());
	}
}
