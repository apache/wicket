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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.lambda.WicketBiConsumer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A button that submits the form via Ajax. <br>
 * Note that an HTML type attribute of "submit" is automatically changed to "button"- Use
 * {@link AjaxFallbackButton} if you want to support non-Ajax form submits too.
 * 
 * @since 1.3
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AjaxButton extends Button
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(AjaxButton.class);

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
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		add(newAjaxFormSubmitBehavior("click"));
	}

	protected AjaxFormSubmitBehavior newAjaxFormSubmitBehavior(String event)
	{
		return new AjaxFormSubmitBehavior(form, event)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				AjaxButton.this.onSubmit(target);
			}

			@Override
			protected void onAfterSubmit(AjaxRequestTarget target)
			{
				AjaxButton.this.onAfterSubmit(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				AjaxButton.this.onError(target);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);

				// do not allow normal form submit to happen
				attributes.setPreventDefault(true);

				AjaxButton.this.updateAjaxAttributes(attributes);
			}

			@Override
			public boolean getDefaultProcessing()
			{
				return AjaxButton.this.getDefaultFormProcessing();
			}
			
			@Override
			public boolean getStatelessHint(Component component)
			{
				return AjaxButton.this.getStatelessHint();
			}
		};
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
	 * This method is never called.
	 * 
	 * @see #onSubmit(AjaxRequestTarget, Form)
	 */
	@Override
	public final void onSubmit()
	{
		logger.warn("unexpected invocation of #onSubmit() on {}", this);
	}

	@Override
	public final void onAfterSubmit()
	{
		logger.warn("unexpected invocation of #onAfterSubmit() on {}", this);
	}

	/**
	 * This method is never called.
	 * 
	 * @see #onError(AjaxRequestTarget, Form)
	 */
	@Override
	public final void onError()
	{
		logger.warn("unexpected invocation of #onError() on {}", this);
	}

	/**
	 * Listener method invoked on form submit with no errors, before {@link Form#onSubmit()}.
	 * 
	 * @param target
	 * @param form
	 */
	protected void onSubmit(AjaxRequestTarget target)
	{
	}

	/**
	 * Listener method invoked on form submit with no errors, after {@link Form#onSubmit()}.
	 *
	 * @param target
	 * @param form
	 */
	protected void onAfterSubmit(AjaxRequestTarget target)
	{
	}

	/**
	 * Listener method invoked on form submit with errors
	 *
	 * @param target
	 * @param form
	 */
	protected void onError(AjaxRequestTarget target)
	{
	}

	/**
	 * Creates an {@link AjaxButton} based on lambda expressions
	 * 
	 * @param id
	 *            the id of the ajax button
	 * @param onSubmit
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link AjaxButton}
	 * @return the {@link AjaxButton}
	 */
	public static AjaxButton onSubmit(String id, WicketBiConsumer<AjaxButton, AjaxRequestTarget> onSubmit)
	{
		Args.notNull(onSubmit, "onSubmit");

		return new AjaxButton(id)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target)
			{
				onSubmit.accept(this, target);
			}
		};
	}

	/**
	 * Creates an {@link AjaxButton} based on lambda expressions
	 * 
	 * @param id
	 *            the id of the ajax button
	 * @param onSubmit
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link AjaxButton}
	 * @param onError
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link AjaxButton}
	 * @return the {@link AjaxButton}
	 */
	public static AjaxButton onSubmit(String id,
	                                    WicketBiConsumer<AjaxButton, AjaxRequestTarget> onSubmit,
	                                    WicketBiConsumer<AjaxButton, AjaxRequestTarget> onError)
	{
		Args.notNull(onSubmit, "onSubmit");
		Args.notNull(onError, "onError");

		return new AjaxButton(id)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target)
			{
				onSubmit.accept(this, target);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				onError.accept(this, target);
			}
		};
	}
	
	@Override
	protected boolean getStatelessHint()
	{
		return false;
	}
}
