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
package org.apache.wicket.ajax.markup.html.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * An ajax submit button that will degrade to a normal request if ajax is not available or
 * javascript is disabled.
 * 
 * @since 1.3
 * 
 * @author Jeremy Thomerson (jthomerson)
 * @author Alastair Maw
 * 
 * @param <T>
 *            The model object type
 */
public abstract class AjaxFallbackButton<T> extends Button<T>
{
	private static final long serialVersionUID = 1L;

	private final Form< ? > mForm;

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param form
	 */
	public AjaxFallbackButton(String id, Form< ? > form)
	{
		this(id, null, form);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 * @param form
	 */
	public AjaxFallbackButton(String id, IModel<T> model, Form< ? > form)
	{
		super(id, model);
		mForm = form;

		add(new AjaxFormSubmitBehavior(form, "onclick")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				AjaxFallbackButton.this.onSubmit(target, AjaxFallbackButton.this.getForm());
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				AjaxFallbackButton.this.onError(target, AjaxFallbackButton.this.getForm());
			}

			@Override
			protected CharSequence getEventHandler()
			{
				return new AppendingStringBuffer(super.getEventHandler()).append("; return false;");
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return AjaxFallbackButton.this.getAjaxCallDecorator();
			}
		});
	}

	/**
	 * Listener method invoked on form submit with errors
	 * 
	 * @param target
	 * @param form
	 * 
	 * TODO 1.3: Make abstract to be consistent with onsubmit()
	 */
	protected void onError(AjaxRequestTarget target, Form< ? > form)
	{
		// created to override
	}

	/**
	 * @see org.apache.wicket.markup.html.form.IFormSubmittingComponent#onSubmit()
	 */
	@Override
	public final void onSubmit()
	{
		if (!(getRequestCycle().getRequestTarget() instanceof AjaxRequestTarget))
		{
			onSubmit(null, getForm());
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.html.form.Button#getForm()
	 */
	@Override
	public Form< ? > getForm()
	{
		return mForm == null ? super.getForm() : mForm;
	}

	/**
	 * Callback for the onClick event. If ajax failed and this event was generated via a normal
	 * submission, the target argument will be null
	 * 
	 * @param target
	 *            ajax target if this linked was invoked using ajax, null otherwise
	 * @param form
	 */
	protected abstract void onSubmit(final AjaxRequestTarget target, final Form< ? > form);

	/**
	 * 
	 * @return
	 */
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return null;
	}

	/**
	 * Helper methods that both checks whether the link is enabled and whether the action ENABLE is
	 * allowed.
	 * 
	 * @return whether the link should be rendered as enabled
	 */
	protected final boolean isButtonEnabled()
	{
		return isEnabled() && isEnableAllowed();
	}
}
