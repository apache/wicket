/*
 * $Id$ $Revision$
 * $Date$
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.FeedbackMessages;
import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.html.HtmlContainer;
import wicket.markup.html.form.persistence.CookieValuePersister;
import wicket.markup.html.form.persistence.IValuePersister;
import wicket.markup.html.form.validation.IFormValidationDelegate;
import wicket.markup.html.form.validation.IValidationErrorHandler;
import wicket.markup.html.form.validation.ValidationErrorMessage;
import wicket.markup.html.form.validation.ValidationErrorModelDecorator;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.PropertyModel;
import wicket.protocol.http.HttpRequestCycle;

/**
 * Base class for HTML forms. TODO elaborate documentation (validation, cookies,
 * etc...)
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public abstract class Form extends HtmlContainer implements IFormSubmitListener
{ // TODO finalize javadoc
	/** Log. */
	private static Log log = LogFactory.getLog(Form.class);

	/** Manager responsible to persist and retrieve FormComponent data. */
	private IValuePersister persister = null;

	/** The delegate to be used for execution of validation of this form. */
	private IFormValidationDelegate validationDelegate = DefaultFormValidationDelegate
			.getInstance();

	/** The validation error handling delegate. */
	private final IValidationErrorHandler validationErrorHandler;

	/**
	 * The default form validation delegate.
	 */
	private static final class DefaultFormValidationDelegate implements IFormValidationDelegate
	{

		/** Single instance of default form validation delegate */
		private static final DefaultFormValidationDelegate instance = new DefaultFormValidationDelegate();
        
		/** Log. */
		private static final Log log = LogFactory.getLog(DefaultFormValidationDelegate.class);

		/**
		 * @return Singleton instance of DefaultFormValidationDelegate
		 */
		private static DefaultFormValidationDelegate getInstance()
		{
			return instance;
		}

		/**
		 * Validates all children of this form, recording all messages that are
		 * returned by the validators.
		 * 
		 * @param form
		 *            the form that the validation is applied to
		 * @return the list of validation messages that were recorded during
		 *         validation
		 */
		public FeedbackMessages validate(Form form)
		{
			final FeedbackMessages messages = FeedbackMessages.get();
			form.visitChildren(FormComponent.class, new IVisitor()
			{
				public Object component(final Component component)
				{
					final ValidationErrorMessage message = ((FormComponent)component).validate();
					if (message != ValidationErrorMessage.NO_MESSAGE)
					{
						if (log.isDebugEnabled())
						{
							log.debug("validation error: " + message);
						}
						messages.add(message);

						// Replace the model
						// TODO examine any easy ways to avoid this
						ValidationErrorModelDecorator deco = new ValidationErrorModelDecorator(
								component, message.getInput());
						component.setModel(deco);
					}

					// Continue until the end
					return IVisitor.CONTINUE_TRAVERSAL;
				}
			});
			return messages;
		}
	}

	/**
	 * Constructor that uses the provided {@link IModel}as its model. All
	 * components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param model
	 *            the model
	 * @param validationErrorHandler
	 *            Interface to a component that can handle/display validation
	 *            errors
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public Form(String name, IModel model, final IValidationErrorHandler validationErrorHandler)
	{
		super(name, model);
		this.validationErrorHandler = validationErrorHandler;
	}

	/**
	 * Constructor that uses the provided instance of {@link IModel}as a
	 * dynamic model. This model will be wrapped in an instance of
	 * {@link PropertyModel}using the provided expression. Thus, using this
	 * constructor is a short-hand for:
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
	 * @param validationErrorHandler
	 *            Interface to a component that can handle/display validation
	 *            errors
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public Form(String name, IModel model, String expression,
			final IValidationErrorHandler validationErrorHandler)
	{
		super(name, model, expression);
		this.validationErrorHandler = validationErrorHandler;
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            Name of this form component
	 * @param validationErrorHandler
	 *            Interface to a component that can handle/display validation
	 *            errors
	 */
	public Form(final String name, final IValidationErrorHandler validationErrorHandler)
	{
		super(name);
		this.validationErrorHandler = validationErrorHandler;
	}

	/**
	 * Constructor that uses the provided object as a simple model. This object
	 * will be wrapped in an instance of {@link Model}. All components have
	 * names. A component's name cannot be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param object
	 *            the object that will be used as a simple model
	 * @param validationErrorHandler
	 *            Interface to a component that can handle/display validation
	 *            errors
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public Form(String name, Serializable object,
			final IValidationErrorHandler validationErrorHandler)
	{
		super(name, object);
		this.validationErrorHandler = validationErrorHandler;
	}

	/**
	 * Constructor that uses the provided object as a dynamic model. This object
	 * will be wrapped in an instance of {@link Model}that will be wrapped in
	 * an instance of {@link PropertyModel}using the provided expression. Thus,
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
	 * @param validationErrorHandler
	 *            Interface to a component that can handle/display validation
	 *            errors
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public Form(String name, Serializable object, String expression,
			final IValidationErrorHandler validationErrorHandler)
	{
		super(name, object, expression);
		this.validationErrorHandler = validationErrorHandler;
	}

	/**
	 * Handles form submissions.
	 */
	public final void formSubmitted()
	{
		// Redirect back to result to avoid postback warnings. But we turn
		// redirecting on as the first thing because the user's handleSubmit
		// implementation may wish to redirect somewhere else. In that case,
		// they can simply call setRedirect(false) in handleSubmit.
		getRequestCycle().setRedirect(true);

		// Validate model using validation delegate
		final FeedbackMessages messages = validationDelegate.validate(this);

		// Update model using form data
		updateFormComponentModels();

		// Persist FormComponents if requested
		persistFormComponentData();

		// If validation or update caused error messages to appear
		if (messages.hasErrorMessages())
		{
			// handle those errors
			handleErrors(messages.getErrorMessages());
		}
		else
		{
			// Model was successfully updated with valid data
			handleSubmit();
		}
	}

	/**
	 * Gets the delegate to be used for execution of validation of this form.
	 * 
	 * @return the delegate to be used for execution of validation of this form
	 */
	public IFormValidationDelegate getValidationDelegate()
	{
		return validationDelegate;
	}

	/**
	 * Convenience method in case there is one known (validation) error that is
	 * to be registered with the form directly.
	 * 
	 * @param message
	 *            the message
	 */
	public final void handleError(final ValidationErrorMessage message)
	{
		handleErrors(FeedbackMessages.get().add(message));
	}

	/**
	 * Sets error messages for form. First all childs (form components) are
	 * asked to do their part of error handling, and after that, the registered
	 * (if any) error handler of this form is called.
	 * 
	 * @param errors
	 *            the recorded errors
	 */
	public final void handleErrors(final FeedbackMessages errors)
	{
		// Traverse children of this form, calling validationError() on any
		// components implementing IValidationErrorHandler.
		visitChildren(IValidationErrorHandler.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				// Call validation error handler
				((IValidationErrorHandler)component).validationError(errors);

				// Traverse all children
				return CONTINUE_TRAVERSAL;
			}
		});

		// Call the validation handler that is registered with this form, if any
		if (validationErrorHandler != null)
		{
			validationErrorHandler.validationError(errors);
		}
	}

	/**
	 * Implemented by subclasses to deal with form submits.
	 */
	public abstract void handleSubmit();

	/**
	 * Removes already persisted data for all FormComponent childs and disable
	 * persistence for the same components.
	 * 
	 * @see Page#removePersistedFormData(Class, boolean)
	 * 
	 * @param disablePersistence
	 *            if true, disable persistence for all FormComponents on that
	 *            page. If false, it will remain unchanged.
	 */
	public void removePersistedFormComponentData(final boolean disablePersistence)
	{
		// The persistence manager responsible to persist and retrieve
		// FormComponent data
		final IValuePersister persister = getValuePersister();

		// Search for FormComponents like TextField etc.
		visitChildren(FormComponent.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				// remove the FormComponents persisted data
				final FormComponent formComponent = (FormComponent)component;
				persister.clear(formComponent);

				// Disable persistence if requested. Leave unchanged otherwise.
				if (formComponent.isPersistent() && disablePersistence)
				{
					formComponent.setPersistent(false);
				}

				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Retrieves FormComponent values related to the page using the persister
	 * and assign the values to the FormComponent. Thus initializing them. NOTE:
	 * THIS METHOD IS FOR INTERNAL USE ONLY AND IS NOT MEANT TO BE USED BY
	 * FRAMEWORK CLIENTS. IT MAY BE REMOVED IN THE FUTURE.
	 */
	final public void setFormComponentValuesFromPersister()
	{
		// Visit all FormComponent contained in the page
		visitChildren(FormComponent.class, new Component.IVisitor()
		{
			// For each FormComponent found on the Page (not Form)
			public Object component(final Component component)
			{
				// Component must implement persister interface and
				// persistence for that component must be enabled.
				// Else ignore the persisted value. It'll be deleted
				// once the user submits the Form containing that FormComponent.
				// Note: if that is true, values may remain persisted longer
				// than really necessary
				final FormComponent formComponent = (FormComponent)component;
				if (formComponent.isPersistent())
				{
					// The persister
					final IValuePersister persister = getValuePersister();

					// Retrieve persisted value
					persister.load((FormComponent)component);
				}
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Sets the delegate to be used for execution of validation of this form.
	 * 
	 * @param validationDelegate
	 *            the delegate to be used for execution of validation of this
	 *            form
	 */
	public void setValidationDelegate(IFormValidationDelegate validationDelegate)
	{
		this.validationDelegate = validationDelegate;
	}

	/**
	 * @see wicket.Component#handleComponentTag(ComponentTag)
	 */
	protected void handleComponentTag(final ComponentTag tag)
	{
		checkTag(tag, "form");
		super.handleComponentTag(tag);
		tag.put("method", "POST");
		String url = getRequestCycle().urlFor(Form.this, IFormSubmitListener.class);
		url = url.replaceAll("&", "&amp;");
		tag.put("action", url);
	}

	/**
	 * Sets the value persister for this form.
	 * 
	 * @param persister
	 *            the CookieValuePersister
	 */
	protected void setFormComponentPersistenceManager(IValuePersister persister)
	{
		this.persister = persister;
	}

	/**
	 * Gets the form component persistence manager; it is lazy loaded.
	 * 
	 * @return The form component value persister
	 */
	private IValuePersister getValuePersister()
	{
		if (persister == null)
		{
			persister = new CookieValuePersister();
		}
		return persister;
	}

	/**
	 * Persist (e.g. Cookie) FormComponent data to be reloaded and re-assigned
	 * to the FormComponent automatically when the page is visited by the user
	 * next time.
	 * 
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	private void persistFormComponentData()
	{
		// Cannot add cookies to request cycle unless it accepts them
		// We could conceivably be HTML over some other protocol!
		if (getRequestCycle() instanceof HttpRequestCycle)
		{
			// The persistence manager responsible to persist and retrieve
			// FormComponent data
			final IValuePersister persister = getValuePersister();

			// Search for FormComponent children. Ignore all other
			visitChildren(FormComponent.class, new IVisitor()
			{
				public Object component(final Component component)
				{
					// Can only a FormComponent
					final FormComponent formComponent = (FormComponent)component;

					// If peristence is switched on for that FormComponent ...
					if (formComponent.isPersistent())
					{
						// Save component's data (e.g. in a cookie)
						persister.save(formComponent);
					}
					else
					{
						// Remove component's data (e.g. cookie)
						persister.clear(formComponent);
					}

					return CONTINUE_TRAVERSAL;
				}
			});
		}
	}

	/**
	 * Update the model of all form components.
	 * 
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	private void updateFormComponentModels()
	{
		visitChildren(FormComponent.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				// Update model of form component
				final FormComponent formComponent = (FormComponent)component;

				// Only update the component when it is visible
				if (formComponent.isVisible())
				{
					formComponent.updateModel();
				}

				return CONTINUE_TRAVERSAL;
			}
		});
	}

	static
	{
		// Allow use of IFormSubmitListener interface
		RequestCycle.registerRequestListenerInterface(IFormSubmitListener.class);
	}
}