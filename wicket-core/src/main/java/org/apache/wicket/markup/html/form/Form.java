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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.IGenericComponent;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.validation.FormValidatorAdapter;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.settings.IApplicationSettings;
import org.apache.wicket.util.encoding.UrlDecoder;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.PrependingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.apache.wicket.util.upload.FileUploadBase.SizeLimitExceededException;
import org.apache.wicket.util.upload.FileUploadException;
import org.apache.wicket.util.value.LongValue;
import org.apache.wicket.util.visit.ClassVisitFilter;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.util.visit.Visits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Container for {@link FormComponent}s (such as {@link CheckBox}es, {@link ListChoice}s or
 * {@link TextField}s). Subclass this class to receive submit notifications through
 * {@link #onSubmit()} or nest multiple {@link IFormSubmittingComponent}s if you want to vary submit
 * behavior. In the former case it is not necessary to use any of Wicket's classes (such as
 * {@link Button} or {@link SubmitLink}), just putting e.g. &lt;input type="submit" value="go"/&gt;
 * suffices.
 * <p>
 * As a {@link IFormSubmitListener} the form gets notified of listener requests in
 * {@link #onFormSubmitted()}. By default, the processing of this submit works like this:
 * <ul>
 * <li>All nested {@link FormComponent}s are notified of new input via
 * {@link FormComponent#inputChanged()}</li>
 * <li>The form submitter is looked up, e.g. a {@link Button} is contained in the component
 * hierarchy of this form and was clicked by the user:
 * <ul>
 * <li>If an {@link IFormSubmitter} was found which
 * {@link IFormSubmitter#getDefaultFormProcessing()} returns {@code false} (default is {@code true}
 * ), it's {@link IFormSubmitter#onSubmit()} method will be called right away, thus all further
 * processing is skipped. This has the same effect as nesting a normal link in the form. <br>
 * If needed the form submitter can continue processing however, by calling {@link #validate()} to
 * execute form validation, {@link #hasError()} to find out whether validate() resulted in
 * validation errors, and {@link #updateFormComponentModels()} to update the models of nested form
 * components.</li>
 * <li>Otherwise this form is further processed via {@link #process(IFormSubmitter)}, resulting in
 * all nested components being validated via {@link FormComponent#validate()}. <br>
 * <ul>
 * <li>If form validation failed, all nested form components will be marked invalid, and
 * {@link #onError()} is called to allow clients to provide custom error handling code.</li>
 * <li>Otherwise the nested components will be asked to update their models via
 * {@link FormComponent#updateModel()}. After that submit notification is delegated to the
 * {@link IFormSubmitter#onSubmit()} (if just found) before calling {@link #onSubmit()} on this
 * form. Subclasses may override {@link #delegateSubmit(IFormSubmitter)} if they want a different
 * behavior.</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * 
 * A Form can be configured for handling uploads with multipart requests (e.g. files) by calling
 * {@link #setMultiPart(boolean)} (although Wicket will try to automatically detect this for you).
 * Use this with {@link FileUploadField} components. You can attach multiple {@link FileUploadField}
 * components for multiple file uploads.
 * <p>
 * In case of an upload error two resource keys are available to specify error messages:
 * {@code uploadTooLarge} and {@code uploadFailed}, i.e. for a form with id {@code myform} in
 * {@code MyPage.properties}:
 * 
 * <pre>
 * myform.uploadTooLarge=You have uploaded a file that is over the allowed limit of 2Mb
 * </pre>
 * 
 * Forms can be nested. You can put a form in another form. Since HTML doesn't allow nested
 * &lt;form&gt; tags, the inner forms will be rendered using the &lt;div&gt; tag. You have to submit
 * the inner forms using explicit components (like {@link Button} or {@link SubmitLink}), you can't
 * rely on implicit submit behavior (by using just &lt;input type="submit"&gt; that is not attached
 * to a component).
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
 * @author David Leangen
 * 
 * @param <T>
 *            The model object type
 */
public class Form<T> extends WebMarkupContainer
	implements
		IFormSubmitListener,
		IGenericComponent<T>
{
	private static final String HIDDEN_DIV_START = "<div style=\"width:0px;height:0px;position:absolute;left:-100px;top:-100px;overflow:hidden\">";

	public static final String ENCTYPE_MULTIPART_FORM_DATA = "multipart/form-data";

	/**
	 * Visitor used for validation
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 */
	public abstract static class ValidationVisitor implements IVisitor<FormComponent<?>, Void>
	{
		@Override
		public void component(final FormComponent<?> formComponent, final IVisit<Void> visit)
		{

			Form<?> form = formComponent.getForm();
			if (!form.isVisibleInHierarchy() || !form.isEnabledInHierarchy())
			{
				// do not validate formComponent or any of formComponent's children
				visit.dontGoDeeper();
				return;
			}

			if (formComponent.isVisibleInHierarchy() && formComponent.isEnabledInHierarchy())
			{
				validate(formComponent);
			}
			if (formComponent.processChildren() == false)
			{
				visit.dontGoDeeper();
			}
		}

		/**
		 * Callback that should be used to validate form component
		 * 
		 * @param formComponent
		 */
		public abstract void validate(FormComponent<?> formComponent);
	}


	/**
	 * Visitor used to update component models
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 */
	private static class FormModelUpdateVisitor implements IVisitor<Component, Void>
	{
		private final Form<?> formFilter;

		/**
		 * Constructor
		 * 
		 * @param formFilter
		 */
		public FormModelUpdateVisitor(Form<?> formFilter)
		{
			this.formFilter = formFilter;
		}

		/** {@inheritDoc} */
		@Override
		public void component(final Component component, final IVisit<Void> visit)
		{
			if (component instanceof IFormModelUpdateListener)
			{
				final Form<?> form = Form.findForm(component);
				if (form != null)
				{
					if (this.formFilter == null || this.formFilter == form)
					{
						if (form.isEnabledInHierarchy())
						{
							if (component.isVisibleInHierarchy() &&
								component.isEnabledInHierarchy())
							{
								((IFormModelUpdateListener)component).updateModel();
							}
						}
					}
				}
			}
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
	 * Any default IFormSubmittingComponent. If set, a hidden submit component will be rendered
	 * right after the form tag, so that when users press enter in a textfield, this submit
	 * component's action will be selected. If no default IFormSubmittingComponent is set, nothing
	 * additional is rendered.
	 * <p>
	 * WARNING: note that this is a best effort only. Unfortunately having a 'default'
	 * IFormSubmittingComponent in a form is ill defined in the standards, and of course IE has it's
	 * own way of doing things.
	 * </p>
	 */
	private IFormSubmittingComponent defaultSubmittingComponent;

	/**
	 * Maximum size of an upload in bytes. If null, the setting
	 * {@link IApplicationSettings#getDefaultMaximumUploadSize()} is used.
	 */
	private Bytes maxSize = null;

	/** True if the form has enctype of multipart/form-data */
	private short multiPart = 0;

	/**
	 * A user has explicitly called {@link #setMultiPart(boolean)} with value {@code true} forcing
	 * it to be true
	 */
	private static final short MULTIPART_HARD = 0x01;

	/**
	 * The form has discovered a multipart component before rendering and is marking itself as
	 * multipart until next render
	 */
	private static final short MULTIPART_HINT = 0x02;

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
	 * @param model
	 *            See Component
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Form(final String id, final IModel<T> model)
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
	 */
	public void add(final IFormValidator validator)
	{
		Args.notNull(validator, "validator");

		if (validator instanceof Behavior)
		{
			add((Behavior)validator);
		}
		else
		{
			add(new FormValidatorAdapter(validator));
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
	public void remove(final IFormValidator validator)
	{
		Args.notNull(validator, "validator");

		Behavior match = null;
		for (Behavior behavior : getBehaviors())
		{
			if (behavior.equals(validator))
			{
				match = behavior;
				break;
			}
			else if (behavior instanceof FormValidatorAdapter)
			{
				if (((FormValidatorAdapter)behavior).getValidator().equals(validator))
				{
					match = behavior;
					break;
				}
			}
		}

		if (match != null)
		{
			remove(match);
		}
		else
		{

			throw new IllegalStateException(
				"Tried to remove form validator that was not previously added. "
					+ "Make sure your validator's equals() implementation is sufficient");
		}
	}

	/**
	 * Clears the input from the form's nested children of type {@link FormComponent}. This method
	 * is typically called when a form needs to be reset.
	 */
	public final void clearInput()
	{
		// Visit all the (visible) form components and clear the input on each.
		visitFormComponentsPostOrder(new IVisitor<FormComponent<?>, Void>()
		{
			@Override
			public void component(final FormComponent<?> formComponent, IVisit<Void> visit)
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
	 * Registers an error feedback message for this component
	 * 
	 * @param error
	 *            error message
	 * @param args
	 *            argument replacement map for ${key} variables
	 */
	public final void error(String error, Map<String, Object> args)
	{
		error(new MapVariableInterpolator(error, args).toString());
	}

	/**
	 * Gets the IFormSubmittingComponent which submitted this form.
	 * 
	 * @return The component which submitted this form, or null if the processing was not triggered
	 *         by a registered IFormSubmittingComponent
	 */
	public final IFormSubmitter findSubmittingButton()
	{
		IFormSubmittingComponent submittingComponent = getPage().visitChildren(
			IFormSubmittingComponent.class, new IVisitor<Component, IFormSubmittingComponent>()
			{
				@Override
				public void component(final Component component,
					final IVisit<IFormSubmittingComponent> visit)
				{
					// Get submitting component
					final IFormSubmittingComponent submittingComponent = (IFormSubmittingComponent)component;
					final Form<?> form = submittingComponent.getForm();

					// Check for component-name or component-name.x request string
					if ((form != null) && (form.getRootForm() == Form.this))
					{
						String name = submittingComponent.getInputName();
						IRequestParameters parameters = getRequest().getRequestParameters();
						if ((!parameters.getParameterValue(name).isNull()) ||
							!parameters.getParameterValue(name + ".x").isNull())
						{
							if (!component.isVisibleInHierarchy())
							{
								throw new WicketRuntimeException("Submit Button " +
									submittingComponent.getInputName() + " (path=" +
									component.getPageRelativePath() + ") is not visible");
							}
							if (!component.isEnabledInHierarchy())
							{
								throw new WicketRuntimeException("Submit Button " +
									submittingComponent.getInputName() + " (path=" +
									component.getPageRelativePath() + ") is not enabled");
							}
							visit.stop(submittingComponent);
						}
					}
				}
			});

		return submittingComponent;
	}

	/**
	 * Gets the default IFormSubmittingComponent. If set (not null), a hidden submit component will
	 * be rendered right after the form tag, so that when users press enter in a textfield, this
	 * submit component's action will be selected. If no default component is set (it is null),
	 * nothing additional is rendered.
	 * <p>
	 * WARNING: note that this is a best effort only. Unfortunately having a 'default' button in a
	 * form is ill defined in the standards, and of course IE has it's own way of doing things.
	 * </p>
	 * There can be only one default submit component per form hierarchy. So if you want to get the
	 * default component on a nested form, it will actually delegate the call to root form. </b>
	 * 
	 * @return The submit component to set as the default IFormSubmittingComponent, or null when you
	 *         want to 'unset' any previously set default IFormSubmittingComponent
	 */
	public final IFormSubmittingComponent getDefaultButton()
	{
		if (isRootForm())
		{
			return defaultSubmittingComponent;
		}
		else
		{
			return getRootForm().getDefaultButton();
		}
	}

	/**
	 * Gets all {@link IFormValidator}s added to this form
	 * 
	 * @return unmodifiable collection of {@link IFormValidator}s
	 */
	public final Collection<IFormValidator> getFormValidators()
	{
		List<IFormValidator> validators = new ArrayList<IFormValidator>();

		for (Behavior behavior : getBehaviors())
		{
			if (behavior instanceof IFormValidator)
			{
				validators.add((IFormValidator)behavior);
			}
		}

		return Collections.unmodifiableCollection(validators);
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
		/*
		 * since the passed in url is handled when the current url is form's action url and not the
		 * current request's url we rerender the passed in url to be relative to the form's action
		 * url
		 */
		UrlRenderer renderer = getRequestCycle().getUrlRenderer();
		Url oldBase = renderer.getBaseUrl();
		try
		{
			Url action = Url.parse(getActionUrl().toString());
			renderer.setBaseUrl(action);
			url = renderer.renderUrl(Url.parse(url.toString()));
		}
		finally
		{
			renderer.setBaseUrl(oldBase);
		}

		Form<?> root = getRootForm();
		return new AppendingStringBuffer("document.getElementById('").append(
			root.getHiddenFieldId())
			.append("').value='")
			.append(url)
			.append("';document.getElementById('")
			.append(root.getMarkupId())
			.append("').submit();");
	}

	/**
	 * Gets the maximum size for uploads. If null, the setting
	 * {@link IApplicationSettings#getDefaultMaximumUploadSize()} is used.
	 * 
	 * 
	 * @return the maximum size
	 */
	public final Bytes getMaxSize()
	{
		/*
		 * NOTE: This method should remain final otherwise it will be impossible to set a default
		 * max size smaller then the one specified in applications settings because the inner form
		 * will return the default unless it is specifically set in the traversal. With this method
		 * remaining final we can tell when the value is explicitly set by the user.
		 * 
		 * If the value needs to be dynamic it can be set in oncofigure() instead of overriding this
		 * method.
		 */

		final Bytes[] maxSize = { this.maxSize };
		if (maxSize[0] == null)
		{
			visitChildren(Form.class, new IVisitor<Form<?>, Bytes>()
			{
				@Override
				public void component(Form<?> component, IVisit<Bytes> visit)
				{
					maxSize[0] = LongValue.maxNullSafe(maxSize[0], component.maxSize);
				}
			});
		}
		if (maxSize[0] == null)
		{
			return getApplication().getApplicationSettings().getDefaultMaximumUploadSize();
		}
		return maxSize[0];
	}

	/**
	 * Returns the root form or this, if this is the root form.
	 * 
	 * @return root form or this form
	 */
	public Form<?> getRootForm()
	{
		Form<?> form;
		Form<?> parent = this;
		do
		{
			form = parent;
			parent = form.findParent(Form.class);
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
	 * THIS METHOD IS NOT PART OF THE WICKET API. DO NOT ATTEMPT TO OVERRIDE OR CALL IT.
	 * 
	 * Handles form submissions.
	 * 
	 * @see #onFormSubmitted(IFormSubmitter)
	 */
	@Override
	public final void onFormSubmitted()
	{
		// check methods match
		if (getRequest().getContainerRequest() instanceof HttpServletRequest)
		{
			String desiredMethod = getMethod();
			String actualMethod = ((HttpServletRequest)getRequest().getContainerRequest()).getMethod();
			if (!actualMethod.equalsIgnoreCase(desiredMethod))
			{
				MethodMismatchResponse response = onMethodMismatch();
				switch (response)
				{
					case ABORT :
						return;
					case CONTINUE :
						break;
					default :
						throw new IllegalStateException("Invalid " +
							MethodMismatchResponse.class.getName() + " value: " + response);
				}
			}
		}
		onFormSubmitted(null);
	}

	/**
	 * Called when a form has been submitted using a method differing from return value of
	 * {@link #getMethod()}. For example, someone can copy and paste the action url and invoke the
	 * form using a {@code GET} instead of the desired {@code POST}. This method allows the user to
	 * react to this situation.
	 * 
	 * @return response that can either abort or continue the processing of the form
	 */
	protected MethodMismatchResponse onMethodMismatch()
	{
		return MethodMismatchResponse.CONTINUE;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET API. DO NOT ATTEMPT TO OVERRIDE OR CALL IT.
	 * 
	 * Handles form submissions.
	 * 
	 * @param submitter
	 *            listener that will receive form processing events, if {@code null} the form will
	 *            attempt to locate one
	 * 
	 * @see Form#validate()
	 */
	public final void onFormSubmitted(IFormSubmitter submitter)
	{
		markFormsSubmitted();

		if (handleMultiPart())
		{
			// Tells FormComponents that a new user input has come
			inputChanged();

			String url = getRequest().getRequestParameters()
				.getParameterValue(getHiddenFieldId())
				.toString();
			if (!Strings.isEmpty(url))
			{
				dispatchEvent(getPage(), url);
			}
			else
			{
				// First, see if the processing was triggered by a Wicket IFormSubmittingComponent
				if (submitter == null)
				{
					submitter = findSubmittingButton();
				}

				// When processing was triggered by a Wicket IFormSubmittingComponent and that
				// component indicates it wants to be called immediately
				// (without processing), call the IFormSubmittingComponent.onSubmit* methods right
				// away.
				if (submitter != null && !submitter.getDefaultFormProcessing())
				{
					submitter.onSubmit();
					submitter.onAfterSubmit();
				}
				else
				{
					// the submit request might be for one of the nested forms, so let's
					// find the right one:
					final Form<?> formToProcess = findFormToProcess(submitter);

					// process the form for this request
					formToProcess.process(submitter);
				}
			}
		}
		// If multi part did fail check if an error is registered and call
		// onError
		else if (hasError())
		{
			callOnError(submitter);
		}
	}

	/**
	 * This method finds the correct form that should be processed based on the submitting component
	 * (if there is one) and correctly handles nested forms by also looking at
	 * {@link #wantSubmitOnNestedFormSubmit()} throughout the form hierarchy. The form that needs to
	 * be processed is:
	 * <ul>
	 * <li>if there is no submitting component (i.e. a "default submit"): this form.</li>
	 * <li>if only one form exists (this): this form.</li>
	 * <li>if nested forms exist:
	 * <ul>
	 * <li>if the submitting component points at the root form: the root form</li>
	 * <li>if the submitting component points at a nested form:
	 * <ul>
	 * <li>starting at that nested form, the outermost form that returns true for
	 * {@link #wantSubmitOnNestedFormSubmit()}</li>
	 * <li>if no outer form returns true for that, the nested form is returned.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </li>
	 * </ul>
	 * 
	 * @param submitter
	 *            The submitting component, if any. May be null.
	 * @return The form that needs to be processed.
	 */
	private Form<?> findFormToProcess(IFormSubmitter submitter)
	{
		if (submitter == null)
		{
			// no submitting component => default form submit => so *this* is the
			// form to process
			return this;
		}
		else
		{
			// some button submitted this request, this is the form it belongs to:
			final Form<?> targetedForm = submitter.getForm();
			if (targetedForm == null)
			{
				throw new IllegalStateException(
					"submitting component must not return 'null' on getForm()");
			}

			final Form<?> rootForm = getRootForm();
			if (targetedForm == rootForm)
			{
				// the submitting component points at the root form => so let's just go with
				// root, everything else will be submitted with it anyway.
				return rootForm;
			}
			else
			{
				// a different form was targeted. let's find the outermost form that wants to be
				// submitted.
				Form<?> formThatWantsToBeSubmitted = targetedForm;
				Form<?> current = targetedForm.findParent(Form.class);
				while (current != null)
				{
					if (current.wantSubmitOnNestedFormSubmit())
					{
						formThatWantsToBeSubmitted = current;
					}
					current = current.findParent(Form.class);
				}
				return formThatWantsToBeSubmitted;
			}
		}
	}

	/**
	 * Whether this form wants to be submitted too if a nested form is submitted. By default, this
	 * is false, so when a nested form is submitted, this form will <em>not</em> be submitted. If
	 * this method is overridden to return true, this form <em>will</em> be submitted.
	 * 
	 * @return Whether this form wants to be submitted too if a nested form is submitted.
	 */
	public boolean wantSubmitOnNestedFormSubmit()
	{
		return false;
	}


	/**
	 * Process the form. Though you can override this method to provide your own algorithm, it is
	 * not recommended to do so.
	 * 
	 * <p>
	 * See the class documentation for further details on the form processing
	 * </p>
	 * 
	 * @param submittingComponent
	 *            component responsible for submitting the form, or <code>null</code> if none (eg
	 *            the form has been submitted via the enter key or javascript calling
	 *            form.onsubmit())
	 * 
	 * @see #delegateSubmit(IFormSubmitter) for an easy way to process submitting component in the
	 *      default manner
	 */
	public void process(IFormSubmitter submittingComponent)
	{
		if (!isEnabledInHierarchy() || !isVisibleInHierarchy())
		{
			// since process() can be called outside of the default form workflow, an additional
			// check is needed

			// FIXME throw listener exception
			return;
		}

		// run validation
		validate();

		// If a validation error occurred
		if (hasError())
		{
			// mark all children as invalid
			markFormComponentsInvalid();

			// let subclass handle error
			callOnError(submittingComponent);
		}
		else
		{
			// mark all children as valid
			markFormComponentsValid();

			// before updating, call the interception method for clients
			beforeUpdateFormComponentModels();

			// Update model using form data
			updateFormComponentModels();

			// validate model objects after input values have been bound
			onValidateModelObjects();
			if (hasError())
			{
				callOnError(submittingComponent);
				return;
			}

			// Form has no error
			delegateSubmit(submittingComponent);
		}
	}

	/**
	 * Calls onError on this {@link Form} and any enabled and visible nested form, if the respective
	 * {@link Form} actually has errors.
	 * 
	 * @param submitter
	 */
	protected void callOnError(IFormSubmitter submitter)
	{
		final Form<?> processingForm = findFormToProcess(submitter);

		if (submitter != null)
		{
			submitter.onError();
		}

		// invoke Form#onSubmit(..) going from innermost to outermost
		Visits.visitPostOrder(processingForm, new IVisitor<Form<?>, Void>()
		{
			@Override
			public void component(Form<?> form, IVisit<Void> visit)
			{
				if (!form.isEnabledInHierarchy() || !form.isVisibleInHierarchy())
				{
					visit.dontGoDeeper();
					return;
				}
				if (form.hasError())
				{
					form.onError();
				}
			}
		}, new ClassVisitFilter(Form.class));
	}


	/**
	 * Sets FLAG_SUBMITTED to true on this form and every enabled nested form.
	 */
	private void markFormsSubmitted()
	{
		setFlag(FLAG_SUBMITTED, true);

		visitChildren(Form.class, new IVisitor<Component, Void>()
		{
			@Override
			public void component(final Component component, final IVisit<Void> visit)
			{
				Form<?> form = (Form<?>)component;
				if (form.isEnabledInHierarchy() && isVisibleInHierarchy())
				{
					form.setFlag(FLAG_SUBMITTED, true);
					return;
				}
				visit.dontGoDeeper();
			}
		});
	}

	/**
	 * Sets the default IFormSubmittingComponent. If set (not null), a hidden submit component will
	 * be rendered right after the form tag, so that when users press enter in a textfield, this
	 * submit component's action will be selected. If no default component is set (so unset by
	 * calling this method with null), nothing additional is rendered.
	 * <p>
	 * WARNING: note that this is a best effort only. Unfortunately having a 'default' button in a
	 * form is ill defined in the standards, and of course IE has it's own way of doing things.
	 * </p>
	 * There can be only one default button per form hierarchy. So if you set default button on a
	 * nested form, it will actually delegate the call to root form. </b>
	 * 
	 * @param submittingComponent
	 *            The component to set as the default submitting component, or null when you want to
	 *            'unset' any previously set default component
	 */
	public final void setDefaultButton(IFormSubmittingComponent submittingComponent)
	{
		if (isRootForm())
		{
			defaultSubmittingComponent = submittingComponent;
		}
		else
		{
			getRootForm().setDefaultButton(submittingComponent);
		}
	}

	/**
	 * Sets the maximum size for uploads. If null, the setting
	 * {@link IApplicationSettings#getDefaultMaximumUploadSize()} is used.
	 * 
	 * @param maxSize
	 *            The maximum size
	 */
	public final void setMaxSize(final Bytes maxSize)
	{
		this.maxSize = maxSize;
	}

	/**
	 * Set to true to use enctype='multipart/form-data', and to process file uploads by default
	 * multiPart = false
	 * 
	 * @param multiPart
	 *            whether this form should behave as a multipart form
	 */
	public void setMultiPart(boolean multiPart)
	{
		if (multiPart)
		{
			this.multiPart |= MULTIPART_HARD;
		}
		else
		{
			this.multiPart &= ~MULTIPART_HARD;
		}
	}

	/**
	 * @see org.apache.wicket.Component#setVersioned(boolean)
	 */
	@Override
	public final Component setVersioned(final boolean isVersioned)
	{
		super.setVersioned(isVersioned);

		// Search for FormComponents like TextField etc.
		visitFormComponents(new IVisitor<FormComponent<?>, Void>()
		{
			@Override
			public void component(final FormComponent<?> formComponent, IVisit<Void> visit)
			{
				formComponent.setVersioned(isVersioned);
			}
		});
		return this;
	}

	/**
	 * Convenient and typesafe way to visit all the form components on a form.
	 * 
	 * @param <R>
	 *            return object type
	 * @param visitor
	 *            The visitor interface to call
	 * @return user provided in callback
	 */
	public final <R> R visitFormComponents(final IVisitor<FormComponent<?>, R> visitor)
	{
		return visitChildren(FormComponent.class, visitor);
	}

	/**
	 * Convenient and typesafe way to visit all the form components on a form postorder (deepest
	 * first)
	 * 
	 * @param <R>
	 *            Return object type
	 * @param visitor
	 *            The visitor interface to call
	 * @return whatever you provided
	 */
	public final <R> R visitFormComponentsPostOrder(
		final IVisitor<? extends FormComponent<?>, R> visitor)
	{
		return FormComponent.visitFormComponentsPostOrder(this, visitor);
	}

	/**
	 * Find out whether there is any registered error for a form component.
	 * 
	 * @return whether there is any registered error for a form component
	 */
	private boolean anyFormComponentError()
	{
		// Check ALL children for error messages irrespective of FormComponents or not
		Boolean error = visitChildren(Component.class, new IVisitor<Component, Boolean>()
		{
			@Override
			public void component(final Component component, final IVisit<Boolean> visit)
			{
				if (component.hasErrorMessage())
				{
					visit.stop(true);
				}
			}
		});

		return (error != null) && error;
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
		// the current request's url is most likely wicket/page?x-y.IFormSubmitListener-path-to-form
		// while the passed in url is most likely page?x.y.IOnChangeListener-path-to-component
		// we transform the passed in url into wicket/page?x-y.IOnChangeListener-path-to-component
		// so the system mapper can interpret it
		String urlWoJSessionId = Strings.stripJSessionId(url);
		Url resolved = new Url(getRequest().getUrl());
		resolved.resolveRelative(Url.parse(urlWoJSessionId));

		IRequestMapper mapper = getApplication().getRootRequestMapper();
		Request request = getRequest().cloneWithUrl(resolved);
		IRequestHandler handler = mapper.mapRequest(request);

		if (handler != null)
		{
			getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
		}
	}

	/**
	 * Visits the form's children FormComponents and inform them that a new user input is available
	 * in the Request
	 */
	private void inputChanged()
	{
		visitFormComponentsPostOrder(new IVisitor<FormComponent<?>, Void>()
		{
			@Override
			public void component(final FormComponent<?> formComponent, IVisit<Void> visit)
			{
				formComponent.inputChanged();
			}
		});
	}

	/**
	 * If a default IFormSubmittingComponent was set on this form, this method will be called to
	 * render an extra field with an invisible style so that pressing enter in one of the textfields
	 * will do a form submit using this component. This method is overridable as what we do is best
	 * effort only, and may not what you want in specific situations. So if you have specific
	 * usability concerns, or want to follow another strategy, you may override this method.
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
		buffer.append(HIDDEN_DIV_START);

		// add an empty textfield (otherwise IE doesn't work)
		buffer.append("<input type=\"text\" autocomplete=\"off\"/>");

		// add the submitting component
		final Component submittingComponent = (Component)defaultSubmittingComponent;
		buffer.append("<input type=\"submit\" name=\"");
		buffer.append(defaultSubmittingComponent.getInputName());
		buffer.append("\" onclick=\" var b=document.getElementById('");
		buffer.append(submittingComponent.getMarkupId());
		buffer.append("'); if (b!=null&amp;&amp;b.onclick!=null&amp;&amp;typeof(b.onclick) != 'undefined') {  var r = Wicket.bind(b.onclick, b)(); if (r != false) b.click(); } else { b.click(); };  return false;\" ");
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
	 * IFormSubmittingComponent of this form. If that is the case, that component's
	 * onSubmitBefore/AfterForm methods are called appropriately..
	 * </p>
	 * <p>
	 * Regardless of whether a submitting component was found, the form's onSubmit method is called
	 * next.
	 * </p>
	 * 
	 * @param submittingComponent
	 *            the component that triggered this form processing, or null if the processing was
	 *            triggered by something else (like a non-Wicket submit button or a javascript
	 *            execution)
	 */
	protected void delegateSubmit(IFormSubmitter submittingComponent)
	{
		final Form<?> processingForm = findFormToProcess(submittingComponent);

		// collect all forms innermost to outermost before any hierarchy is changed
		final List<Form<?>> forms = Generics.newArrayList(3);
		Visits.visitPostOrder(processingForm, new IVisitor<Form<?>, Void>()
		{
			@Override
			public void component(Form<?> form, IVisit<Void> visit)
			{
				if (form.isEnabledInHierarchy() && form.isVisibleInHierarchy())
				{
					forms.add(form);
				}
			}
		}, new ClassVisitFilter(Form.class));

		// process submitting component (if specified)
		if (submittingComponent != null)
		{
			// invoke submit on component
			submittingComponent.onSubmit();
		}

		// invoke Form#onSubmit(..)
		for (Form<?> form : forms)
		{
			form.onSubmit();
		}

		if (submittingComponent != null)
		{
			submittingComponent.onAfterSubmit();
		}
	}

	/**
	 * Returns the HiddenFieldId which will be used as the name and id property of the hiddenfield
	 * that is generated for event dispatches.
	 * 
	 * @return The name and id of the hidden field.
	 */
	public final String getHiddenFieldId()
	{
		return getInputNamePrefix() + getMarkupId() + "_hf_0";
	}

	/**
	 * Gets the HTTP submit method that will appear in form markup. If no method is specified in the
	 * template, "post" is the default. Note that the markup-declared HTTP method may not correspond
	 * to the one actually used to submit the form; in an Ajax submit, for example, JavaScript event
	 * handlers may submit the form with a "get" even when the form method is declared as "post."
	 * Therefore this method should not be considered a guarantee of the HTTP method used, but a
	 * value for the markup only. Override if you have a requirement to alter this behavior.
	 * 
	 * @return the submit method specified in markup.
	 */
	protected String getMethod()
	{
		String method = getMarkupAttributes().getString("method");
		return (method != null) ? method : METHOD_POST;
	}

	/**
	 * 
	 * @see org.apache.wicket.Component#getStatelessHint()
	 */
	@Override
	protected boolean getStatelessHint()
	{
		return false;
	}

	/**
	 * @return True if is multipart
	 */
	public boolean isMultiPart()
	{
		if (multiPart != 0)
		{
			return true;
		}

		Boolean anyEmbeddedMultipart = visitChildren(Component.class,
			new IVisitor<Component, Boolean>()
			{
				@Override
				public void component(final Component component, final IVisit<Boolean> visit)
				{
					boolean isMultiPart = false;
					if (component instanceof Form<?>)
					{
						Form<?> form = (Form<?>)component;
						if (form.isVisibleInHierarchy() && form.isEnabledInHierarchy())
						{
							isMultiPart = (form.multiPart != 0);
						}
					}
					else if (component instanceof FormComponent<?>)
					{
						FormComponent<?> fc = (FormComponent<?>)component;
						if (fc.isVisibleInHierarchy() && fc.isEnabledInHierarchy())
						{
							isMultiPart = fc.isMultiPart();
						}
					}

					if (isMultiPart)
					{
						visit.stop(true);
					}
				}

			});

		boolean mp = Boolean.TRUE.equals(anyEmbeddedMultipart);

		if (mp)
		{
			multiPart |= MULTIPART_HINT;
		}
		return mp;
	}

	/**
	 * Handles multi-part processing of the submitted data. <h3>
	 * WARNING</h3> If this method is overridden it can break {@link FileUploadField}s on this form
	 * 
	 * @return false if form is multipart and upload failed
	 */
	protected boolean handleMultiPart()
	{
		if (isMultiPart())
		{
			// Change the request to a multipart web request so parameters are
			// parsed out correctly
			try
			{
				ServletWebRequest request = (ServletWebRequest)getRequest();
				final WebRequest multipartWebRequest = request.newMultipartWebRequest(getMaxSize(),
					getPage().getId());
				// TODO: Can't this be detected from header?
				getRequestCycle().setRequest(multipartWebRequest);
			}
			catch (final FileUploadException fux)
			{
				// Create model with exception and maximum size values
				final Map<String, Object> model = new HashMap<String, Object>();
				model.put("exception", fux);
				model.put("maxSize", getMaxSize());

				onFileUploadException(fux, model);

				// don't process the form if there is a FileUploadException
				return false;
			}
		}
		return true;
	}

	/**
	 * The default message may look like ".. may not exceed 10240 Bytes..". Which is ok, but
	 * sometimes you may want something like "10KB". By subclassing this method you may replace
	 * maxSize in the model or add you own property and use that in your error message.
	 * <p>
	 * Don't forget to call super.onFileUploadException(e, model) at the end of your method.
	 * 
	 * @param e
	 * @param model
	 */
	protected void onFileUploadException(final FileUploadException e,
		final Map<String, Object> model)
	{
		if (e instanceof SizeLimitExceededException)
		{
			// Resource key should be <form-id>.uploadTooLarge to
			// override default message
			final String defaultValue = "Upload must be less than " + getMaxSize();
			String msg = getString(getId() + '.' + UPLOAD_TOO_LARGE_RESOURCE_KEY,
				Model.ofMap(model), defaultValue);
			error(msg);
		}
		else
		{
			// Resource key should be <form-id>.uploadFailed to override
			// default message
			final String defaultValue = "Upload failed: " + e.getLocalizedMessage();
			String msg = getString(getId() + '.' + UPLOAD_FAILED_RESOURCE_KEY, Model.ofMap(model),
				defaultValue);
			error(msg);

			log.warn(msg, e);
		}
	}

	/**
	 * @see org.apache.wicket.Component#internalOnModelChanged()
	 */
	@Override
	protected void internalOnModelChanged()
	{
		// Visit all the form components and validate each
		visitFormComponentsPostOrder(new IVisitor<FormComponent<?>, Void>()
		{
			@Override
			public void component(final FormComponent<?> formComponent, IVisit<Void> visit)
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
		visitFormComponentsPostOrder(new IVisitor<FormComponent<?>, Void>()
		{
			@Override
			public void component(final FormComponent<?> formComponent, IVisit<Void> visit)
			{
				if (formComponent.isVisibleInHierarchy())
				{
					formComponent.invalid();
				}
			}
		});
	}

	/**
	 * Mark each form component on this form and on nested forms valid.
	 */
	protected final void markFormComponentsValid()
	{
		internalMarkFormComponentsValid();
		markNestedFormComponentsValid();
	}

	/**
	 * Mark each form component on nested form valid.
	 */
	private void markNestedFormComponentsValid()
	{
		visitChildren(Form.class, new IVisitor<Form<?>, Void>()
		{
			@Override
			public void component(final Form<?> form, final IVisit<Void> visit)
			{
				if (form.isEnabledInHierarchy() && form.isVisibleInHierarchy())
				{
					form.internalMarkFormComponentsValid();
				}
				else
				{
					visit.dontGoDeeper();
				}
			}
		});
	}

	/**
	 * Mark each form component on this form valid.
	 */
	private void internalMarkFormComponentsValid()
	{
		// call valid methods of all nested form components
		visitFormComponentsPostOrder(new IVisitor<FormComponent<?>, Void>()
		{
			@Override
			public void component(final FormComponent<?> formComponent, IVisit<Void> visit)
			{
				if (formComponent.getForm() == Form.this && formComponent.isVisibleInHierarchy())
				{
					formComponent.valid();
				}
			}
		});
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);

		checkComponentTag(tag, "form");

		if (isRootForm())
		{
			String method = getMethod().toLowerCase(Locale.ENGLISH);
			tag.put("method", method);
			String url = getActionUrl().toString();
			if (encodeUrlInHiddenFields())
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
				tag.put("action", url);
			}

			if (isMultiPart())
			{
				if (METHOD_GET.equalsIgnoreCase(method))
				{
					log.warn("Form with id '{}' is multipart. It should use method 'POST'!",
						getId());
					tag.put("method", METHOD_POST.toLowerCase(Locale.ENGLISH));
				}

				tag.put("enctype", ENCTYPE_MULTIPART_FORM_DATA);
				//
				// require the application-encoding for multipart/form-data to be sure to
				// get multipart-uploaded characters with the proper encoding on the following
				// request.
				//
				// for details see: http://stackoverflow.com/questions/546365
				//
				tag.put("accept-charset", getApplication().getRequestCycleSettings()
					.getResponseRequestEncoding());
			}
			else
			{
				// sanity check
				String enctype = (String)tag.getAttributes().get("enctype");
				if (ENCTYPE_MULTIPART_FORM_DATA.equalsIgnoreCase(enctype))
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
	 * Generates the action url for the form
	 * 
	 * @return action url
	 */
	protected CharSequence getActionUrl()
	{
		return urlFor(IFormSubmitListener.INTERFACE, new PageParameters());
	}

	/**
	 * @see org.apache.wicket.Component#renderPlaceholderTag(org.apache.wicket.markup.ComponentTag,
	 *      org.apache.wicket.request.Response)
	 */
	@Override
	protected void renderPlaceholderTag(ComponentTag tag, Response response)
	{
		if (isRootForm())
		{
			super.renderPlaceholderTag(tag, response);
		}
		else
		{
			// rewrite inner form tag as div
			response.write("<div style=\"display:none\"");
			if (getOutputMarkupId())
			{
				response.write(" id=\"");
				response.write(getMarkupId());
				response.write("\"");
			}
			response.write("></div>");
		}
	}

	/**
	 * 
	 * @return true if form's method is 'get'
	 */
	protected boolean encodeUrlInHiddenFields()
	{
		return METHOD_GET.equalsIgnoreCase(getMethod());
	}

	/**
	 * Append an additional hidden input tag to support anchor tags that can submit a form.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 */
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		if (isRootForm())
		{
			// get the hidden field id
			String nameAndId = getHiddenFieldId();

			// render the hidden field
			AppendingStringBuffer buffer = new AppendingStringBuffer(HIDDEN_DIV_START).append(
				"<input type=\"hidden\" name=\"")
				.append(nameAndId)
				.append("\" id=\"")
				.append(nameAndId)
				.append("\" />");

			// if it's a get, did put the parameters in the action attribute,
			// and have to write the url parameters as hidden fields
			if (encodeUrlInHiddenFields())
			{
				String url = getActionUrl().toString();
				int i = url.indexOf('?');
				String queryString = (i > -1) ? url.substring(i + 1) : url;
				String[] params = Strings.split(queryString, '&');

				writeParamsAsHiddenFields(params, buffer);
			}
			buffer.append("</div>");
			getResponse().write(buffer);

			// if a default submitting component was set, handle the rendering of that
			if (defaultSubmittingComponent instanceof Component)
			{
				final Component submittingComponent = (Component)defaultSubmittingComponent;
				if (submittingComponent.isVisibleInHierarchy() &&
					submittingComponent.isEnabledInHierarchy())
				{
					appendDefaultButtonField(markupStream, openTag);
				}
			}
		}

		// do the rest of the processing
		super.onComponentTagBody(markupStream, openTag);
	}

	/**
	 * 
	 * @param params
	 * @param buffer
	 */
	protected void writeParamsAsHiddenFields(String[] params, AppendingStringBuffer buffer)
	{
		for (String param : params)
		{
			String[] pair = Strings.split(param, '=');

			buffer.append("<input type=\"hidden\" name=\"")
				.append(recode(pair[0]))
				.append("\" value=\"")
				.append(pair.length > 1 ? recode(pair[1]) : "")
				.append("\" />");
		}
	}

	/**
	 * Take URL-encoded query string value, decode it and return HTML-escaped version
	 * 
	 * @param s
	 *            value to decode
	 * @return URL decoded and HTML escaped value
	 */
	private String recode(String s)
	{
		String un = UrlDecoder.QUERY_INSTANCE.decode(s, getRequest().getCharset());
		return Strings.escapeMarkup(un).toString();
	}

	/**
	 * @see org.apache.wicket.Component#onDetach()
	 */
	@Override
	protected void onDetach()
	{
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

	@Override
	protected void onBeforeRender()
	{
		// clear multipart hint, it will be set if necessary by the visitor
		this.multiPart &= ~MULTIPART_HINT;

		super.onBeforeRender();
	}

	/**
	 * Implemented by subclasses to deal with form submits.
	 */
	protected void onSubmit()
	{
	}

	/**
	 * Update the model of all components on this form and nested forms using the fields that were
	 * sent with the current request. This method only updates models when the Form.validate() is
	 * called first that takes care of the conversion for the FormComponents.
	 * 
	 * Normally this method will not be called when a validation error occurs in one of the form
	 * components.
	 * 
	 * @see org.apache.wicket.markup.html.form.FormComponent#updateModel()
	 */
	protected final void updateFormComponentModels()
	{
		internalUpdateFormComponentModels();
		updateNestedFormComponentModels();
	}

	/**
	 * Update the model of all components on nested forms.
	 * 
	 * @see #updateFormComponentModels()
	 */
	private final void updateNestedFormComponentModels()
	{
		visitChildren(Form.class, new IVisitor<Form<?>, Void>()
		{
			@Override
			public void component(final Form<?> form, final IVisit<Void> visit)
			{
				if (form.isEnabledInHierarchy() && form.isVisibleInHierarchy())
				{
					form.internalUpdateFormComponentModels();

				}
				else
				{
					visit.dontGoDeeper();
				}
			}
		});
	}

	/**
	 * Update the model of all components on this form.
	 * 
	 * @see #updateFormComponentModels()
	 */
	private void internalUpdateFormComponentModels()
	{
		FormComponent.visitComponentsPostOrder(this, new FormModelUpdateVisitor(this));
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
	protected final void validate()
	{
		// since this method can be called directly by users, this additional check is needed
		if (isEnabledInHierarchy() && isVisibleInHierarchy())
		{
			validateNestedForms();
			validateComponents();
			validateFormValidators();
			onValidate();
		}
	}

	/**
	 * Callback during the validation stage of the form
	 */
	protected void onValidate()
	{

	}

	/**
	 * Called after form components have updated their models. This is a late-stage validation that
	 * allows outside frameworks to validate any beans that the form is updating.
	 * 
	 * This validation method is not preferred because at this point any errors will not unroll any
	 * changes to the model object, so the model object is in a modified state potentially
	 * containing illegal values. However, with external frameworks there may not be an alternate
	 * way to validate the model object. A good example of this is a JSR303 Bean Validator
	 * validating the model object to check any class-level constraints, in order to check such
	 * constaints the model object must contain the values set by the user.
	 */
	protected void onValidateModelObjects()
	{

	}

	/**
	 * Triggers type conversion on form components
	 */
	protected final void validateComponents()
	{
		visitFormComponentsPostOrder(new ValidationVisitor()
		{
			@Override
			public void validate(final FormComponent<?> formComponent)
			{
				final Form<?> form = formComponent.getForm();
				if (form == Form.this && form.isEnabledInHierarchy() && form.isVisibleInHierarchy())
				{
					formComponent.validate();
				}
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
	private boolean isFormComponentVisibleInPage(FormComponent<?> fc)
	{
		if (fc == null)
		{
			throw new IllegalArgumentException("Argument `fc` cannot be null");
		}
		return fc.isVisibleInHierarchy();
	}


	/**
	 * Validates form with the given form validator
	 * 
	 * @param validator
	 */
	protected final void validateFormValidator(final IFormValidator validator)
	{
		Args.notNull(validator, "validator");

		final FormComponent<?>[] dependents = validator.getDependentFormComponents();

		boolean validate = true;

		if (dependents != null)
		{
			for (final FormComponent<?> dependent : dependents)
			{
				// check if the dependent component is valid
				if (!dependent.isValid())
				{
					validate = false;
					break;
				}
				// check if the dependent component is visible and is attached to
				// the page
				else if (!isFormComponentVisibleInPage(dependent))
				{
					if (log.isWarnEnabled())
					{
						log.warn("IFormValidator in form `" +
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
		for (Behavior behavior : getBehaviors())
		{
			if (behavior instanceof IFormValidator)
			{
				validateFormValidator((IFormValidator)behavior);
			}
		}
	}

	/**
	 * Validates {@link FormComponent}s as well as {@link IFormValidator}s in nested {@link Form}s.
	 * 
	 * @see #validate()
	 */
	private void validateNestedForms()
	{
		Visits.visitPostOrder(this, new IVisitor<Form<?>, Void>()
		{
			@Override
			public void component(final Form<?> form, final IVisit<Void> visit)
			{
				if (form == Form.this)
				{
					// skip self, only process children
					visit.stop();
					return;
				}

				if (form.isEnabledInHierarchy() && form.isVisibleInHierarchy())
				{
					form.validateComponents();
					form.validateFormValidators();
					form.onValidate();
				}
			}
		}, new ClassVisitFilter(Form.class));
	}


	/**
	 * Allows to customize input names of form components inside this form.
	 * 
	 * @return String that well be used as prefix to form component input names
	 */
	protected String getInputNamePrefix()
	{
		return "";
	}

	@Override
	@SuppressWarnings("unchecked")
	public final IModel<T> getModel()
	{
		return (IModel<T>)getDefaultModel();
	}

	@Override
	public final void setModel(IModel<T> model)
	{
		setDefaultModel(model);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final T getModelObject()
	{
		return (T)getDefaultModelObject();
	}

	@Override
	public final void setModelObject(T object)
	{
		setDefaultModelObject(object);
	}

	/**
	 * @param component
	 * @return The parent form for component
	 */
	public static Form<?> findForm(Component component)
	{
		return component.findParent(Form.class);
	}

	/** {@inheritDoc} */
	@Override
	public void renderHead(IHeaderResponse response)
	{
		if (!isRootForm() && isMultiPart())
		{
			// register some metadata so we can later properly handle multipart ajax posts for
			// embedded forms
			registerJavaScriptNamespaces(response);
			response.render(JavaScriptHeaderItem.forScript("Wicket.Forms[\"" + getMarkupId() +
				"\"]={multipart:true};", Form.class.getName() + '.' + getMarkupId() + ".metadata"));
		}
	}

	/**
	 * Produces javascript that registereds Wicket.Forms namespaces
	 * 
	 * @param response
	 */
	protected void registerJavaScriptNamespaces(IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forScript(
			"if (typeof(Wicket)=='undefined') { Wicket={}; } if (typeof(Wicket.Forms)=='undefined') { Wicket.Forms={}; }",
			Form.class.getName()));
	}

	/**
	 * Utility method to assemble an id to distinct form components from different nesting levels.
	 * Useful to generate input names attributes.
	 * 
	 * @param component
	 * @return form relative identification string
	 */
	public static String getRootFormRelativeId(Component component)
	{
		String id = component.getId();
		final PrependingStringBuffer inputName = new PrependingStringBuffer(id.length());
		Component c = component;
		while (true)
		{
			inputName.prepend(id);
			c = c.getParent();
			if (c == null || (c instanceof Form<?> && ((Form<?>)c).isRootForm()) ||
				c instanceof Page)
			{
				break;
			}
			inputName.prepend(Component.PATH_SEPARATOR);
			id = c.getId();
		}

		/*
		 * having input name "submit" causes problems with JavaScript, so we create a unique string
		 * to replace it by prepending a path separator, as this identification can be assigned to
		 * an submit form component name
		 */
		if ("submit".equals(inputName.toString()))
		{
			inputName.prepend(Component.PATH_SEPARATOR);
		}
		return inputName.toString();
	}

	/**
	 * Response when a submission method mismatch is detected
	 * 
	 * @see Form#getMethod()
	 * 
	 * @author igor
	 */
	public static enum MethodMismatchResponse {
		/**
		 * Continue processing.
		 */
		CONTINUE,

		/**
		 * Abort processing.
		 */
		ABORT;
	}
}
