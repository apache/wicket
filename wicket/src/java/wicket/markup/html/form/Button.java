/*
 * $Id$ $Revision$
 * $Date$
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

import wicket.markup.ComponentTag;
import wicket.model.IModel;
import wicket.util.value.ValueMap;
import wicket.version.undo.Change;

/**
 * A form button.
 * <p>
 * Within a form, you can nest Button components. Note that you don't have to do
 * this to let the form work (a simple &lt;input type="submit".. suffices), but
 * if you want to have different kinds of submit behaviour it might be a good
 * idea to use Buttons.
 * </p>
 * <p>
 * When you add a Wicket Button to a form, and that button is clicked, by
 * default the button's onSubmit method is called first, and after that the
 * form's onSubmit button is called. If you want to change this (e.g. you don't
 * want to call the form's onSubmit method, or you want it called before the
 * button's onSubmit method), you can override Form.delegateSubmit.
 * </p>
 * <p>
 * One other option you should know of is the 'defaultFormProcessing' property
 * of Button components. When you set this to false (default is true), all
 * validation and formupdating is bypassed and the onSubmit method of that
 * button is called directly, and the onSubmit method of the parent form is not
 * called. A common use for this is to create a cancel button.
 * </p>
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public class Button extends FormComponent
{
	/**
	 * Indicates that this button should be called without any validation done.
	 * By default, defaultFormProcessing is enabled.
	 */
	private boolean defaultFormProcessing = true;

	/**
	 * @see wicket.Component#Component(String)
	 */
	public Button(String id)
	{
		super(id);
	}
	
	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public Button(final String id, final IModel object)
	{
		super(id, object);
	}
	

	/**
	 * Gets the defaultFormProcessing property. When false (default is true),
	 * all validation and formupdating is bypassed and the onSubmit method of
	 * that button is called directly, and the onSubmit method of the parent
	 * form is not called. A common use for this is to create a cancel button.
	 * 
	 * @return defaultFormProcessing
	 */
	public final boolean getDefaultFormProcessing()
	{
		return defaultFormProcessing;
	}

	/**
	 * Sets the defaultFormProcessing property. When true (default is false),
	 * all validation and formupdating is bypassed and the onSubmit method of
	 * that button is called directly, and the onSubmit method of the parent
	 * form is not called. A common use for this is to create a cancel button.
	 * 
	 * @param defaultFormProcessing
	 *            defaultFormProcessing
	 * @return This
	 */
	public final Button setDefaultFormProcessing(boolean defaultFormProcessing)
	{
		if (this.defaultFormProcessing != defaultFormProcessing)
		{
			addStateChange(new Change()
			{
				boolean formerValue = Button.this.defaultFormProcessing;

				public void undo()
				{
					Button.this.defaultFormProcessing = formerValue;
				}
			});
		}

		this.defaultFormProcessing = defaultFormProcessing;
		return this;
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	public void updateModel()
	{
	}

	/**
	 * @return Any onClick JavaScript that should be used
	 */
	protected String getOnClickScript()
	{
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
		// Get tag attributes
		final ValueMap attributes = tag.getAttributes();

		// Default handling for component tag
		super.onComponentTag(tag);

		try
		{
			String value = getValue();
			if(value != null)
			{
				tag.put("value", getValue());
			}
		} 
		catch(Exception e) 
		{
			// ignore.
		}
		
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
}