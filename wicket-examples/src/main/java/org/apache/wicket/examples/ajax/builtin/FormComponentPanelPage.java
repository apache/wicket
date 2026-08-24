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
package org.apache.wicket.examples.ajax.builtin;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.examples.forminput.Multiply;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * FormComponentPanel with Ajax example
 */
public class FormComponentPanelPage extends BasePage
{
	/**
	 * The result of the multiplication.
	 */
	private final IModel<Integer> result = Model.of(0);

	/**
	 * Constructor.
	 */
	public FormComponentPanelPage()
	{
		Form<?> form = new Form("form");
		add(form);

		Multiply multiply = new Multiply("multiply", result);
		Component multiplyLabel = new Label("multiplyLabel", result)
				.setOutputMarkupId(true);
		multiply.add(new AjaxFormComponentUpdatingBehavior("input change")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				target.add(multiplyLabel);
			}

			@Override
			protected void onError(AjaxRequestTarget target, RuntimeException e)
			{
				result.setObject(null);
				target.add(multiplyLabel);
			}
		});
		form.add(multiply);
		// display the multiply result
		form.add(multiplyLabel);
	}
}
