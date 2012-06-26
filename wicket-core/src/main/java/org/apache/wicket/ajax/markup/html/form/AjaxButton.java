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
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;

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

		add(new AjaxFormSubmitBehavior(form, "onclick")
		{
			private static final long serialVersionUID = 1L;

			/**
			 * 
			 * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
			 */
			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				AjaxButton.this.onSubmit(target, AjaxButton.this.getForm());
			}

			/**
			 * 
			 * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
			 */
			@Override
			protected void onSubmitAfterForm(AjaxRequestTarget target)
			{
				AjaxButton.this.onSubmitAfterForm(target, AjaxButton.this.getForm());
			}

			/**
			 * 
			 * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onError(org.apache.wicket.ajax.AjaxRequestTarget)
			 */
			@Override
			protected void onError(AjaxRequestTarget target)
			{
				AjaxButton.this.onError(target, AjaxButton.this.getForm());
			}

			/**
			 * 
			 * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#getEventHandler()
			 */
			@Override
			protected CharSequence getEventHandler()
			{
				final String script = AjaxButton.this.getOnClickScript();

				AppendingStringBuffer handler = new AppendingStringBuffer();

				if (!Strings.isEmpty(script))
				{
					handler.append(script).append(";");
				}

				handler.append(super.getEventHandler());
				handler.append("; return false;");
				return handler;
			}

			/**
			 * 
			 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getAjaxCallDecorator()
			 */
			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return AjaxButton.this.getAjaxCallDecorator();
			}

			@Override
			protected AjaxChannel getChannel()
			{
				return AjaxButton.this.getChannel();
			}

			@Override
			public boolean getDefaultProcessing()
			{
				return AjaxButton.this.getDefaultFormProcessing();
			}
		});
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
	 * Returns the {@link IAjaxCallDecorator} that will be used to modify the generated javascript.
	 * This is the preferred way of changing the javascript in the onclick handler
	 * 
	 * @return call decorator used to modify the generated javascript or null for none
	 */
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return null;
	}

	/**
	 * @return the channel that manages how Ajax calls are executed
	 * @see AbstractDefaultAjaxBehavior#getChannel()
	 */
	protected AjaxChannel getChannel()
	{
		return null;
	}

	/**
	 * Listener method invoked on form submit with no errors
	 * 
	 * @param target
	 * @param form
	 */
	protected void onSubmit(AjaxRequestTarget target, Form<?> form)
	{
	}

	/**
	 * Listener method invoked on form submit with no errors
	 * 
	 * @param target
	 * @param form
	 */
	protected void onSubmitAfterForm(AjaxRequestTarget target, Form<?> form)
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
