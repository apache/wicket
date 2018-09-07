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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.value.ValueMap;

/**
 * @author marrink
 */
public class AjaxFormSubmitTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Indicates form handled submit.
	 */
	public static final int FORM_SUBMIT = 1;

	/**
	 * Indicates form handled error.
	 */
	public static final int FORM_ERROR = 2;

	/**
	 * Indicates button handled submit.
	 */
	public static final int BUTTON_SUBMIT = 4;

	/**
	 * Indicates button handled error.
	 */
	public static final int BUTTON_ERROR = 8;

	private int formSubmitted;

	/**
	 * Returns the component(s) that handled the form submit.
	 * 
	 * @return flag indicating the component(s)
	 */
	public final int getFormSubmitted()
	{
		return formSubmitted;
	}

	/**
	 * Construct.
	 */
	public AjaxFormSubmitTestPage(boolean defaultProcessing)
	{
		super(new CompoundPropertyModel<ValueMap>(new ValueMap("txt1=foo,txt2=bar")));
		Form<?> form = new Form<Void>("form")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit()
			{
				formSubmitted = formSubmitted | FORM_SUBMIT;
			}

			@Override
			protected void onError()
			{
				formSubmitted = formSubmitted | FORM_ERROR;
			}
		};
		add(form);
		form.add(new TextField<String>("txt1").setRequired(true));
		form.add(new TextField<String>("txt2").setRequired(true));
		form.add(new AjaxFallbackButton("submit", form)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(Optional<AjaxRequestTarget> target)
			{
				assertEquals(target.isPresent(), getRequestCycle().find(AjaxRequestTarget.class).isPresent());
				formSubmitted = formSubmitted | BUTTON_SUBMIT;
			}

			protected void onError(Optional<AjaxRequestTarget> target) {
				assertEquals(target.isPresent(), getRequestCycle().find(AjaxRequestTarget.class).isPresent());

				formSubmitted = formSubmitted | BUTTON_ERROR;
			}

			protected void onAfterSubmit(Optional<AjaxRequestTarget> target) {
				assertEquals(target.isPresent(), getRequestCycle().find(AjaxRequestTarget.class).isPresent());
			}
		}.setDefaultFormProcessing(defaultProcessing));
	}


}
