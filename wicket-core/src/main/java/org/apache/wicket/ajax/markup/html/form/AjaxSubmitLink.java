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
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.AbstractSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A link that submits a form via ajax. Since this link takes the form as a constructor argument it
 * does not need to be inside form's component hierarchy.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AjaxSubmitLink extends AbstractSubmitLink
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(AjaxSubmitLink.class);

	private final Form<?> form;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public AjaxSubmitLink(String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param form
	 */
	public AjaxSubmitLink(String id, final Form<?> form)
	{
		super(id, form);

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
			protected void onError(AjaxRequestTarget target)
			{
				AjaxSubmitLink.this.onError(target);
			}

			@Override
			protected Form<?> findForm()
			{
				return AjaxSubmitLink.this.getForm();
			}

			@Override
			public boolean getDefaultProcessing()
			{
				return AjaxSubmitLink.this.getDefaultFormProcessing();
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				AjaxSubmitLink.this.updateAjaxAttributes(attributes);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				AjaxSubmitLink.this.onSubmit(target);
			}

			@Override
			protected void onAfterSubmit(AjaxRequestTarget target)
			{
				AjaxSubmitLink.this.onAfterSubmit(target);
			}
			
			@Override
			public boolean getStatelessHint(Component component)
			{
				return AjaxSubmitLink.this.getStatelessHint();
			}
		};
	}

	/**
	 * Override this method to provide special submit handling in a multi-button form. This method
	 * will be called <em>before</em> the form's onSubmit method.
	 * 
	 * @param target the {@link AjaxRequestTarget}
	 */
	protected void onSubmit(AjaxRequestTarget target)
	{
	}

	/**
	 * Override this method to provide special submit handling in a multi-button form. This method
	 * will be called <em>after</em> the form's onSubmit method.
	 * 
	 * @param target the {@link AjaxRequestTarget}
	 */
	protected void onAfterSubmit(AjaxRequestTarget target)
	{
	}

	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		if (isEnabledInHierarchy())
		{
			String tagName = tag.getName();
			
			if (tagName.equalsIgnoreCase("a") || tagName.equalsIgnoreCase("link")
				|| tagName.equalsIgnoreCase("area"))
			{
				// disable any href attr in markup
				tag.put("href", "javascript:;");
			}
			else if (tagName.equalsIgnoreCase("button"))
			{
				// WICKET-5597 prevent default submit
				tag.put("type", "button");
			}
			else if (tagName.equalsIgnoreCase("input") &&
				"submit".equalsIgnoreCase(tag.getAttribute("type")))
			{
				// WICKET-5879 prevent default submit
				tag.getAttributes().put("type", "button");
			}
		}
		else
		{
			disableLink(tag);
		}
	}

	/**
	 * Final implementation of the Button's onError. AjaxSubmitLinks have their own onError which is
	 * called.
	 * 
	 * @see org.apache.wicket.markup.html.form.Button#onError()
	 */
	@Override
	public final void onError()
	{
		logger.warn("unexpected invocation of #onError() on {}", this);
	}


	/**
	 * Listener method invoked on form submit with errors. This method is called <em>before</em>
	 * {@link Form#onError()}.
	 * 
	 * @param target
	 */
	protected void onError(AjaxRequestTarget target)
	{
	}

	/**
	 * Use {@link #onSubmit(AjaxRequestTarget)} instead.
	 */
	@Override
	public final void onSubmit()
	{
		logger.warn("unexpected invocation of #onSubmit() on {}", this);
	}

	/**
	 * Use {@link #onAfterSubmit(AjaxRequestTarget)} instead.
	 */
	@Override
	public final void onAfterSubmit()
	{
		logger.warn("unexpected invocation of #onAfterSubmit() on {}", this);
	}

	/**
	 * Creates an {@link AjaxSubmitLink} based on lambda expressions
	 *
	 * @param id
	 *            the id of ajax submit link
	 * @param onSubmit
	 *            the consumer which accepts the link and an {@link AjaxRequestTarget}
	 * @return the {@link AjaxSubmitLink}
	 */
	public static AjaxSubmitLink onSubmit(String id, WicketBiConsumer<AjaxSubmitLink, AjaxRequestTarget> onSubmit)
	{
		Args.notNull(onSubmit, "onSubmit");

		return new AjaxSubmitLink(id)
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
	 * Creates an {@link AjaxSubmitLink} based on lambda expressions
	 *
	 * @param id
	 *            the id of ajax submit link
	 * @param onSubmit
	 *            the consumer of the submitted link and an {@link AjaxRequestTarget}
	 * @param onError
	 *            the consumer of the link in error and an {@link AjaxRequestTarget}
	 * @return the {@link AjaxSubmitLink}
	 */
	public static AjaxSubmitLink onSubmit(String id,
	                                            WicketBiConsumer<AjaxSubmitLink, AjaxRequestTarget> onSubmit,
	                                            WicketBiConsumer<AjaxSubmitLink, AjaxRequestTarget> onError)
	{
		Args.notNull(onSubmit, "onSubmit");
		Args.notNull(onError, "onError");

		return new AjaxSubmitLink(id)
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
