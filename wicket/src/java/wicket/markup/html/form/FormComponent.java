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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wicket.Component;
import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.validation.IValidator;
import wicket.markup.html.form.validation.TypeValidator;
import wicket.model.IModel;
import wicket.util.string.StringList;

/**
 * An HTML form component knows how to validate itself. Validators that
 * implement IValidator can be added to the component. They will be evaluated in
 * the order they were added and the first Validator that returns an error
 * message determines the error message returned by the component.
 * <p>
 * FormComponents are not versioned by default. If you need versioning for your
 * FormComponents, you will need to call Form.setVersioned(true), which will set
 * versioning on for the form and all form component children.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public abstract class FormComponent extends WebMarkupContainer
{
	/**
	 * Make empty strings null values boolean. Used by AbstractTextComponent
	 * subclass.
	 */
	protected static final short FLAG_CONVERT_EMPTY_INPUT_STRING_TO_NULL = FLAG_RESERVED1;

	/**
	 * Special flag value to indicate when there is no invalid input, since null
	 * is a valid value!
	 */
	protected static final String NO_INVALID_INPUT = "[No invalid input]";

	/**
	 * Whether this form component should save and restore state between
	 * sessions. This is false by default.
	 */
	private static final short FLAG_PERSISTENT = FLAG_RESERVED2;

	/**
	 * When the user input does not validate, this is a temporary store for the
	 * input he/she provided. We have to store it somewhere as we loose the
	 * request parameter when redirecting.
	 */
	private String invalidInput = NO_INVALID_INPUT;

	/**
	 * The list of validators for this form component as either an IValidator
	 * instance or an array of IValidator instances.
	 */
	private Object validators = null;

	/**
	 * Typesafe interface to code that is called when visiting a form component
	 * 
	 * @author Jonathan Locke
	 */
	public interface IVisitor
	{
		/**
		 * Called when visiting a form component
		 * 
		 * @param formComponent
		 *            The form component
		 */
		public void formComponent(FormComponent formComponent);
	}

	/**
	 * @see wicket.Component#Component(String)
	 */
	public FormComponent(final String id)
	{
		super(id);
		setVersioned(false);
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public FormComponent(final String id, IModel model)
	{
		super(id, model);
		setVersioned(false);
	}

	/**
	 * Adds a validator to this form component.
	 * 
	 * @param validator
	 *            The validator
	 * @return This
	 */
	public final FormComponent add(final IValidator validator)
	{
		validators_add(validator);
		return this;
	}

	/**
	 * @return The parent form for this form component
	 */
	public final Form getForm()
	{
		// Look for parent form
		final Form form = (Form)findParent(Form.class);
		if (form == null)
		{
			throw new WicketRuntimeException("Could not find Form parent for " + this);
		}
		return form;
	}

	/**
	 * Gets the request parameter for this component as a string.
	 * 
	 * @return The value in the request for this component
	 */
	public String getInput()
	{
		return getRequest().getParameter(getInputName());
	}

    /**
     * Gets the string to be used for the <tt>name</tt> attribute of the form element.
     * Generated using the path from the form to the component, excluding the form itself.
     * Override it if you want even a smaller name. E.g. if you know for sure that the id
     * is unique within a form.
     *
     * @return The string to use as the form element's name attribute
     */
    protected String getInputName()
	{
    	String id = getId();
    	final StringBuffer inputName = new StringBuffer(id.length());
    	Component c = this;
    	while (true)
    	{
			inputName.insert(0, id);
			c = c.getParent();
			if (c == null || c instanceof Form || c instanceof Page)
			{
				break;
			}
			inputName.insert(0, ':');
			id = c.getId();
    	}
		return inputName.toString();		
	}

	/**
	 * Gets the type for any TypeValidator assigned to this component.
	 * 
	 * @return Any type assigned to this component via type validation, or null
	 *         if no TypeValidator has been added.
	 */
	public final Class getValidationType()
	{
		// Loop through validators
		final int size = validators_size();
		for (int i = 0; i < size; i++)
		{
			// If validator is a TypeValidator
			final IValidator validator = validators_get(i);
			if (validator instanceof TypeValidator)
			{
				// Return the type validator's type
				return ((TypeValidator)validator).getType();
			}
		}
		return null;
	}
	
	/**
	 * Gets an unmodifiable list of validators for this FormComponent.
	 * 
	 * @return List of validators
	 */
	public final List getValidators()
	{
		final int size = validators_size();
		if (size == 0)
		{
			return Collections.EMPTY_LIST;
		}
		else
		{
			final List list = new ArrayList();
			for (int i = 0; i < size; i++)
			{
				list.add(validators_get(i));
			}
			return Collections.unmodifiableList(list);
		}
	}

	/**
	 * Gets current value for a form component.
	 * 
	 * @return The value
	 */
	public final String getValue()
	{
		return NO_INVALID_INPUT.equals(invalidInput) ? getModelValue() : invalidInput;
	}

	/**
	 * Called to indicate that
	 */
	public final void invalid()
	{
		onInvalid();
	}

	/**
	 * @return True if this component encodes data in a multipart form submit
	 */
	public boolean isMultiPart()
	{
		return false;
	}

	/**
	 * @return True if this component supports persistence AND it has been asked
	 *         to persist itself with setPersistent().
	 */
	public final boolean isPersistent()
	{
		return supportsPersistence() && getFlag(FLAG_PERSISTENT);
	}

	/**
	 * Gets whether this component is 'valid'. Valid in this context means that
	 * no validation errors were reported the last time the form component was
	 * processed. This variable not only is convenient for 'business' use, but
	 * is also nescesarry as we don't want the form component models updated
	 * with invalid input.
	 * 
	 * @return valid whether this component is 'valid'
	 */
	public final boolean isValid()
	{
		return !hasErrorMessage();
	}

	/**
	 * Gets whether this component is to be validated.
	 * 
	 * @return True if this component has one or more validators
	 */
	public final boolean isValidated()
	{
		return this.validators != null;
	}

	/**
	 * Sets the value for a form component.
	 * 
	 * @param value
	 *            The value
	 */
	public void setModelValue(final String value)
	{
		setModelObject(value);
	}

	/**
	 * Sets whether this component is to be persisted.
	 * 
	 * @param persistent
	 *            True if this component is to be persisted.
	 */
	public final void setPersistent(final boolean persistent)
	{
		if (supportsPersistence())
		{
			setFlag(FLAG_PERSISTENT, persistent);
		}
		else
		{
			throw new UnsupportedOperationException("FormComponent " + getClass()
					+ " does not support cookies");
		}
	}

	/**
	 * Implemented by form component subclass to update the form component's
	 * model. DO NOT CALL THIS METHOD DIRECTLY UNLESS YOU ARE SURE WHAT YOU ARE
	 * DOING. USUALLY UPDATING YOUR MODEL IS HANDLED BY THE FORM, NOT DIRECTLY
	 * BY YOU.
	 */
	public abstract void updateModel();

	/**
	 * Called to indicate that
	 */
	public final void valid()
	{
		onValid();
	}

	/**
	 * Validates this component using the component's validator.
	 */
	public final void validate()
	{
		final int size = validators_size();
		for (int i = 0; i < size; i++)
		{
			validators_get(i).validate(this);
		}
	}

	/**
	 * @return Value to return when model value is needed
	 */
	protected String getModelValue()
	{
		return getModelObjectAsString();
	}

	/**
	 * Gets the request parameter for this component as an int.
	 * 
	 * @return The value in the request for this component
	 */
	protected final int inputAsInt()
	{
		final String string = getInput();
		try
		{
			return Integer.parseInt(string);
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException(exceptionMessage("Internal error.  Request string '"
					+ string + "' not a valid integer"));
		}
	}

	/**
	 * Gets the request parameter for this component as an int, using the given
	 * default in case no corresponding request parameter was found.
	 * 
	 * @param defaultValue
	 *            Default value to return if request does not have an integer
	 *            for this component
	 * @return The value in the request for this component
	 */
	protected final int inputAsInt(final int defaultValue)
	{
		final String string = getInput();
		if (string != null)
		{
			try
			{
				return Integer.parseInt(string);
			}
			catch (NumberFormatException e)
			{
				throw new IllegalArgumentException(exceptionMessage("Request string '" + string
						+ "' is not a valid integer"));
			}
		}
		else
		{
			return defaultValue;
		}
	}

	/**
	 * Gets the request parameters for this component as ints.
	 * 
	 * @return The values in the request for this component
	 */
	protected final int[] inputAsIntArray()
	{
		final String[] strings = inputAsStringArray();
		if (strings != null)
		{
			final int[] ints = new int[strings.length];
			for (int i = 0; i < strings.length; i++)
			{
				ints[i] = Integer.parseInt(strings[i]);
			}
			return ints;
		}
		return null;
	}

	/**
	 * Gets the request parameters for this component as strings.
	 * 
	 * @return The values in the request for this component
	 */
	protected final String[] inputAsStringArray()
	{
		return getRequest().getParameters(getInputName());
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
		tag.put("name", getInputName());
		super.onComponentTag(tag);
	}

	/**
	 * Handle invalidation by storing the user input for form repopulation
	 */
	protected void onInvalid()
	{
		// Get input as String array
		final String[] input = inputAsStringArray();

		// If there is any input
		if (input != null)
		{
			// join the values together with ";", for example, "id1;id2;id3"
			invalidInput = StringList.valueOf(input).join(";");
		}
		else
		{
			// no input
			invalidInput = null;
		}
	}

	/**
	 * @see wicket.Component#onModelChanged()
	 */
	protected void onModelChanged()
	{
		// If the model for this form component changed, we should make it
		// valid again because there can't be any invalid input for it anymore.
		valid();
	}

	/**
	 * Handle validation
	 */
	protected void onValid()
	{
		invalidInput = NO_INVALID_INPUT;
	}

	/**
	 * @return True if this type of FormComponent can be persisted.
	 */
	protected boolean supportsPersistence()
	{
		return false;
	}

	/**
	 * @param validator
	 *            The validator to add to the validators Object (which may be an
	 *            array of IValidators or a single instance, for efficiency)
	 */
	private void validators_add(final IValidator validator)
	{
		if (this.validators == null)
		{
			this.validators = validator;
		}
		else
		{
			// Get current list size
			final int size = validators_size();

			// Create array that holds size + 1 elements
			final IValidator[] validators = new IValidator[size + 1];

			// Loop through existing validators copying them
			for (int i = 0; i < size; i++)
			{
				validators[i] = validators_get(i);
			}

			// Add new validator to the end
			validators[size] = validator;

			// Save new validator list
			this.validators = validators;
		}
	}

	/**
	 * Gets validator from validators Object (which may be an array of
	 * IValidators or a single instance, for efficiency) at the given index
	 * 
	 * @param index
	 *            The index of the validator to get
	 * @return The validator
	 */
	private IValidator validators_get(int index)
	{
		if (this.validators == null)
		{
			throw new IndexOutOfBoundsException();
		}
		if (this.validators instanceof IValidator[])
		{
			return ((IValidator[])validators)[index];
		}
		return (IValidator)validators;
	}

	/**
	 * @return The number of validators in the validators Object (which may be
	 *         an array of IValidators or a single instance, for efficiency)
	 */
	private int validators_size()
	{
		if (this.validators == null)
		{
			return 0;
		}
		if (this.validators instanceof IValidator[])
		{
			return ((IValidator[])validators).length;
		}
		return 1;
	}
}