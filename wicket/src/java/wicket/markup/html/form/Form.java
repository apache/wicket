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

import java.util.HashMap;

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
import wicket.model.IModel;
import wicket.model.Model;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.util.lang.Bytes;
import wicket.util.string.Strings;
import wicket.util.upload.FileUploadException;
import wicket.util.upload.FileUploadBase.SizeLimitExceededException;

/**
 * Base class for forms. To implement a form, subclass this class, add
 * FormComponents (such as CheckBoxes, ListChoices or TextFields) to the form.
 * You can nest multiple buttons if you want to vary submit behaviour. However,
 * it is not nescesarry to use Wicket's button class, just putting e.g.
 * &lt;input type="submit" value="go"&gt; suffices.
 * <p>
 * By default, the processing of a form works like this:
 * <li> The submitting button is looked up. A submitting button is a button that
 * is nested in this form (is a child component) and that was clicked by the
 * user. If a submitting button was found, and it has the immediate field true
 * (default is false), it's onSubmit method will be called right away, thus no
 * validition is done, and things like updating form component models that would
 * normally be done are skipped. In that respect, nesting a button with the
 * immediate field set to true has the same effect as nesting a normal link. If
 * you want you can call validate() to execute form validation, hasError() to
 * find out whether validate() resulted in validation errors, and
 * updateFormComponentModels() to update the models of nested form components.
 * </li>
 * <li> When no immediate submitting button was found, this form is validated
 * (method validate()). Now, two possible paths exist:
 * <ul>
 * <li> Form validation failed. All nested form components will be marked valid,
 * and onError() is called to allow clients to provide custom error handling
 * code. </li>
 * <li> Form validation succeeded. The nested components will be asked to update
 * their models and persist their data is applicable. After that, method
 * delegateSubmit with optionally the submitting button is called. The default
 * when there is a submitting button is to first call onSubmit on that button,
 * and after that call onSubmit on this form. Clients may override
 * delegateSubmit if they want different behaviour. </li>
 * </ul>
 * </li>
 * </li>
 * </p>
 * 
 * Form for handling (file) uploads with multipart requests is supported by
 * callign setMultiPart(true). Use this with
 * {@link wicket.markup.html.form.upload.FileUploadField} components. You can
 * attach mutliple FileInput fields for muliple file uploads.
 * <p>
 * Using multipart forms causes a runtime dependency on <a
 * href="http://jakarta.apache.org/commons/fileupload/">Commons FileUpload</a>,
 * version 1.0.
 * </p>
 * 
 * 
 * <p>
 * If you want to have multiple buttons which submit the same form, simply put
 * two or more button components somewhere in the hierarchy of components that
 * are children of the form.
 * </p>
 * <p>
 * To get form components to persist their values for users via cookies, simply
 * call setPersistent(true) on the form component.
 * </p>
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 * @author Cameron Braid
 */
public class Form extends WebMarkupContainer implements IFormSubmitListener
{
	/** Log. */
	private static Log log = LogFactory.getLog(Form.class);

	/** Maximum size of an upload in bytes */
	private Bytes maxSize = Bytes.MAX;

	/** True if the form has enctype of multipart/form-data */
	private boolean multiPart = false;

	/**
	 * Constructs a form with no validation.
	 * 
	 * @param id
	 *            See Component
	 */
	public Form(final String id)
	{
		super(id);
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @see wicket.Component#Component(String, IModel)
	 */
	public Form(final String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * @return the maxSize of uploaded files
	 */
	public Bytes getMaxSize()
	{
		return this.maxSize;
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
				if (formComponent.isVisibleInHierarchy() && formComponent.isPersistent())
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
	public final void onFormSubmitted()
	{
		if (handleMultiPart())
		{
			// First, see if the processing was triggered by a Wicket button
			final Button submittingButton = findSubmittingButton();

			// When processing was triggered by a Wicket button and that button
			// indicates it wants to be called immediately (without processing),
			// call Button.onSubmit() right away.
			if (submittingButton != null && !submittingButton.getDefaultFormProcessing())
			{
				submittingButton.onSubmit();
			}
			else
			{
				// process the form for this request
				if (process())
				{
					// let clients handle further processing
					delegateSubmit(submittingButton);
				}
			}
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
				if (formComponent.isVisibleInHierarchy())
				{
					// remove the FormComponent's persisted data
					persister.clear(formComponent);

					// Disable persistence if requested. Leave unchanged
					// otherwise.
					if (formComponent.isPersistent() && disablePersistence)
					{
						formComponent.setPersistent(false);
					}
				}
			}
		});
	}

	/**
	 * @param maxSize
	 *            The maxSize for uploaded files
	 */
	public void setMaxSize(final Bytes maxSize)
	{
		this.maxSize = maxSize;
	}

	/**
	 * Set to true to use enctype='multipart/form-data', and to process file
	 * uplloads by default multiPart = false
	 * 
	 * @param multiPart
	 *            whether this form should behave as a multipart form
	 */
	public void setMultiPart(boolean multiPart)
	{
		this.multiPart = multiPart;
	}

	/**
	 * @see wicket.Component#setVersioned(boolean)
	 */
	public Component setVersioned(final boolean isVersioned)
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
		return this;
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

	/**
	 * Template method to allow clients to do any processing (like recording the
	 * current model so that, in case onSubmit does further validation, the
	 * model can be rolled back) before the actual updating of form component
	 * models is done.
	 */
	protected void beforeUpdateFormComponentModels()
	{
	}

	/**
	 * Called (by the default implementation of 'process') when all fields
	 * validated, the form was updated and it's data was allowed to be
	 * persisted. It is meant for delegating further processing to clients.
	 * <p>
	 * This implementation first finds out whether the form processing was
	 * triggered by a nested button of this form. If that is the case, that
	 * button's onSubmit is called first.
	 * </p>
	 * <p>
	 * Regardless of whether a submitting button was found, the form's onSubmit
	 * method is called next.
	 * </p>
	 * 
	 * @param submittingButton
	 *            the button that triggered this form processing, or null if the
	 *            processing was triggered by something else (like a non-Wicket
	 *            submit button or a javascript execution)
	 */
	protected void delegateSubmit(Button submittingButton)
	{
		// when the given button is not null, it means that it was the
		// submitting button
		if (submittingButton != null)
		{
			submittingButton.onSubmit();
		}

		// Model was successfully updated with valid data
		onSubmit();
	}

	/**
	 * Gets the button which submitted this form.
	 * 
	 * @return The button which submitted this form or null if the processing
	 *         was not trigger by a registered button component
	 */
	protected final Button findSubmittingButton()
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
	 * Gets the form component persistence manager; it is lazy loaded.
	 * 
	 * @return The form component value persister
	 */
	protected IValuePersister getValuePersister()
	{
		return new CookieValuePersister();
	}

	/**
	 * Gets whether the current form has any error registered.
	 * 
	 * @return True if this form has at least one error.
	 */
	protected final boolean hasError()
	{
		// if this form itself has an error message
		if (hasErrorMessage())
		{
			return true;
		}

		// the form doesn't have any errors, now check any nested form
		// components
		return anyFormComponentError();
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
	 * Mark each form component on this form invalid.
	 */
	protected final void markFormComponentsInvalid()
	{
		// call invalidate methods of all nested form components
		visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				if (formComponent.isVisibleInHierarchy())
				{
					formComponent.invalid();
				}
			}
		});
	}

	/**
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "form");
		super.onComponentTag(tag);
		tag.put("method", "post");
		tag.put("action", Strings.replaceAll(urlFor(IFormSubmitListener.class), "&", "&amp;"));
		if (multiPart)
		{
			tag.put("enctype", "multipart/form-data");
		}
	}

	/**
	 * Method to override if you want to do something special when an error
	 * occurs (other than simply displaying validation errors).
	 */
	protected void onError()
	{
	}

	/**
	 * @see wicket.Component#onRender()
	 */
	protected void onRender()
	{
		// Force multi-part on if any child form component is multi-part
		visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(FormComponent formComponent)
			{
				if (formComponent.isVisible() && formComponent.isMultiPart())
				{
					setMultiPart(true);
				}
			}
		});

		super.onRender();
	}

	/**
	 * Implemented by subclasses to deal with form submits.
	 */
	protected void onSubmit()
	{
	}

	/**
	 * Process the form. Though you can override this method to provide your
	 * whole own algorithm, it is not recommended to do so.
	 * <p>
	 * See the class documentation for further details on the form processing
	 * </p>
	 * 
	 * @return False if the form had an error
	 */
	protected boolean process()
	{
		// Execute validation now before anything else
		validate();

		// If a validation error occurred
		if (hasError())
		{
			// mark all children as invalid
			markFormComponentsInvalid();

			// let subclass handle error
			onError();

			// Form has an error
			return false;
		}
		else
		{
			// before updating, call the interception method for clients
			beforeUpdateFormComponentModels();

			// Update model using form data
			updateFormComponentModels();

			// Persist FormComponents if requested
			persistFormComponentData();

			// Form has no error
			return true;
		}
	}

	/**
	 * Update the model of all form components using the fields that were sent
	 * with the current request.
	 * 
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	protected final void updateFormComponentModels()
	{
		visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				// Only update the component when it is visible and valid
				if (formComponent.isVisibleInHierarchy() && formComponent.isValid())
				{
					// Potentially update the model
					formComponent.updateModel();
				}
			}
		});
	}

	/**
	 * Validates the form's nested children of type {@link FormComponent}. This
	 * method is typically called before updating any models.
	 */
	protected void validate()
	{
		// Validate model using validation strategy
		// Visit all the form components and validate each
		visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				if (formComponent.isVisibleInHierarchy())
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
			}
		});
	}

	/**
	 * Find out whether there is any registered error for a form component.
	 * 
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
	 * @return False if form is multipart and upload failed
	 */
	private final boolean handleMultiPart()
	{
		if (multiPart)
		{
			// Change the request to a multipart web request so parameters are
			// parsed out correctly
			try
			{
				final WebRequest multipartWebRequest = ((WebRequest)getRequest()).newMultipartWebRequest(this.maxSize);
				getRequestCycle().setRequest(multipartWebRequest);
			}
			catch (WicketRuntimeException wre)
			{
				if ( wre.getCause() == null || !(wre.getCause() instanceof FileUploadException) )
				{
					throw wre;
				}
				
				FileUploadException e = (FileUploadException)wre.getCause();
				// Create model with exception and maximum size values
				final HashMap model = new HashMap();
				model.put("exception", e);
				model.put("maxSize", maxSize);

				if (e instanceof SizeLimitExceededException)
				{
					// Resource key should be <form-id>.uploadTooLarge to
					// override default message
					final String defaultValue = "Upload must be less than " + maxSize;
					String msg = getString(getId() + ".uploadTooLarge", Model.valueOf(model),
							defaultValue);
					error(msg);

					if (log.isDebugEnabled())
					{
						log.error(msg, e);
					}
					else
					{
						log.error(msg);
					}
				}
				else
				{
					// Resource key should be <form-id>.uploadFailed to override
					// default message
					final String defaultValue = "Upload failed: " + e.getLocalizedMessage();
					String msg = getString(getId() + ".uploadFailed", Model.valueOf(model),
							defaultValue);
					error(msg);

					log.error(msg, e);
				}

				// don't process the form if there is a FileUploadException
				return false;
			}
		}
		return true;
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
					if (formComponent.isVisibleInHierarchy())
					{
						// If peristence is switched on for that FormComponent
						// ...
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
				}
			});
		}
	}

	static
	{
		// Allow use of IFormSubmitListener interface
		RequestCycle.registerRequestListenerInterface(IFormSubmitListener.class);
	}
}