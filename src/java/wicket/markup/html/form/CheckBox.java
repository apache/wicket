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

import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.model.IModel;
import wicket.util.string.StringValueConversionException;
import wicket.util.string.Strings;

/**
 * HTML checkbox input component.
 * <p>
 * Java:
 * <pre>
 * form.add(new CheckBox("bool"));
 * </pre>
 * HTML:
 * <pre>
 * &lt;input type="checkbox" wicket:id="bool" /&gt;
 * </pre>
 * </p>
 * <p>
 * You can can extend this class and override method wantOnSelectionChangedNotifications()
 * to force server roundtrips on each selection change.
 * </p>
 * 
 * @author Jonathan Locke
 */
public class CheckBox extends FormComponent implements IOnChangeListener
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
	 *            The newly selected object of the backing model NOTE this is
	 *            the same as you would get by calling getModelObject() if the
	 *            new selection were current
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

		// Should a roundtrip be made (have onSelectionChanged called) when the checkbox is clicked?
		if (wantOnSelectionChangedNotifications())
		{
			final String url = urlFor(IOnChangeListener.class);

			// NOTE: do not encode the url as that would give invalid JavaScript
			tag.put("onclick", "location.href='" + url + "&" + getInputName()
					+ "=' + this.checked;");
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
	public void updateModel()
	{
		// TODO can't test here for disabled input.. null value is a valid input for checkbox
		try
		{
			setModelObject(Strings.toBoolean(getInput()));
		}
		catch (StringValueConversionException e)
		{
			throw new WicketRuntimeException("Invalid boolean input value posted \"" + getInput() + "\"");
		}
	}

	static
	{
		// Allow optional use of the IOnChangeListener interface
		RequestCycle.registerRequestListenerInterface(IOnChangeListener.class);
	}
}
