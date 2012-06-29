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

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

/**
 * A button that submits the form via ajax.
 * 
 * @since 1.3
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AjaxButton extends Button
{
	private static final long serialVersionUID = 1L;

	private final Form<?> form;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public AjaxButton(String id)
	{
		this(id, null, null);
	}

	/**
	 * 
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 *            model used to set <code>value</code> markup attribute
	 */
	public AjaxButton(String id, IModel<String> model)
	{
		this(id, model, null);
	}

	/**
	 * 
	 * Construct.
	 * 
	 * @param id
	 * @param form
	 */
	public AjaxButton(String id, Form<?> form)
	{
		this(id, null, form);
	}


	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 *            model used to set <code>value</code> markup attribute
	 * @param form
	 */
	public AjaxButton(String id, IModel<String> model, final Form<?> form)
	{
		super(id, model);
		this.form = form;

		add(new AjaxFormSubmitBehavior(form, "click")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				AjaxButton.this.onSubmit(target, AjaxButton.this.getForm());
			}

			@Override
			protected void onAfterSubmit(AjaxRequestTarget target)
			{
				AjaxButton.this.onAfterSubmit(target, AjaxButton.this.getForm());
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				AjaxButton.this.onError(target, AjaxButton.this.getForm());
			}

			@Override
			protected AjaxChannel getChannel()
			{
				return AjaxButton.this.getChannel();
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				AjaxButton.this.updateAjaxAttributes(attributes);
			}

			@Override
			public boolean getDefaultProcessing()
			{
				return AjaxButton.this.getDefaultFormProcessing();
			}
		});
	}

	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
	}

	/**
	 * Returns the form if it was set in constructor, otherwise returns the form nearest in parent
	 * hierarchy.
	 * 
	 * @see org.apache.wicket.markup.html.form.FormComponent#getForm()
	 */
	@Override
	public Form<?> getForm()
	{
		if (form != null)
		{
			return form;
		}
		else
		{
			return super.getForm();
		}
	}

	/**
	 * @return the channel that manages how Ajax calls are executed
	 * @see AbstractDefaultAjaxBehavior#getChannel()
	 */
	@Deprecated
	protected AjaxChannel getChannel()
	{
		return null;
	}

	/**
	 * Listener method invoked on form submit with no errors, before {@link Form#onSubmit()}.
	 * 
	 * @param target
	 * @param form
	 */
	protected void onSubmit(AjaxRequestTarget target, Form<?> form)
	{
	}

	/**
	 * Listener method invoked on form submit with no errors, after {@link Form#onSubmit()}.
	 * 
	 * @param target
	 * @param form
	 */
	protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form)
	{
	}

	/**
	 * Listener method invoked on form submit with errors
	 * 
	 * @param target
	 * @param form
	 */
	protected void onError(AjaxRequestTarget target, Form<?> form)
	{
	}
}
