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

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.form.model.IChoice;
import wicket.markup.html.form.model.IChoiceList;
import wicket.model.IModel;

/**
 * A choice subclass that shows choices in radio style.
 * 
 * @author Jonathan Locke
 */
public class RadioChoice extends AbstractSingleSelectChoice
{
	/**
	 * @see AbstractChoice#AbstractChoice(String, Collection)
	 */
	public RadioChoice(final String id, final Collection choices)
	{
		super(id, choices);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(String, IChoiceList)
	 */
	public RadioChoice(final String id, final IChoiceList choices)
	{
		super(id, choices);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(String, IModel, Collection)
	 */
	public RadioChoice(final String id, IModel model, final Collection choices)
	{
		super(id, model, choices);
	}

	/**
	 * @see AbstractChoice#AbstractChoice(String, IModel, IChoiceList)
	 */
	public RadioChoice(final String id, IModel model, final IChoiceList choices)
	{
		super(id, model, choices);
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
		final IChoiceList choices = getChoices();

		// Loop through choices
		for (int i = 0; i < choices.size(); i++)
		{
			// Get next choice
			final IChoice choice = choices.get(i);

			// Get label for choice
			final String label = choice.getDisplayValue();

			// If there is a display value for the choice, then we know that the
			// choice is automatic in some way. If label is /null/ then we know
			// that the choice is a manually created radio tag at some random
			// location in the page markup!
			if (label != null)
			{
				// Append option suffix
				buffer.append(getPrefix());

				// Add radio tag
				buffer.append("<input name=\"" + getPath() + "\"" + " type=\"radio\""
						+ (isSelected(choice) ? " checked" : "") + " value=\"" + choice.getId()
						+ "\">");

				// Add label for radio button
				buffer.append(getLocalizer().getString(getId() + "." + label, this, label));

				// Append option suffix
				buffer.append(getSuffix());
			}
		}

		// Replace body
		replaceComponentTagBody(markupStream, openTag, buffer.toString());
	}
}
