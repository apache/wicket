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
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * This is a Ajax Component Update Behavior that is meant for choices/groups that are not one
 * component in the html but many.
 * <p>
 * Use the normal {@link AjaxFormComponentUpdatingBehavior} for the normal single component
 * fields
 * <p>
 * In order to be supported by this behavior the group components must output children with markup
 * id in format of 'groupId-childId'
 * 
 * @author jcompagner
 * 
 * @see RadioChoice
 * @see CheckBoxMultipleChoice
 * @see RadioGroup
 * @see CheckGroup
 */
public abstract class AjaxFormChoiceComponentUpdatingBehavior extends AbstractDefaultAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		// a function that registers a click listener which dynamically
		// extracts the currently selected checkboxes/radios on every click
		AppendingStringBuffer asb = new AppendingStringBuffer();
		asb.append("function attachChoiceHandlers(markupId, attrs) {\n");
		asb.append(" function getInputValues(groupId) {\n");
		asb.append("  var result = [];\n");
		asb.append("  var inputNodes = Wicket.$(groupId).getElementsByTagName('input');\n");
		asb.append("  for (var i = 0 ; i < inputNodes.length ; i ++) {\n");
		asb.append("   var inputNode = inputNodes[i];\n");
		asb.append("   if (!inputNode.checked) continue;\n");
		asb.append("   if (!inputNode.type) continue;\n");
		asb.append("   if (!(inputNode.className.indexOf('wicket-'+markupId)>=0)&&!(inputNode.id.indexOf(markupId+'-')>=0)) continue;\n");
		asb.append("   var inputType = inputNode.type.toLowerCase();\n");
		asb.append("   if (inputType === 'checkbox' || inputType === 'radio') {\n");
		asb.append("    var name = inputNode.name, value = inputNode.value;\n");
		asb.append("    result.push({ name: name, value: value });\n");
		asb.append("   }\n"); // if
		asb.append("  }\n");  // for
		asb.append("  return result;\n");
		asb.append(" }\n"); // function getInputValues()
		asb.append(" attrs.dep = [ function() { var deps = getInputValues(markupId); return deps; } ];\n");
		asb.append(" Wicket.Ajax.post(attrs);\n");
		asb.append("}\n"); // function attachChoiceHandlers()

		response.render(JavaScriptHeaderItem.forScript(asb, "attachChoice-" + component.getMarkupId()));

		String onLoadScript = String.format("attachChoiceHandlers('%s', %s)",
				component.getMarkupId(), renderAjaxAttributes(component));
		response.render(OnLoadHeaderItem.forScript(onLoadScript));

	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);
		attributes.setEventNames("click");
		attributes.setAllowDefault(true);
	}

	/**
	 * Listener invoked on the ajax request. This listener is invoked after the component's model
	 * has been updated.
	 * 
	 * @param target
	 *      the current request handler
	 */
	protected abstract void onUpdate(AjaxRequestTarget target);

	/**
	 * Called to handle any error resulting from updating form component. Errors thrown from
	 * {@link #onUpdate(org.apache.wicket.ajax.AjaxRequestTarget)} will not be caught here.
	 * 
	 * The RuntimeException will be null if it was just a validation or conversion error of the
	 * FormComponent
	 * 
	 * @param target
	 *      the current request handler
	 * @param e
	 *      the error that occurred while updating the component
	 */
	protected void onError(AjaxRequestTarget target, RuntimeException e)
	{
		if (e != null)
		{
			throw e;
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.behavior.AbstractAjaxBehavior#onBind()
	 */
	@Override
	protected void onBind()
	{
		super.onBind();

		if (!AjaxFormChoiceComponentUpdatingBehavior.appliesTo(getComponent()))
		{
			throw new WicketRuntimeException("Behavior " + getClass().getName() +
				" can only be added to an instance of a RadioChoice/CheckboxChoice/RadioGroup/CheckGroup");
		}

		if (getComponent() instanceof RadioGroup || getComponent() instanceof CheckGroup)
		{
			getComponent().setRenderBodyOnly(false);
		}
	}

	/**
	 * 
	 * @return FormComponent
	 */
	protected final FormComponent<?> getFormComponent()
	{
		return (FormComponent<?>)getComponent();
	}

	/**
	 * 
	 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#respond(org.apache.wicket.ajax.AjaxRequestTarget)
	 */
	@Override
	protected final void respond(final AjaxRequestTarget target)
	{
		final FormComponent<?> formComponent = getFormComponent();

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

	/**
	 * @param component
	 *      the component to check
	 * @return if the component applies to the {@link AjaxFormChoiceComponentUpdatingBehavior}
	 */
	static boolean appliesTo(Component component)
	{
		return (component instanceof RadioChoice) ||
			(component instanceof CheckBoxMultipleChoice) || (component instanceof RadioGroup) ||
			(component instanceof CheckGroup);
	}
}
