/**
 * 
 */
package org.apache.wicket.markup.html.pages;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * @author Rakesh.A
 * 
 * @param <M> ClientPropertiesBean
 * @param <N> WebClientInfo
 */
public abstract class AbstractBrowserInfoForm<M extends ClientPropertiesBean, N extends WebClientInfo> extends Panel
{
	private static final ResourceReference BROWSER_INFO_JS = new JavaScriptResourceReference(AbstractBrowserInfoForm.class, "wicket-browser-info.js");

	/**
	 * The special form that submits the client/browser info
	 */
	private final Form<M> form;

	/**
	 * @param id
	 */
	public AbstractBrowserInfoForm(String id)
	{
		super(id);

		this.form = createForm("postback");
		form.setOutputMarkupId(true);
		add(form);
	}

	/**
	 * @return The markup id of the form that submits the client info
	 */
	public final String getFormMarkupId()
	{
		return form.getMarkupId();
	}

	/**
	 * Getter method to access the {@code Form} component instance.
	 * 
	 * @return Form<M>
	 */
	protected final Form<M> getForm()
	{
		return this.form;
	}

	/**
	 * method creates and returns {@code CompoundPropertyModel} instance to be used for the
	 * {@code Form}
	 * 
	 * @return CompoundPropertyModel<M>
	 */
	protected abstract CompoundPropertyModel<M> createFormModel();

	/**
	 * method executed on-submit event.
	 * 
	 * @param propertiesBean ClientPropertiesBean
	 */
	@SuppressWarnings("unchecked")
	private void doOnSubmit(M propertiesBean)
	{
		WebSession session = getWebSession();
		N clientInfo = (N) session.getClientInfo();

		if (clientInfo == null)
		{
			clientInfo = WebSession.get().createClientInfo(RequestCycle.get());
			getSession().setClientInfo(clientInfo);
		}

		ClientProperties properties = clientInfo.getProperties();
		propertiesBean.merge(properties);

		afterSubmit(clientInfo, propertiesBean);
	}

	/**
	 * method which will be called on-submit event, and is for sub-classes to add and execute
	 * functionality.
	 * 
	 * @param clientInfo
	 * @param propertiesBean
	 */
	protected void afterSubmit(N clientInfo, M propertiesBean)
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(BROWSER_INFO_JS));
	}

	/**
	 * Creates the form
	 *
	 * @param componentId
	 *            the id for the Form component
	 * @return the Form that will submit the data
	 */
	protected Form<M> createForm(String componentId)
	{
		Form<M> form = new Form<M>(componentId, createFormModel())
		{
			@Override
			protected void onSubmit()
			{
				doOnSubmit(getModelObject());
			}
		};

		form.add(new TextField<String>("navigatorAppName"));
		form.add(new TextField<String>("navigatorAppVersion"));
		form.add(new TextField<String>("navigatorAppCodeName"));
		form.add(new TextField<Boolean>("navigatorCookieEnabled"));
		form.add(new TextField<Boolean>("navigatorJavaEnabled"));
		form.add(new TextField<String>("navigatorLanguage"));
		form.add(new TextField<String>("navigatorPlatform"));
		form.add(new TextField<String>("navigatorUserAgent"));
		form.add(new TextField<String>("screenWidth"));
		form.add(new TextField<String>("screenHeight"));
		form.add(new TextField<String>("screenColorDepth"));
		form.add(new TextField<String>("utcOffset"));
		form.add(new TextField<String>("utcDSTOffset"));
		form.add(new TextField<String>("browserWidth"));
		form.add(new TextField<String>("browserHeight"));
		form.add(new TextField<String>("hostname"));

		return form;
	}

}
