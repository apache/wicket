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
package org.apache.wicket.ajaxng.form;

import org.apache.wicket.Component;
import org.apache.wicket.Component.IVisitor;
import org.apache.wicket.ajaxng.AjaxRequestAttributes;
import org.apache.wicket.ajaxng.AjaxRequestTarget;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.time.Duration;

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
	public AjaxFormValidatingBehavior(Form< ? > form, String event)
	{
		super(form, event);
	}

	/**
	 * 
	 * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
	 */
	@Override
	protected void onSubmit(final AjaxRequestTarget target)
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
		getBoundComponents().get(0).getPage().visitChildren(IFeedback.class, new IVisitor<Component>()
		{
			public Object component(Component component)
			{
				target.addComponent(component);
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Adds this behavior to all form components of the specified form
	 * 
	 * @param form
	 * @param event
	 */
	public static void addToAllFormComponents(final Form< ? > form, final String event)
	{
		addToAllFormComponents(form, event, null);
	}

	private static void addThrottlingBehavior(Form<?> form, Component component, String event, final Duration throttleDelay)
	{
		AjaxFormValidatingBehavior behavior = new AjaxFormValidatingBehavior(form, event)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void updateAttributes(AjaxRequestAttributes attributes,
				Component component)
			{
				super.updateAttributes(attributes, component);
				attributes.setThrottle((int)throttleDelay.getMilliseconds());
				attributes.setThrottlePostpone(true);
			}
		};
		
		component.add(behavior);	
	}
	
	/**
	 * Adds this behavior to all form components of the specified form
	 * 
	 * @param form
	 * @param event
	 * @param throttleDelay
	 */
	public static void addToAllFormComponents(final Form< ? > form, final String event,
		final Duration throttleDelay)
	{
		form.visitChildren(FormComponent.class, new IVisitor<Component>()
		{
			public Object component(Component component)
			{
				addThrottlingBehavior(form, component, event, throttleDelay);
				return IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
			}
		});
	}
}
