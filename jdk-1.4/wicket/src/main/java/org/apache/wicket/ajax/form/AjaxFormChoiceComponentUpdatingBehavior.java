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

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * @author jcompagner
 *
 */
public abstract class AjaxFormChoiceComponentUpdatingBehavior extends AbstractDefaultAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public AjaxFormChoiceComponentUpdatingBehavior()
	{
		super();
	}

	/**
	 * @see wicket.ajax.AbstractDefaultAjaxBehavior#renderHead(wicket.markup.html.IHeaderResponse)
	 */
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		
		AppendingStringBuffer asb = new AppendingStringBuffer();
		asb.append("function attachChoiceHandlers(markupid, callbackscript) {\n");
		asb.append(" var choiceElement = document.getElementById(markupid);\n");
		asb.append(" for( var x = 0; x < choiceElement.childNodes.length; x++ ) {\n");
		asb.append("   if (choiceElement.childNodes[x] && choiceElement.childNodes[x].tagName) {\n");
		asb.append("     var tag = choiceElement.childNodes[x].tagName.toLowerCase();\n");
		asb.append("     if (tag == 'input') {\n");
		asb.append("       Wicket.Event.add(choiceElement.childNodes[x],'click', callbackscript);");
		asb.append("     }\n");		
		asb.append("   }\n");
		asb.append(" }\n");
		asb.append("}\n");
		
		response.renderJavascript(asb, "attachChoice");

		response.renderOnLoadJavascript("attachChoiceHandlers('" + getComponent().getMarkupId()
				+ "', function() {" + getEventHandler() + "});");

	}
	
	/**
	 * Listener invoked on the ajax request. This listener is invoked after the
	 * component's model has been updated.
	 * 
	 * @param target
	 */
	protected abstract void onUpdate(AjaxRequestTarget target);
	
	/**
	 * Called to handle any error resulting from updating form component. Errors
	 * thrown from {@link #onUpdate(AjaxRequestTarget)} will not be caught here.
	 * 
	 * The RuntimeException will be null if it was just a validation or conversion 
	 * error of the FormComponent
	 * 
	 * @param target
	 * @param e
	 */
	protected void onError(AjaxRequestTarget target, RuntimeException e)
	{
		if(e != null)
		{
			throw e;
		}
	}


	/**
	 * 
	 * @see wicket.behavior.AbstractAjaxBehavior#onBind()
	 */
	protected void onBind()
	{
		super.onBind();


		if (!(getComponent() instanceof RadioChoice) &&
				!(getComponent() instanceof CheckBoxMultipleChoice) &&
				!(getComponent() instanceof RadioGroup) &&
				!(getComponent() instanceof CheckGroup))
		{
			throw new WicketRuntimeException("Behavior " + getClass().getName()
					+ " can only be added to an instance of a RadioChoice/CheckboxChoice/RadioGroup/CheckGroup");
		}

		if (getComponent() instanceof RadioGroup || getComponent() instanceof CheckGroup)
		{
			getComponent().setRenderBodyOnly(false);
			IModel model = getComponent().getModel();
			if (model == null)
			{
				getComponent().setModel(new Model(null));
			}
		}
	}

	/**
	 * 
	 * @return FormComponent
	 */
	protected final FormComponent getFormComponent()
	{
		return (FormComponent)getComponent();
	}

	/**
	 * @return event handler
	 */
	protected final CharSequence getEventHandler()
	{
		return generateCallbackScript(new AppendingStringBuffer("wicketAjaxPost('").append(
				getCallbackUrl()).append(
				"', wicketSerializeForm(document.getElementById('" + getComponent().getMarkupId()
						+ "',false))"));
	}

	/**
	 * 
	 * @see wicket.ajax.AbstractDefaultAjaxBehavior#respond(wicket.ajax.AjaxRequestTarget)
	 */
	protected final void respond(final AjaxRequestTarget target)
	{
		final FormComponent formComponent = getFormComponent();

		try
		{
			formComponent.inputChanged();
			formComponent.validate();
			if (formComponent.hasErrorMessage())
			{
				formComponent.invalid();
				
				onError(target, null);
			}
			else
			{
				formComponent.valid();
				formComponent.updateModel();
				onUpdate(target);
			}
		}
		catch (RuntimeException e)
		{
			onError(target, e);

		}
	}
}
