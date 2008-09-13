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
package org.apache._wicket.ajax.markup.html.form;

import org.apache._wicket.ajax.AjaxRequestAttributes;
import org.apache._wicket.ajax.AjaxRequestTarget;
import org.apache._wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;

public abstract class AjaxButton extends Button
{

	private static final long serialVersionUID = 1L;

	public AjaxButton(String id)
	{
		this(id, null);
	}

	public AjaxButton(String id, Form<?> form)
	{
		super(id);
		
		add(new AjaxFormSubmitBehavior(form, "click") 
		{

			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				AjaxButton.this.onSubmit(target,getForm());
			}
			@Override
			protected void onError(AjaxRequestTarget target)
			{
				AjaxButton.this.onError(target, getForm());
			}
			@Override
			protected void updateAttributes(AjaxRequestAttributes attributes, Component component)
			{
				super.updateAttributes(attributes, component);
				AjaxButton.this.updateAttributes(attributes);
			}
		});
	}
	
	protected void updateAttributes(AjaxRequestAttributes attributes)
	{
		
	}
	
	@Override
	public final void onSubmit()
	{
		if (AjaxRequestTarget.get() == null)
		{
			onSubmit(null, getForm());
		}
	}
	
	protected abstract void onSubmit(AjaxRequestTarget target, Form<?> form);
	
	protected void onError(AjaxRequestTarget target, Form<?> form)
	{
		
	}
	

}
