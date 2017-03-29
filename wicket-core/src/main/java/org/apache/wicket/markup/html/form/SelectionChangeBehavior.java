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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class SelectionChangeBehavior extends Behavior implements IRequestListener
{

	private FormComponent<?> formComponent;

	@Override
	public boolean getStatelessHint(Component component)
	{
		return false;
	}
	
	@Override
	public void bind(Component component)
	{
		this.formComponent = (FormComponent<?>)component;
		
		formComponent.setRenderBodyOnly(false);
	}

	public FormComponent<?> getFormComponent()
	{
		return formComponent;
	}
	
	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		CharSequence url = component.urlForListener(this, new PageParameters());

		String event = getJSEvent();
		
		String condition = String.format("if (event.target.name !== '%s') return; ", formComponent.getInputName());
		
		Form<?> form = component.findParent(Form.class);
		if (form != null)
		{
			tag.put(event, condition + form.getJsForListenerUrl(url.toString()));
		}
		else
		{
			char separator = url.toString().indexOf('?') > -1 ? '&' : '?';
				
			tag.put(event,
				condition + String.format("window.location.href='%s%s%s=' + %s;", url, separator, formComponent.getInputName(), getJSValue()));
		}
	}

	
	private String getJSEvent()
	{
		if (formComponent instanceof DropDownChoice) {
			return "onchange";
		} else {
			return "onclick";
		}
	}

	private String getJSValue()
	{
		if (formComponent instanceof DropDownChoice) {
			return "this.options[this.selectedIndex].value";
		} else if (formComponent instanceof CheckBox) {
			return "this.checked";
		} else {
			return "event.target.value";
		}
	}

	private void process() {
		formComponent.convertInput();
		formComponent.updateModel();
		onSelectionChanged();
	}
	
	protected void onSelectionChanged()
	{
	}

	@Override
	public final void onRequest()
	{
		Form<?> form = formComponent.findParent(Form.class);
		if (form == null) {
			process();
		} else {
			form.getRootForm().onFormSubmitted(new IFormSubmitter()
			{
				@Override
				public void onSubmit()
				{
					process();
				}
				
				@Override
				public void onError()
				{
				}
				
				@Override
				public void onAfterSubmit()
				{
				}
				
				@Override
				public Form<?> getForm()
				{
					return formComponent.getForm();
				}
				
				@Override
				public boolean getDefaultFormProcessing()
				{
					return false;
				}
			});
		}
	}
}
