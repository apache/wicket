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

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.Page;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.persistence.CookieValuePersister;
import wicket.markup.html.form.persistence.IValuePersister;
import wicket.markup.html.form.validation.IFormValidationDelegate;
import wicket.markup.html.form.validation.IValidationFeedback;
import wicket.protocol.http.WebRequestCycle;
import wicket.util.string.Strings;

/**
 * Base class for forms. To implement a form, subclass this class, add
 * FormComponents (such as CheckBoxes, ListChoices or TextFields) to the form
 * and provide an implementation of handleValidSubmit(). The handleValidSubmit()
 * method will be called by validate() when the form passes validation. If your
 * form has only one button, there is nothing else to do. However, if you want
 * to have multiple buttons which submit the same form, simply put two or more
 * button components somewhere in the hierarchy of components that are children
 * of the form. Forms which have two or more buttons do not automatically
 * validate themselves via validate(). Instead, they determine which Button
 * submitted the form and call that Button's onSubmit() method. In any
 * onSubmit() method where you want to attempt to validate the form and update
 * models, simply call validate().
 * <p>
 * If you want to do something when validation errors occur you can override
 * onError(), but if you do, you probably will want to call super.onError() to
 * get the default handling to occur.
 * <p>
 * To get form components to persist their values for users via cookies, simply
 * call setPersistent(true) on the form component.
 *
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public abstract class Form extends WebMarkupContainer implements IFormSubmitListener
{
	/** Log. */
	private static Log log = LogFactory.getLog(Form.class);

	/** The validation error handling delegate. */
	private final IValidationFeedback validationFeedback;

	/**
	 * Trivial class for holding button count while counting buttons
	 *
	 * @author Jonathan Locke
	 */
	private static class Count
	{
		int count;
	}

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
		 */
		public void validate(final Form form)
		{
			// Visit all the form components and validate each
			form.visitChildren(FormComponent.class, new IVisitor()
			{
				public Object component(final Component component)
				{
					// Get form component
					final FormComponent formComponent = (FormComponent)component;

					// Validate form component
					formComponent.validate();

					// If component is not valid (has an error)
					if (!formComponent.isValid())
					{
						// tell component to deal with invalidity
						formComponent.onInvalid();
					}
					else
					{
						// tell component that it is valid now
						formComponent.onValid();
					}

					// Continue until the end
					return IVisitor.CONTINUE_TRAVERSAL;
				}
			});
		}
	}

	/**
	 * Constructs a form with no validation.
	 *
	 * @param name
	 *            Name of this form
	 */
	public Form(final String name)
	{
		this(name, null);
	}

	/**
	 * @see wicket.Component#Component(String, Serializable)
	 * @param name
	 *            See Component constructor
	 * @param validationFeedback
	 *            Interface to a component that can handle/display validation
	 *            errors
	 */
	public Form(final String name, final IValidationFeedback validationFeedback)
	{
		super(name);
		this.validationFeedback = validationFeedback;
	}

	/**
	 * @see wicket.Component#Component(String, Serializable)
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param validationFeedback
	 *            Interface to a component that can handle/display validation
	 *            errors
	 */
	public Form(String name, Serializable object, final IValidationFeedback validationFeedback)
	{
		super(name, object);
		this.validationFeedback = validationFeedback;
	}

	/**
	 * @see wicket.Component#Component(String, Serializable)
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param expression
	 *            See Component constructor
	 * @param validationFeedback
	 *            Interface to a component that can handle/display validation
	 *            errors
	 */
	public Form(String name, Serializable object, String expression,
			final IValidationFeedback validationFeedback)
	{
		super(name, object, expression);
		this.validationFeedback = validationFeedback;
	}

	/**
	 * Gets the delegate to be used for execution of validation of this form.
	 *
	 * @return the delegate to be used for execution of validation of this form
	 */
	public IFormValidationDelegate getValidationDelegate()
	{
		return DefaultFormValidationDelegate.getInstance();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Retrieves FormComponent values related to the page using the persister
	 * and assign the values to the FormComponent. Thus initializing them.
	 */
	public final void loadPersistentFormComponentValues()
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
	 * THIS METHOD IS NOT PART OF THE WICKET API.  DO NOT ATTEMPT TO OVERRIDE
	 * OR CALL IT.
	 *
	 * Handles form submissions. By default, this method simply calls validate()
	 * to validate the form and update the model if there is only one button.
	 * If there is more than one button, it calls the onClick() method for the
	 * button which submitted the form.
	 *
	 * @see Form#validate()
	 */
	public void onFormSubmitted()
	{
		onValidate();
	}

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
	public void removePersistentFormComponentValues(final boolean disablePersistence)
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
	 * Gets the form component persistence manager; it is lazy loaded.
	 *
	 * @return The form component value persister
	 */
	protected IValuePersister getValuePersister()
	{
		return new CookieValuePersister();
	}

	/**
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "form");
		super.onComponentTag(tag);
		tag.put("method", "POST");
		String url = getRequestCycle().urlFor(Form.this, IFormSubmitListener.class);
		url = url.replaceAll("&", "&amp;");
		tag.put("action", url);
	}

	/**
	 * Sets error messages for form. First all childs (form components) are
	 * asked to do their part of error handling, and after that, the registered
	 * (if any) error handler of this form is called.
	 */
	protected void onError()
	{
		// Traverse children of this form, calling validationError() on any
		// components implementing IValidationFeedback.
		visitChildren(IValidationFeedback.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				// Call validation error handler
				((IValidationFeedback)component).updateValidationFeedback();

				// Traverse all children
				return CONTINUE_TRAVERSAL;
			}
		});

		// Call the validation handler that is registered with this form, if any
		if (validationFeedback != null)
		{
			validationFeedback.updateValidationFeedback();
		}
	}

	/**
	 * Implemented by subclasses to deal with form submits.
	 */
	protected abstract void onSubmit();

	/**
	 * Called when a form that has been submitted needs to be validated.
	 */
	protected void onValidate()
	{
		final int buttons = countButtons();
		if (buttons <= 1)
		{
			validate();
		}
		else if (buttons > 1)
		{
			invokeButtonClicked();
		}
	}

	/**
	 * Validates the form and updates the models of form components. If the form
	 * validates successfully, handleValidSubmit() is called. If not,
	 * handleErrors() is called.
	 *
	 * @see Form#onSubmit()
	 * @see Form#onError()
	 */
	protected final void validate()
	{
		// Redirect back to result to avoid postback warnings. But we turn
		// redirecting on as the first thing because the user's handleSubmit
		// implementation may wish to redirect somewhere else. In that case,
		// they can simply call setRedirect(false) in handleSubmit.
		getRequestCycle().setRedirect(true);

		// Validate model using validation delegate
		getValidationDelegate().validate(this);

		// Update model using form data
		updateFormComponentModels();

		// Persist FormComponents if requested
		persistFormComponentData();

		// If validation or update caused error message(s) to appear
		if (hasError())
		{
			// handle those errors
			onError();
		}
		else
		{
			// Model was successfully updated with valid data
			onSubmit();
		}
	}

	private int countButtons()
	{
		final Count count = new Count();
		visitChildren(Button.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				count.count++;
				return CONTINUE_TRAVERSAL;
			}
		});
		return count.count;
	}

	private Button findSubmittingButton()
	{
		return (Button)visitChildren(Button.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				// Get button
				final Button button = (Button)component;

				// Check for button-name or button-name.x request string
				if (!Strings.isEmpty(button.getRequestString())
						|| !Strings.isEmpty(getRequest().getParameter(button.getPath() + ".x")))
				{
					return button;
				}
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * @return True if this form has at least one error.
	 */
	private boolean hasError()
	{
		final Object value = visitChildren(new IVisitor()
		{
			public Object component(final Component component)
			{
				if (component.hasErrorMessage())
				{
					return STOP_TRAVERSAL;
				}

				// Traverse all children
				return CONTINUE_TRAVERSAL;
			}
		});

		return value == IVisitor.STOP_TRAVERSAL ? true : false;
	}

	private void invokeButtonClicked()
	{
		final Button button = findSubmittingButton();
		if (button == null)
		{
			throw new WicketRuntimeException("Unable to find submitting button");
		}
		else
		{
			button.onSubmit();
		}
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
		if (getRequestCycle() instanceof WebRequestCycle)
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

				// Only update the component when it is visible and valid
				if (formComponent.isVisible() && formComponent.isValid())
				{
					// Get model lock since we're going to change the model
					synchronized (formComponent.getModelLock())
					{
						// Potentially update the model
						formComponent.updateModel();
					}
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