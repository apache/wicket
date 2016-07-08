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
import org.apache.wicket.lambda.Lambdas;
import org.apache.wicket.lambda.WicketBiConsumer;
import org.apache.wicket.lambda.WicketConsumer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.lang.Args;

/**
 * A behavior that updates the hosting {@link FormComponent} via Ajax when value of the component is
 * changed.
 * <p>
 * This behavior uses best available method to track changes on different types of form components.
 * To accomplish this for text input form components it uses a custom event, named 'inputchange',
 * that is handled in the best way for the specific browser. For other form component types the
 * 'change' event is used.
 * </p>
 * 
 * @author Janne Hietam&auml;ki (janne)
 * 
 * @since 1.3
 * @see org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior#onUpdate(org.apache.wicket.ajax.AjaxRequestTarget)
 * @see org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior#onError(org.apache.wicket.ajax.AjaxRequestTarget, RuntimeException)
 */
public abstract class OnChangeAjaxBehavior extends AjaxFormComponentUpdatingBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * 'inputchange' event delegates to 'input', 'keyup', 'cut' and 'paste' events
	 * for text input form component depending on the browser.
	 * 'change' is used as a fallback for all other form component types.
	 */
	public static final String EVENT_NAME = "inputchange change";

	/**
	 * the change event
	 */
	public static final String EVENT_CHANGE = "change";

	/**
	 * Constructor.
	 */
	public OnChangeAjaxBehavior()
	{
		super(EVENT_NAME);
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) 
	{
		super.updateAjaxAttributes(attributes);

		Component component = getComponent();

		// textfiels and textareas will trigger this behavior with either 'inputchange' or 'change' events
		// all the other components will use just 'change'
		if (!(component instanceof TextField || component instanceof TextArea))
		{
			attributes.setEventNames(EVENT_CHANGE);
		}
	}

	/**
	 * Creates an {@link OnChangeAjaxBehavior} based on lambda expressions
	 * 
	 * @param onChange
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link OnChangeAjaxBehavior}
	 */
	public static OnChangeAjaxBehavior onChange(WicketConsumer<AjaxRequestTarget> onChange)
	{
		Args.notNull(onChange, "onChange");

		return new OnChangeAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onChange.accept(target);
			}
		};
	}

	/**
	 * Creates an {@link OnChangeAjaxBehavior} based on lambda expressions
	 * 
	 * @param onChange
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @param onError
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link RuntimeException}
	 * @return the {@link OnChangeAjaxBehavior}
	 */
	public static OnChangeAjaxBehavior onChange(WicketConsumer<AjaxRequestTarget> onChange,
	                                            WicketBiConsumer<AjaxRequestTarget, RuntimeException> onError)
	{
		Args.notNull(onChange, "onChange");
		Args.notNull(onError, "onError");

		return new OnChangeAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onChange.accept(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, RuntimeException e)
			{
				onError.accept(target, e);
			}
		};
	}

}
