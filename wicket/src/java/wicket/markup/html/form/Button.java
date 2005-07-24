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
import wicket.version.undo.Change;

/**
 * A form button.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public class Button extends FormComponent
{
	/**
	 * Indicates that this button should be called without any validation done.
	 * By default, immediate is false.
	 */
	private Boolean immediate;

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
	 * @see wicket.Component#initModel()
	 */
	protected IModel initModel()
	{
		// Buttons don't have models and so we don't want
		// Component.initModel() to try to attach one automatically.
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
		String lowerCaseTagName = tag.getName().toLowerCase();
		if (! ( lowerCaseTagName.equals("input") ||
				lowerCaseTagName.equals("button") ))
		{
			findMarkupStream().throwMarkupException(
					"Component " + getId() +
					" must be applied to a tag of type 'input' or 'button', not " +
					tag.toUserDebugString());
		}

		// Get tag attributes
		final ValueMap attributes = tag.getAttributes();

		// Check for type of button, image or submit
		final String type = attributes.getString("type");
		if (type == null ||
				!(type.equalsIgnoreCase("button") || type.equalsIgnoreCase("image") ||
						type.equalsIgnoreCase("submit")) )
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

	/**
	 * Gets the immediate.
	 * @return immediate
	 */
	public final boolean isImmediate()
	{
		return (immediate != null) ? immediate.booleanValue() : false;
	}

	/**
	 * Sets the immediate.
	 * @param immediate immediate
	 * @return This
	 */
	public final Button setImmediate(boolean immediate)
	{
		if (this.immediate != null)
		{
			if (this.immediate.booleanValue() != immediate)
			{
				addStateChange(new Change()
				{
					boolean formerValue = Button.this.immediate.booleanValue();

					public void undo()
					{
						Button.this.immediate = (formerValue) ? Boolean.TRUE : Boolean.FALSE;
					}
				});
			}
		}
		else
		{
			this.immediate = (immediate) ? Boolean.TRUE : Boolean.FALSE;
		}

		return this;
	}
}