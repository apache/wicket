/*
 * $Id$ $Revision:
 * 1.10 $ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.util.string.Strings;
import wicket.util.value.ValueMap;

/**
 * A form button.
 * 
 * @author Jonathan Locke
 */
public class Button extends FormComponent
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -2913294206388017417L;

	/**
	 * @see wicket.Component#Component(String)
	 */
	public Button(String name)
	{
		super(name);
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	protected void updateModel()
	{
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#handleComponentTag(ComponentTag)
	 */
	protected final void handleComponentTag(final ComponentTag tag)
	{
		// Must be attached to an input tag
		checkComponentTag(tag, "input");

		// Get tag attributes
		final ValueMap attributes = tag.getAttributes();

		// Check for type of button, image or submit
		final String type = attributes.getString("type");
		if (type == null
				|| (!type.equalsIgnoreCase("button") && !type.equalsIgnoreCase("image") && !type
						.equalsIgnoreCase("submit")))
		{
			throw new WicketRuntimeException(
					"Button tag must have a type of 'button', 'image' or 'submit'");
		}

		// Check for non-empty value 
		final String value = tag.getAttributes().getString("value");
		if (Strings.isEmpty(value))
		{
			throw new WicketRuntimeException("Button tag must have non-empty value attribute");
		}

		// Default handling for component tag
		super.handleComponentTag(tag);
	}
	
	/**
	 * Override this method to provide special submit handling in a multi-button form
	 */
	protected void onSubmit()
	{
	}
}