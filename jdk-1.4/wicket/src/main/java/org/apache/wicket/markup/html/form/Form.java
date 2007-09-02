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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.form.persistence.CookieValuePersister;
import org.apache.wicket.markup.html.form.persistence.IValuePersister;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.component.listener.ListenerInterfaceRequestTarget;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.apache.wicket.util.upload.FileUploadException;
import org.apache.wicket.util.upload.FileUploadBase.SizeLimitExceededException;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.IValidatorAddListener;
import org.apache.wicket.version.undo.Change;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base class for forms. To implement a form, subclass this class, add FormComponents (such as
 * CheckBoxes, ListChoices or TextFields) to the form. You can nest multiple buttons if you want to
 * vary submit behavior. However, it is not necessary to use Wicket's button class, just putting
 * e.g. &lt;input type="submit" value="go"&gt; suffices.
 * <p>
 * By default, the processing of a form works like this:
 * <li> The submitting button is looked up. A submitting button is a button that is nested in this
 * form (is a child component) and that was clicked by the user. If a submitting button was found,
 * and it has the defaultFormProcessing field set to false (default is true), it's onSubmit method
 * will be called right away, thus no validition is done, and things like updating form component
 * models that would normally be done are skipped. In that respect, nesting a button with the
 * defaultFormProcessing field set to false has the same effect as nesting a normal link. If you
 * want you can call validate() to execute form validation, hasError() to find out whether
 * validate() resulted in validation errors, and updateFormComponentModels() to update the models of
 * nested form components. </li>
 * <li> When no submitting button with defaultFormProcessing set to false was found, this form is
 * processed (method process()). Now, two possible paths exist:
 * <ul>
 * <li> Form validation failed. All nested form components will be marked invalid, and onError() is
 * called to allow clients to provide custom error handling code. </li>
 * <li> Form validation succeeded. The nested components will be asked to update their models and
 * persist their data is applicable. After that, method delegateSubmit with optionally the
 * submitting button is called. The default when there is a submitting button is to first call
 * onSubmit on that button, and after that call onSubmit on this form. Clients may override
 * delegateSubmit if they want different behavior. </li>
 * </ul>
 * </li>
 * </li>
 * </p>
 * 
 * Form for handling (file) uploads with multipart requests is supported by callign
 * setMultiPart(true) ( although wicket will try to automatically detect this for you ). Use this
 * with {@link org.apache.wicket.markup.html.form.upload.FileUploadField} components. You can attach
 * mutliple FileUploadField components for muliple file uploads.
 * <p>
 * In case of an upload error two resource keys are available to specify error messages:
 * uploadTooLarge and uploadFailed
 * 
 * ie in [page].properties
 * 
 * [form-id].uploadTooLarge=You have uploaded a file that is over the allowed limit of 2Mb
 * 
 * <p>
 * If you want to have multiple buttons which submit the same form, simply put two or more button
 * components somewhere in the hierarchy of components that are children of the form.
 * </p>
 * <p>
 * To get form components to persist their values for users via cookies, simply call
 * setPersistent(true) on each component.
 * </p>
 * <p>
 * Forms can be nested. You can put a form in another form. Since HTML doesn't allow nested
 * &lt;form&gt; tags, the inner forms will be rendered using the &lt;div&gt; tag. You have to submit
 * the inner forms using explicit components (like Button or SubmitLink), you can't rely on implicit
 * submit behavior (by using just &lt;input type="submit"&gt; that is not attached to a component).
 * </p>
 * <p>
 * When a nested form is submitted, the user entered values in outer (parent) forms are preserved
 * and only the fields in the submitted form are validated. </b>
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
	public static abstract class ValidationVisitor implements FormComponent.IVisitor
	{
		/**
		 * @see org.apache.wicket.markup.html.form.FormComponent.IVisitor#formComponent(org.apache.wicket.markup.html.form.IFormVisitorParticipant)
		 */
		public Object formComponent(IFormVisitorParticipant component)
		{
			if (component instanceof FormComponent)
			{
				FormComponent formComponent = (FormComponent)component;
				if (formComponent.isVisibleInHierarchy() && formComponent.isValid() &&
						formComponent.isEnabled() && formComponent.isEnableAllowed())
				{
					validate(formComponent);
				}
			}
			if (component.processChildren())
			{
				return Component.IVisitor.CONTINUE_TRAVERSAL;
			}
			else
			{
				return Component.IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
			}
		}

		/**
		 * Callback that should be used to validate form component
		 * 
		 * @param formComponent
		 */
		public abstract void validate(FormComponent formComponent);
	}

	/**
	 * 
	 */
	class FormDispatchRequest extends Request
	{
		private final ValueMap params = new ValueMap();

		private final Request realRequest;

		private final String url;

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

			String queryString = this.url.substring(this.url.indexOf("?") + 1);
			RequestUtils.decodeParameters(queryString, params);
		}

		/**
		 * @see org.apache.wicket.Request#getLocale()
		 */
		public Locale getLocale()
		{
			return realRequest.getLocale();
		}

		/**
		 * @see org.apache.wicket.Request#getParameter(java.lang.String)
		 */
		public String getParameter(String key)
		{
			return (String)params.get(key);
		}

		/**
		 * @see org.apache.wicket.Request#getParameterMap()
		 */
		public Map getParameterMap()
		{
			return params;
		}

		/**
		 * @see org.apache.wicket.Request#getParameters(java.lang.String)
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
		 * @see org.apache.wicket.Request#getPath()
		 */
		public String getPath()
		{
			return realRequest.getPath();
		}

		public String getRelativePathPrefixToContextRoot()
		{
			return realRequest.getRelativePathPrefixToContextRoot();
		}

		public String getRelativePathPrefixToWicketHandler()
		{
			return realRequest.getRelativePathPrefixToWicketHandler();
		}

		/**
		 * @see org.apache.wicket.Request#getURL()
		 */
		public String getURL()
		{
			return url;
		}
	}

	/**
	 * Constant for specifying how a form is submitted, in this case using get.
	 */
	public static final String METHOD_GET = "get";

	/**
	 * Constant for specifying how a form is submitted, in this case using post.
	 */
	public static final String METHOD_POST = "post";

	/** Flag that indicates this form has been submitted during this request */
	private static final short FLAG_SUBMITTED = FLAG_RESERVED1;

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(Form.class);

	private static final long serialVersionUID = 1L;

	private static final String UPLOAD_FAILED_RESOURCE_KEY = "uploadFailed";

	private static final String UPLOAD_TOO_LARGE_RESOURCE_KEY = "uploadTooLarge";

	/**
	 * Any default button. If set, a hidden submit button will be rendered right after the form tag,
	 * so that when users press enter in a textfield, this button's action will be selected. If no
	 * default button is set, nothing additional is rendered.
	 * <p>
	 * WARNING: note that this is a best effort only. Unfortunately having a 'default' button in a
	 * form is ill defined in the standards, and of course IE has it's own way of doing things.
	 * </p>
	 */
	private Button defaultButton;

	/** multi-validators assigned to this form */
	private Object formValidators = null;

	private String javascriptId;

	/**
	 * Maximum size of an upload in bytes. If null, the setting
	 * {@link IApplicationSettings#getDefaultMaximumUploadSize()} is used.
	 */
	private Bytes maxSize = null;

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
		setOutputMarkupId(true);
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Form(final String id, IModel model)
	{
		super(id, model);
		setOutputMarkupId(true);
	}

	/**
	 * Adds a form validator to the form.
	 * 
	 * @param validator
	 *            validator
	 * @throws IllegalArgumentException
	 *             if validator is null
	 * @see IFormValidator
	 * @see IValidatorAddListener
	 */
	public void add(IFormValidator validator)
	{
		if (validator == null)
		{
			throw new IllegalArgumentException("Argument `validator` cannot be null");
		}

		// add the validator
		formValidators_add(validator);

		// see whether the validator listens for add events
		if (validator instanceof IValidatorAddListener)
		{
			((IValidatorAddListener)validator).onAdded(this);
		}
	}

	/**
	 * Removes a form validator from the form.
	 * 
	 * @param validator
	 *            validator
	 * @throws IllegalArgumentException
	 *             if validator is null
	 * @see IFormValidator
	 */
	public void remove(IFormValidator validator)
	{
		if (validator == null)
		{
			throw new IllegalArgumentException("Argument `validator` cannot be null");
		}

		IFormValidator removed = formValidators_remove(validator);
		if (removed == null)
		{
			throw new IllegalStateException(
					"Tried to remove form validator that was not previously added. "
							+ "Make sure your validator's equals() implementation is sufficient");
		}
		addStateChange(new FormValidatorRemovedChange(removed));
	}

	private final int formValidators_indexOf(IFormValidator validator)
	{
		if (formValidators != null)
		{
			if (formValidators instanceof IFormValidator)
			{
				final IFormValidator v = (IFormValidator)formValidators;
				if (v == validator || v.equals(validator))
				{
					return 0;
				}
			}
			else
			{
				final IFormValidator[] validators = (IFormValidator[])formValidators;
				for (int i = 0; i < validators.length; i++)
				{
					final IFormValidator v = validators[i];
					if (v == validator || v.equals(validator))
					{
						return i;
					}
				}
			}
		}
		return -1;
	}

	private final IFormValidator formValidators_remove(IFormValidator validator)
	{
		int index = formValidators_indexOf(validator);
		if (index != -1)
		{
			return formValidators_remove(index);
		}
		return null;
	}

	private final IFormValidator formValidators_remove(int index)
	{
		if (formValidators instanceof IFormValidator)
		{
			if (index == 0)
			{
				final IFormValidator removed = (IFormValidator)formValidators;
				formValidators = null;
				return removed;
			}
			else
			{
				throw new IndexOutOfBoundsException();
			}
		}
		else
		{
			final IFormValidator[] validators = (IFormValidator[])formValidators;
			final IFormValidator removed = validators[index];
			// check if we can collapse array of 1 element into a single object
			if (validators.length == 2)
			{
				formValidators = validators[1 - index];
			}
			else
			{
				IFormValidator[] newValidators = new IFormValidator[validators.length - 1];
				int j = 0;
				for (int i = 0; i < validators.length; i++)
				{
					if (i != index)
					{
						newValidators[j++] = validators[i];
					}
				}
				formValidators = newValidators;
			}
			return removed;
		}
	}


	/**
	 * Clears the input from the form's nested children of type {@link FormComponent}. This method
	 * is typically called when a form needs to be reset.
	 */
	public final void clearInput()
	{
		// Visit all the (visible) form components and clear the input on each.
		visitFormComponentsPostOrder(new FormComponent.AbstractVisitor()
		{
			public void onFormComponent(final FormComponent formComponent)
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

	/**
	 * Gets the button which submitted this form.
	 * 
	 * @return The button which submitted this form or null if the processing was not trigger by a
	 *         registered button component
	 */
	public final IFormSubmittingComponent findSubmittingButton()
	{
		IFormSubmittingComponent submittingButton = (IFormSubmittingComponent)getPage()
				.visitChildren(IFormSubmittingComponent.class, new IVisitor()
				{
					public Object component(final Component component)
					{
						// Get button
						final IFormSubmittingComponent submit = (IFormSubmittingComponent)component;

						// Check for button-name or button-name.x request string
						if (submit.getForm() != null &&
								submit.getForm().getRootForm() == Form.this &&
								(getRequest().getParameter(submit.getInputName()) != null || getRequest()
										.getParameter(submit.getInputName() + ".x") != null))
						{
							if (!component.isVisible())
							{
								throw new WicketRuntimeException("Submit Button " +
										submit.getInputName() + " (path=" +
										component.getPageRelativePath() + ") is not visible");
							}
							return submit;
						}
						return CONTINUE_TRAVERSAL;
					}
				});

		return submittingButton;
	}

	/**
	 * Gets the default button. If set (not null), a hidden submit button will be rendered right
	 * after the form tag, so that when users press enter in a textfield, this button's action will
	 * be selected. If no default button is set (it is null), nothing additional is rendered.
	 * <p>
	 * WARNING: note that this is a best effort only. Unfortunately having a 'default' button in a
	 * form is ill defined in the standards, and of course IE has it's own way of doing things.
	 * </p>
	 * There can be only one default button per form hierarchy. So if you want to get the default
	 * button on a nested form, it will actually delegate the call to root form. </b>
	 * 
	 * @return The button to set as the default button, or null when you want to 'unset' any
	 *         previously set default button
	 */
	public final Button getDefaultButton()
	{
		if (isRootForm())
		{
			return defaultButton;
		}
		else
		{
			return getRootForm().getDefaultButton();
		}
	}

	/**
	 * This generates a piece of javascript code that sets the url in the special hidden field and
	 * submits the form.
	 * 
	 * Warning: This code should only be called in the rendering phase for form components inside
	 * the form because it uses the css/javascript id of the form which can be stored in the markup.
	 * 
	 * @param url
	 *            The interface url that has to be stored in the hidden field and submitted
	 * @return The javascript code that submits the form.
	 */
	public final CharSequence getJsForInterfaceUrl(CharSequence url)
	{
		return new AppendingStringBuffer("document.getElementById('").append(getHiddenFieldId())
				.append("').value='").append(url).append("';document.getElementById('").append(
						getJavascriptId()).append("').submit();");
	}

	/**
	 * Gets the maximum size for uploads. If null, the setting
	 * {@link IApplicationSettings#getDefaultMaximumUploadSize()} is used.
	 * 
	 * @return the maximum size
	 */
	public Bytes getMaxSize()
	{
		if (maxSize == null)
		{
			return getApplication().getApplicationSettings().getDefaultMaximumUploadSize();
		}
		return maxSize;
	}

	/**
	 * Returns the root form or this, if this is the root form.
	 * 
	 * @return root form or this form
	 */
	public Form getRootForm()
	{
		Form form;
		Form parent = this;
		do
		{
			form = parent;
			parent = (Form)form.findParent(Form.class);
		}
		while (parent != null);

		return form;
	}

	/**
	 * Returns the prefix used when building validator keys. This allows a form to use a separate
	 * "set" of keys. For example if prefix "short" is returned, validator key short.Required will
	 * be tried instead of Required key.
	 * <p>
	 * This can be useful when different designs are used for a form. In a form where error messages
	 * are displayed next to their respective form components as opposed to at the top of the form,
	 * the ${label} attribute is of little use and only causes redundant information to appear in
	 * the message. Forms like these can return the "short" (or any other string) validator prefix
	 * and declare key: short.Required=required to override the longer message which is usually
	 * declared like this: Required=${label} is a required field
	 * <p>
	 * Returned prefix will be used for all form components. The prefix can also be overridden on
	 * form component level by overriding {@link FormComponent#getValidatorKeyPrefix()}
	 * 
	 * @return prefix prepended to validator keys
	 */
	public String getValidatorKeyPrefix()
	{
		return null;
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
	 * Returns whether the form is a root form, which means that there's no other form in it's
	 * parent hierarchy.
	 * 
	 * @return true if form is a root form, false otherwise
	 */
	public boolean isRootForm()
	{
		return findParent(Form.class) == null;
	}

	/**
	 * Checks if this form has been submitted during the current request
	 * 
	 * @return true if the form has been submitted during this request, false otherwise
	 */
	public final boolean isSubmitted()
	{
		return getFlag(FLAG_SUBMITTED);
	}

	/**
	 * Method made final because we want to ensure users call setVersioned.
	 * 
	 * @see org.apache.wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return super.isVersioned();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Retrieves FormComponent values related to the page using the persister and assign the values
	 * to the FormComponent. Thus initializing them.
	 */
	public final void loadPersistentFormComponentValues()
	{
		visitFormComponentsPostOrder(new FormComponent.AbstractVisitor()
		{
			public void onFormComponent(final FormComponent formComponent)
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
	 * THIS METHOD IS NOT PART OF THE WICKET API. DO NOT ATTEMPT TO OVERRIDE OR CALL IT.
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
				final IFormSubmittingComponent submittingButton = findSubmittingButton();

				// When processing was triggered by a Wicket button and that
				// button indicates it wants to be called immediately
				// (without processing), call Button.onSubmit() right away.
				if (submittingButton != null && !submittingButton.getDefaultFormProcessing())
				{
					submittingButton.onSubmit();
				}
				else
				{
					// this is the root form
					Form formToProcess = this;

					// find out whether it was a nested form that was submitted
					if (submittingButton != null)
					{
						formToProcess = submittingButton.getForm();
					}
					// process the form for this request
					if (formToProcess.process())
					{
						// let clients handle further processing
						delegateSubmit(submittingButton);
					}
				}
			}
		}
		// If multi part did fail check if an error is registered and call
		// onError
		else if (hasError())
		{
			onError();
		}
	}

	/**
	 * Process the form. Though you can override this method to provide your whole own algorithm, it
	 * is not recommended to do so.
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
	 * Removes already persisted data for all FormComponent childs and disable persistence for the
	 * same components.
	 * 
	 * @see Page#removePersistedFormData(Class, boolean)
	 * 
	 * @param disablePersistence
	 *            if true, disable persistence for all FormComponents on that page. If false, it
	 *            will remain unchanged.
	 */
	public void removePersistentFormComponentValues(final boolean disablePersistence)
	{
		// The persistence manager responsible to persist and retrieve
		// FormComponent data
		final IValuePersister persister = getValuePersister();

		// Search for FormComponents like TextField etc.
		visitFormComponentsPostOrder(new FormComponent.AbstractVisitor()
		{
			public void onFormComponent(final FormComponent formComponent)
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
	 * Sets the default button. If set (not null), a hidden submit button will be rendered right
	 * after the form tag, so that when users press enter in a textfield, this button's action will
	 * be selected. If no default button is set (so unset by calling this method with null), nothing
	 * additional is rendered.
	 * <p>
	 * WARNING: note that this is a best effort only. Unfortunately having a 'default' button in a
	 * form is ill defined in the standards, and of course IE has it's own way of doing things.
	 * </p>
	 * There can be only one default button per form hierarchy. So if you set default button on a
	 * nested form, it will actually delegate the call to root form. </b>
	 * 
	 * @param button
	 *            The button to set as the default button, or null when you want to 'unset' any
	 *            previously set default button
	 */
	public final void setDefaultButton(Button button)
	{
		if (isRootForm())
		{
			defaultButton = button;
		}
		else
		{
			getRootForm().setDefaultButton(button);
		}
	}

	/**
	 * Sets the maximum size for uploads. If null, the setting
	 * {@link IApplicationSettings#getDefaultMaximumUploadSize()} is used.
	 * 
	 * @param maxSize
	 *            The maximum size
	 */
	public void setMaxSize(final Bytes maxSize)
	{
		this.maxSize = maxSize;
	}

	/**
	 * Set to true to use enctype='multipart/form-data', and to process file uplloads by default
	 * multiPart = false
	 * 
	 * @param multiPart
	 *            whether this form should behave as a multipart form
	 */
	public void setMultiPart(boolean multiPart)
	{
		this.multiPart = multiPart;
	}

	/**
	 * @see org.apache.wicket.Component#setVersioned(boolean)
	 */
	public final Component setVersioned(final boolean isVersioned)
	{
		super.setVersioned(isVersioned);

		// Search for FormComponents like TextField etc.
		visitFormComponents(new FormComponent.AbstractVisitor()
		{
			public void onFormComponent(final FormComponent formComponent)
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

		/**
		 * TODO Post 1.2 General: Maybe we should re-think how Borders are implemented, because
		 * there are just too many exceptions in the code base because of borders. This time it is
		 * to solve the problem tested in BoxBorderTestPage_3 where the Form is defined in the box
		 * border and the FormComponents are in the "body". Thus, the formComponents are not childs
		 * of the form. They are rather childs of the border, as the Form itself.
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
	 * Convenient and typesafe way to visit all the form components on a form postorder (deepest
	 * first)
	 * 
	 * @param visitor
	 *            The visitor interface to call
	 */
	public final void visitFormComponentsPostOrder(final FormComponent.IVisitor visitor)
	{
		FormComponent.visitFormComponentsPostOrder(this, visitor);

		/**
		 * TODO Post 1.2 General: Maybe we should re-think how Borders are implemented, because
		 * there are just too many exceptions in the code base because of borders. This time it is
		 * to solve the problem tested in BoxBorderTestPage_3 where the Form is defined in the box
		 * border and the FormComponents are in the "body". Thus, the formComponents are not childs
		 * of the form. They are rather childs of the border, as the Form itself.
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
	 * Method for dispatching/calling a interface on a page from the given url. Used by
	 * {@link org.apache.wicket.markup.html.form.Form#onFormSubmitted()} for dispatching events
	 * 
	 * @param page
	 *            The page where the event should be called on.
	 * @param url
	 *            The url which describes the component path and the interface to be called.
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
					"Attempt to access unknown request listener interface " +
							requestParameters.getInterfaceName());
		}
	}

	/**
	 * @param validator
	 *            The form validator to add to the formValidators Object (which may be an array of
	 *            IFormValidators or a single instance, for efficiency)
	 */
	private void formValidators_add(final IFormValidator validator)
	{
		if (formValidators == null)
		{
			formValidators = validator;
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
			formValidators = validators;
		}
	}

	/**
	 * Gets form validator from formValidators Object (which may be an array of IFormValidators or a
	 * single instance, for efficiency) at the given index
	 * 
	 * @param index
	 *            The index of the validator to get
	 * @return The form validator
	 */
	private IFormValidator formValidators_get(int index)
	{
		if (formValidators == null)
		{
			throw new IndexOutOfBoundsException();
		}
		if (formValidators instanceof IFormValidator[])
		{
			return ((IFormValidator[])formValidators)[index];
		}
		return (IFormValidator)formValidators;
	}

	/**
	 * @return The number of form validators in the formValidators Object (which may be an array of
	 *         IFormValidators or a single instance, for efficiency)
	 */
	private int formValidators_size()
	{
		if (formValidators == null)
		{
			return 0;
		}
		if (formValidators instanceof IFormValidator[])
		{
			return ((IFormValidator[])formValidators).length;
		}
		return 1;
	}

	/**
	 * Visits the form's children FormComponents and inform them that a new user input is available
	 * in the Request
	 */
	private void inputChanged()
	{
		visitFormComponentsPostOrder(new FormComponent.AbstractVisitor()
		{
			public void onFormComponent(final FormComponent formComponent)
			{
				if (formComponent.isVisibleInHierarchy())
				{
					formComponent.inputChanged();
				}
			}
		});
	}

	/**
	 * Persist (e.g. Cookie) FormComponent data to be reloaded and re-assigned to the FormComponent
	 * automatically when the page is visited by the user next time.
	 * 
	 * @see org.apache.wicket.markup.html.form.FormComponent#updateModel()
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
			visitFormComponentsPostOrder(new FormComponent.AbstractVisitor()
			{
				public void onFormComponent(final FormComponent formComponent)
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
	 * If a default button was set on this form, this method will be called to render an extra field
	 * with an invisible style so that pressing enter in one of the textfields will do a form submit
	 * using this button. This method is overridable as what we do is best effort only, and may not
	 * what you want in specific situations. So if you have specific usability concerns, or want to
	 * follow another strategy, you may override this method.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 */
	protected void appendDefaultButtonField(final MarkupStream markupStream,
			final ComponentTag openTag)
	{

		AppendingStringBuffer buffer = new AppendingStringBuffer();

		// div that is not visible (but not display:none either)
		buffer
				.append("<div style=\"width:0px;height:0px;position:absolute;left:-100px;top:-100px;overflow:hidden\">");

		// add an empty textfield (otherwise IE doesn't work)
		buffer.append("<input type=\"text\" autocomplete=\"false\"/>");

		// add the button
		buffer.append("<input type=\"submit\" onclick=\" var b=Wicket.$('");
		buffer.append(defaultButton.getMarkupId());
		buffer
				.append("'); if (typeof(b.onclick) != 'undefined') {  var r = b.onclick.bind(b)(); if (r != false) b.click(); } else { b.click(); };  return false;\" ");
		buffer.append(" />");

		// close div
		buffer.append("</div>");

		getResponse().write(buffer);
	}

	/**
	 * Template method to allow clients to do any processing (like recording the current model so
	 * that, in case onSubmit does further validation, the model can be rolled back) before the
	 * actual updating of form component models is done.
	 */
	protected void beforeUpdateFormComponentModels()
	{
	}

	/**
	 * Called (by the default implementation of 'process') when all fields validated, the form was
	 * updated and it's data was allowed to be persisted. It is meant for delegating further
	 * processing to clients.
	 * <p>
	 * This implementation first finds out whether the form processing was triggered by a nested
	 * button of this form. If that is the case, that button's onSubmit is called first.
	 * </p>
	 * <p>
	 * Regardless of whether a submitting button was found, the form's onSubmit method is called
	 * next.
	 * </p>
	 * 
	 * @param submittingButton
	 *            the button that triggered this form processing, or null if the processing was
	 *            triggered by something else (like a non-Wicket submit button or a javascript
	 *            execution)
	 */
	protected void delegateSubmit(IFormSubmittingComponent submittingButton)
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
	 * Returns the HiddenFieldId which will be used as the name and id property of the hiddenfield
	 * that is generated for event dispatches.
	 * 
	 * @return The name and id of the hidden field.
	 */
	protected final String getHiddenFieldId()
	{
		return getJavascriptId() + "_hf_0";
	}

	/**
	 * Returns the javascript/css id of this form that will be used to generated the id="xxx"
	 * attribute.
	 * 
	 * @return The javascript/css id of this form.
	 * @deprecated use {@link #getMarkupId()}
	 */
	protected final String getJavascriptId()
	{
		return getMarkupId();
	}


	/**
	 * Gets the method used to submit the form. Defaults to either what is explicitly defined in the
	 * markup or 'post'. Override this if you have a requirement to alter this behavior.
	 * 
	 * @return the method used to submit the form.
	 */
	protected String getMethod()
	{
		String method = getMarkupAttributes().getString("method");
		return (method != null) ? method : METHOD_POST;
	}

	protected boolean getStatelessHint()
	{
		return false;
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
	 * Handles multi-part processing of the submitted data.
	 * 
	 * WARNING
	 * 
	 * If this method is overridden it can break {@link FileUploadField}s on this form
	 * 
	 * @return false if form is multipart and upload failed
	 */
	protected boolean handleMultiPart()
	{
		if (multiPart)
		{
			// Change the request to a multipart web request so parameters are
			// parsed out correctly
			try
			{
				final WebRequest multipartWebRequest = ((WebRequest)getRequest())
						.newMultipartWebRequest(getMaxSize());
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
				model.put("maxSize", getMaxSize());

				if (e instanceof SizeLimitExceededException)
				{
					// Resource key should be <form-id>.uploadTooLarge to
					// override default message
					final String defaultValue = "Upload must be less than " + getMaxSize();
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
	 * @see org.apache.wicket.Component#internalOnModelChanged()
	 */
	protected void internalOnModelChanged()
	{
		// Visit all the form components and validate each
		visitFormComponentsPostOrder(new FormComponent.AbstractVisitor()
		{
			public void onFormComponent(final FormComponent formComponent)
			{
				// If form component is using form model
				if (formComponent.sameInnermostModel(Form.this))
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
		visitFormComponentsPostOrder(new FormComponent.AbstractVisitor()
		{
			public void onFormComponent(final FormComponent formComponent)
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
		visitFormComponentsPostOrder(new FormComponent.AbstractVisitor()
		{
			public void onFormComponent(final FormComponent formComponent)
			{
				if (formComponent.isVisibleInHierarchy())
				{
					formComponent.valid();
				}
			}
		});
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);

		checkComponentTag(tag, "form");

		if (isRootForm())
		{
			String method = getMethod().toLowerCase();
			tag.put("method", method);
			String url = urlFor(IFormSubmitListener.INTERFACE).toString();
			if (method.equals("get"))
			{
				int i = url.indexOf('?');
				String action = (i > -1) ? url.substring(0, i) : "";
				tag.put("action", action);
				// alternatively, we could just put an empty string here, so
				// that mounted paths stay in good order. I decided against this
				// as I'm not sure whether that could have side effects with
				// other encoders
			}
			else
			{
				tag.put("action", Strings.replaceAll(url, "&", "&amp;"));
			}

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
					// though not set explicitly in Java, this is a multipart
					// form
					setMultiPart(true);
				}
			}
		}
		else
		{
			tag.setName("div");
			tag.remove("method");
			tag.remove("action");
			tag.remove("enctype");
		}
	}

	/**
	 * Append an additional hidden input tag to support anchor tags that can submit a form.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		if (isRootForm())
		{
			// get the hidden field id
			String nameAndId = getHiddenFieldId();

			// render the hidden field
			AppendingStringBuffer buffer = new AppendingStringBuffer(
					"<div style=\"display:none\"><input type=\"hidden\" name=\"").append(nameAndId)
					.append("\" id=\"").append(nameAndId).append("\" />");
			String method = getMethod().toLowerCase();
			// if it's a get, did put the parameters in the action attribute,
			// and have to write the url parameters as hidden fields
			if (method.equals("get"))
			{
				String url = urlFor(IFormSubmitListener.INTERFACE).toString();
				int i = url.indexOf('?');
				String[] params = ((i > -1) ? url.substring(i + 1) : url).split("&");
				for (int j = 0; j < params.length; j++)
				{
					String[] pair = params[j].split("=");
					buffer.append("<input type=\"hidden\" name=\"").append(pair[0]).append(
							"\" value=\"").append(pair.length > 1 ? pair[1] : "").append("\" />");
				}
			}
			buffer.append("</div>");
			getResponse().write(buffer);

			// if a default button was set, handle the rendering of that
			if (defaultButton != null && defaultButton.isVisibleInHierarchy() &&
					defaultButton.isEnabled())
			{
				appendDefaultButtonField(markupStream, openTag);
			}
		}

		// do the rest of the processing
		super.onComponentTagBody(markupStream, openTag);
	}

	/**
	 * @see org.apache.wicket.Component#onDetach()
	 */
	protected void onDetach()
	{
		super.internalOnDetach();
		setFlag(FLAG_SUBMITTED, false);

		super.onDetach();
	}

	/**
	 * Method to override if you want to do something special when an error occurs (other than
	 * simply displaying validation errors).
	 */
	protected void onError()
	{
	}

	/**
	 * @see org.apache.wicket.Component#onRender(MarkupStream)
	 */
	protected void onRender(final MarkupStream markupStream)
	{
		// Force multi-part on if any child form component is multi-part
		visitFormComponents(new FormComponent.AbstractVisitor()
		{
			public void onFormComponent(FormComponent formComponent)
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
	 * Update the model of all form components using the fields that were sent with the current
	 * request.
	 * 
	 * @see org.apache.wicket.markup.html.form.FormComponent#updateModel()
	 */
	protected final void updateFormComponentModels()
	{
		visitFormComponentsPostOrder(new ValidationVisitor()
		{
			public void validate(FormComponent formComponent)
			{
				// Potentially update the model
				formComponent.updateModel();
			}
		});
	}

	/**
	 * Validates the form by checking required fields, converting raw input and running validators
	 * for every form component, and last running global form validators. This method is typically
	 * called before updating any models.
	 * <p>
	 * NOTE: in most cases, custom validations on the form can be achieved using an IFormValidator
	 * that can be added using addValidator().
	 * </p>
	 */
	protected void validate()
	{
		validateComponents();
		validateFormValidators();
	}

	/**
	 * Triggers type conversion on form components
	 */
	protected final void validateComponents()
	{
		visitFormComponentsPostOrder(new ValidationVisitor()
		{
			public void validate(final FormComponent formComponent)
			{
				formComponent.validate();
			}
		});
	}

	/**
	 * Checks if the specified form component visible and is attached to a page
	 * 
	 * @param fc
	 *            form component
	 * 
	 * @return true if the form component and all its parents are visible and there component is in
	 *         page's hierarchy
	 */
	private boolean isFormComponentVisibleInPage(FormComponent fc)
	{
		if (fc == null)
		{
			throw new IllegalArgumentException("Argument `fc` cannot be null");
		}
		Component c = fc;
		Component last = fc;
		while (c != null)
		{
			if (c.isRenderAllowed() && c.isVisible())
			{
				last = c;
				c = c.getParent();
			}
			else
			{
				return false;
			}
		}
		return last == findPage();
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
				// check if the dependent component is valid
				if (!dependent.isValid())
				{
					validate = false;
					break;
				}
				// check if the dependent componet is visible and is attached to
				// the page
				else if (!isFormComponentVisibleInPage(dependent))
				{
					if (log.isWarnEnabled())
					{
						log
								.warn("IFormValidator in form `" +
										getPageRelativePath() +
										"` depends on a component that has been removed from the page or is no longer visible. " +
										"Offending component id `" + dependent.getId() + "`.");
					}
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
	 * Triggers any added {@link IFormValidator}s.
	 */
	protected final void validateFormValidators()
	{
		final int count = formValidators_size();
		for (int i = 0; i < count; i++)
		{
			validateFormValidator(formValidators_get(i));
		}

		// traverse nested forms and invoke the form validators on them
		visitChildren(Form.class, new IVisitor()
		{
			public Object component(Component component)
			{
				final Form form = (Form)component;
				final int count = form.formValidators_size();
				for (int i = 0; i < count; i++)
				{
					form.validateFormValidator(form.formValidators_get(i));
				}

				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Change object to keep track of form validator removals
	 * 
	 * @author Igor Vaynberg (ivaynberg at apache dot org)
	 */
	private class FormValidatorRemovedChange extends Change
	{
		private static final long serialVersionUID = 1L;

		private final IFormValidator removed;

		/**
		 * Construct.
		 * 
		 * @param removed
		 */
		public FormValidatorRemovedChange(final IFormValidator removed)
		{
			super();
			this.removed = removed;
		}


		public void undo()
		{
			add(removed);
		}

	}
}
