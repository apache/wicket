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

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A behavior that updates the hosting FormComponent via ajax when an event it is attached to is
 * triggered. This behavior encapsulates the entire form-processing workflow as relevant only to
 * this component so if validation is successful the component's model will be updated according to
 * the submitted value.
 * <p>
 * NOTE: This behavior does not validate any {@link IFormValidator}s attached to this form even
 * though they may reference the component being updated.
 * <p>
 * NOTE: This behavior does not work on Choices or Groups use the
 * {@link AjaxFormChoiceComponentUpdatingBehavior} for that.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @see #onUpdate(org.apache.wicket.ajax.AjaxRequestTarget)
 * @see #onError(org.apache.wicket.ajax.AjaxRequestTarget, RuntimeException)
 */
public abstract class AjaxFormComponentUpdatingBehavior extends AjaxEventBehavior
{
	private static final Logger log = LoggerFactory
		.getLogger(AjaxFormComponentUpdatingBehavior.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param event
	 *            event to trigger this behavior
	 */
	public AjaxFormComponentUpdatingBehavior(final String event)
	{
		super(event);
	}

	/**
	 * 
	 * @see org.apache.wicket.behavior.AbstractAjaxBehavior#onBind()
	 */
	@Override
	protected void onBind()
	{
		super.onBind();

		Component component = getComponent();
		if (!(component instanceof FormComponent))
		{
			throw new WicketRuntimeException("Behavior " + getClass().getName()
				+ " can only be added to an instance of a FormComponent");
		}

		checkComponent((FormComponent<?>)component);
	}

	/**
	 * Check the component this behavior is bound to.
	 * <p>
	 * Logs a warning in development mode when an {@link AjaxFormChoiceComponentUpdatingBehavior}
	 * should be used.
	 * 
	 * @param component
	 *            bound component
	 */
	protected void checkComponent(FormComponent<?> component)
	{
		if (Application.get().usesDevelopmentConfig()
			&& AjaxFormChoiceComponentUpdatingBehavior.appliesTo(component))
		{
			log.warn(String
				.format(
					"AjaxFormComponentUpdatingBehavior is not supposed to be added in the form component at path: \"%s\". "
						+ "Use the AjaxFormChoiceComponentUpdatingBehavior instead, that is meant for choices/groups that are not one component in the html but many",
					component.getPageRelativePath()));
		}
	}

	/**
	 * 
	 * @return FormComponent
	 */
	protected final FormComponent<?> getFormComponent()
	{
		return (FormComponent<?>)getComponent();
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);

		attributes.setMethod(Method.POST);
	}

	/**
	 * 
	 * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
	 */
	@Override
	protected final void onEvent(final AjaxRequestTarget target)
	{
		final FormComponent<?> formComponent = getFormComponent();

		if ("blur".equals(getEvent().toLowerCase()) && disableFocusOnBlur())
		{
			target.focusComponent(null);
		}

		try
		{
			formComponent.inputChanged();
			formComponent.validate();
			if (formComponent.isValid())
			{
				formComponent.valid();
				if (getUpdateModel())
				{
					formComponent.updateModel();
				}

				onUpdate(target);
			}
			else
			{
				formComponent.invalid();

				onError(target, null);
			}
		}
		catch (RuntimeException e)
		{
			onError(target, e);
		}
		formComponent.updateAutoLabels(target);
	}

	/**
	 * @return true if the model of form component should be updated, false otherwise
	 */
	protected boolean getUpdateModel()
	{
		return true;
	}

	/**
	 * Determines whether the focus will not be restored when the event is blur. By default this is
	 * true, as we don't want to re-focus component on blur event.
	 * 
	 * @return <code>true</code> if refocusing should be disabled, <code>false</code> otherwise
	 */
	protected boolean disableFocusOnBlur()
	{
		return true;
	}

	/**
	 * Listener invoked on the ajax request. This listener is invoked after the component's model
	 * has been updated.
	 * 
	 * @param target
	 *            the current request handler
	 */
	protected abstract void onUpdate(AjaxRequestTarget target);

	/**
	 * Called to handle any error resulting from updating form component. Errors thrown from
	 * {@link #onUpdate(org.apache.wicket.ajax.AjaxRequestTarget)} will not be caught here.
	 * 
	 * The RuntimeException will be null if it was just a validation or conversion error of the
	 * FormComponent
	 * 
	 * @param target
	 *            the current request handler
	 * @param e
	 *            the error that occurred during the update of the component
	 */
	protected void onError(AjaxRequestTarget target, RuntimeException e)
	{
		if (e != null)
		{
			throw e;
		}
	}
}
