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
public final class RadioOption extends FormComponent
{ // TODO finalize javadoc
	/** Serial Version ID */
	private static final long serialVersionUID = -2933133745573428936L;

	/** the optional label to use. */
	private String label = null;

	/**
	 * Constructor that uses the provided {@link IModel}as its model and that
	 * uses either the model object's string value as the resource key for the
	 * label or, if the resource key cannot be found, the object's string value
	 * as the label itself. All components have names. A component's name cannot
	 * be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param model
	 *            the model
	 * @throws wicket.WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public RadioOption(String name, IModel model)
	{
		super(name, model);
	}

	/**
	 * Constructor that uses the provided instance of {@link IModel}as a
	 * dynamic model and that uses either the model object's string value as the
	 * resource key for the label or, if the resource key cannot be found, the
	 * object's string value as the label itself. This model will be wrapped in
	 * an instance of {@link wicket.model.PropertyModel}using the provided
	 * expression. Thus, using this constructor is a short-hand for:
	 * 
	 * <pre>
	 * new MyComponent(name, new PropertyModel(myIModel, expression));
	 * </pre>
	 * 
	 * All components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param model
	 *            the instance of {@link IModel}from which the model object
	 *            will be used as the subject for the given expression
	 * @param expression
	 *            the OGNL expression that works on the given object
	 * @throws wicket.WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public RadioOption(String name, IModel model, String expression)
	{
		super(name, model, expression);
	}

	/**
	 * Constructor that uses the provided object as a simple model and that uses
	 * either the model object's string value as the resource key for the label
	 * or, if the resource key cannot be found, the object's string value as the
	 * label itself. This object will be wrapped in an instance of
	 * {@link wicket.model.Model}. All components have names. A component's
	 * name cannot be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param object
	 *            the object that will be used as a simple model
	 * @throws wicket.WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public RadioOption(String name, Serializable object)
	{
		super(name, object);
	}

	/**
	 * Constructor that uses the provided object as a dynamic model and that
	 * uses either the model object's string value as the resource key for the
	 * label or, if the resource key cannot be found, the object's string value
	 * as the label itself. This object will be wrapped in an instance of
	 * {@link wicket.model.Model}that will be wrapped in an instance of
	 * {@link wicket.model.PropertyModel}using the provided expression. Thus,
	 * using this constructor is a short-hand for:
	 * 
	 * <pre>
	 * new MyComponent(name, new PropertyModel(new Model(object), expression));
	 * </pre>
	 * 
	 * All components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param object
	 *            the object that will be used as the subject for the given
	 *            expression
	 * @param expression
	 *            the OGNL expression that works on the given object
	 * @throws wicket.WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public RadioOption(String name, Serializable object, String expression)
	{
		super(name, object, expression);
	}

	/**
	 * Constructor that uses the provided {@link IModel}as its model and the
	 * provided label either as a resource key or as the static label itself.
	 * All components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param label
	 *            the label for this option as either a resource key to lookup
	 *            the label text, or, if that key cannot be found, as the static
	 *            text itself
	 * @param model
	 *            the model
	 * @throws wicket.WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public RadioOption(String name, String label, IModel model)
	{
		super(name, model);
		this.label = label;
	}

	/**
	 * Constructor that uses the provided instance of {@link IModel}as a
	 * dynamic model and the provided label either as a resource key or as the
	 * static label itself. This model will be wrapped in an instance of
	 * {@link wicket.model.PropertyModel}using the provided expression. Thus,
	 * using this constructor is a short-hand for:
	 * 
	 * <pre>
	 * new MyComponent(name, new PropertyModel(myIModel, expression));
	 * </pre>
	 * 
	 * All components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param label
	 *            the label for this option as either a resource key to lookup
	 *            the label text, or, if that key cannot be found, as the static
	 *            text itself
	 * @param model
	 *            the instance of {@link IModel}from which the model object
	 *            will be used as the subject for the given expression
	 * @param expression
	 *            the OGNL expression that works on the given object
	 * @throws wicket.WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public RadioOption(String name, String label, IModel model, String expression)
	{
		super(name, model, expression);
		this.label = label;
	}

	/**
	 * Constructor that uses the provided object as a simple model and the
	 * provided label either as a resource key or as the static label itself.
	 * This object will be wrapped in an instance of {@link wicket.model.Model}.
	 * All components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param label
	 *            the label for this option as either a resource key to lookup
	 *            the label text, or, if that key cannot be found, as the static
	 *            text itself
	 * @param object
	 *            the object that will be used as a simple model
	 * @throws wicket.WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public RadioOption(String name, String label, Serializable object)
	{
		super(name, object);
		this.label = label;
	}

	/**
	 * Constructor that uses the provided object as a dynamic model and the
	 * provided label either as a resource key or as the static label itself.
	 * This object will be wrapped in an instance of {@link wicket.model.Model}
	 * that will be wrapped in an instance of {@link wicket.model.PropertyModel}
	 * using the provided expression. Thus, using this constructor is a
	 * short-hand for:
	 * 
	 * <pre>
	 * new MyComponent(name, new PropertyModel(new Model(object), expression));
	 * </pre>
	 * 
	 * All components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param label
	 *            the label for this option as either a resource key to lookup
	 *            the label text, or, if that key cannot be found, as the static
	 *            text itself
	 * @param object
	 *            the object that will be used as the subject for the given
	 *            expression
	 * @param expression
	 *            the OGNL expression that works on the given object
	 * @throws wicket.WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public RadioOption(String name, String label, Serializable object, String expression)
	{
		super(name, object, expression);
		this.label = label;
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	public void updateModel()
	{
	}

	/**
	 * @see wicket.Container#handleBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	protected void handleBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		Object value = getModelObject();
		final String displayLabel;

		if (label != null)
		{
			displayLabel = label;
		}
		else
		{
			displayLabel = String.valueOf(value);
		}

		String s = getLocalizer().getString(getName() + "." + displayLabel, this, displayLabel);

		replaceBody(markupStream, openTag, s);
	}

	/**
	 * @see wicket.Component#handleComponentTag(ComponentTag)
	 */
	protected void handleComponentTag(final ComponentTag tag)
	{
		// Check that this option is attached to a radio input
		checkTag(tag, "input");
		checkAttribute(tag, "type", "radio");

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


