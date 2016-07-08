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

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

/**
 * An ajax submit button that will degrade to a normal request if ajax is not available or
 * javascript is disabled.
 * 
 * @since 1.3
 * 
 * @author Jeremy Thomerson (jthomerson)
 * @author Alastair Maw
 * 
 * 
 */
public abstract class AjaxFallbackButton extends Button
{
	private static final long serialVersionUID = 1L;

	private final Form<?> mForm;

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param form
	 */
	public AjaxFallbackButton(String id, Form<?> form)
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
	public AjaxFallbackButton(String id, IModel<String> model, Form<?> form)
	{
		super(id, model);
		mForm = form;

		add(new AjaxFormSubmitBehavior(form, "click")
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				AjaxFallbackButton.this.onSubmit(Optional.ofNullable(target));
			}

			@Override
			protected void onAfterSubmit(AjaxRequestTarget target)
			{
				AjaxFallbackButton.this.onAfterSubmit(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				AjaxFallbackButton.this.onError(target);
			}

			@Override
			public boolean getDefaultProcessing()
			{
				return AjaxFallbackButton.this.getDefaultFormProcessing();
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);

				// do not allow normal form submit to happen
				attributes.setPreventDefault(true);

				AjaxFallbackButton.this.updateAjaxAttributes(attributes);
			}

		});
	}

	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
	}

	/**
	 * Listener method invoked on form submit with errors. If ajax failed and this event was
	 * generated via a normal submission, the target argument will be null.
	 * 
	 * @param target
	 * @param form
	 */
	protected void onError(AjaxRequestTarget target)
	{
	}

	@Override
	public final void onError()
	{
		if (getRequestCycle().find(AjaxRequestTarget.class).isPresent() == false)
		{
			onError(null);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.form.IFormSubmittingComponent#onSubmit()
	 */
	@Override
	public final void onSubmit()
	{
		if (getRequestCycle().find(AjaxRequestTarget.class).isPresent() == false)
		{
			onSubmit(Optional.empty());
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.form.IFormSubmittingComponent#onAfterSubmit()
	 */
	@Override
	public final void onAfterSubmit()
	{
		if (getRequestCycle().find(AjaxRequestTarget.class).isPresent() == false)
		{
			onAfterSubmit(null);
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.html.form.Button#getForm()
	 */
	@Override
	public Form<?> getForm()
	{
		return mForm == null ? super.getForm() : mForm;
	}

	/**
	 * Callback for the onClick event. If ajax failed and this event was generated via a normal
	 * submission, the target argument will be null. This method will be called <em>before</em>
	 * {@link Form#onSubmit()}.
	 * 
	 * @param target
	 *            ajax target if this linked was invoked using ajax, null otherwise
	 * @param form
	 */
	protected void onSubmit(final Optional<AjaxRequestTarget> target)
	{
	}

	/**
	 * Callback for the onClick event. If ajax failed and this event was generated via a normal
	 * submission, the target argument will be null. This method will be called <em>after</em>
	 * {@link Form#onSubmit()}.
	 * 
	 * @param target
	 *            ajax target if this linked was invoked using ajax, null otherwise
	 * @param form
	 */
	protected void onAfterSubmit(final AjaxRequestTarget target)
	{
	}

	/**
	 * Helper methods that both checks whether the link is enabled and whether the action ENABLE is
	 * allowed.
	 * 
	 * @return whether the link should be rendered as enabled
	 */
	protected final boolean isButtonEnabled()
	{
		return isEnabledInHierarchy();
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		String tagName = tag.getName();
		if (!("input".equalsIgnoreCase(tagName) || "button".equalsIgnoreCase(tagName)))
		{
			String msg = String.format("%s must be used only with <input type=\"submit\"> or <input type=\"submit\"> markup elements. " +
					"The fallback functionality doesn't work for other markup elements. " +
					"Component path: %s, markup element: <%s>.",
					AjaxFallbackButton.class.getSimpleName(), getClassRelativePath(), tagName);
			findMarkupStream().throwMarkupException(msg);
		}

		super.onComponentTag(tag);
	}
}
