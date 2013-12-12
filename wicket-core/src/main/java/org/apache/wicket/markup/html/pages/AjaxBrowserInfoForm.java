package org.apache.wicket.markup.html.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.core.request.ClientInfo;
import org.apache.wicket.markup.html.form.Form;

/**
 * An extension of BrowserInfoForm that uses AjaxFormSubmitBehavior
 * to collect the client info without redirect to another page
 */
public class AjaxBrowserInfoForm extends BrowserInfoForm
{
	/**
	 * Constructor.
	 *
	 * @param id component id
	 */
	public AjaxBrowserInfoForm(String id)
	{
		super(id);
	}

	@Override
	protected Form<? extends ClientPropertiesBean> createForm(String componentId)
	{
		Form<? extends ClientPropertiesBean> f = super.createForm(componentId);
		f.add(new AjaxFormSubmitBehavior(f, "domready")
		{
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				AjaxCallListener listener = new AjaxCallListener();
				listener.onBefore(String.format("Wicket.BrowserInfo.populateFields('%s');", getFormMarkupId()));
				attributes.getAjaxCallListeners().add(listener);
			}

			@Override
			protected void onAfterSubmit(AjaxRequestTarget target)
			{
				super.onAfterSubmit(target);
				ClientInfo info = getSession().getClientInfo();
				AjaxBrowserInfoForm.this.onAfterSubmit(target, info);
			}
		});
		return f;
	}

	/**
	 * A callback method called when the client info is collected
	 *
	 * @param target
	 *      The Ajax request handler
	 * @param info
	 *      The client info
	 */
	protected void onAfterSubmit(AjaxRequestTarget target, ClientInfo info)
	{
	}

	/**
	 * Does nothing.
	 *
	 * Use {@linkplain #onAfterSubmit(org.apache.wicket.ajax.AjaxRequestTarget, org.apache.wicket.core.request.ClientInfo)}
	 */
	@Override
	protected final void afterSubmit()
	{
		super.afterSubmit();
	}
}
