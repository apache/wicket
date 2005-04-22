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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.IFeedback;
import wicket.Page;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.persistence.CookieValuePersister;
import wicket.markup.html.form.persistence.IValuePersister;
import wicket.markup.html.form.validation.IFormValidationStrategy;
import wicket.model.IModel;
import wicket.protocol.http.WebRequestCycle;
import wicket.util.string.Strings;
import wicket.util.value.Count;

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
	private final IFeedback feedback;

	/**
	 * The default form validation strategy.
	 */
	private static final class DefaultFormValidationStrategy implements IFormValidationStrategy
	{
		/** Single instance of default form validation strategy */
		private static final DefaultFormValidationStrategy instance = new DefaultFormValidationStrategy();

		/** Log. */
		private static final Log log = LogFactory.getLog(DefaultFormValidationStrategy.class);

		/**
		 * @return Singleton instance of DefaultFormValidationStrategy
		 */
		private static DefaultFormValidationStrategy getInstance()
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
			form.visitFormComponents(new FormComponent.IVisitor()
			{
				public void formComponent(final FormComponent formComponent)
				{
					// Validate form component
					formComponent.validate();

					// If component is not valid (has an error)
					if (!formComponent.isValid())
					{
						// tell component to deal with invalidity
						formComponent.invalid();
					}
					else
					{
						// tell component that it is valid now
						formComponent.valid();
					}
				}
			});
		}
	}

	/**
	 * Constructs a form with no validation.
	 * 
	 * @param id
	 *            See Component
	 */
	public Form(final String id)
	{
		this(id, null);
	}

	/**
	 * @param id
	 *            See Component
	 * @param feedback
	 *            Interface to a component that can handle/display validation
	 *            errors
	 * @see wicket.Component#Component(String)
	 */
	public Form(final String id, final IFeedback feedback)
	{
		super(id);
		this.feedback = feedback;
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param feedback
	 *            Interface to a component that can handle/display validation
	 *            errors
	 * @see wicket.Component#Component(String, IModel)
	 */
	public Form(final String id, IModel model, final IFeedback feedback)
	{
		super(id, model);
		this.feedback = feedback;
	}

	/**
	 * Gets the strategy to be used for form validation
	 * 
	 * @return The strategy to be used for validating this form
	 */
	public IFormValidationStrategy getValidationStrategy()
	{
		return DefaultFormValidationStrategy.getInstance();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Retrieves FormComponent values related to the page using the persister
	 * and assign the values to the FormComponent. Thus initializing them.
	 */
	public final void loadPersistentFormComponentValues()
	{
		visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				// Component must implement persister interface and
				// persistence for that component must be enabled.
				// Else ignore the persisted value. It'll be deleted
				// once the user submits the Form containing that FormComponent.
				// Note: if that is true, values may remain persisted longer
				// than really necessary
				if (formComponent.isPersistent())
				{
					// The persister
					final IValuePersister persister = getValuePersister();

					// Retrieve persisted value
					persister.load(formComponent);
				}
			}
		});
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET API. DO NOT ATTEMPT TO OVERRIDE OR
	 * CALL IT.
	 * 
	 * Handles form submissions. By default, this method simply calls validate()
	 * to validate the form and update the model if there is only one button. If
	 * there is more than one button, it calls the onClick() method for the
	 * button which submitted the form.
	 * 
	 * @see Form#validate()
	 */
	public void onFormSubmitted()
	{
		// Validate form
		onValidate();

		// Maurice pointed out that onValidate() calls user code in onSubmit()
		// which may do something like replace the whole form with something
		// else!
		if (findPage() != null)
		{
			// Update validation feedback no matter how the form was validated
			addFeedback();
		}
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
		visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				// remove the FormComponent's persisted data
				persister.clear(formComponent);

				// Disable persistence if requested. Leave unchanged otherwise.
				if (formComponent.isPersistent() && disablePersistence)
				{
					formComponent.setPersistent(false);
				}
			}
		});
	}

	/**
	 * @see wicket.Component#setVersioned(boolean)
	 */
	public void setVersioned(final boolean isVersioned)
	{
		super.setVersioned(isVersioned);

		// Search for FormComponents like TextField etc.
		visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				formComponent.setVersioned(isVersioned);
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
		tag.put("action", Strings.replaceAll(urlFor(IFormSubmitListener.class), "&", "&amp;"));
	}

	/**
	 * Method to override if you want to do something special when an error
	 * occurs (other than simply displaying validation errors).
	 */
	protected void onError()
	{
	}

	/**
	 * @see wicket.Component#internalOnModelChanged()
	 */
	protected void internalOnModelChanged()
	{
		// Visit all the form components and validate each
		visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				// If form component is using form model
				if (formComponent.sameRootModel(Form.this))
				{
					formComponent.modelChanged();
				}
			}
		});
	}

	/**
	 * Implemented by subclasses to deal with form submits.
	 */
	protected void onSubmit()
	{
	}

	/**
	 * Called when a form that has been submitted needs to be validated.
	 */
	protected void onValidate()
	{
		// Validate the form
		if (validate())
		{
			// If there is more than one button, we also call the Button's
			// onSubmit() handler
			if (countButtons() > 1)
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
		}
	}

	/**
	 * Validates the form and updates the models of form components. If the form
	 * validates successfully, handleValidSubmit() is called. If not,
	 * handleErrors() is called.
	 * 
	 * @return True if the form validated
	 * 
	 * @see Form#onSubmit()
	 * @see Form#onError()
	 */
	protected final boolean validate()
	{
		// Redirect back to result to avoid postback warnings. But we turn
		// redirecting on as the first thing because the user's handleSubmit
		// implementation may wish to redirect somewhere else. In that case,
		// they can simply call setRedirect(false) in handleSubmit.
		setRedirect(true);

		// Validate model using validation strategy
		getValidationStrategy().validate(this);

		// If a validation error occurred
		if (hasError())
		{
			// mark all children as invalid
			invalid();

			// let subclass handle error
			onError();
			
			// Form failed to validate
			return false;
		}
		else
		{
			// Persist FormComponents if requested
			persistFormComponentData();

			// Update model using form data
			updateFormComponentModels();

			// Model was successfully updated with valid data
			onSubmit();
			
			// Form validated
			return true;
		}
	}

	/**
	 * Convenient and typesafe way to visit all the form components on a form
	 * 
	 * @param visitor
	 *            The visitor interface to call
	 */
	protected void visitFormComponents(final FormComponent.IVisitor visitor)
	{
		visitChildren(FormComponent.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				visitor.formComponent((FormComponent)component);
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Updates feedback on each feedback component on or attached to the form.
	 */
	private void addFeedback()
	{
		// Traverse children of this form, calling validationError() on any
		// components implementing IFeedback.
		visitChildren(IFeedback.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				// Call validation error handler
				((IFeedback)component).addFeedbackMessages(Form.this, true);

				// Traverse all children
				return CONTINUE_TRAVERSAL;
			}
		});

		// Add feedback messages to the feedback display that is registered with
		// this form, if any
		if (feedback != null)
		{
			feedback.addFeedbackMessages(this, true);
		}
	}

	/**
	 * @return Number of buttons on this form
	 */
	private int countButtons()
	{
		final Count count = new Count();
		visitChildren(Button.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				count.increment();
				return CONTINUE_TRAVERSAL;
			}
		});
		return count.getCount();
	}

	/**
	 * @return The button which submitted this form
	 */
	private Button findSubmittingButton()
	{
		return (Button)visitChildren(Button.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				// Get button
				final Button button = (Button)component;

				// Check for button-name or button-name.x request string
				if (!Strings.isEmpty(button.getInput())
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

	/**
	 * Mark each form component on this form invalid
	 */
	private void invalid()
	{
		visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				formComponent.invalid();
			}
		});
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
			visitFormComponents(new FormComponent.IVisitor()
			{
				public void formComponent(final FormComponent formComponent)
				{
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
		visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				// Only update the component when it is visible and valid
				if (formComponent.isVisible() && formComponent.isValid())
				{
					// Potentially update the model
					formComponent.updateModel();
				}
			}
		});
	}

	static
	{
		// Allow use of IFormSubmitListener interface
		RequestCycle.registerRequestListenerInterface(IFormSubmitListener.class);
	}
}