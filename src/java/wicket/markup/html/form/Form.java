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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.IFeedback;
import wicket.IFeedbackBoundary;
import wicket.Page;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.persistence.CookieValuePersister;
import wicket.markup.html.form.persistence.IValuePersister;
import wicket.markup.html.form.validation.IFormValidationStrategy;
import wicket.markup.html.form.validation.IFormValidator;
import wicket.markup.html.form.validation.IValidator;
import wicket.model.IModel;
import wicket.protocol.http.WebRequestCycle;
import wicket.util.lang.Classes;
import wicket.util.string.StringList;
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
public abstract class Form extends WebMarkupContainer
	implements IFormSubmitListener, IFeedbackBoundary
{
	/** Log. */
	private static Log log = LogFactory.getLog(Form.class);

	/** The validator or validator list for this form. */
	IFormValidator validator = IFormValidator.NULL;

	/** form validation strategy instance. */
	private IFormValidationStrategy formValidationStrategy;

	/**
	 * A convenient and memory efficent representation for a list of validators.
	 */
	private static final class ValidatorList implements IFormValidator
	{
		/** Left part of linked list. */
		private final IFormValidator left;

		/** Right part of linked list. */
		private IFormValidator right;

		/**
		 * Constructs a list with validators in it.
		 * @param left The left validator
		 * @param right The right validator
		 */
		ValidatorList(final IFormValidator left, final IFormValidator right)
		{
			this.left = left;
			this.right = right;
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
				stringList.add(Classes.name(current.left.getClass()) + " "
						+ current.left.toString());

				if (current.right instanceof ValidatorList)
				{
					current = (ValidatorList)current.right;
				}
				else
				{
					stringList.add(Classes.name(current.right.getClass()) + " "
							+ current.right.toString());

					break;
				}
			}

			return stringList.toString();
		}

		/**
		 * @see wicket.markup.html.form.validation.IFormValidator#validate(wicket.markup.html.form.Form)
		 */
		public void validate(final Form form)
		{
			left.validate(form);
			right.validate(form);
		}

		/**
		 * Adds the given validator to this list of validators.
		 * @param validator The validator
		 */
		void add(final IFormValidator validator)
		{
			ValidatorList current = this;

			while (current.right instanceof ValidatorList)
			{
				current = (ValidatorList)current.right;
			}

			current.right = new ValidatorList(current.right, validator);
		}

		/**
		 * Gets the validators as a List.
		 * @return the validators as a List
		 */
		List asList()
		{
			ValidatorList current = this;
			List validators = new ArrayList();
			while (true)
			{
				validators.add(current.left);
				if (current.right instanceof ValidatorList)
				{
					current = (ValidatorList)current.right;
				}
				else
				{
					validators.add(current.right);
					break;
				}
			}
			return validators;
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
		if(feedback != null)
		{
			feedback.setCollectingComponent(this);
		}
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
		if(feedback != null)
		{
			feedback.setCollectingComponent(this);
		}
	}

	/**
	 * Adds a validator to this form component.
	 * 
	 * @param validator
	 *            The validator
	 * @return This
	 */
	public final Form add(final IFormValidator validator)
	{
		// If we don't yet have a validator
		if (this.validator == IFormValidator.NULL)
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
				((ValidatorList)this.validator).add(validator);
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
	 * Gets the strategy to be used for form validation
	 * 
	 * @return The strategy to be used for validating this form
	 */
	public IFormValidationStrategy getValidationStrategy()
	{
		// lazy construct
		if (formValidationStrategy == null)
		{
			formValidationStrategy = new DefaultFormValidationStrategy();
		}
		return formValidationStrategy;
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
			// Update model using form data
			updateFormComponentModels();

			// Persist FormComponents if requested
			persistFormComponentData();

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
	public final void visitFormComponents(final FormComponent.IVisitor visitor)
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
//
//	/**
//	 * Called to indicate that
//	 */
//	public final void valid()
//	{
//		onValid();
//	}
//
//	/**
//	 * Handle validation
//	 */
//	protected void onValid()
//	{
//	}

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
	 * Gets whether this component is to be validated.
	 * 
	 * @return True if this component has one or more validators
	 */
	public final boolean isValidated()
	{
		return this.validator != IValidator.NULL;
	}

	/**
	 * @return True if this form has at least one error.
	 */
	boolean hasError()
	{
		// if this form itself has an error message
		if (hasErrorMessage())
		{
			return true;
		}

		// the form doesn't have any errors, now check any nested form components
		return anyFormComponentError();
	}

	/**
	 * Find out whether there is any registered error for a form component.
	 * @return whether there is any registered error for a form component
	 */
	private boolean anyFormComponentError()
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
		// call invalidate methods of all nested form components
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
	void updateFormComponentModels()
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