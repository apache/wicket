/*
 * $Id$
 * $Revision$ $Date$
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

import java.io.Serializable;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.model.IModel;

/**
 * Represents a radio option specified in HTML which can be added to a
 * RadioChoice component. The radio option updates the state of the radio choice
 * when a form submit happens.
 * 
 * @author Jonathan Locke
 */
public class RadioOption extends FormComponent
{
	/** Serial Version ID */
	private static final long serialVersionUID = -2933133745573428936L;

	/** the optional label to use. */
	private String label = null;

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public RadioOption(String name, IModel model)
	{
		super(name, model);
	}

	/**
     * @see wicket.Component#Component(String, IModel, String)
	 */
	public RadioOption(String name, IModel model, String expression)
	{
		super(name, model, expression);
	}

	/**
     * @see wicket.Component#Component(String, Serializable)
	 */
	public RadioOption(String name, Serializable object)
	{
		super(name, object);
	}

	/**
     * @see wicket.Component#Component(String, Serializable, String)
	 */
	public RadioOption(String name, Serializable object, String expression)
	{
		super(name, object, expression);
	}

	/**
     * @param name See Component constructor
	 * @param label Label for option
	 * @param model See Component constructor
	 * @see wicket.Component#Component(String, IModel)
	 */
	public RadioOption(String name, String label, IModel model)
	{
		super(name, model);
		this.label = label;
	}

	/**
     * @param name See Component constructor
     * @param label Label for option
     * @param model See Component constructor
     * @param expression See Component constructor
     * @see wicket.Component#Component(String, IModel, String)
	 */
	public RadioOption(String name, String label, IModel model, String expression)
	{
		super(name, model, expression);
		this.label = label;
	}

	/**
     * @param name See Component constructor
     * @param label Label for option
     * @param object See Component constructor
     * @see wicket.Component#Component(String, Serializable)
	 */
	public RadioOption(String name, String label, Serializable object)
	{
		super(name, object);
		this.label = label;
	}

	/**
     * @param name See Component constructor
     * @param label Label for option
     * @param object See Component constructor
     * @param expression See Component constructor
     * @see wicket.Component#Component(String, Serializable, String)
	 */
	public RadioOption(String name, String label, Serializable object, String expression)
	{
		super(name, object, expression);
		this.label = label;
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	public final void updateModel()
	{
	}

	/**
	 * @see wicket.Container#handleComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	protected final void handleComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		final String displayLabel;

		if (label != null)
		{
			displayLabel = label;
		}
		else
		{
			displayLabel = String.valueOf(getModelObject());
		}

		final String s = getLocalizer().getString(getName() + "." + displayLabel, this, displayLabel);

		replaceComponentTagBody(markupStream, openTag, s);
	}

	/**
	 * @see wicket.Component#handleComponentTag(ComponentTag)
	 */
	protected final void handleComponentTag(final ComponentTag tag)
	{
		// Check that this option is attached to a radio input
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "radio");

		// Let superclass do whatever
		super.handleComponentTag(tag);

		// Find parent RadioChoice
		final RadioChoice parent = (RadioChoice)findParent(RadioChoice.class);

		// Name of this component is the name of the parent
		tag.put("name", parent.getPath());

		Object value = getModelObject();

		// Value is the index of the option when added to the parent
		tag.put("value", parent.addRadioOption(value));

		// Add checked property if this is the selected component
		if (parent.getModelObject() == value)
		{
			tag.put("checked", "true");
		}
	}
}