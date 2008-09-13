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
package org.apache._wicket.ajax.form;

import org.apache._wicket.ajax.AjaxEventBehavior;
import org.apache._wicket.ajax.AjaxRequestAttributes;
import org.apache._wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

/**
 * Ajax event behavior that submits a form via ajax when the event it is attached to is invoked.
 * <p>
 *  
 * @see AjaxEventBehavior
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AjaxFormSubmitBehavior extends AjaxEventBehavior
{
	private static final long serialVersionUID = 1L;

	private Form<?> form;

	/**
	 * Constructor. This constructor can only be used when the component this behavior is attached
	 * to is inside a form.
	 * 
	 * @param event
	 *            javascript event this behavior is attached to
	 */
	public AjaxFormSubmitBehavior(String event)
	{
		this(null, event);
	}

	/**
	 * Construct.
	 * 
	 * @param form
	 *            form that will be submitted
	 * @param event
	 *            javascript event this behavior is attached to
	 */
	public AjaxFormSubmitBehavior(Form<?> form, String event)
	{
		super(event);
		this.form = form;
	}

	/**
	 * @return Form that will be submitted by this behavior
	 */
	@Override
	protected Form<?> getForm(Component component)
	{
		if (form == null)
		{
			// try to find form in the hierarchy of owning component
			form = component.findParent(Form.class);
			if (form == null)
			{
				throw new IllegalStateException(
					"form was not specified in the constructor and cannot "
						+ "be found in the hierarchy of the component this behavior "
						+ "is attached to");
			}
		}
		return form;
	}

	protected Form<?> getForm()
	{
		Component component = getBoundComponents().iterator().next();
		return getForm(component);
	}
	
	@Override
	protected void onEvent(AjaxRequestTarget target)
	{
		getForm().getRootForm().onFormSubmitted();
		if (!getForm().isSubmitted())
		{ // only process the form submission if the form was actually submitted -> needs to be
			// enabled and visible
			return;
		}
		if (!getForm().hasError())
		{
			onSubmit(target);
		}
		if (form.findParent(Page.class) != null)
		{
			/*
			 * there can be cases when a form is replaced with another component in the onsubmit()
			 * handler of this behavior. in that case form no longer has a page and so calling
			 * .hasError on it will cause an exception, thus the check above.
			 */
			if (getForm().hasError())
			{
				onError(target);
			}
		}
		
		updateFeedback(target);
	}
	
	@Override
	protected void updateAttributes(AjaxRequestAttributes attributes, Component component)
	{
		super.updateAttributes(attributes, component);
		
		if (component instanceof IFormSubmittingComponent)
		{
			attributes.getUrlArguments().put(((IFormSubmittingComponent)component).getInputName(), 1);
		}
	}
	
	/**
	 * Listener method that is invoked after the form has been submitted and processed without
	 * errors
	 * 
	 * @param target
	 */
	protected abstract void onSubmit(AjaxRequestTarget target);

	/**
	 * Listener method invoked when the form has been processed and errors occurred
	 * 
	 * @param target
	 * 
	 */
	protected abstract void onError(AjaxRequestTarget target);

	/**
	 * Convenience method that is called whether the form submit succeeds or not. Useful for
	 * refreshing components that should be updated regardless of whether the form failed to
	 * validate or not, such as {@link FeedbackPanel}s.
	 * 
	 * @param target
	 */
	protected void updateFeedback(AjaxRequestTarget target)
	{
	}
}
