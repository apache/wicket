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

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.persistence.IValuePersister;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * A behavior that updates the hosting FormComponent via ajax when an event it
 * is attached to is triggered. This behavior encapsulates the entire
 * form-processing workflow as relevant only to this component so if validation
 * is successfull the component's model will be updated according to the
 * submitted value.
 * <p>
 * NOTE: This behavior does not support persisting form component values into
 * cookie or other {@link IValuePersister}. If this is necessary please add a
 * request for enhancement.
 * <p>
 * NOTE: This behavior does not validate any {@link IFormValidator}s attached
 * to this form even though they may reference the component being updated.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AjaxFormComponentUpdatingBehavior extends AjaxEventBehavior
{
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
	protected void onBind()
	{
		super.onBind();

		if (!(getComponent() instanceof FormComponent))
		{
			throw new WicketRuntimeException("Behavior " + getClass().getName()
					+ " can only be added to an instance of a FormComponent");
		}
	}

	/**
	 * 
	 * @return FormComponent
	 */
	protected final FormComponent getFormComponent()
	{
		return (FormComponent)getComponent();
	}

	/**
	 * @see org.apache.wicket.ajax.AjaxEventBehavior#getEventHandler()
	 */
	protected final CharSequence getEventHandler()
	{
		return generateCallbackScript(new AppendingStringBuffer("wicketAjaxPost('").append(
				getCallbackUrl(false)).append(
				"', wicketSerialize(Wicket.$('" + getComponent().getMarkupId()
						+ "'))"));
	}

	/**
	 * @see org.apache.wicket.ajax.AjaxEventBehavior#onCheckEvent(java.lang.String)
	 */
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
	protected final void onEvent(final AjaxRequestTarget target)
	{
		final FormComponent formComponent = getFormComponent();

		try
		{
			formComponent.inputChanged();
			formComponent.validate();
			if (formComponent.hasErrorMessage())
			{
				formComponent.invalid();
				
				onError(target, null);
			}
			else
			{
				formComponent.valid();
				formComponent.updateModel();
				onUpdate(target);
			}
		}
		catch (RuntimeException e)
		{
			onError(target, e);

		}
	}

	/**
	 * Listener invoked on the ajax request. This listener is invoked after the
	 * component's model has been updated.
	 * 
	 * @param target
	 */
	protected abstract void onUpdate(AjaxRequestTarget target);

	/**
	 * Called to handle any error resulting from updating form component. Errors
	 * thrown from {@link #onUpdate(AjaxRequestTarget)} will not be caught here.
	 * 
	 * The RuntimeException will be null if it was just a validation or conversion 
	 * error of the FormComponent
	 * 
	 * @param target
	 * @param e
	 */
	protected void onError(AjaxRequestTarget target, RuntimeException e)
	{
		if(e != null) throw e;
	}
}
