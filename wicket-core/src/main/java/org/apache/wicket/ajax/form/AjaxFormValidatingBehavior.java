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

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * Ajax event behavior that submits the form and updates all form feedback panels on the page.
 * Useful for providing instant feedback.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class AjaxFormValidatingBehavior extends Behavior
{
	private static final long serialVersionUID = 1L;

	private final String event;
	private final Duration throttleDelay;

	/**
	 * The form that will be submitted via ajax
	 */
	private Form<?> form;

	/**
	 * A flag indicating whether this behavior has been rendered at least once
	 */
	private boolean hasBeenRendered = false;

	/**
	 * Construct.
	 * 
	 * @param event
	 *            javascript event this behavior will be invoked on, like onclick
	 */
	public AjaxFormValidatingBehavior(String event)
	{
		this(event, null);
	}

	/**
	 * Construct.
	 *
	 * @param event
	 *            javascript event this behavior will be invoked on, like onclick
	 * @param throttleDelay
	 *            the duration for which the Ajax call should be throttled
	 */
	public AjaxFormValidatingBehavior(String event, Duration throttleDelay)
	{
		this.event = event;
		this.throttleDelay = throttleDelay;
	}

	@Override
	public void bind(Component component)
	{
		super.bind(component);

		if (component instanceof Form<?>)
		{
			form = (Form<?>) component;
		}
		else
		{
			form = Form.findForm(component);
			if (form == null)
			{
				throw new WicketRuntimeException(AjaxFormValidatingBehavior.class.getSimpleName() +
						" should be bound to a Form component or a component that is inside a form!");
			}
		}
	}

	@Override
	public void onConfigure(Component component)
	{
		super.onConfigure(component);

		if (hasBeenRendered == false)
		{
			hasBeenRendered = true;

			form.visitChildren(FormComponent.class, new FormValidateVisitor());
		}
	}

	protected void onSubmit(final AjaxRequestTarget target)
	{
		addFeedbackPanels(target);
	}

	protected void onAfterSubmit(final AjaxRequestTarget target)
	{
	}

	protected void onError(AjaxRequestTarget target)
	{
		addFeedbackPanels(target);
	}

	/**
	 * Adds all feedback panels on the page to the ajax request target so they are updated
	 * 
	 * @param target
	 */
	protected final void addFeedbackPanels(final AjaxRequestTarget target)
	{
		form.getPage().visitChildren(IFeedback.class, new IVisitor<Component, Void>()
		{
			@Override
			public void component(final Component component, final IVisit<Void> visit)
			{
				target.add(component);
			}
		});
	}

	protected void updateAjaxAttributes(final AjaxRequestAttributes attributes)
	{
	}

	private class FormValidateVisitor implements IVisitor<FormComponent, Void>, IClusterable
	{
		@Override
		public void component(final FormComponent component, final IVisit<Void> visit)
		{
			final AjaxFormSubmitBehavior behavior = new AjaxFormSubmitBehavior(form, event)
			{
				@Override
				protected void updateAjaxAttributes(final AjaxRequestAttributes attributes)
				{
					super.updateAjaxAttributes(attributes);

					if (throttleDelay != null)
					{
						String id = "throttle-" + component.getMarkupId();
						ThrottlingSettings throttlingSettings = new ThrottlingSettings(id,
							throttleDelay);
						attributes.setThrottlingSettings(throttlingSettings);
						attributes.setPreventDefault(false); // WICKET-5194
					}

					AjaxFormValidatingBehavior.this.updateAjaxAttributes(attributes);
				}

				@Override
				protected void onSubmit(AjaxRequestTarget target)
				{
					super.onSubmit(target);
					AjaxFormValidatingBehavior.this.onSubmit(target);
				}

				@Override
				protected void onAfterSubmit(AjaxRequestTarget target)
				{
					super.onAfterSubmit(target);
					AjaxFormValidatingBehavior.this.onAfterSubmit(target);
				}

				@Override
				protected void onError(AjaxRequestTarget target)
				{
					super.onError(target);
					AjaxFormValidatingBehavior.this.onError(target);
				}
			};
			component.add(behavior);
			visit.dontGoDeeper();
		}
	}
}
