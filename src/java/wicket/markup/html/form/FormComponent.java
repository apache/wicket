/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.form;

import java.io.Serializable;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.html.HtmlContainer;
import wicket.markup.html.form.validation.IValidator;
import wicket.markup.html.form.validation.ValidationErrorMessage;
import wicket.model.IModel;
import wicket.util.lang.Classes;
import wicket.util.string.StringList;


/**
 * An html form component knows how to validate itself. Validators that implement
 * IValidator can be added to the component. They will be evaluated in the order they were
 * added and the first Validator that returns an error message determines the error
 * message returned by the component.
 *
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public abstract class FormComponent extends HtmlContainer
{ // TODO finalize javadoc
    /** The validator or validator list for this component. */
    private IValidator validator = IValidator.NULL;

    /**
     * Whether this form component should save and restore state from cookies.
     * This is false by default.
     */
    private boolean persistenceEnabled = false;

    /**
     * Constructor.
     * @param componentName The name of this component
     */
    public FormComponent(final String componentName)
    {
        super(componentName);
    }

    /**
     * Constructor that uses the provided {@link IModel} as its model. All components have
     * names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the model
     * @throws wicket.RenderException Thrown if the component
     * has been given a null name.
     */
    public FormComponent(String name, IModel model)
    {
        super(name, model);
    }

    /**
     * Constructor that uses the provided instance of {@link IModel} as a dynamic model.
     * This model will be wrapped in an instance of {@link wicket.model.PropertyModel}
     * using the provided expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(myIModel, expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the instance of {@link IModel} from which the model object will be
     *            used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.RenderException Thrown if the component has
     * been given a null name.
     */
    public FormComponent(String name, IModel model, String expression)
    {
        super(name, model, expression);
    }

    /**
     * Constructor that uses the provided object as a simple model. This object will be
     * wrapped in an instance of {@link wicket.model.Model}. All components
     * have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as a simple model
     * @throws wicket.RenderException Thrown if the component has
     * been given a null name.
     */
    public FormComponent(String name, Serializable object)
    {
        super(name, object);
    }

    /**
     * Constructor that uses the provided object as a dynamic model. This object will be
     * wrapped in an instance of {@link wicket.model.Model} that will be wrapped i
     * n an instance of {@link wicket.model.PropertyModel} using the provided
     * expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(new Model(object), expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.RenderException Thrown if the component has
     * been given a null name.
     */
    public FormComponent(String name, Serializable object, String expression)
    {
        super(name, object, expression);
    }

    /**
     * Gets whether this component is to be persisted.
     * @return whether this component is to be persisted
     */
    public final boolean isPersistenceEnabled()
    {
        return persistenceEnabled;
    }

    /**
     * Sets whether this component is to be persisted.
     * @param persistenceEnabled whether this component is to be persisted.
     */
    public final void setPersistenceEnabled(final boolean persistenceEnabled)
    {
        if (this instanceof ICookieValue)
        {
            this.persistenceEnabled = persistenceEnabled;
        }
        else
        {
            throw new UnsupportedOperationException("FormComponent "
                    + getClass().getName() + " does not support cookies");
        }
    }

    /**
     * Implemented by form component subclass to update the form component's model.
     * @param cycle The request cycle
     */
    public abstract void updateModel(final RequestCycle cycle);

    /**
     * Validates this component and returns an optional message. If a message
     * is returned, and its level is higher than ValidationErrorMessage.ERROR, the form
     * will be in error state. A message with a lower level than that however, will
     * be recorded, but will not have the form fail.
     * @return the {@link ValidationErrorMessage} or null (ValidationErrorMessage.NO_MESSAGE).
     */
    public final ValidationErrorMessage validate()
    {
        // Perform validation
        final ValidationErrorMessage message = validator.validate(getInput(), this);
        // Return message
        return message;
    }

    /**
     * Gets the input for this form component from the request.
     * Override this method for any other behaviour than just getting the string
     * from the request for this component.
     * @return the request input for this component. This implementation returns
     * a string. Override this method if you need anything else.
     */
    public Serializable getInput()
    {
        return getRequestString(RequestCycle.get());
    }

    /**
     * @see wicket.Component#handleComponentTag(RequestCycle, ComponentTag)
     */
    protected void handleComponentTag(final RequestCycle cycle, final ComponentTag tag)
    {
        super.handleComponentTag(cycle, tag);
        tag.put("name", getPath());
    }

    /**
     * Adds a validator to this form component.
     * @param validator The validator
     * @return This
     */
    public final FormComponent add(final IValidator validator)
    {
        // If we don't yet have a validator
        if (this.validator == IValidator.NULL)
        {
            // Just add the validator directly
            this.validator = validator;
        }
        else
        {
            // Create a validator list?
            if (this.validator instanceof ValidatorList)
            {
                // Already have a list. Just add new validator to list
                ((ValidatorList) this.validator).add(validator);
            }
            else
            {
                // Create a set of the current validator and the new validator
                this.validator = new ValidatorList(this.validator, validator);
            }
        }

        return this;
    }

    /**
     * Gets whether this components is to be validated.
     * @return True if this component has one or more validators
     */
    public final boolean isValidated()
    {
        return this.validator != IValidator.NULL;
    }

    /**
     * Interface to components that support getting and setting cookie values to allow for
     * persistent state in the user's browser.
     */
    public interface ICookieValue
    {
        /**
         * Gets a cookie value for a form component.
         * @return The cookie value
         */
        public String getCookieValue();

        /**
         * Sets a cookie value for a form component.
         * @param value The cookie value
         */
        public void setCookieValue(final String value);
    }

    /**
     * A convenient and memory efficent representation for a list of validators.
     */
    static private final class ValidatorList implements IValidator
    {
        /**
         * Left part of linked list.
         */
        private final IValidator left;

        /**
         * Right part of linked list.
         */
        private IValidator right;

        /**
         * Constructs a list with validators in it.
         * @param left The left validator
         * @param right The right validator
         */
        ValidatorList(final IValidator left, final IValidator right)
        {
            this.left = left;
            this.right = right;
        }

        /**
         * Adds the given code validator to this list of code validators.
         * @param validator The validator
         */
        void add(final IValidator validator)
        {
            ValidatorList current = this;

            while (current.right instanceof ValidatorList)
            {
                current = (ValidatorList) current.right;
            }

            current.right = new ValidatorList(current.right, validator);
        }

        /**
         * Validates the given component.
         * @param input the input
         * @param component The component to validate
         * @return The error returned by the first validator in the list which reported an
         *         error or null if no validator reported an error
         */
        public ValidationErrorMessage validate(final Serializable input, final FormComponent component)
        {
            final ValidationErrorMessage message = left.validate(input, component);

            if (message != ValidationErrorMessage.NO_MESSAGE && message.isLevelError())
            {
                return message;
            }

            return right.validate(input, component);
        }

        /**
         * Gets the string representation of this object.
         * @return String representation of this object
         */
        public String toString()
        {
            final StringList stringList = new StringList();
            ValidatorList current = this;

            while (true)
            {
                stringList.add(Classes.name(current.left.getClass())
                        + " " + current.left.toString());

                if (current.right instanceof ValidatorList)
                {
                    current = (ValidatorList) current.right;
                }
                else
                {
                    stringList.add(Classes.name(current.right.getClass())
                            + " " + current.right.toString());

                    break;
                }
            }

            return stringList.toString();
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
