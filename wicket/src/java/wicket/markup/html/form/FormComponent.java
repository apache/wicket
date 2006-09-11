/*
 * $Id: FormComponent.java 4952 2006-03-15 05:21:36 -0800 (Wed, 15 Mar 2006)
 * joco01 $ $Revision$ $Date: 2006-03-15 05:21:36 -0800 (Wed, 15 Mar
 * 2006) $
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

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.Localizer;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.convert.ConversionException;
import wicket.util.convert.IConverter;
import wicket.util.lang.Classes;
import wicket.util.string.PrependingStringBuffer;
import wicket.util.string.StringList;
import wicket.util.string.Strings;
import wicket.util.string.interpolator.MapVariableInterpolator;
import wicket.validation.IMessageSource;
import wicket.validation.IValidatable;
import wicket.validation.IValidationError;
import wicket.validation.IValidator;
import wicket.validation.ValidationError;
import wicket.version.undo.Change;

/**
 * An HTML form component knows how to validate itself. Validators that
 * implement IValidator can be added to the component. They will be evaluated in
 * the order they were added and the first Validator that returns an error
 * message determines the error message returned by the component.
 * <p>
 * FormComponents are not versioned by default. If you need versioning for your
 * FormComponents, you will need to call Form.setVersioned(true), which will set
 * versioning on for the form and all form component children.
 * <p>
 * If this component is required and that fails, the error key that is used is
 * the "RequiredValidator"; if the type conversion fails, it will use the key
 * "TypeValidator".
 * 
 * FIXME 2.0: johan: document TypeValidator.type-name key and Conversion,
 * Conversion.type-name keys
 * 
 * <p>
 * Resolution of error messages:
 * <ul>
 * <li> Error keys tried by this message source are:
 * <ol>
 * <li><code>prefix + getId() + "." + key</code></li>
 * <li><code>prefix + key</code></li>
 * </ol>
 * where prefix is the result of {@link FormComponent#getValidatorKeyPrefix()}
 * </li>
 * <li> The following variable are added (if not already present) to the
 * variable substituion map:
 * <ul>
 * <li>input - the raw string value entered by the user</li>
 * <li>name - the id of the this form component</li>
 * <li>label - the label of the component - either comes from
 * FormComponent.labelModel or resource key [form-component-id] in that order</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * 
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class FormComponent<T> extends WebMarkupContainer<T>
{
	private static final long serialVersionUID = 1L;

	private static final String[] EMPTY_STRING_ARRAY = new String[] { "" };

	/**
	 * The value separator
	 */
	public static String VALUE_SEPARATOR = ";";

	/**
	 * Typesafe interface to code that is called when visiting a form component.
	 */
	public static interface IVisitor
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
	 * Change object to capture the required flag change
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 */
	private final class RequiredStateChange extends Change
	{
		private static final long serialVersionUID = 1L;

		private final boolean required = isRequired();

		/**
		 * @see wicket.version.undo.Change#undo()
		 */
		@Override
		public void undo()
		{
			setRequired(required);
		}
	}

	/**
	 * Attribute modifier model that returns 'disabled' if a form component is
	 * disabled or null otherwise (resulting in no attribute being appended).
	 */
	private final class DisabledAttributeModel extends Model<String>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @see wicket.model.IModel#getObject()
		 */
		@Override
		public String getObject()
		{
			return (FormComponent.this.isActionAuthorized(ENABLE) && FormComponent.this.isEnabled()
					? null
					: "disabled");
		}
	}

	/**
	 * Attribute modifier that adds 'disabled="disabled"' to the component tag's
	 * attribute if a form component is disabled.
	 */
	private final class DisabledAttributeModifier extends AttributeModifier
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param model
		 */
		public DisabledAttributeModifier(DisabledAttributeModel model)
		{
			super("disabled", true, model);
		}
	}

	/**
	 * Type that the raw input string will be converted to
	 */
	private Class type;

	/**
	 * Make empty strings null values boolean. Used by AbstractTextComponent
	 * subclass.
	 */
	protected static final short FLAG_CONVERT_EMPTY_INPUT_STRING_TO_NULL = FLAG_RESERVED1;

	/**
	 * Whether this form component should save and restore state between
	 * sessions. This is false by default.
	 */
	private static final short FLAG_PERSISTENT = FLAG_RESERVED2;


	/** Whether or not this component's value is required (non-empty) */
	private static final short FLAG_REQUIRED = FLAG_RESERVED3;

	private static final String NO_RAW_INPUT = "[-NO-RAW-INPUT-]";

	/**
	 * Raw Input entered by the user or NO_RAW_INPUT if nothing is filled in.
	 */
	private String rawInput = NO_RAW_INPUT;

	/**
	 * The list of validators for this form component as either an IValidator
	 * instance or an array of IValidator instances.
	 */
	private Object validators = null;

	/**
	 * The value will be made available to the validator property by means of
	 * ${label}. It does not have any specific meaning to FormComponent itself.
	 */
	private IModel labelModel = null;

	private transient T convertedInput;

	/**
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public FormComponent(MarkupContainer parent, final String id)
	{
		super(parent, id);
		add(new DisabledAttributeModifier(new DisabledAttributeModel()));
		// the form decides whether form components are versioned or not
		// see Form.setVersioned
		setVersioned(false);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public FormComponent(MarkupContainer parent, final String id, IModel<T> model)
	{
		super(parent, id, model);
		add(new DisabledAttributeModifier(new DisabledAttributeModel()));
		// the form decides whether form components are versioned or not
		// see Form.setVersioned
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
	public Form getForm()
	{
		// Look for parent form
		final Form form = findParent(Form.class);
		if (form == null)
		{
			throw new WicketRuntimeException("Could not find Form parent for " + this);
		}
		return form;
	}

	/**
	 * The value will be made available to the validator property by means of
	 * ${label}. It does not have any specific meaning to FormComponent itself.
	 * 
	 * @param labelModel
	 * @return this for chaining
	 */
	public FormComponent setLabel(final IModel labelModel)
	{
		this.labelModel = labelModel;
		return this;
	}

	/**
	 * The value will be made available to the validator property by means of
	 * ${label}. It does not have any specific meaning to FormComponent itself.
	 * 
	 * @return labelModel
	 */
	public IModel getLabel()
	{
		return this.labelModel;
	}

	/**
	 * Gets the request parameter for this component as a string.
	 * 
	 * @return The value in the request for this component
	 */
	public final String getInput()
	{
		String[] input = getInputAsArray();
		if (input == null || input.length == 0)
		{
			return null;
		}
		else
		{
			return input[0];
		}
	}

	/**
	 * Gets the request parameters for this component as strings.
	 * 
	 * @return The values in the request for this component
	 */
	public String[] getInputAsArray()
	{
		String[] values = getRequest().getParameters(getInputName());
		if (!isInputNullable())
		{
			if (values != null && values.length == 1 && values[0] == null)
			{
				// we the key got passed in (otherwise values would be null),
				// but the value was set to null.
				// As the servlet spec isn't clear on what to do with 'empty'
				// request values - most return an empty string, but some null -
				// we have to workaround here and deliberately set to an empty
				// string if the the component is not nullable (text components)
				return EMPTY_STRING_ARRAY;
			}
		}
		return values;
	}

	/**
	 * Gets the string to be used for the <tt>name</tt> attribute of the form
	 * element. Generated using the path from the form to the component,
	 * excluding the form itself. Override it if you want even a smaller name.
	 * E.g. if you know for sure that the id is unique within a form.
	 * 
	 * @return The string to use as the form element's name attribute
	 */
	public String getInputName()
	{
		String id = getId();
		final PrependingStringBuffer inputName = new PrependingStringBuffer(id.length());
		Component c = this;
		while (true)
		{
			inputName.prepend(id);
			c = c.getParent();
			if (c == null || c instanceof Form || c instanceof Page)
			{
				break;
			}
			inputName.prepend(Component.PATH_SEPARATOR);
			id = c.getId();
		}
		return inputName.toString();
	}

	/**
	 * Gets an unmodifiable list of validators for this FormComponent.
	 * 
	 * @return List of validators
	 */
	public final List<IValidator> getValidators()
	{
		final int size = validators_size();
		if (size == 0)
		{
			return Collections.emptyList();
		}
		else
		{
			final List<IValidator> list = new ArrayList<IValidator>();
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
		if (NO_RAW_INPUT.equals(rawInput))
		{
			return getModelValue();
		}
		else
		{
			if (getEscapeModelStrings() && rawInput != null)
			{
				return Strings.escapeMarkup(rawInput).toString();
			}
			return rawInput;
		}
	}

	/**
	 * Use hasRawInput() to check if this component has raw input because null
	 * can mean 2 things: It doesn't have rawinput or the rawinput is really
	 * null.
	 * 
	 * @return The raw form input that is stored for this formcomponent
	 */
	public final String getRawInput()
	{
		return rawInput == NO_RAW_INPUT ? null : rawInput;
	}

	/**
	 * This method can be called to know if this component really has raw input.
	 * 
	 * @return boolean if this form component has rawinput.
	 */
	public final boolean hasRawInput()
	{
		return rawInput != NO_RAW_INPUT;
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
	 * Gets whether this component's input can be null. By default, components
	 * that do not get input will have null values passed in for input. However,
	 * component TextField is an example (possibly the only one) that never gets
	 * a null passed in, even if the field is left empty UNLESS it had attribute
	 * <code>disabled="disabled"</code> set.
	 * 
	 * @return True if this component's input can be null. Returns true by
	 *         default.
	 */
	public boolean isInputNullable()
	{
		return true;
	}

	/**
	 * Sets the value for a form component this value will be split the string
	 * with {@link FormComponent#VALUE_SEPARATOR} and calls
	 * setModelValue(String[]) with that.
	 * 
	 * @param value
	 *            The value
	 * 
	 * @depricated call or override setModelValue(String[])
	 */
	public void setModelValue(final String value)
	{
		setModelValue(value.split(VALUE_SEPARATOR));
	}

	/**
	 * Sets the value for a form component.
	 * 
	 * @param value
	 *            The value
	 */
	public void setModelValue(final String[] value)
	{
		convertedInput = convertValue(value);
		updateModel();
	}

	/**
	 * Sets whether this component is to be persisted.
	 * 
	 * @param persistent
	 *            True if this component is to be persisted.
	 * @return this for chaining
	 */
	public final FormComponent setPersistent(final boolean persistent)
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
		return this;
	}

	/**
	 * Updates this components' model from the request, it expect that the
	 * object is already converted through the convert() call. By default it
	 * just does this:
	 * 
	 * <pre>
	 * setModelObject(getConvertedInput());
	 * </pre>
	 * 
	 * DO NOT CALL THIS METHOD DIRECTLY UNLESS YOU ARE SURE WHAT YOU ARE DOING.
	 * USUALLY UPDATING YOUR MODEL IS HANDLED BY THE FORM, NOT DIRECTLY BY YOU.
	 */
	public void updateModel()
	{
		setModelObject(getConvertedInput());
	}

	/**
	 * Called to indicate that the user input is valid.
	 */
	public final void valid()
	{
		clearInput();

		onValid();
	}

	/**
	 * Clears the user input.
	 */
	public final void clearInput()
	{
		rawInput = NO_RAW_INPUT;
	}

	/**
	 * Checks if the form component's 'required' requirement is met
	 * 
	 * @return true if the 'required' requirement is met, false otherwise
	 */
	public final boolean checkRequired()
	{
		if (isRequired())
		{
			final String input = getInput();

			// when null, check whether this is natural for that component, or
			// whether - as is the case with text fields - this can only happen
			// when the component was disabled
			if (input == null && !isInputNullable())
			{
				// this value must have come from a disabled field
				// do not perform validation
				return true;
			}

			// peform validation by looking whether the value is null or empty
			if (Strings.isEmpty(input))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the raw input value is not null if this component is required
	 */
	protected final void validateRequired()
	{
		if (!checkRequired())
		{
			error(new ValidationError().addMessageKey("RequiredValidator"));
		}
	}


	/**
	 * Converts and validates the conversion of the raw input string into the
	 * object specified by {@link FormComponent#getType()} and records any
	 * errors. Converted value is available thorugh
	 * {@link FormComponent#getConvertedInput()}
	 */
	@SuppressWarnings("unchecked")
	protected final void convert()
	{
		if (type == null)
		{
			try
			{
				convertedInput = convertValue(getInputAsArray());
			}
			catch (ConversionException e)
			{
				ValidationError error = new ValidationError();
				if (e.getTargetType() != null)
				{
					error.addMessageKey("ConversionError." + Classes.simpleName(e.getTargetType()));
				}
				error.addMessageKey("ConversionError");

				final Locale locale = e.getLocale();
				if (locale != null)
				{
					error.setVar("locale", locale);
				}
				error.setVar("exception", e);
				Format format = e.getFormat();
				if (format instanceof SimpleDateFormat)
				{
					error.setVar("format", ((SimpleDateFormat)format).toLocalizedPattern());
				}

				error(error);

			}
		}
		else if (!Strings.isEmpty(getInput()))
		{
			final IConverter converter = getConverter(type);
			try
			{
				convertedInput = (T)converter.convertToObject(getInput(), getLocale());
			}
			catch (ConversionException e)
			{
				ValidationError error = new ValidationError();
				error.addMessageKey("TypeValidator." + Classes.simpleName(type));
				error.addMessageKey("TypeValidator");


				error.setVar("type", Classes.simpleName(type));
				final Locale locale = e.getLocale();
				if (locale != null)
				{
					error.setVar("locale", locale);
				}
				error.setVar("exception", e);
				Format format = e.getFormat();
				if (format instanceof SimpleDateFormat)
				{
					error.setVar("format", ((SimpleDateFormat)format).toLocalizedPattern());
				}

				error(error);
			}
		}
	}

	/**
	 * Subclasses should overwrite this if the conversion is not done through
	 * the type field and the IConverter. <strong>WARNING: this method may be
	 * removed in future versions.</strong>
	 * 
	 * If conversion fails then a ConversionException should be thrown
	 * 
	 * @param value
	 *            The value can be the getInput() or through a cookie
	 * 
	 * @return The converted value. default returns just the given value
	 * @throws ConversionException
	 *             If input can't be converted
	 */
	@SuppressWarnings("unchecked")
	protected T convertValue(String[] value) throws ConversionException
	{
		return (T)(value != null && value.length > 0 && value[0] != null ? value[0].trim() : null);
	}

	/**
	 * Performs full validation of the form component, which consists of calling
	 * validateRequired(), validateTypeConversion(), and validateValidators().
	 * This method should only be used if the form component needs to be fully
	 * validated outside the form process.
	 */
	public final void validate()
	{
		validateRequired();
		convert();
		validateValidators();
	}

	/**
	 * Validates this component using the component's validators.
	 */
	@SuppressWarnings("unchecked")
	protected final void validateValidators()
	{
		final int size = validators_size();

		final IValidatable<T> validatable = new ValidatableAdapter();

		int i = 0;
		IValidator<T> validator = null;

		try
		{
			for (i = 0; i < size; i++)
			{
				validator = validators_get(i);
				validator.validate(validatable);
				if (!isValid())
				{
					break;
				}
			}
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Exception '" + e + "' occurred during validation "
					+ validator.getClass().getName() + " on component " + this.getPath(), e);
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
		final String[] strings = getInputAsArray();
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
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		tag.put("name", getInputName());
		super.onComponentTag(tag);
	}

	/**
	 * Handle invalidation
	 */
	protected void onInvalid()
	{
	}


	/**
	 * @see wicket.Component#internalOnModelChanged()
	 */
	@Override
	protected void internalOnModelChanged()
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

	/**
	 * Used by Form to tell the FormComponent that a new user input is available
	 */
	public final void inputChanged()
	{
		if (isVisibleInHierarchy() && isEnabled())
		{
			// Get input as String array
			final String[] input = getInputAsArray();

			// If there is any input
			if (input != null && input.length > 0 && input[0] != null)
			{
				// join the values together with ";", for example, "id1;id2;id3"
				rawInput = StringList.valueOf(input).join(VALUE_SEPARATOR);
			}
			else if (isInputNullable())
			{
				// no input
				rawInput = null;
			}
			else
			{
				rawInput = NO_RAW_INPUT;
			}
		}
	}

	/**
	 * Sets the required flag
	 * 
	 * @param required
	 * @return this for chaining
	 */
	public final FormComponent setRequired(final boolean required)
	{
		if (!required && type != null && type.isPrimitive())
		{
			throw new WicketRuntimeException(
					"FormComponent can't be not required when the type is primitive class: " + this);
		}
		if (required != isRequired())
		{
			addStateChange(new RequiredStateChange());
		}
		setFlag(FLAG_REQUIRED, required);
		return this;
	}

	/**
	 * @return whether or not this component's value is required
	 */
	public boolean isRequired()
	{
		return getFlag(FLAG_REQUIRED);
	}

	/**
	 * @return value of input converted into appropriate type if any was set
	 */
	public final T getConvertedInput()
	{
		return convertedInput;
	}

	@Override
	protected void onDetach()
	{
		super.onDetach();
		convertedInput = null;
	}

	/**
	 * @return the type to use when updating the model for this form component
	 */
	public final Class getType()
	{
		return type;
	}

	/**
	 * Sets the type that will be used when updating the model for this
	 * component. If no type is specified String type is assumed.
	 * 
	 * @param type
	 * @return this for chaining
	 */
	public final FormComponent setType(Class type)
	{
		this.type = type;
		if (type != null && type.isPrimitive())
		{
			setRequired(true);
		}
		return this;
	}

	/**
	 * @see Form#getValidatorKeyPrefix()
	 * @return prefix used when constructing validator key messages
	 */
	public String getValidatorKeyPrefix()
	{
		Form form = (Form)findParent(Form.class);
		if (form != null)
		{
			return getForm().getValidatorKeyPrefix();
		}
		return null;
	}

	/**
	 * Reports a validation error against this form component
	 * 
	 * @param error
	 *            validation error
	 */
	public void error(IValidationError error)
	{
		if (error == null)
		{
			throw new IllegalArgumentException("Argument [[error]] cannot be null");
		}

		String message = error.getErrorMessage(new MessageSource());

		if (message == null)
		{
			// XXX maybe make message source remember tried resource keys so a
			// more specific error message can be created
			error("Could not locate error message for error: " + error.toString());
		}
		else
		{
			error(message);
		}

	}

	/**
	 * This method will retrieve the request parameter, validate it, and if
	 * valid update the model. These are the same steps as would be performed by
	 * the form.
	 * 
	 * This is useful when a formcomponent is used outside a form.
	 * 
	 */
	public final void processInput()
	{
		inputChanged();
		validate();
		if (hasErrorMessage())
		{
			invalid();
		}
		else
		{
			valid();
			updateModel();
		}
	}

	/**
	 * Adapter that makes this component appear as {@link IValidatable}
	 * 
	 * @author ivaynberg
	 */
	private class ValidatableAdapter implements IValidatable<T>
	{

		/**
		 * @see wicket.validation.IValidatable#error(wicket.validation.IValidationError)
		 */
		public void error(IValidationError error)
		{
			FormComponent.this.error(error);
		}

		/**
		 * @see wicket.validation.IValidatable#getValue()
		 */
		public T getValue()
		{
			return FormComponent.this.getConvertedInput();
		}

	}

	/**
	 * {@link IMessageSource} used for error messags against this form
	 * components.
	 * 
	 * @author ivaynberg
	 */
	private class MessageSource implements IMessageSource
	{

		/**
		 * @see wicket.validation.IMessageSource#getMessage(java.lang.String)
		 */
		public String getMessage(String key)
		{
			// retrieve prefix that will be used to construct message keys
			String prefix = FormComponent.this.getValidatorKeyPrefix();
			if (Strings.isEmpty(prefix))
			{
				prefix = "";
			}

			final Localizer localizer = FormComponent.this.getLocalizer();

			String resource = prefix + getId() + "." + key;

			// Note: It is important that the default value of "" is provided
			// to getString() not to throw a MissingResourceException or to
			// return a default string like "[Warning: String ..."
			String message = localizer.getString(resource, FormComponent.this, "");

			// If not found, than ...
			if (Strings.isEmpty(message))
			{
				// Try a variation of the resource key

				resource = prefix + key;

				message = localizer.getString(resource, FormComponent.this, "");
			}

			// convert empty string to null in case our default value of "" was
			// returned from localizer
			if (Strings.isEmpty(message))
			{
				message = null;
			}
			return message;
		}

		/**
		 * Creates a new params map that additionaly contains the default input,
		 * name, label parameters
		 * 
		 * @param params
		 *            original params map
		 * @return new params map
		 */
		private Map<String, Object> addDefaultVars(Map<String, Object> params)
		{
			// create and fill the new params map
			final HashMap<String, Object> fullParams;
			if (params == null)
			{
				fullParams = new HashMap<String, Object>(6);
			}
			else
			{
				fullParams = new HashMap<String, Object>(params.size() + 6);
				fullParams.putAll(params);
			}

			// add the input param if not already present
			if (!fullParams.containsKey("input"))
			{
				fullParams.put("input", FormComponent.this.getInput());
			}

			// add the name param if not already present
			if (!fullParams.containsKey("name"))
			{
				fullParams.put("name", FormComponent.this.getId());
			}

			// add the label param if not already present
			if (!fullParams.containsKey("label"))
			{
				fullParams.put("label", getLabel());
			}
			return fullParams;
		}

		/**
		 * @return value of label param for this form component
		 */
		private Object getLabel()
		{
			Object label = null;

			// first try the label model ...
			if (FormComponent.this.getLabel() != null)
			{
				label = FormComponent.this.getLabel().getObject();
			}
			// ... then try a resource of format [form-component-id] with
			// default of [form-component-id]
			if (label == null)
			{
				final String id = FormComponent.this.getId();
				final Localizer localizer = FormComponent.this.getLocalizer();
				label = localizer.getString(id, FormComponent.this, id);
			}
			return label;
		}


		/**
		 * @see wicket.validation.IMessageSource#substitute(java.lang.String,
		 *      java.util.Map)
		 */
		public String substitute(String string, Map<String, Object> vars)
				throws IllegalStateException
		{
			return new MapVariableInterpolator(string, addDefaultVars(vars), true).toString();
		}
	}

}
