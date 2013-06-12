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
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A behavior that updates the hosting FormComponent via ajax when an event it is attached to is
 * triggered. This behavior encapsulates the entire form-processing workflow as relevant only to
 * this component so if validation is successful the component's model will be updated according to
 * the submitted value.
 * <p>
 * NOTE: This behavior does not support persisting form component values into cookie or other
 * {@link IValuePersister}. If this is necessary please add a request for enhancement.
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
 */
public abstract class AjaxFormComponentUpdatingBehavior extends AjaxEventBehavior
{
	private static final Logger log = LoggerFactory.getLogger(AjaxFormComponentUpdatingBehavior.class);

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

		if (!(getComponent() instanceof FormComponent))
		{
			throw new WicketRuntimeException("Behavior " + getClass().getName() +
				" can only be added to an instance of a FormComponent");
		}
		else if (Application.get().usesDevelopmentConfig() &&
			AjaxFormChoiceComponentUpdatingBehavior.appliesTo(getComponent()))
		{
			log.warn(String.format(
				"AjaxFormComponentUpdatingBehavior is not suposed to be added in the form component at path: \"%s\". "
					+ "Use the AjaxFormChoiceComponentUpdatingBehavior instead, that is meant for choices/groups that are not one component in the html but many",
				getComponent().getPageRelativePath()));
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

	/**
	 * @see org.apache.wicket.ajax.AjaxEventBehavior#getEventHandler()
	 */
	@Override
	protected final CharSequence getEventHandler()
	{
		return generateCallbackScript(new AppendingStringBuffer("wicketAjaxPost('").append(
			getCallbackUrl()).append(
			"', wicketSerialize(Wicket.$('" + getComponent().getMarkupId() + "'))"));
	}

	/**
	 * @see org.apache.wicket.ajax.AjaxEventBehavior#onCheckEvent(java.lang.String)
	 */
	@Override
	protected void onCheckEvent(String event)
	{
		if ("href".equalsIgnoreCase(event))
		{
			throw new IllegalArgumentException(
				"this behavior cannot be attached to an 'href' event");
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
	 */
	@Override
	protected final void onEvent(final AjaxRequestTarget target)
	{
		final FormComponent<?> formComponent = getFormComponent();

		if (getEvent().toLowerCase().equals("onblur") && disableFocusOnBlur())
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
	 */
	protected abstract void onUpdate(AjaxRequestTarget target);

	/**
	 * Called to handle any error resulting from updating form component. Errors thrown from
	 * {@link #onUpdate(AjaxRequestTarget)} will not be caught here.
	 * 
	 * The RuntimeException will be null if it was just a validation or conversion error of the
	 * FormComponent
	 * 
	 * @param target
	 * @param e
	 */
	protected void onError(AjaxRequestTarget target, RuntimeException e)
	{
		if (e != null)
		{
			throw e;
		}
	}
}
