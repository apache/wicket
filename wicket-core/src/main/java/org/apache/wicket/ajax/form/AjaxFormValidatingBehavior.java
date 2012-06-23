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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
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
public class AjaxFormValidatingBehavior extends AjaxFormSubmitBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param form
	 *            form that will be submitted via ajax
	 * @param event
	 *            javascript event this behavior will be invoked on, like onclick
	 */
	public AjaxFormValidatingBehavior(Form<?> form, String event)
	{
		super(form, event);
	}

	/**
	 * 
	 * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onSubmitBeforeForm(org.apache.wicket.ajax.AjaxRequestTarget)
	 */
	@Override
	protected void onSubmitBeforeForm(final AjaxRequestTarget target)
	{
		addFeedbackPanels(target);
	}

	/**
	 * 
	 * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onError(org.apache.wicket.ajax.AjaxRequestTarget)
	 */
	@Override
	protected void onError(AjaxRequestTarget target)
	{
		addFeedbackPanels(target);
	}

	/**
	 * Adds all feedback panels on the page to the ajax request target so they are updated
	 * 
	 * @param target
	 */
	private void addFeedbackPanels(final AjaxRequestTarget target)
	{
		getComponent().getPage().visitChildren(IFeedback.class, new IVisitor<Component, Void>()
		{
			@Override
			public void component(final Component component, final IVisit<Void> visit)
			{
				target.add(component);
			}
		});
	}

	/**
	 * Adds this behavior to all form components of the specified form
	 * 
	 * @param form
	 * @param event
	 */
	public static void addToAllFormComponents(final Form<?> form, final String event)
	{
		addToAllFormComponents(form, event, null);
	}

	/**
	 * Adds this behavior to all form components of the specified form
	 * 
	 * @param form
	 * @param event
	 * @param throttleDelay
	 */
	public static void addToAllFormComponents(final Form<?> form, final String event,
		final Duration throttleDelay)
	{
		form.visitChildren(FormComponent.class, new FormValidateVisitor(form, event, throttleDelay));
	}

	private static class FormValidateVisitor implements IVisitor<Component, Void>, IClusterable
	{
		private final Form<?> form;
		private final String event;
		private final Duration throttleDelay;

		private FormValidateVisitor(Form<?> form, String event, Duration throttleDelay)
		{
			this.form = form;
			this.event = event;
			this.throttleDelay = throttleDelay;
		}

		@Override
		public void component(final Component component, final IVisit<Void> visit)
		{
			final AjaxFormValidatingBehavior behavior = new AjaxFormValidatingBehavior(form, event)
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
					}
				}
			};
			component.add(behavior);
			visit.dontGoDeeper();
		}
	}
}
