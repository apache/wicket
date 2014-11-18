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
package org.apache.wicket.ajax.form;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;

/**
 * Ajax event behavior that submits a form via ajax when the event it is attached to, is invoked.
 * <p>
 * The form must have an id attribute in the markup or have MarkupIdSetter added.
 * 
 * @see AjaxEventBehavior
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @see #onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
 * @see #onAfterSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
 * @see #onError(org.apache.wicket.ajax.AjaxRequestTarget)
 */
public abstract class AjaxFormSubmitBehavior extends AjaxEventBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * should never be accessed directly (thus the __ cause its overkill to create a super class),
	 * instead always use #getForm()
	 */
	private Form<?> __form;

	private boolean defaultProcessing = true;

	/**
	 * Constructor. This constructor can only be used when the component this behavior is attached
	 * to is inside a form.
	 * 
	 * @param event
	 *            javascript event this behavior is attached to, like onclick
	 */
	public AjaxFormSubmitBehavior(String event)
	{
		this(null, event);
	}

	/**
	 * Construct.
	 * 
	 * @param form
	 *            form that will be submitted
	 * @param event
	 *            javascript event this behavior is attached to, like onclick
	 */
	public AjaxFormSubmitBehavior(Form<?> form, String event)
	{
		super(event);
		__form = form;

		if (form != null)
		{
			form.setOutputMarkupId(true);
		}
	}

	/**
	 * @return Form that will be submitted by this behavior
	 */
	public final Form<?> getForm()
	{
		if (__form == null)
		{
			__form = findForm();

			if (__form == null)
			{
				throw new IllegalStateException(
					"form was not specified in the constructor and cannot " +
						"be found in the hierarchy of the component this behavior " +
						"is attached to: Component=" + getComponent().toString(false));
			}
		}
		return __form;
	}

	/**
	 * @return the bound component if it implements {@link org.apache.wicket.markup.html.form.IFormSubmittingComponent},
	 * otherwise - {@code null}
	 */
	private IFormSubmittingComponent getFormSubmittingComponent()
	{
		IFormSubmittingComponent submittingComponent = null;
		Component component = getComponent();
		if (component instanceof IFormSubmittingComponent)
		{
			submittingComponent = ((IFormSubmittingComponent) component);
		}
		return submittingComponent;
	}

	/**
	 * Finds form that will be submitted
	 * 
	 * @return form to submit or {@code null} if none found
	 */
	protected Form<?> findForm()
	{
		// try to find form in the hierarchy of owning component
		Component component = getComponent();
		if (component instanceof Form<?>)
		{
			return (Form<?>)component;
		}
		else
		{
			return component.findParent(Form.class);
		}
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);

		Form<?> form = getForm();
		attributes.setFormId(form.getMarkupId());

		String formMethod = form.getMarkupAttributes().getString("method");
		if (formMethod == null || "POST".equalsIgnoreCase(formMethod))
		{
			attributes.setMethod(Method.POST);
		}

		if (form.getRootForm().isMultiPart())
		{
			attributes.setMultipart(true);
			attributes.setMethod(Method.POST);
		}

		IFormSubmittingComponent submittingComponent = getFormSubmittingComponent();
		if (submittingComponent != null)
		{
			String submittingComponentName = submittingComponent.getInputName();
			attributes.setSubmittingComponentName(submittingComponentName);
		}
	}

	@Override
	protected void onEvent(final AjaxRequestTarget target)
	{
		getForm().getRootForm().onFormSubmitted(new AjaxFormSubmitter(this, target));
	}

	/**
	 * A publicly reachable class that allows to introspect the submitter, e.g. to
	 * check what is the input name of the submitting component if there is such.
	 */
	public static class AjaxFormSubmitter implements IFormSubmitter
	{
		private final AjaxFormSubmitBehavior submitBehavior;
		private final AjaxRequestTarget target;

		private AjaxFormSubmitter(AjaxFormSubmitBehavior submitBehavior, AjaxRequestTarget target)
		{
			this.submitBehavior = submitBehavior;
			this.target = target;
		}

		@Override
		public Form<?> getForm()
		{
			return submitBehavior.getForm();
		}

		public IFormSubmittingComponent getFormSubmittingComponent()
		{
			return submitBehavior.getFormSubmittingComponent();
		}

		@Override
		public boolean getDefaultFormProcessing()
		{
			return submitBehavior.getDefaultProcessing();
		}

		@Override
		public void onError()
		{
			submitBehavior.onError(target);
		}

		@Override
		public void onSubmit()
		{
			submitBehavior.onSubmit(target);
		}

		@Override
		public void onAfterSubmit()
		{
			submitBehavior.onAfterSubmit(target);
		}
	}

	/**
	 * Override this method to provide special submit handling in a multi-button form. This method
	 * will be called <em>after</em> the form's onSubmit method.
	 */
	protected void onAfterSubmit(AjaxRequestTarget target)
	{
	}

	/**
	 * Override this method to provide special submit handling in a multi-button form. This method
	 * will be called <em>before</em> the form's onSubmit method.
	 */
	protected void onSubmit(AjaxRequestTarget target)
	{
	}


	/**
	 * Listener method invoked when the form has been processed and errors occurred
	 * 
	 * @param target
	 */
	protected void onError(AjaxRequestTarget target)
	{
	}

	/**
	 * @see Button#getDefaultFormProcessing()
	 * 
	 * @return {@code true} for default processing
	 */
	public boolean getDefaultProcessing()
	{
		return defaultProcessing;
	}

	/**
	 * @see Button#setDefaultFormProcessing(boolean)
	 * @param defaultProcessing
	 */
	public void setDefaultProcessing(boolean defaultProcessing)
	{
		this.defaultProcessing = defaultProcessing;
	}
}
