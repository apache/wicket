/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 07:46:36 +0200 (vr, 26 mei 2006) $
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

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.model.IModel;
import wicket.version.undo.Change;

/**
 * A form button.
 * <p>
 * Within a form, you can nest Button components. Note that you don't have to do
 * this to let the form work (a simple &lt;input type="submit".. suffices), but
 * if you want to have different kinds of submit behavior it might be a good
 * idea to use Buttons.
 * </p>
 * <p>
 * The model property is used to set the &quot;value&quot; attribute. It will
 * thus be the label of the button that shows up for end users. If you want the
 * attribute to keep it's markup attribute value, don't provide a model, or let
 * it return an empty string.
 * </p>
 * <p>
 * When you add a Wicket Button to a form, and that button is clicked, by
 * default the button's onSubmit method is called first, and after that the
 * form's onSubmit method is called. If you want to change this (e.g. you don't
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
 * @param <T>
 *            The type
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public abstract class Button<T> extends FormComponent<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * If false, all standard processing like validating and model updating is
	 * skipped.
	 */
	private boolean defaultFormProcessing = true;

	/**
	 * Constructor without a model. Buttons without models leave the markup
	 * attribute &quot;value&quot;. Provide a model if you want to set the
	 * button's label dynamically.
	 * 
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public Button(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * Constructor taking an model for rendering the 'label' of the button (the
	 * value attribute of the input/button tag). Use a
	 * {@link wicket.model.StringResourceModel} for a localized value.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            Component id
	 * @param model
	 *            The model property is used to set the &quot;value&quot;
	 *            attribute. It will thus be the label of the button that shows
	 *            up for end users. If you want the attribute to keep it's
	 *            markup attribute value, don't provide a model, or let it
	 *            return an empty string.
	 */
	public Button(MarkupContainer parent, final String id, final IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * Override of the default initModel behaviour. This component <strong>will
	 * not</strong> use any compound model a parent, but only a model that is
	 * explicitly set.
	 * 
	 * @see wicket.Component#initModel()
	 */
	@Override
	protected IModel<T> initModel()
	{
		return null;
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
	 * Sets the defaultFormProcessing property. When false (default is true),
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
				private static final long serialVersionUID = 1L;

				boolean formerValue = Button.this.defaultFormProcessing;

				@Override
				public void undo()
				{
					Button.this.defaultFormProcessing = formerValue;
				}

				@Override
				public String toString()
				{
					return "DefaultFormProcessingChange[component: " + getPath()
							+ ", default processing: " + formerValue + "]";
				}
			});
		}

		this.defaultFormProcessing = defaultFormProcessing;
		return this;
	}

	/**
	 * This method does nothing, as any model of a button is only used to
	 * display the button's label (by setting it's markup attribute
	 * &quot;value&quot;).
	 * 
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	@Override
	public void updateModel()
	{
	}

	/**
	 * Gets any script that should rendered as the &quot;onclick&quot; attribute
	 * of the button. Returns null by default, override this method to provide
	 * any script.
	 * 
	 * @return Any onClick JavaScript that should be used, returns null by
	 *         default
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
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		// Default handling for component tag
		super.onComponentTag(tag);

		try
		{
			String value = getModelObjectAsString();
			if (value != null && !"".equals(value))
			{
				tag.put("value", value);
			}
		}
		catch (Exception e)
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
	 * form. It is called whenever the user clicks this particular button.
	 */
	protected abstract void onSubmit(); 

}