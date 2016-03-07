package org.apache.wicket.markup.html.pages;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;

/**
 * @author Rakesh.A
 */
public abstract class AbstractBrowserInfoPage <M extends ClientProperties, N extends AbstractBrowserInfoForm<M>> extends WebPage
{
	private N browserInfoForm;

	public AbstractBrowserInfoPage()
	{
		initComps();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isVersioned()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		BrowserInfoAttributes attrs = getAttributes();
		CharSequence script = attrs.getCallbackScript();

		response.render(OnLoadHeaderItem.forScript(script));
	}

	/**
	 * method creates and returns instance of sub-class of {@code AbstractBrowserInfoForm}.
	 * 
	 * @param formMarkupId String
	 * @param properties IModel<M> - where M is {@code ClientProperties} or its sub-class
	 * @return N instance of {@code AbstractBrowserInfoForm}'s sub-class
	 */
	protected abstract N createBrowserInfoForm(String formMarkupId, IModel<M> properties);

	/**
	 * @param attrs
	 *            BrowserInfoAttributes
	 */
	protected void updateBrowserInfoAttributes(BrowserInfoAttributes attrs)
	{
	}

	private BrowserInfoAttributes getAttributes()
	{
		BrowserInfoAttributes attrs = new BrowserInfoAttributes(this.browserInfoForm.getFormMarkupId());
		updateBrowserInfoAttributes(attrs);
		return attrs;
	}

	/**
	 * Adds components.
	 */
	private void initComps()
	{
		IModel<M> properties = new AbstractReadOnlyModel<M>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public M getObject()
			{
				WebClientInfo clientInfo = WebSession.get().getClientInfo();
				return (M)clientInfo.getProperties();
			}
		};

		add(new ContinueLink<M>("link", properties));

		browserInfoForm = createBrowserInfoForm("postback", properties);
		add(browserInfoForm);
	}

	private static class ContinueLink<M extends ClientProperties> extends Link<M> {

		public ContinueLink(String id, IModel<M> properties)
		{
			super(id, properties);
		}

		@Override
		public void renderHead(IHeaderResponse response)
		{
			String content = "0; url=" + getURL();

			response.render(new MetaDataHeaderItem(MetaDataHeaderItem.META_TAG).addTagAttribute("http-equiv", "refresh").addTagAttribute("content", content));
		}
		
		@Override
		public void onClick()
		{
			getModelObject().setJavaScriptEnabled(false);

			continueToOriginalDestination();

			// switch to home page if no original destination was intercepted
			setResponsePage(getApplication().getHomePage());
		}
	};
}
