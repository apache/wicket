/*
 * $Id: AjaxFormSubmitBehavior.java 5113 2006-03-25 01:48:00 -0800 (Sat, 25 Mar
 * 2006) ivaynberg $ $Revision$ $Date: 2006-03-25 01:48:00 -0800 (Sat, 25
 * Mar 2006) $
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

import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.ClientEvent;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.util.string.AppendingStringBuffer;

/**
 * Ajax event behavior that submits a form via ajax when the event it is
 * attached to is invoked.
 * <p>
 * The form must have an id attribute in the markup or have MarkupIdSetter
 * added.
 * 
 * @see AjaxEventBehavior
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AjaxFormSubmitBehavior extends AjaxEventBehavior
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Form form;


	/**
	 * Construct.
	 * 
	 * @param form
	 *            form that will be submitted
	 * @param event
	 *            javascript event this behavior is attached to, like onclick
	 */
	public AjaxFormSubmitBehavior(Form form, ClientEvent event)
	{
		super(event);
		this.form = form;
	}

	@Override
	protected CharSequence getEventHandler()
	{
		final String formId = form.getMarkupId();
		final CharSequence url = getCallbackUrl();


		AppendingStringBuffer call = new AppendingStringBuffer("wicketSubmitFormById('").append(
				formId).append("', '").append(url).append("', ");

		if (getComponent() instanceof Button)
		{
			call.append("'").append(((FormComponent)getComponent()).getInputName()).append("' ");
		}
		else
		{
			call.append("null");
		}

		return getCallbackScript(call, null, null) + ";";
	}

	@Override
	protected void onEvent(AjaxRequestTarget target)
	{
		form.onFormSubmitted();
		if (!form.hasError())
		{
			onSubmit(target);
		}
		else
		{
			onError(target);
		}
	}

	/**
	 * Listener method that is invoked after the form has ben submitted and
	 * processed without errors
	 * 
	 * @param target
	 */
	protected abstract void onSubmit(AjaxRequestTarget target);

	/**
	 * Listener method invoked when the form has been processed and errors
	 * occured
	 * 
	 * @param target
	 * 
	 * TODO 1.3: make abstract to be consistent with onsubmit()
	 * 
	 */
	protected void onError(AjaxRequestTarget target)
	{

	}

}
