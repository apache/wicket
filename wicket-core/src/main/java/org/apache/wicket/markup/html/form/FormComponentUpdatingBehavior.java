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

import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;

/**
 * A behavior to get notifications when a {@link FormComponent} changes its value.
 * <p>
 * Contrary to {@link AjaxFormComponentUpdatingBehavior} all notification are sent via
 * standard HTTP requests and the full page is rendered as a response.
 * <p>
 * Notification is triggered by an event suitable for the host component this
 * behavior is added to - if needed {@link #getEvent()} can be overridden to change the default
 * ({@value change} for {@link DropDownChoice}, {@link ListMultipleChoice} and {@link AbstractTextComponent}, 
 * {@value click} for anything else).
 * <p>
 * Note: This behavior has limited support for {@link FormComponent}s outside of a form, i.e. multiple
 * choice components ({@link ListMultipleChoice} and {@link RadioGroup}) will send their last selected
 * choice only.
 * 
 * @see FormComponentUpdatingBehavior#onUpdate()
 */
public class FormComponentUpdatingBehavior extends Behavior implements IRequestListener
{

	private FormComponent<?> formComponent;

	@Override
	public boolean getStatelessHint(Component component)
	{
		return false;
	}

	@Override
	public final void bind(final Component component)
	{
		Args.notNull(component, "component");

		if (!(component instanceof FormComponent))
		{
			throw new WicketRuntimeException("Behavior " + getClass().getName()
				+ " can only be added to an instance of a FormComponent");
		}

		if (formComponent != null)
		{
			throw new IllegalStateException("this kind of handler cannot be attached to " +
				"multiple components; it is already attached to component " + formComponent +
				", but component " + component + " wants to be attached too");
		}

		this.formComponent = (FormComponent<?>)component;

		formComponent.setRenderBodyOnly(false);

		// call the callback
		onBind();
	}

	/**
	 * Called when the component was bound to it's host component. You can get the bound host
	 * component by calling {@link #getFormComponent()}.
	 */
	protected void onBind()
	{
	}

	/**
	 * Get the hosting component.
	 * 
	 * @return hosting component
	 */
	public final FormComponent<?> getFormComponent()
	{
		return formComponent;
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		CharSequence url = component.urlForListener(this, new PageParameters());

		String event = getEvent();

		String condition = String.format("if (event.target.name !== '%s') return; ",
			formComponent.getInputName());

		Form<?> form = component.findParent(Form.class);
		if (form != null)
		{
			tag.put("on" + event, condition + form.getJsForListenerUrl(url.toString()));
		}
		else
		{
			char separator = url.toString().indexOf('?') > -1 ? '&' : '?';

			tag.put("on" + event, condition + String.format("window.location.href='%s%s%s=' + %s;", url,
				separator, formComponent.getInputName(), getJSValue()));
		}
	}

	/**
	 * Which JavaScript event triggers notification.
	 * 
	 * @return {@value change} or {@value click}, depending on the host component 
	 */
	protected String getEvent()
	{
		if (formComponent instanceof DropDownChoice || formComponent instanceof ListMultipleChoice|| formComponent instanceof AbstractTextComponent)
		{
			return "change";
		}
		else
		{
			return "click";
		}
	}

	/**
	 * How to get the current value via JavaScript. 
	 */
	private String getJSValue()
	{
		if (formComponent instanceof DropDownChoice)
		{
			return "this.options[this.selectedIndex].value";
		}
		else if (formComponent instanceof CheckBox)
		{
			return "this.checked";
		}
		else
		{
			return "event.target.value";
		}
	}

	/**
	 * Process the form component.
	 */
	private void process()
	{
		try
		{
			formComponent.validate();
			if (formComponent.isValid())
			{
				if (getUpdateModel())
				{
					formComponent.valid();
					formComponent.updateModel();
				}
	
				onUpdate();
			}
			else
			{
				formComponent.invalid();
				
				onError(null);
			}
		}
		catch (RuntimeException e)
		{
			onError(e);
		}
	}

	/**
	 * Gives the control to the application to decide whether the form component model should
	 * be updated automatically or not. Make sure to call {@link org.apache.wicket.markup.html.form.FormComponent#valid()}
	 * additionally in case the application want to update the model manually.
	 *
	 * @return true if the model of form component should be updated, false otherwise
	 */
	protected boolean getUpdateModel()
	{
		return true;
	}

	/**
	 * Hook method invoked when the component is updated.
	 * <p>
	 * Note: {@link #onError(AjaxRequestTarget, RuntimeException)} is called instead when processing
	 * of the {@link FormComponent} failed with conversion or validation errors!  
	 */
	protected void onUpdate()
	{
	}

	/**
	 * Hook method invoked when updating of the component resulted in an error.
	 * <p>
	 * The {@link RuntimeException} will be null if it was just a validation or conversion error of the
	 * FormComponent.
	 * 
	 * @param e optional runtime exception
	 */
	protected void onError(RuntimeException e)
	{
		if (e != null)
		{
			throw e;
		}
	}
	
	@Override
	public final void onRequest()
	{
		Form<?> form = formComponent.findParent(Form.class);
		if (form == null)
		{
			process();
		}
		else
		{
			form.getRootForm().onFormSubmitted(new IFormSubmitter()
			{
				@Override
				public void onSubmit()
				{
					process();
				}

				@Override
				public void onError()
				{
				}

				@Override
				public void onAfterSubmit()
				{
				}

				@Override
				public Form<?> getForm()
				{
					return formComponent.getForm();
				}

				@Override
				public boolean getDefaultFormProcessing()
				{
					return false;
				}
			});
		}
	}
}
