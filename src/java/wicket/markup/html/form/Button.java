/*
 * $Id$ $Revision:
 * 1.10 $ $Date$
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

import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.model.IModel;
import wicket.util.string.Strings;
import wicket.util.value.ValueMap;

/**
 * A form button.
 * 
 * @author Jonathan Locke
 */
public class Button extends FormComponent
{
	/**
	 * @see wicket.Component#Component(String)
	 */
	public Button(String id)
	{
		super(id);
	}

	/**
	 * @return Any onClick JavaScript that should be used
	 */
	protected String getOnClickScript()
	{
		return null;
	}

	/**
	 * @see FormComponent#initModel()
	 */
	protected IModel initModel()
	{
		// Buttons don't have models and so we don't want
		// FormComponent.initModel() to try to attach one automatically.
		return null;
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
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
		super.onComponentTag(tag);

		// If the subclass specified javascript, use that
		final String onClickJavaScript = getOnClickScript();
		if (onClickJavaScript != null)
		{
			tag.put("onclick", onClickJavaScript);
		}
	}

	/**
	 * Override this method to provide special submit handling in a multi-button
	 * form
	 */
	protected void onSubmit()
	{
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	protected void updateModel()
	{
	}
}