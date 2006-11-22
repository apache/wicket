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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.IRequestTarget;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.Request;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.border.Border;
import wicket.markup.html.form.persistence.CookieValuePersister;
import wicket.markup.html.form.persistence.IValuePersister;
import wicket.markup.html.form.validation.IFormValidator;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.request.WebClientInfo;
import wicket.request.IRequestCycleProcessor;
import wicket.request.RequestParameters;
import wicket.request.target.component.listener.ListenerInterfaceRequestTarget;
import wicket.util.lang.Bytes;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;
import wicket.util.string.interpolator.MapVariableInterpolator;
import wicket.util.upload.FileUploadException;
import wicket.util.upload.FileUploadBase.SizeLimitExceededException;

/**
 * Base class for forms. To implement a form, subclass this class, add
 * FormComponents (such as CheckBoxes, ListChoices or TextFields) to the form.
 * You can nest multiple buttons if you want to vary submit behavior. However,
 * it is not necessary to use Wicket's button class, just putting e.g. &lt;input
 * type="submit" value="go"&gt; suffices.
 * <p>
 * By default, the processing of a form works like this:
 * <li> The submitting button is looked up. A submitting button is a button that
 * is nested in this form (is a child component) and that was clicked by the
 * user. If a submitting button was found, and it has the defaultFormProcessing
 * field set to false (default is true), it's onSubmit method will be called
 * right away, thus no validition is done, and things like updating form
 * component models that would normally be done are skipped. In that respect,
 * nesting a button with the defaultFormProcessing field set to false has the
 * same effect as nesting a normal link. If you want you can call validate() to
 * execute form validation, hasError() to find out whether validate() resulted
 * in validation errors, and updateFormComponentModels() to update the models of
 * nested form components. </li>
 * <li> When no submitting button with defaultFormProcessing set to false was
 * found, this form is processed (method process()). Now, two possible paths
 * exist:
 * <ul>
 * <li> Form validation failed. All nested form components will be marked
 * invalid, and onError() is called to allow clients to provide custom error
 * handling code. </li>
 * <li> Form validation succeeded. The nested components will be asked to update
 * their models and persist their data is applicable. After that, method
 * delegateSubmit with optionally the submitting button is called. The default
 * when there is a submitting button is to first call onSubmit on that button,
 * and after that call onSubmit on this form. Clients may override
 * delegateSubmit if they want different behavior. </li>
 * </ul>
 * </li>
 * </li>
 * </p>
 * 
 * Form for handling (file) uploads with multipart requests is supported by
 * callign setMultiPart(true) ( although wicket will try to automatically detect
 * this for you ). Use this with
 * {@link wicket.markup.html.form.upload.FileUploadField} components. You can
 * attach mutliple FileUploadField components for muliple file uploads.
 * <p>
 * In case of an upload error two resource keys are available to specify error
 * messages: uploadTooLarge and uploadFailed
 * 
 * ie in [page].properties
 * 
 * [form-id].uploadTooLarge=You have uploaded a file that is over the allowed
 * limit of 2Mb
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
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 */
public class Form extends WebMarkupContainer implements IFormSubmitListener
{
	/**
	 * Visitor used for validation
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 */
	private static abstract class ValidationVisitor implements FormComponent.IVisitor
	{

		/**
		 * @see wicket.markup.html.form.FormComponent.IVisitor#formComponent(wicket.markup.html.form.FormComponent)
		 */
		public void formComponent(FormComponent formComponent)
		{
			if (formComponent.isVisibleInHierarchy() && formComponent.isValid()
					&& formComponent.isEnabled() && formComponent.isEnableAllowed())
			{
				validate(formComponent);
			}
		}

		/**
		 * Callback that should be used to validate form component
		 * 
		 * @param formComponent
		 */
		public abstract void validate(FormComponent formComponent);

	}

	private static final String UPLOAD_TOO_LARGE_RESOURCE_KEY = "uploadTooLarge";

	private static final String UPLOAD_FAILED_RESOURCE_KEY = "uploadFailed";

	/** Flag that indicates this form has been submitted during this request */
	private static final short FLAG_SUBMITTED = FLAG_RESERVED1;

	private static final long serialVersionUID = 1L;

	/** Log. */
	private static final Log log = LogFactory.getLog(Form.class);

	/** Maximum size of an upload in bytes */
	private Bytes maxSize = Bytes.MAX;

	/** True if the form has enctype of multipart/form-data */
	private boolean multiPart = false;

	private String javascriptId;

	/** multi-validators assigned to this form */
	private Object formValidators = null;

	/**
	 * Any default button. If set, a hidden submit button will be rendered right
	 * after the form tag, so that when users press enter in a textfield, this
	 * button's action will be selected. If no default button is set, nothing
	 * additional is rendered.
	 * <p>
	 * WARNING: note that this is a best effort only. Unfortunately having a
	 * 'default' button in a form is ill defined in the standards, and of course
	 * IE has it's own way of doing things.
	 * </p>
	 */
	private Button defaultButton;

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

	protected boolean getStatelessHint()
	{
		return false;
	}
	
	/**
	 * Gets the default button. If set (not null), a hidden submit button will
	 * be rendered right after the form tag, so that when users press enter in a
	 * textfield, this button's action will be selected. If no default button is
	 * set (it is null), nothing additional is rendered.
	 * <p>
	 * WARNING: note that this is a best effort only. Unfortunately having a
	 * 'default' button in a form is ill defined in the standards, and of course
	 * IE has it's own way of doing things.
	 * </p>
	 * 
	 * @return The button to set as the default button, or null when you want to
	 *         'unset' any previously set default button
	 */
	public final Button getDefaultButton()
	{
		return defaultButton;
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
	 * Handles form submissions.
	 * 
	 * @see Form#validate()
	 */
	public final void onFormSubmitted()
	{
		setFlag(FLAG_SUBMITTED, true);

		if (handleMultiPart())
		{
			// Tells FormComponents that a new user input has come
			inputChanged();

			String url = getRequest().getParameter(getHiddenFieldId());
			if (!Strings.isEmpty(url))
			{
				dispatchEvent(getPage(), url);
			}
			else
			{
				// First, see if the processing was triggered by a Wicket button
				final Button submittingButton = findSubmittingButton();

				// When processing was triggered by a Wicket button and that
				// button indicates it wants to be called immediately
				// (without processing), call Button.onSubmit() right away.
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
		// If multi part did fail check if an error is registered and call onError
		else if(hasError())
		{
			onError();
		}
	}

	/**
	 * Checks if this form has been submitted during the current request
	 * 
	 * @return true if the form has been submitted during this request, false
	 *         otherwise
	 */
	public final boolean isSubmitted()
	{
		return getFlag(FLAG_SUBMITTED);
	}

	/**
	 * @see wicket.Component#internalOnDetach()
	 */
	protected void internalOnDetach()
	{
		super.internalOnDetach();
		setFlag(FLAG_SUBMITTED, false);
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
	 * Sets the default button. If set (not null), a hidden submit button will
	 * be rendered right after the form tag, so that when users press enter in a
	 * textfield, this button's action will be selected. If no default button is
	 * set (so unset by calling this method with null), nothing additional is
	 * rendered.
	 * <p>
	 * WARNING: note that this is a best effort only. Unfortunately having a
	 * 'default' button in a form is ill defined in the standards, and of course
	 * IE has it's own way of doing things.
	 * </p>
	 * 
	 * @param button
	 *            The button to set as the default button, or null when you want
	 *            to 'unset' any previously set default button
	 */
	public final void setDefaultButton(Button button)
	{
		this.defaultButton = button;
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
	public final Component setVersioned(final boolean isVersioned)
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
	 * Method made final because we want to ensure users call setVersioned.
	 * 
	 * @see wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return super.isVersioned();
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

		/**
		 * TODO Post 1.2 General: Maybe we should re-think how Borders are
		 * implemented, because there are just too many exceptions in the code
		 * base because of borders. This time it is to solve the problem tested
		 * in BoxBorderTestPage_3 where the Form is defined in the box border
		 * and the FormComponents are in the "body". Thus, the formComponents
		 * are not childs of the form. They are rather childs of the border, as
		 * the Form itself.
		 */
		if (getParent() instanceof Border)
		{
			MarkupContainer border = getParent();
			Iterator iter = border.iterator();
			while (iter.hasNext())
			{
				Component child = (Component)iter.next();
				if (child instanceof FormComponent)
				{
					visitor.formComponent((FormComponent)child);
				}
			}
		}
	}

	/**
	 * If a default button was set on this form, this method will be called to
	 * render an extra field with an invisible style so that pressing enter in
	 * one of the textfields will do a form submit using this button. This
	 * method is overridable as what we do is best effort only, and may not what
	 * you want in specific situations. So if you have specific usability
	 * concerns, or want to follow another strategy, you may override this
	 * method.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 */
	protected void appendDefaultButtonField(final MarkupStream markupStream,
			final ComponentTag openTag)
	{
		String nameAndId = getHiddenFieldId();
		AppendingStringBuffer buffer = new AppendingStringBuffer();
		// get the value, first seeing whether the value attribute is set
		// by a model
		String value = defaultButton.getModelObjectAsString();
		if (value == null || "".equals(value))
		{
			// nope it isn't; try to read from the attributes
			// note that we're only trying lower case here
			value = defaultButton.getMarkupAttributes().getString("value");
		}

		// append the button
		String userAgent = ((WebClientInfo)getSession().getClientInfo()).getUserAgent();
		buffer.append("<input type=\"submit\" value=\"").append(value).append("\" name=\"").append(
				defaultButton.getInputName()).append("\"");
		if (userAgent != null && userAgent.indexOf("MSIE") != -1)
		{
			buffer.append("style=\"width: 0px; height: 0px; position: absolute;\"");
		}
		else
		{
			buffer.append(" style=\"display: none\"");
		}
		buffer.append(" />");
		getResponse().write(buffer);
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
	public final Button findSubmittingButton()
	{
		Button button = (Button)visitChildren(Button.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				// Get button
				final Button button = (Button)component;

				// Check for button-name or button-name.x request string
				if (getRequest().getParameter(button.getInputName()) != null
						|| getRequest().getParameter(button.getInputName() + ".x") != null)
				{
					if (!button.isVisible())
					{
						throw new WicketRuntimeException("Submit Button " + button.getInputName()
								+ " (path=" + button.getPageRelativePath() + ") is not visible");
					}
					return button;
				}
				return CONTINUE_TRAVERSAL;
			}
		});

		if (button == null)
		{
			button = (Button)getPage().visitChildren(SubmitLink.class, new IVisitor()
			{
				public Object component(final Component component)
				{
					// Get button
					final SubmitLink button = (SubmitLink)component;

					// Check for button-name or button-name.x request string
					if (button.getForm() == Form.this
							&& (getRequest().getParameter(button.getInputName()) != null || getRequest()
									.getParameter(button.getInputName() + ".x") != null))
					{
						if (!button.isVisible())
						{
							throw new WicketRuntimeException("Submit Button is not visible");
						}
						return button;
					}
					return CONTINUE_TRAVERSAL;
				}
			});
		}
		return button;
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
	public final boolean hasError()
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
	 * Mark each form component on this form valid.
	 */
	protected final void markFormComponentsValid()
	{
		// call invalidate methods of all nested form components
		visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				if (formComponent.isVisibleInHierarchy())
				{
					formComponent.valid();
				}
			}
		});
	}

	/**
	 * Returns the HiddenFieldId which will be used as the name and id property
	 * of the hiddenfield that is generated for event dispatches.
	 * 
	 * @return The name and id of the hidden field.
	 */
	protected final String getHiddenFieldId()
	{
		return getJavascriptId() + ":hf:0";
	}

	/**
	 * Returns the javascript/css id of this form that will be used to generated
	 * the id="xxx" attribute. it will be generated if not set already in the
	 * onComponentTag. Where it will be tried to load from the markup first
	 * before it is generated.
	 * 
	 * @return The javascript/css id of this form.
	 */
	protected final String getJavascriptId()
	{
		if (Strings.isEmpty(javascriptId))
		{
			javascriptId = getMarkupId();
		}
		return javascriptId;
	}

	/**
	 * Append an additional hidden input tag to support anchor tags that can
	 * submit a form.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		// get the hidden field id
		String nameAndId = getHiddenFieldId();


		// render the hidden field
		AppendingStringBuffer buffer = new AppendingStringBuffer(
				"<div style=\"display:none\"><input type=\"hidden\" name=\"").append(nameAndId)
				.append("\" id=\"").append(nameAndId).append("\" /></div>");
		getResponse().write(buffer);

		// if a default button was set, handle the rendering of that
		if (defaultButton != null && defaultButton.isVisibleInHierarchy()
				&& defaultButton.isEnabled())
		{
			appendDefaultButtonField(markupStream, openTag);
		}

		// do the rest of the processing
		super.onComponentTagBody(markupStream, openTag);
	}

	/**
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "form");
		super.onComponentTag(tag);

		// If the javascriptid is already generated then use that on even it was
		// before the first render. Bbecause there could be a component which
		// already uses it to submit the forum. This should be fixed when we
		// pre parse the markup so that we know the id is at front.
		if (!Strings.isEmpty(javascriptId))
		{
			tag.put("id", javascriptId);
		}
		else
		{
			javascriptId = (String)tag.getAttributes().get("id");
			if (Strings.isEmpty(javascriptId))
			{
				javascriptId = getJavascriptId();
				tag.put("id", javascriptId);
			}
		}
		tag.put("method", "post");
		tag.put("action", Strings.replaceAll(urlFor(IFormSubmitListener.INTERFACE), "&", "&amp;"));
		if (multiPart)
		{
			tag.put("enctype", "multipart/form-data");
		}
		else
		{
			// sanity check
			String enctype = (String)tag.getAttributes().get("enctype");
			if ("multipart/form-data".equalsIgnoreCase(enctype))
			{
				// though not set explicitly in Java, this is a multipart form
				setMultiPart(true);
			}
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
	 * @see wicket.Component#onRender(MarkupStream)
	 */
	protected void onRender(final MarkupStream markupStream)
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

		super.onRender(markupStream);
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
	public boolean process()
	{
		// run validation
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
			// mark all childeren as valid
			markFormComponentsValid();

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
				if (formComponent.isVisibleInHierarchy() && formComponent.isEnabled()
						&& formComponent.isValid() && formComponent.isEnableAllowed())
				{
					// Potentially update the model
					formComponent.updateModel();
				}
			}
		});
	}


	/**
	 * Clears the input from the form's nested children of type
	 * {@link FormComponent}. This method is typically called when a form needs
	 * to be reset.
	 */
	public final void clearInput()
	{
		// Visit all the (visible) form components and clear the input on each.
		visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				if (formComponent.isVisibleInHierarchy())
				{
					// Clear input from form component
					formComponent.clearInput();
				}
			}
		});
	}

	/**
	 * Validates the form. This method is typically called before updating any
	 * models.
	 */
	protected void validate()
	{
		validateRequired();

		validateConversion();

		validateValidators();

		validateFormValidators();
	}

	/**
	 * Triggers input required attribute validation on all form components
	 */
	private void validateRequired()
	{
		visitFormComponents(new ValidationVisitor()
		{
			public void validate(final FormComponent formComponent)
			{
				formComponent.validateRequired();
			}
		});
	}

	/**
	 * Triggers type conversion on form components
	 */
	private void validateConversion()
	{
		visitFormComponents(new ValidationVisitor()
		{
			public void validate(final FormComponent formComponent)
			{
				formComponent.convert();
			}
		});
	}

	/**
	 * Triggers all IValidator validators added to the form components
	 */
	private void validateValidators()
	{
		visitFormComponents(new ValidationVisitor()
		{
			public void validate(final FormComponent formComponent)
			{
				formComponent.validateValidators();
			}
		});
	}

	/**
	 * Triggers any added {@link IFormValidator}s.
	 */
	private void validateFormValidators()
	{
		final int count = formValidators_size();
		for (int i = 0; i < count; i++)
		{
			validateFormValidator(formValidators_get(i));
		}
	}

	/**
	 * Validates form with the given form validator
	 * 
	 * @param validator
	 */
	protected final void validateFormValidator(final IFormValidator validator)
	{
		if (validator == null)
		{
			throw new IllegalArgumentException("Argument [[validator]] cannot be null");
		}

		final FormComponent[] dependents = validator.getDependentFormComponents();

		boolean validate = true;

		if (dependents != null)
		{
			for (int j = 0; j < dependents.length; j++)
			{
				final FormComponent dependent = dependents[j];
				if (!dependent.isValid())
				{
					validate = false;
					break;
				}
			}
		}

		if (validate)
		{
			validator.validate(this);
		}
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
				final WebRequest multipartWebRequest = ((WebRequest)getRequest())
						.newMultipartWebRequest(this.maxSize);
				getRequestCycle().setRequest(multipartWebRequest);
			}
			catch (WicketRuntimeException wre)
			{
				if (wre.getCause() == null || !(wre.getCause() instanceof FileUploadException))
				{
					throw wre;
				}

				FileUploadException e = (FileUploadException)wre.getCause();
				// Create model with exception and maximum size values
				final Map model = new HashMap();
				model.put("exception", e);
				model.put("maxSize", maxSize);

				if (e instanceof SizeLimitExceededException)
				{
					// Resource key should be <form-id>.uploadTooLarge to
					// override default message
					final String defaultValue = "Upload must be less than " + maxSize;
					String msg = getString(getId() + "." + UPLOAD_TOO_LARGE_RESOURCE_KEY, Model
							.valueOf(model), defaultValue);
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
					String msg = getString(getId() + "." + UPLOAD_FAILED_RESOURCE_KEY, Model
							.valueOf(model), defaultValue);
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

	/**
	 * Method for dispatching/calling a interface on a page from the given url.
	 * Used by {@link wicket.markup.html.form.Form#onFormSubmitted()} for
	 * dispatching events
	 * 
	 * @param page
	 *            The page where the event should be called on.
	 * @param url
	 *            The url which describes the component path and the interface
	 *            to be called.
	 */
	private void dispatchEvent(final Page page, final String url)
	{
		RequestCycle rc = RequestCycle.get();
		IRequestCycleProcessor processor = rc.getProcessor();
		final RequestParameters requestParameters = processor.getRequestCodingStrategy().decode(
				new FormDispatchRequest(rc.getRequest(), url));
		IRequestTarget rt = processor.resolve(rc, requestParameters);
		if (rt instanceof ListenerInterfaceRequestTarget)
		{
			ListenerInterfaceRequestTarget interfaceTarget = ((ListenerInterfaceRequestTarget)rt);
			interfaceTarget.getRequestListenerInterface().invoke(page, interfaceTarget.getTarget());
		}
		else
		{
			throw new WicketRuntimeException(
					"Attempt to access unknown request listener interface "
							+ requestParameters.getInterfaceName());
		}
	}

	/**
	 * Visits the form's children FormComponents and inform them that a new user
	 * input is available in the Request
	 */
	private void inputChanged()
	{
		visitFormComponents(new FormComponent.IVisitor()
		{
			public void formComponent(final FormComponent formComponent)
			{
				if (formComponent.isVisibleInHierarchy())
				{
					formComponent.inputChanged();
				}
			}
		});
	}

	/**
	 * This generates a piece of javascript code that sets the url in the
	 * special hidden field and submits the form.
	 * 
	 * Warning: This code should only be called in the rendering phase for form
	 * components inside the form because it uses the css/javascript id of the
	 * form which can be stored in the markup.
	 * 
	 * @param url
	 *            The interface url that has to be stored in the hidden field
	 *            and submitted
	 * @return The javascript code that submits the form.
	 */
	public final CharSequence getJsForInterfaceUrl(CharSequence url)
	{
		return new AppendingStringBuffer("document.getElementById('").append(getHiddenFieldId())
				.append("').value='").append(url).append("';document.getElementById('").append(
						getJavascriptId()).append("').submit();");
	}

	/**
	 * 
	 */
	class FormDispatchRequest extends Request
	{
		private final Request realRequest;

		private final String url;

		private final Map params = new HashMap(4);

		/**
		 * Construct.
		 * 
		 * @param realRequest
		 * @param url
		 */
		public FormDispatchRequest(final Request realRequest, final String url)
		{
			this.realRequest = realRequest;
			this.url = realRequest.decodeURL(url);

			String queryPart = this.url.substring(this.url.indexOf("?") + 1);
			StringTokenizer paramsSt = new StringTokenizer(queryPart, "&");
			while (paramsSt.hasMoreTokens())
			{
				String param = paramsSt.nextToken();
				int equalsSign = param.indexOf("=");
				if (equalsSign >= 0)
				{
					String paramName = param.substring(0, equalsSign);
					String value = param.substring(equalsSign + 1);
					params.put(paramName, value);
				}
				else
				{
					params.put(param, "");
				}
			}
		}

		/**
		 * @see wicket.Request#getLocale()
		 */
		public Locale getLocale()
		{
			return realRequest.getLocale();
		}

		/**
		 * @see wicket.Request#getParameter(java.lang.String)
		 */
		public String getParameter(String key)
		{
			return (String)params.get(key);
		}

		/**
		 * @see wicket.Request#getParameterMap()
		 */
		public Map getParameterMap()
		{
			return params;
		}

		/**
		 * @see wicket.Request#getParameters(java.lang.String)
		 */
		public String[] getParameters(String key)
		{
			String param = (String)params.get(key);
			if (param != null)
			{
				return new String[] { param };
			}
			return new String[0];
		}

		/**
		 * @see wicket.Request#getPath()
		 */
		public String getPath()
		{
			return realRequest.getPath();
		}

		/**
		 * @see wicket.Request#getRelativeURL()
		 */
		public String getRelativeURL()
		{
			int tmp = url.indexOf("/", 1);
			if (tmp != -1)
			{
				return url.substring(tmp);
			}
			return url;
		}

		/**
		 * @see wicket.Request#getURL()
		 */
		public String getURL()
		{
			return url;
		}
	}

	/**
	 * Returns the prefix used when building validator keys. This allows a form
	 * to use a separate "set" of keys. For example if prefix "short" is
	 * returned, validator key short.RequiredValidator will be tried instead of
	 * RequiredValidator key.
	 * <p>
	 * This can be useful when different designs are used for a form. In a form
	 * where error messages are displayed next to their respective form
	 * components as opposed to at the top of the form, the ${label} attribute
	 * is of little use and only causes redundant information to appear in the
	 * message. Forms like these can return the "short" (or any other string)
	 * validator prefix and declare key: short.RequiredValidator=required to
	 * override the longer message which is usually declared like this:
	 * RequiredValidator=${label} is a required field
	 * <p>
	 * Returned prefix will be used for all form components. The prefix can also
	 * be overridden on form component level by overriding
	 * {@link FormComponent#getValidatorKeyPrefix()}
	 * 
	 * @return prefix prepended to validator keys
	 */
	public String getValidatorKeyPrefix()
	{
		return null;
	}

	/**
	 * Adds a form validator to the form.
	 * 
	 * @see IFormValidator
	 * @param validator
	 *            validator
	 */
	public void add(IFormValidator validator)
	{
		if (validator == null)
		{
			throw new IllegalArgumentException("validator argument cannot be null");
		}
		formValidators_add(validator);
	}

	/**
	 * @param validator
	 *            The form validator to add to the formValidators Object (which
	 *            may be an array of IFormValidators or a single instance, for
	 *            efficiency)
	 */
	private void formValidators_add(final IFormValidator validator)
	{
		if (this.formValidators == null)
		{
			this.formValidators = validator;
		}
		else
		{
			// Get current list size
			final int size = formValidators_size();

			// Create array that holds size + 1 elements
			final IFormValidator[] validators = new IFormValidator[size + 1];

			// Loop through existing validators copying them
			for (int i = 0; i < size; i++)
			{
				validators[i] = formValidators_get(i);
			}

			// Add new validator to the end
			validators[size] = validator;

			// Save new validator list
			this.formValidators = validators;
		}
	}

	/**
	 * Gets form validator from formValidators Object (which may be an array of
	 * IFormValidators or a single instance, for efficiency) at the given index
	 * 
	 * @param index
	 *            The index of the validator to get
	 * @return The form validator
	 */
	private IFormValidator formValidators_get(int index)
	{
		if (this.formValidators == null)
		{
			throw new IndexOutOfBoundsException();
		}
		if (this.formValidators instanceof IFormValidator[])
		{
			return ((IFormValidator[])formValidators)[index];
		}
		return (IFormValidator)formValidators;
	}

	/**
	 * @return The number of form validators in the formValidators Object (which
	 *         may be an array of IFormValidators or a single instance, for
	 *         efficiency)
	 */
	private int formValidators_size()
	{
		if (this.formValidators == null)
		{
			return 0;
		}
		if (this.formValidators instanceof IFormValidator[])
		{
			return ((IFormValidator[])formValidators).length;
		}
		return 1;
	}

	/**
	 * /** Registers an error feedback message for this component
	 * 
	 * @param error
	 *            error message
	 * @param args
	 *            argument replacement map for ${key} variables
	 */
	public final void error(String error, Map args)
	{
		error(new MapVariableInterpolator(error, args).toString());
	}


}
