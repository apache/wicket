/*
 * $Id$ $Revision:
 * 1.19 $ $Date$
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
import wicket.util.string.StringValueConversionException;
import wicket.util.string.Strings;

/**
 * HTML checkbox input component.
 * 
 * @author Jonathan Locke
 */
public class CheckBox extends FormComponent
{
	/**
	 * @see wicket.Component#Component(String)
	 */
	public CheckBox(final String id)
	{
		super(id);
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public CheckBox(final String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * @see FormComponent#setModelValue(java.lang.String)
	 */
	public final void setModelValue(String value)
	{
		try
		{
			setModelObject(Strings.toBoolean(value));
		}
		catch (StringValueConversionException e)
		{
			throw new WicketRuntimeException("Invalid boolean value \"" + value + "\"");
		}
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected final void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "checkbox");

		final String value = getValue();
		if (value != null)
		{
			try
			{
				if (Strings.isTrue(value))
				{
					tag.put("checked", "checked");
				}
				else
				{
					// In case the attribute was added at design time
					tag.remove("checked");
				}
			}
			catch (StringValueConversionException e)
			{
				throw new WicketRuntimeException("Invalid boolean value \"" + value + "\"", e);
			}
		}

		super.onComponentTag(tag);
	}

	/**
	 * @see FormComponent#supportsPersistence()
	 */
	protected final boolean supportsPersistence()
	{
		return true;
	}

	/**
	 * Updates this components' model from the request.
	 * 
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	protected void updateModel()
	{
		try
		{
			setModelObject(Strings.toBoolean(getInput()));
		}
		catch (StringValueConversionException e)
		{
			throw new WicketRuntimeException("Invalid boolean input value posted \"" + getInput() + "\"");
		}
	}
}
