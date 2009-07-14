/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.form;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Localizer;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IPropertyReflectionAwareModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.PrependingStringBuffer;
import org.apache.wicket.util.string.StringList;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.IValidatorAddListener;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.version.undo.Change;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An HTML form component knows how to validate itself. Validators that implement IValidator can be
 * added to the component. They will be evaluated in the order they were added and the first
 * Validator that returns an error message determines the error message returned by the component.
 * <p>
 * FormComponents are not versioned by default. If you need versioning for your FormComponents, you
 * will need to call Form.setVersioned(true), which will set versioning on for the form and all form
 * component children.
 * <p>
 * If this component is required and that fails, the error key that is used is the "Required"; if
 * the type conversion fails, it will use the key "IConverter" if the conversion failed in a
 * converter, or "ConversionError" if type was explicitly specified via {@link #setType(Class)} or a
 * {@link IPropertyReflectionAwareModel} was used. Notice that both "IConverter" and
 * "ConversionError" have a more specific variant of "key.classname" where classname is the type
 * that we failed to convert to. Classname is not full qualified, so only the actual name of the
 * class is used.
 * 
 * Property expressions that can be used in error messages are:
 * <ul>
 * <li>${input}: the input the user did give</li>
 * <li>${name}: the name of the component that failed</li>
 * <li>${label}: the label of the component</li>
 * </ul>
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @param <T>
 *            The model object type
 * 
 */
public abstract class FormComponent<T> extends LabeledWebMarkupContainer
	implements
		IFormVisitorParticipant,
		IFormModelUpdateListener
{
	private static final Logger logger = LoggerFactory.getLogger(FormComponent.class);

	/**
	 * Visitor for traversing form components
	 */
	public static abstract class AbstractVisitor implements IVisitor
	{
		/**
		 * @see org.apache.wicket.markup.html.form.FormComponent.IVisitor#formComponent(IFormVisitorParticipant)
		 */
		public Object formComponent(IFormVisitorParticipant component)
		{
			if (component instanceof FormComponent)
			{
				onFormComponent((FormComponent<?>)component);
			}
			return Component.IVisitor.CONTINUE_TRAVERSAL;
		}

		protected abstract void onFormComponent(FormComponent<?> formComponent);
	}

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
		 * @return component
		 */
		public Object formComponent(IFormVisitorParticipant formComponent);
	}

	/**
	 * {@link IErrorMessageSource} used for error messages against this form components.
	 * 
	 * @author ivaynberg
	 */
	private class MessageSource implements IErrorMessageSource
	{
		private final Set<String> triedKeys = new LinkedHashSet<String>();

		/**
		 * @see org.apache.wicket.validation.IErrorMessageSource#getMessage(java.lang.String)
		 */
		public String getMessage(String key)
		{
			final FormComponent<T> formComponent = FormComponent.this;

			// Use the following log4j config for detailed logging on the property resolution
			// process
			// log4j.logger.org.apache.wicket.resource.loader=DEBUG
			// log4j.logger.org.apache.wicket.Localizer=DEBUG

			final Localizer localizer = formComponent.getLocalizer();

			// retrieve prefix that will be used to construct message keys
			String prefix = formComponent.getValidatorKeyPrefix();
			String message = null;

			// first try the full form of key [form-component-id].[key]
			String resource = getId() + "." + prefix(prefix, key);
			message = getString(localizer, resource, formComponent);

			// if not found, try a more general form (without prefix)
			// [form-component-id].[prefix].[key]
			if (Strings.isEmpty(message) && Strings.isEmpty(prefix))
			{
				resource = getId() + "." + key;
				message = getString(localizer, resource, formComponent);
			}

			// If not found try a more general form [prefix].[key]
			if (Strings.isEmpty(message))
			{
				resource = prefix(prefix, key);
				message = getString(localizer, key, formComponent);
			}

			// If not found try the most general form [key]
			if (Strings.isEmpty(message) && Strings.isEmpty(prefix))
			{
				// Try a variation of the resource key
				message = getString(localizer, key, formComponent);
			}

			// convert empty string to null in case our default value of "" was
			// returned from localizer
			if (Strings.isEmpty(message))
			{
				message = null;
			}
			return message;
		}

		private String prefix(String prefix, String key)
		{
			if (!Strings.isEmpty(prefix))
			{
				return prefix + "." + key;
			}
			else
			{
				return key;
			}
		}

		/**
		 * 
		 * @param localizer
		 * @param key
		 * @param component
		 * @return string
		 */
		private String getString(Localizer localizer, String key, Component component)
		{
			triedKeys.add(key);

			// Note: It is important that the default value of "" is
			// provided to getString() not to throw a MissingResourceException or to
			// return a default string like "[Warning: String ..."
			return localizer.getString(key, component, "");
		}

		/**
		 * @see org.apache.wicket.validation.IErrorMessageSource#substitute(java.lang.String,
		 *      java.util.Map)
		 */
		public String substitute(String string, Map<String, Object> vars)
			throws IllegalStateException
		{
			return new MapVariableInterpolator(string, addDefaultVars(vars), Application.get()
				.getResourceSettings()
				.getThrowExceptionOnMissingResource()).toString();
		}

		/**
		 * Creates a new params map that additionally contains the default input, name, label
		 * parameters
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
				fullParams.put("input", getInput());
			}

			// add the name param if not already present
			if (!fullParams.containsKey("name"))
			{
				fullParams.put("name", getId());
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
		private String getLabel()
		{
			final FormComponent<T> fc = FormComponent.this;
			String label = null;

			// first try the label model ...
			if (fc.getLabel() != null)
			{
				label = fc.getLabel().getObject();
			}
			// ... then try a resource of format [form-component-id] with
			// default of '[form-component-id]'
			if (label == null)
			{

				label = fc.getLocalizer().getString(fc.getId(), fc.getParent(), fc.getId());
			}
			return label;
		}
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
		 * @see org.apache.wicket.version.undo.Change#undo()
		 */
		@Override
		public void undo()
		{
			setRequired(required);
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
		 * @see org.apache.wicket.validation.IValidatable#error(org.apache.wicket.validation.IValidationError)
		 */
		public void error(IValidationError error)
		{
			FormComponent.this.error(error);
		}

		/**
		 * @see org.apache.wicket.validation.IValidatable#getValue()
		 */
		public T getValue()
		{
			return getConvertedInput();
		}

		/**
		 * @see org.apache.wicket.validation.IValidatable#isValid()
		 */
		public boolean isValid()
		{
			return FormComponent.this.isValid();
		}
	}

	/**
	 * The value separator
	 */
	public static String VALUE_SEPARATOR = ";";

	private static final String[] EMPTY_STRING_ARRAY = new String[] { "" };

	/**
	 * Whether this form component should save and restore state between sessions. This is false by
	 * default.
	 */
	private static final short FLAG_PERSISTENT = FLAG_RESERVED2;

	/** Whether or not this component's value is required (non-empty) */
	private static final short FLAG_REQUIRED = FLAG_RESERVED3;

	private static final String NO_RAW_INPUT = "[-NO-RAW-INPUT-]";

	private static final long serialVersionUID = 1L;

	/**
	 * Make empty strings null values boolean. Used by AbstractTextComponent subclass.
	 */
	protected static final short FLAG_CONVERT_EMPTY_INPUT_STRING_TO_NULL = FLAG_RESERVED1;

	/**
	 * Visits any form components inside component if it is a container, or component itself if it
	 * is itself a form component
	 * 
	 * @param component
	 *            starting point of the traversal
	 * 
	 * @param visitor
	 *            The visitor to call
	 */
	public static final void visitFormComponentsPostOrder(Component component,
		final FormComponent.IVisitor visitor)
	{
		if (visitor == null)
		{
			throw new IllegalArgumentException("Argument `visitor` cannot be null");
		}

		visitFormComponentsPostOrderHelper(component, visitor);
	}

	/**
	 * 
	 * @param component
	 * @param visitor
	 * @return Object
	 */
	private static final Object visitFormComponentsPostOrderHelper(Component component,
		final FormComponent.IVisitor visitor)
	{
		if (component instanceof MarkupContainer)
		{
			final MarkupContainer container = (MarkupContainer)component;
			if (container.size() > 0)
			{
				boolean visitChildren = true;
				if (container instanceof IFormVisitorParticipant)
				{
					visitChildren = ((IFormVisitorParticipant)container).processChildren();
				}
				if (visitChildren)
				{
					final Iterator<? extends Component> children = container.iterator();
					while (children.hasNext())
					{
						final Component child = children.next();
						Object value = visitFormComponentsPostOrderHelper(child, visitor);
						if (value == Component.IVisitor.STOP_TRAVERSAL)
						{
							return value;
						}
					}
				}
			}
		}

		if (component instanceof FormComponent)
		{
			final FormComponent<?> fc = (FormComponent<?>)component;
			return visitor.formComponent(fc);
		}

		return null;
	}

	/**
	 * Visits any form components inside component if it is a container, or component itself if it
	 * is itself a form component
	 * 
	 * @param component
	 *            starting point of the traversal
	 * 
	 * @param visitor
	 *            The visitor to call
	 */
	public static final void visitComponentsPostOrder(Component component,
		final Component.IVisitor<Component> visitor)
	{
		if (visitor == null)
		{
			throw new IllegalArgumentException("Argument `visitor` cannot be null");
		}

		visitComponentsPostOrderHelper(component, visitor);
	}

	/**
	 * 
	 * @param component
	 * @param visitor
	 * @return Object
	 */
	private static final Object visitComponentsPostOrderHelper(Component component,
		final Component.IVisitor<Component> visitor)
	{
		if (component instanceof MarkupContainer)
		{
			final MarkupContainer container = (MarkupContainer)component;
			if (container.size() > 0)
			{
				boolean visitChildren = true;
				if (container instanceof IFormVisitorParticipant)
				{
					visitChildren = ((IFormVisitorParticipant)container).processChildren();
				}
				if (visitChildren)
				{
					final Iterator<? extends Component> children = container.iterator();
					while (children.hasNext())
					{
						final Component child = children.next();
						Object value = visitComponentsPostOrderHelper(child, visitor);
						if (value == Component.IVisitor.STOP_TRAVERSAL)
						{
							return value;
						}
						else if (value == Component.IVisitor.CONTINUE_TRAVERSAL)
						{
							// noop
						}
						else if (value == Component.IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER)
						{
							// noop
						}
						else
						{
							return value;
						}
					}
				}
			}
		}
		return visitor.component(component);
	}


	private transient T convertedInput;

	/**
	 * Raw Input entered by the user or NO_RAW_INPUT if nothing is filled in.
	 */
	private String rawInput = NO_RAW_INPUT;

	/**
	 * Type that the raw input string will be converted to
	 */
	private String typeName;

	/**
	 * The list of validators for this form component as either an IValidator instance or an array
	 * of IValidator instances.
	 */
	private Object validators = null;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public FormComponent(final String id)
	{
		super(id);
		// the form decides whether form components are versioned or not
		// see Form.setVersioned
		setVersioned(false);
	}

	/**
	 * @param id
	 * @param model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public FormComponent(final String id, IModel<T> model)
	{
		super(id, model);
		// the form decides whether form components are versioned or not
		// see Form.setVersioned
		setVersioned(false);
	}

	/**
	 * Adds a validator to this form component
	 * 
	 * @param validator
	 *            validator to be added
	 * @return <code>this</code> for chaining
	 * @throws IllegalArgumentException
	 *             if validator is null
	 * @see IValidator
	 * @see IValidatorAddListener
	 * 
	 */
	public final FormComponent<T> add(final IValidator<T> validator)
	{
		if (validator == null)
		{
			throw new IllegalArgumentException("validator argument cannot be null");
		}
		// add the validator
		validators_add(validator);

		// see whether the validator listens for add events
		if (validator instanceof IValidatorAddListener)
		{
			((IValidatorAddListener)validator).onAdded(this);
		}
		return this;
	}

	/**
	 * Adds a validator to this form component.
	 * 
	 * @param validators
	 *            The validator(s) to be added
	 * @return This
	 * @throws IllegalArgumentException
	 *             if validator is null
	 * @see IValidator
	 * @see IValidatorAddListener
	 */
	public final FormComponent<T> add(final IValidator<T>... validators)
	{
		if (validators == null)
		{
			throw new IllegalArgumentException("validator argument cannot be null");
		}

		for (IValidator<T> validator : validators)
		{
			add(validator);
		}

		// return this for chaining
		return this;
	}

	/**
	 * Checks if the form component's 'required' requirement is met by first checking
	 * {@link #isRequired()} to see if it has to check for requirement. If that is true then by
	 * default it checks if the input is null or an empty String
	 * {@link Strings#isEmpty(CharSequence)}
	 * <p>
	 * Subclasses that overwrite this method should also call {@link #isRequired()} first.
	 * </p>
	 * 
	 * @return true if the 'required' requirement is met, false otherwise
	 * 
	 * @see Strings#isEmpty(CharSequence)
	 * @see #isInputNullable()
	 */
	public boolean checkRequired()
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
			return !Strings.isEmpty(input);
		}
		return true;
	}

	/**
	 * Clears the user input.
	 */
	public final void clearInput()
	{
		rawInput = NO_RAW_INPUT;
	}

	/**
	 * Reports a validation error against this form component.
	 * 
	 * The actual error is reported by creating a {@link ValidationErrorFeedback} object that holds
	 * both the validation error and the generated error message - so a custom feedback panel can
	 * have access to both.
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
		MessageSource source = new MessageSource();
		String message = error.getErrorMessage(source);

		if (message == null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append("Could not locate error message for component: ");
			buffer.append(Classes.simpleName(getClass()));
			buffer.append("@");
			buffer.append(getPageRelativePath());
			buffer.append(" and error: ");
			buffer.append(error.toString());
			buffer.append(". Tried keys: ");
			Iterator<String> keys = source.triedKeys.iterator();
			while (keys.hasNext())
			{
				buffer.append(keys.next());
				if (keys.hasNext())
				{
					buffer.append(", ");
				}
			}
			buffer.append(".");
			message = buffer.toString();
			logger.warn(message);
		}
		error(new ValidationErrorFeedback(error, message));
	}

	/**
	 * Gets the converted input. The converted input is set earlier though the implementation of
	 * {@link #convertInput()}.
	 * 
	 * @return value of input possibly converted into an appropriate type
	 */
	public final T getConvertedInput()
	{
		return convertedInput;
	}

	/**
	 * Sets the converted input. This method is typically not called by clients, unless they
	 * override {@link #convertInput()}, in which case they should call this method to update the
	 * input for this component instance.
	 * 
	 * @param convertedInput
	 *            the converted input
	 */
	public final void setConvertedInput(T convertedInput)
	{
		this.convertedInput = convertedInput;
	}

	/**
	 * @return The parent form for this form component
	 */
	public Form<?> getForm()
	{
		Form<?> form = Form.findForm(this);
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
		String[] input = getInputAsArray();
		if (input == null || input.length == 0)
		{
			return null;
		}
		else
		{
			return trim(input[0]);
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
	 * Gets the string to be used for the <tt>name</tt> attribute of the form element. Generated
	 * using the path from the form to the component, excluding the form itself. Override it if you
	 * want even a smaller name. E.g. if you know for sure that the id is unique within a form.
	 * 
	 * @return The string to use as the form element's name attribute
	 */
	public String getInputName()
	{
		// TODO: keep this in sync with AbstractSubmitLink#getInputName
		String id = getId();
		final PrependingStringBuffer inputName = new PrependingStringBuffer(id.length());
		Component c = this;
		while (true)
		{
			inputName.prepend(id);
			c = c.getParent();
			if (c == null || (c instanceof Form && ((Form<?>)c).isRootForm()) || c instanceof Page)
			{
				break;
			}
			inputName.prepend(Component.PATH_SEPARATOR);
			id = c.getId();
		}

		// having input name "submit" causes problems with javascript, so we
		// create a unique string to replace it by prepending a path separator
		if (inputName.equals("submit"))
		{
			inputName.prepend(Component.PATH_SEPARATOR);
		}
		Form<?> form = findParent(Form.class);

		if (form != null)
		{
			return form.getInputNamePrefix() + inputName.toString();
		}
		else
		{
			return inputName.toString();
		}
	}

	/**
	 * Use hasRawInput() to check if this component has raw input because null can mean 2 things: It
	 * doesn't have rawinput or the rawinput is really null.
	 * 
	 * @return The raw form input that is stored for this formcomponent
	 */
	public final String getRawInput()
	{
		return NO_RAW_INPUT.equals(rawInput) ? null : rawInput;
	}

	/**
	 * @return the type to use when updating the model for this form component
	 */
	@SuppressWarnings("unchecked")
	public final Class<T> getType()
	{
		return typeName == null ? null : (Class<T>)Classes.resolveClass(typeName);
	}

	/**
	 * @see Form#getValidatorKeyPrefix()
	 * @return prefix used when constructing validator key messages
	 */
	public String getValidatorKeyPrefix()
	{
		Form<?> form = findParent(Form.class);
		if (form != null)
		{
			return getForm().getValidatorKeyPrefix();
		}
		return null;
	}

	/**
	 * Gets an unmodifiable list of validators for this FormComponent.
	 * 
	 * @return List of validators
	 */
	public final List<IValidator<T>> getValidators()
	{
		final int size = validators_size();
		if (size == 0)
		{
			return Collections.emptyList();
		}
		else
		{
			final List<IValidator<T>> list = new ArrayList<IValidator<T>>(size);
			for (int i = 0; i < size; i++)
			{
				list.add(validators_get(i));
			}
			return Collections.unmodifiableList(list);
		}
	}

	/**
	 * Gets current value for a form component, which can be either input data entered by the user,
	 * or the component's model object if no input was provided.
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
	 * Returns whether this component has raw input. Raw input is unconverted input straight from
	 * the client.
	 * 
	 * @return boolean whether this component has raw input.
	 */
	public final boolean hasRawInput()
	{
		return !NO_RAW_INPUT.equals(rawInput);
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
	 * Indicate that validation of this form component failed.
	 */
	public final void invalid()
	{
		onInvalid();
	}

	/**
	 * Gets whether this component's input can be null. By default, components that do not get input
	 * will have null values passed in for input. However, component TextField is an example
	 * (possibly the only one) that never gets a null passed in, even if the field is left empty
	 * UNLESS it had attribute <code>disabled="disabled"</code> set.
	 * 
	 * @return True if this component's input can be null. Returns true by default.
	 */
	public boolean isInputNullable()
	{
		return true;
	}

	/**
	 * @return True if this component encodes data in a multipart form submit
	 */
	public boolean isMultiPart()
	{
		return false;
	}

	/**
	 * @return True if this component supports persistence AND it has been asked to persist itself
	 *         with setPersistent().
	 */
	public final boolean isPersistent()
	{
		return supportsPersistence() && getFlag(FLAG_PERSISTENT);
	}

	/**
	 * @return whether or not this component's value is required
	 */
	public boolean isRequired()
	{
		return getFlag(FLAG_REQUIRED);
	}

	/**
	 * Gets whether this component is 'valid'. Valid in this context means that no validation errors
	 * were reported the last time the form component was processed. This variable not only is
	 * convenient for 'business' use, but is also necessary as we don't want the form component
	 * models updated with invalid input.
	 * 
	 * @return valid whether this component is 'valid'
	 */
	public final boolean isValid()
	{
		class IsValidVisitor implements IVisitor
		{
			boolean valid = true;

			public Object formComponent(IFormVisitorParticipant formComponent)
			{
				final FormComponent<?> fc = (FormComponent<?>)formComponent;
				if (fc.hasErrorMessage())
				{
					valid = false;
					return Component.IVisitor.STOP_TRAVERSAL;
				}
				return Component.IVisitor.CONTINUE_TRAVERSAL;
			}
		}
		IsValidVisitor tmp = new IsValidVisitor();
		visitFormComponentsPostOrder(this, tmp);
		return tmp.valid;
	}

	/**
	 * @see IFormVisitorParticipant#processChildren()
	 */
	public boolean processChildren()
	{
		return true;
	}

	/**
	 * This method will retrieve the request parameter, validate it, and if valid update the model.
	 * These are the same steps as would be performed by the form.
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
	 * The value will be made available to the validator property by means of ${label}. It does not
	 * have any specific meaning to FormComponent itself.
	 * 
	 * @param labelModel
	 * @return this for chaining
	 */
	public FormComponent<T> setLabel(IModel<String> labelModel)
	{
		setLabelInternal(labelModel);
		return this;
	}

	/**
	 * Sets the value for a form component this value will be split the string with
	 * {@link FormComponent#VALUE_SEPARATOR} and calls setModelValue(String[]) with that.
	 * 
	 * @param value
	 *            The value
	 * 
	 * @deprecated call or override setModelValue(String[])
	 */
	@Deprecated
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
	public final FormComponent<T> setPersistent(final boolean persistent)
	{
		if (supportsPersistence())
		{
			setFlag(FLAG_PERSISTENT, persistent);
		}
		else
		{
			throw new UnsupportedOperationException("FormComponent " + getClass() +
				" does not support cookies");
		}
		return this;
	}

	/**
	 * Sets the required flag
	 * 
	 * @param required
	 * @return this for chaining
	 */
	public final FormComponent<T> setRequired(final boolean required)
	{
		if (!required && getType() != null && getType().isPrimitive())
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
	 * Sets the type that will be used when updating the model for this component. If no type is
	 * specified String type is assumed.
	 * 
	 * @param type
	 * @return this for chaining
	 */
	public final FormComponent<T> setType(Class<?> type)
	{
		typeName = type == null ? null : type.getName();
		if (type != null && type.isPrimitive())
		{
			setRequired(true);
		}
		return this;
	}

	/**
	 * Updates this components model from the request, it expects that the object is already
	 * converted through the convertInput() call that is called by the validate() method when a form
	 * is being processed.
	 * 
	 * By default it just does this:
	 * 
	 * <pre>
	 * setModelObject(getConvertedInput());
	 * </pre>
	 * 
	 * DO NOT CALL THIS METHOD DIRECTLY UNLESS YOU ARE SURE WHAT YOU ARE DOING. USUALLY UPDATING
	 * YOUR MODEL IS HANDLED BY THE FORM, NOT DIRECTLY BY YOU.
	 */
	public void updateModel()
	{
		setDefaultModelObject(getConvertedInput());
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
	 * Performs full validation of the form component, which consists of calling validateRequired(),
	 * convertInput(), and validateValidators(). This method should only be used if the form
	 * component needs to be fully validated outside the form process.
	 */
	public void validate()
	{
		validateRequired();
		if (isValid())
		{
			convertInput();
			if (isValid())
			{
				if (isRequired() && getConvertedInput() == null && isInputNullable())
				{
					reportRequiredError();
				}
				else
				{
					validateValidators();
				}
			}
		}
	}

	/**
	 * @param validator
	 *            The validator to add to the validators Object (which may be an array of
	 *            IValidators or a single instance, for efficiency)
	 */
	@SuppressWarnings("unchecked")
	private void validators_add(final IValidator<T> validator)
	{
		if (validators == null)
		{
			validators = validator;
		}
		else
		{
			// Get current list size
			final int size = validators_size();

			// Create array that holds size + 1 elements
			final IValidator<T>[] validators = new IValidator[size + 1];

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
	 * Gets validator from validators Object (which may be an array of IValidators or a single
	 * instance, for efficiency) at the given index
	 * 
	 * @param index
	 *            The index of the validator to get
	 * @return The validator
	 */
	@SuppressWarnings("unchecked")
	private IValidator<T> validators_get(int index)
	{
		if (validators == null)
		{
			throw new IndexOutOfBoundsException();
		}
		if (validators instanceof IValidator[])
		{
			return ((IValidator[])validators)[index];
		}
		return (IValidator<T>)validators;
	}

	/**
	 * @return The number of validators in the validators Object (which may be an array of
	 *         IValidators or a single instance, for efficiency)
	 */
	private int validators_size()
	{
		if (validators == null)
		{
			return 0;
		}
		if (validators instanceof IValidator[])
		{
			return ((IValidator[])validators).length;
		}
		return 1;
	}

	/**
	 * Converts and validates the conversion of the raw input string into the object specified by
	 * {@link FormComponent#getType()} and records any errors. Converted value is available through
	 * {@link FormComponent#getConvertedInput()}.
	 * 
	 * <p>
	 * Usually the user should do custom conversions by specifying an {@link IConverter} by
	 * registering it with the application by overriding {@link Application#getConverterLocator()},
	 * or at the component level by overriding {@link #getConverter()}.
	 * </p>
	 * 
	 * @see IConverterLocator
	 * @see Application#newConverterLocator()
	 */
	@SuppressWarnings("unchecked")
	protected void convertInput()
	{
		if (typeName == null)
		{
			try
			{
				convertedInput = convertValue(getInputAsArray());
			}
			catch (ConversionException e)
			{
				ValidationError error = new ValidationError();
				if (e.getResourceKey() != null)
				{
					error.addMessageKey(e.getResourceKey());
				}
				if (e.getTargetType() != null)
				{
					error.addMessageKey("ConversionError." + Classes.simpleName(e.getTargetType()));
				}
				error.addMessageKey("ConversionError");
				reportValidationError(e, error);
			}
		}
		else
		{
			final IConverter converter = getConverter(getType());

			try
			{
				convertedInput = (T)converter.convertToObject(getInput(), getLocale());
			}
			catch (ConversionException e)
			{
				ValidationError error = new ValidationError();
				if (e.getResourceKey() != null)
				{
					error.addMessageKey(e.getResourceKey());
				}
				String simpleName = Classes.simpleName(getType());
				error.addMessageKey("IConverter." + simpleName);
				error.addMessageKey("IConverter");
				error.setVariable("type", simpleName);
				reportValidationError(e, error);
			}
		}
	}

	/**
	 * 
	 * @param e
	 * @param error
	 */
	private void reportValidationError(ConversionException e, ValidationError error)
	{
		final Locale locale = e.getLocale();
		if (locale != null)
		{
			error.setVariable("locale", locale);
		}
		error.setVariable("exception", e);
		Format format = e.getFormat();
		if (format instanceof SimpleDateFormat)
		{
			error.setVariable("format", ((SimpleDateFormat)format).toLocalizedPattern());
		}

		Map<String, Object> variables = e.getVariables();
		if (variables != null)
		{
			error.getVariables().putAll(variables);
		}

		error((IValidationError)error);
	}

	/**
	 * Subclasses should overwrite this if the conversion is not done through the type field and the
	 * {@link IConverter}. <strong>WARNING: this method may be removed in future versions.</strong>
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
		return (T)(value != null && value.length > 0 && value[0] != null ? trim(value[0]) : null);
	}

	/**
	 * @return Value to return when model value is needed
	 */
	protected String getModelValue()
	{
		return getDefaultModelObjectAsString();
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
			throw new IllegalArgumentException(
				exceptionMessage("Internal error.  Request string '" + string +
					"' not a valid integer"));
		}
	}

	/**
	 * Gets the request parameter for this component as an int, using the given default in case no
	 * corresponding request parameter was found.
	 * 
	 * @param defaultValue
	 *            Default value to return if request does not have an integer for this component
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
				throw new IllegalArgumentException(exceptionMessage("Request string '" + string +
					"' is not a valid integer"));
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
	 * @see org.apache.wicket.Component#internalOnModelChanged()
	 */
	@Override
	protected void internalOnModelChanged()
	{
		// If the model for this form component changed, we should make it
		// valid again because there can't be any invalid input for it anymore.
		valid();
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		tag.put("name", getInputName());

		if (!isEnabledInHierarchy())
		{
			onDisabled(tag);
		}

		super.onComponentTag(tag);
	}

	/**
	 * Sets the temporary converted input value to null.
	 * 
	 * @see org.apache.wicket.Component#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		super.onDetach();
		convertedInput = null;
	}

	/**
	 * Called by {@link #onComponentTag(ComponentTag)} when the component is disabled. By default,
	 * this method will add a disabled="disabled" attribute to the tag. Components may override this
	 * method to tweak the tag as they think is fit.
	 * 
	 * @param tag
	 *            the tag that is being rendered
	 */
	protected void onDisabled(final ComponentTag tag)
	{
		tag.put("disabled", "disabled");
	}

	/**
	 * Handle invalidation
	 */
	protected void onInvalid()
	{
	}

	/**
	 * Handle validation
	 */
	protected void onValid()
	{
	}

	/**
	 * Determines whether or not this component should trim its input prior to processing it. The
	 * default value is <code>true</code>
	 * 
	 * @return True if the input should be trimmed.
	 */
	protected boolean shouldTrimInput()
	{
		return true;
	}

	/**
	 * Trims the input according to {@link #shouldTrimInput()}
	 * 
	 * @param string
	 * @return trimmed input if {@link #shouldTrimInput()} returns true, unchanged input otherwise
	 */
	protected final String trim(String string)
	{
		String trimmed = string;
		if (trimmed != null && shouldTrimInput())
		{
			trimmed = trimmed.trim();
		}
		return trimmed;
	}

	/**
	 * @return True if this type of FormComponent can be persisted.
	 */
	protected boolean supportsPersistence()
	{
		return false;
	}

	/**
	 * Checks if the raw input value is not null if this component is required.
	 */
	protected final void validateRequired()
	{
		if (!checkRequired())
		{
			reportRequiredError();
		}
	}

	/**
	 * Reports required error against this component
	 */
	private void reportRequiredError()
	{
		error((IValidationError)new ValidationError().addMessageKey("Required"));
	}

	/**
	 * Validates this component using the component's validators.
	 */
	protected final void validateValidators()
	{
		final int size = validators_size();

		final IValidatable<T> validatable = newValidatable();

		int i = 0;
		IValidator<T> validator = null;

		boolean isNull = getConvertedInput() == null;

		try
		{
			for (i = 0; i < size; i++)
			{
				validator = validators_get(i);

				if (isNull == false || validator instanceof INullAcceptingValidator)
				{
					validator.validate(validatable);
				}
				if (!isValid())
				{
					break;
				}
			}
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Exception '" + e + "' occurred during validation " +
				validator.getClass().getName() + " on component " + getPath(), e);
		}
	}

	/**
	 * Creates an IValidatable that can be used to validate this form component. This validatable
	 * encorporates error key lookups that correspend to this form component.
	 * 
	 * This method is useful when validation needs to happen outside the regular validation workflow
	 * but error messages should still be properly reported against the form component.
	 * 
	 * @return IValidatable<T> for this form component
	 */
	public final IValidatable<T> newValidatable()
	{
		return new ValidatableAdapter();
	}

	/**
	 * Gets model
	 * 
	 * @return model
	 */
	@SuppressWarnings("unchecked")
	public final IModel<T> getModel()
	{
		return (IModel<T>)getDefaultModel();
	}

	/**
	 * Sets model
	 * 
	 * @param model
	 */
	public final void setModel(IModel<T> model)
	{
		setDefaultModel(model);
	}

	/**
	 * Gets model object
	 * 
	 * @return model object
	 */
	@SuppressWarnings("unchecked")
	public final T getModelObject()
	{
		return (T)getDefaultModelObject();
	}

	/**
	 * Sets model object
	 * 
	 * @param object
	 */
	public final void setModelObject(T object)
	{
		setDefaultModelObject(object);
	}
}
