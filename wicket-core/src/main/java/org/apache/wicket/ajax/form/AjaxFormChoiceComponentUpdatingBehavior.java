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
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * This is a Ajax Component Update Behavior that is meant for choices/groups that are not one
 * component in the html but many.
 * <p>
 * Use the normal {@link AjaxFormComponentUpdatingBehavior} for the normal single component fields.
 * <p>
 * When using multiple instances of this behavior on nested groups, you'll have to allow JavaScript
 * event bubbling by overriding {@link #updateAjaxAttributes(AjaxRequestAttributes)}:
 * 
 * <pre>
 * <code>
 * &#064;Override
 * protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
 * {
 * 	super.updateAjaxAttributes(attributes);
 * 
 * 	attributes.setEventPropagation(EventPropagation.BUBBLE);
 * }
 * </code>
 * </pre>
 * 
 * @author jcompagner
 * @author svenmeier
 *
 * @see org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior#onUpdate(org.apache.wicket.ajax.AjaxRequestTarget)
 * @see org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior#onError(org.apache.wicket.ajax.AjaxRequestTarget, RuntimeException)
 * @see RadioChoice
 * @see CheckBoxMultipleChoice
 * @see RadioGroup
 * @see CheckGroup
 */
public abstract class AjaxFormChoiceComponentUpdatingBehavior extends
	AjaxFormComponentUpdatingBehavior
{
	private static final ResourceReference CHOICE_JS = new JavaScriptResourceReference(
		AjaxFormChoiceComponentUpdatingBehavior.class, "AjaxFormChoiceComponentUpdatingBehavior.js");

	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public AjaxFormChoiceComponentUpdatingBehavior()
	{
		super("click");
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(CHOICE_JS));
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);

		attributes.getAjaxCallListeners().add(new AjaxCallListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public CharSequence getPrecondition(Component component)
			{
				return String.format("return Wicket.Choice.acceptInput('%s', attrs)",
					getFormComponent().getInputName());
			}
		});

		attributes.getDynamicExtraParameters().add(
			String.format("return Wicket.Choice.getInputValues('%s', attrs)",
				getFormComponent().getInputName()));
	}

	/**
	 * 
	 * @see org.apache.wicket.behavior.AbstractAjaxBehavior#onBind()
	 */
	@Override
	protected void onBind()
	{
		super.onBind();

		if (getComponent() instanceof RadioGroup || getComponent() instanceof CheckGroup)
		{
			getComponent().setRenderBodyOnly(false);
		}
	}

	@Override
	protected void checkComponent(FormComponent<?> component)
	{
		if (!AjaxFormChoiceComponentUpdatingBehavior.appliesTo(getComponent()))
		{
			throw new WicketRuntimeException("Behavior " + getClass().getName() +
				" can only be added to an instance of a RadioChoice/CheckboxChoice/RadioGroup/CheckGroup");
		}
	}

	/**
	 * @param component
	 *            the component to check
	 * @return if the component applies to the {@link AjaxFormChoiceComponentUpdatingBehavior}
	 */
	static boolean appliesTo(Component component)
	{
		return (component instanceof RadioChoice) ||
			(component instanceof CheckBoxMultipleChoice) || (component instanceof RadioGroup) ||
			(component instanceof CheckGroup);
	}
}
