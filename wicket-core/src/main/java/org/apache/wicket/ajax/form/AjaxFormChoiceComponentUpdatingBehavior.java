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
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.lambda.OnAjaxError;
import org.apache.wicket.lambda.OnAjaxEvent;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.util.lang.Args;

/**
 * This is a Ajax Component Update Behavior that is meant for choices/groups that are not one
 * component in the html but many.
 * <p>
 * Use the normal {@link AjaxFormComponentUpdatingBehavior} for the normal single component fields
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
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public AjaxFormChoiceComponentUpdatingBehavior()
	{
		super("click");
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);

		attributes.setSerializeRecursively(true);
		attributes.getAjaxCallListeners().add(new AjaxCallListener()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public CharSequence getPrecondition(Component component)
			{
				return String.format("return attrs.event.target.name === '%s'", getFormComponent().getInputName());
			}
		});
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

	/**
	 * Creates an {@link AjaxFormChoiceComponentUpdatingBehavior} based on lambda expressions
	 * 
	 * @param onUpdateChoice
	 *            the lambda that receives the {@link AjaxRequestTarget}
	 * @return the {@link AjaxFormChoiceComponentUpdatingBehavior}
	 */
	public static AjaxFormChoiceComponentUpdatingBehavior onUpdateChoice(OnAjaxEvent onUpdateChoice) {
		Args.notNull(onUpdateChoice, "onUpdateChoice");
		return new AjaxFormChoiceComponentUpdatingBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onUpdateChoice.onEvent(target);
			}
		};
	}

	/**
	 * Creates an {@link AjaxFormChoiceComponentUpdatingBehavior} based on lambda expressions
	 * 
	 * @param onUpdateChoice
	 *            the lambda that receives the {@link AjaxRequestTarget}
	 * @param onError
	 *            the lambda that receives the {@link AjaxRequestTarget} and the
	 *            {@link RuntimeException}
	 * @return the {@link AjaxFormChoiceComponentUpdatingBehavior}
	 */
	public static AjaxFormChoiceComponentUpdatingBehavior onUpdateChoice(OnAjaxEvent onUpdateChoice,
	                                                                     OnAjaxError onError) {
		Args.notNull(onUpdateChoice, "onUpdateChoice");
		Args.notNull(onError, "onError");
		return new AjaxFormChoiceComponentUpdatingBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onUpdateChoice.onEvent(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, RuntimeException e)
			{
				onError.onError(target, e);
			}
		};
	}
}
