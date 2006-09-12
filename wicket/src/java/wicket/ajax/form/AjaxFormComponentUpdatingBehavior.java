/*
 * $Id: AjaxFormComponentUpdatingBehavior.java,v 1.4 2006/02/02 18:49:46
 * ivaynberg Exp $ $Revision$ $Date: 2006-03-09 01:08:00 -0800 (Thu, 09
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

import wicket.WicketRuntimeException;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.ClientEvent;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.persistence.IValuePersister;
import wicket.util.string.AppendingStringBuffer;

/**
 * A behavior that updates the hosting FormComponent via ajax when an event it
 * is attached to is triggered. This behavior encapsulates the entire
 * form-processing workflow as relevant only to this component so if validation
 * is successfull the component's model will be updated according to the
 * submitted value.
 * <p>
 * NOTE: This behavior does not support persisting form component values into
 * cookie or other {@link IValuePersister}. If this is necessary please add a
 * request for enhancement.
 * <p>
 * NOTE: This behavior does not validate any {@link IFormValidator}s attached
 * to this form even though they may reference the component being updated.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AjaxFormComponentUpdatingBehavior extends AjaxEventBehavior
{
	/**
	 * Construct.
	 * 
	 * @param event
	 *            event to trigger this behavior
	 */
	public AjaxFormComponentUpdatingBehavior(final ClientEvent event)
	{
		super(event);
	}

	/**
	 * 
	 * @see wicket.behavior.AbstractAjaxBehavior#onBind()
	 */
	@Override
	protected void onBind()
	{
		super.onBind();

		if (!(getComponent() instanceof FormComponent))
		{
			throw new WicketRuntimeException("Behavior " + getClass().getName()
					+ " can only be added to an isntance of a FormComponent");
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
	 * @see wicket.ajax.AjaxEventBehavior#getEventHandler()
	 */
	@Override
	protected final CharSequence getEventHandler()
	{
		return getCallbackScript(new AppendingStringBuffer("wicketAjaxPost('").append(
				getCallbackUrl()).append(
				"', wicketSerialize(document.getElementById('" + getComponent().getMarkupId()
						+ "'))"), null, null);
	}

	/**
	 * @see wicket.ajax.AjaxEventBehavior#onCheckEvent(java.lang.String)
	 */
	@Override
	protected void onCheckEvent(ClientEvent event)
	{
		if (event == ClientEvent.HREF)
		{
			throw new IllegalArgumentException(
					"this behavior cannot be attached to an 'href' event");
		}
	}

	/**
	 * 
	 * @see wicket.ajax.AjaxEventBehavior#onEvent(wicket.ajax.AjaxRequestTarget)
	 */
	@Override
	protected final void onEvent(final AjaxRequestTarget target)
	{
		final FormComponent formComponent = getFormComponent();
		boolean callOnUpdate = true;

		try
		{
			formComponent.inputChanged();
			formComponent.validate();
			if (formComponent.hasErrorMessage())
			{
				formComponent.invalid();
			}
			else
			{
				formComponent.valid();
				formComponent.updateModel();
			}
		}
		catch (RuntimeException e)
		{
			callOnUpdate = false;
			onError(target, e);
		}

		if (callOnUpdate)
		{
			onUpdate(target);
		}
	}

	/**
	 * Called to handle any error resulting from updating form component. Errors
	 * thrown from {@link #onUpdate(AjaxRequestTarget)} will not be caught here.
	 * 
	 * @param target
	 * @param e
	 */
	private void onError(AjaxRequestTarget target, RuntimeException e)
	{
		throw e;
	}

	/**
	 * Listener invoked on the ajax request. This listener is invoked after the
	 * component's model has been updated.
	 * 
	 * @param target
	 */
	protected abstract void onUpdate(final AjaxRequestTarget target);
}
