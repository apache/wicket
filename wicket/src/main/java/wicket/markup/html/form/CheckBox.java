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

import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.model.IModel;
import wicket.util.convert.ConversionException;
import wicket.util.string.StringValueConversionException;
import wicket.util.string.Strings;

/**
 * HTML checkbox input component.
 * <p>
 * Java:
 * 
 * <pre>
 * form.add(new CheckBox(&quot;bool&quot;));
 * </pre>
 * 
 * HTML:
 * 
 * <pre>
 *  &lt;input type=&quot;checkbox&quot; wicket:id=&quot;bool&quot; /&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * You can can extend this class and override method
 * wantOnSelectionChangedNotifications() to force server roundtrips on each
 * selection change.
 * </p>
 * 
 * @author Jonathan Locke
 */
public class CheckBox extends FormComponent implements IOnChangeListener
{
	private static final long serialVersionUID = 1L;

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
	 * @see wicket.markup.html.form.IOnChangeListener#onSelectionChanged()
	 */
	public void onSelectionChanged()
	{
		convert();
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
	 * Whether this component's onSelectionChanged event handler should called
	 * using javascript if the selection changes. If true, a roundtrip will be
	 * generated with each selection change, resulting in the model being
	 * updated (of just this component) and onSelectionChanged being called.
	 * This method returns false by default.
	 * 
	 * @return True if this component's onSelectionChanged event handler should
	 *         called using javascript if the selection changes
	 */
	protected boolean wantOnSelectionChangedNotifications()
	{
		return false;
	}

	/**
	 * @see wicket.MarkupContainer#getStatelessHint()
	 */
	protected boolean getStatelessHint()
	{
		if (wantOnSelectionChangedNotifications())
		{
			return false;
		}
		return super.getStatelessHint();
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

		// Should a roundtrip be made (have onSelectionChanged called) when the
		// checkbox is clicked?
		if (wantOnSelectionChangedNotifications())
		{
			final CharSequence url = urlFor(IOnChangeListener.INTERFACE);

			Form form = (Form)findParent(Form.class);
			if (form != null)
			{
				tag.put("onclick", form.getJsForInterfaceUrl(url));
			}
			else
			{
				// NOTE: do not encode the url as that would give invalid
				// JavaScript
				tag.put("onclick", "window.location.href='" + url + "&" + getInputName()
						+ "=' + this.checked;");
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
	 * @see wicket.markup.html.form.FormComponent#convertValue(String[])
	 */
	protected Object convertValue(String[] value)
	{
		String tmp = value != null && value.length > 0 ? value[0] : null;
		try
		{
			return Strings.toBoolean(tmp);
		}
		catch (StringValueConversionException e)
		{
			throw new ConversionException("Invalid boolean input value posted \"" + getInput()
					+ "\"", e).setTargetType(Boolean.class);
		}
	}
}
