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
package wicket.ajax.form;

import wicket.Component;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
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

	private Component owner;

	private Form form;

	/**
	 * Constructor. This constructor can only be used when the component this
	 * behavior is attached to is inside a form.
	 * 
	 * @param event
	 *            javascript event this behavior is attached to, like onclick
	 */
	public AjaxFormSubmitBehavior(String event)
	{
		this(null, event);
	}

	/**
	 * Construct.
	 * 
	 * @param form
	 *            form that will be submitted
	 * @param event
	 *            javascript event this behavior is attached to, like onclick
	 */
	public AjaxFormSubmitBehavior(Form form, String event)
	{
		super(event);
		this.form = form;
	}

	private Form getForm()
	{
		if (form == null)
		{
			// try to find form in the hierarchy of owning component
			Component cursor = getComponent();
			while (cursor != null && !(cursor instanceof Form))
			{
				cursor = cursor.getParent();
			}
			if (cursor == null)
			{
				throw new IllegalStateException(
						"form was not specified in the constructor and cannot "
								+ "be found in the hierarchy of the component this behavior "
								+ "is attached to");
			}
			else
			{
				form = (Form)cursor;
			}
		}
		return form;
	}


	protected CharSequence getEventHandler()
	{
		final String formId = getForm().getMarkupId();
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

	protected void onEvent(AjaxRequestTarget target)
	{
		getForm().onFormSubmitted();
		if (!getForm().hasError())
		{
			onSubmit(target);
		}
		if (getForm().hasError())
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
