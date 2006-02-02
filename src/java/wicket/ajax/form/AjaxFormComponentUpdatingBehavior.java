/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.ajax.form;

import wicket.WicketRuntimeException;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.markup.html.form.FormComponent;
import wicket.util.string.AppendingStringBuffer;

/**
 * 
 */
public abstract class AjaxFormComponentUpdatingBehavior extends AjaxEventBehavior
{
	/**
	 * Construct.
	 * 
	 * @param event
	 */
	public AjaxFormComponentUpdatingBehavior(final String event)
	{
		super(event);
	}

	/**
	 * 
	 * @see wicket.behavior.AbstractAjaxBehavior#onBind()
	 */
	protected void onBind()
	{
		if (!(getComponent() instanceof FormComponent))
		{
			throw new WicketRuntimeException("Behavior " + getClass().getName()
					+ " can only be added to an isntance of a FormComponent");
		}
	}

	/**
	 * 
	 * @see wicket.ajax.AjaxEventBehavior#onCheckEvent(java.lang.String)
	 */
	protected void onCheckEvent(final String event)
	{
		// TODO check event
	}

	/**
	 * 
	 * @return FormComponent
	 */
	protected FormComponent getFormComponent()
	{
		return (FormComponent)getComponent();
	}

	/**
	 * 
	 * @see wicket.ajax.AjaxEventBehavior#getEventHandler()
	 */
	protected String getEventHandler()
	{
		FormComponent fc = getFormComponent();

		String url = getCallbackUrl();
		AppendingStringBuffer buff = new AppendingStringBuffer(url.length() + 128);
		buff.append("wicketAjaxGet('");
		buff.append(url);
		buff.append("&");
		buff.append(fc.getInputName());
		buff.append("='+");
		buff.append("wicketGetValue(this));");

		return buff.toString();
	}

	/**
	 * 
	 * @see wicket.ajax.AjaxEventBehavior#onEvent(wicket.ajax.AjaxRequestTarget)
	 */
	protected final void onEvent(final AjaxRequestTarget target)
	{
		FormComponent fc = getFormComponent();
		fc.registerNewUserInput();
		fc.validate();
		if (fc.hasErrorMessage())
		{
			fc.invalid();
		}
		else
		{
			fc.valid();
			fc.updateModel();
			// TODO Ajax: do we need to persist values for persistent components
		}

		onUpdate(target);
	}

	protected abstract void onUpdate(final AjaxRequestTarget target);
}
