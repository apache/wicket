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

import wicket.markup.ComponentTag;
import wicket.model.IModel;
import wicket.util.value.ValueMap;
import wicket.version.undo.Change;

/**
 * A form button.
 *<p>
 * Within a form, you can nest Button components. Note that you don't have to do this to
 * let the form work (a simple &lt;input type="submit".. suffices), but if you want to have
 * different kinds of submit behaviour it might be a good idea to use Buttons.
 *</p>
 *<p>
 * When you add a Wicket Button to a form, and that button is clicked, by default the button's
 * onSubmit method is called first, and after that the form's onSubmit button is called.
 * If you want to change this (e.g. you don't want to call the form's onSubmit method, or you
 * want it called before the button's onSubmit method), you can override Form.delegateSubmit.
 *</p>
 *<p>
 * One other option you should know of is the 'immediate' property of Button components.
 * When you set this to true (default is false), all validation and formupdating is bypassed
 * and the onSubmit method of that button is called directly, and the onSubmit method of the
 * parent form is not called. A common use for this is to create a cancel button.
 *</p>
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
		// Get tag attributes
		final ValueMap attributes = tag.getAttributes();

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
	public void updateModel()
	{
	}

	/**
	 * Gets the immediate property. When true (default is false), all validation and
	 * formupdating is bypassed and the onSubmit method of that button is called directly,
	 * and the onSubmit method of the parent form is not called. A common use for this is
	 * to create a cancel button.
	 * @return immediate
	 */
	public final boolean isImmediate()
	{
		return (immediate != null) ? immediate.booleanValue() : false;
	}

	/**
	 * Sets the immediate property. When true (default is false), all validation and
	 * formupdating is bypassed and the onSubmit method of that button is called directly,
	 * and the onSubmit method of the parent form is not called. A common use for this is
	 * to create a cancel button.
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