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
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 * A behavior that updates the hosting {@link FormComponent} via ajax when value of the component is
 * changed.
 * 
 * This behavior uses best available method to track changes on different types of form components.
 * 
 * @author Janne Hietam&auml;ki (janne)
 * 
 * @since 1.3
 * @see AjaxFormComponentUpdatingBehavior
 */
public abstract class OnChangeAjaxBehavior extends AjaxFormComponentUpdatingBehavior
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public OnChangeAjaxBehavior()
	{
		super("onchange");
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		if (component instanceof AbstractTextComponent)
		{
			final String id = getComponent().getMarkupId();
			response.renderOnDomReadyJavaScript("new Wicket.ChangeHandler('" + id + "');");
		}
	}
}
