package org.apache.wicket.markup.html.pages;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * @author Rakesh.A
 */
public abstract class AbstractBrowserInfoPage <M extends WebClientInfo, N extends AbstractBrowserInfoForm<? extends ClientPropertiesBean, M>> extends WebPage
{
	private N browserInfoForm;

	public AbstractBrowserInfoPage()
	{
		initComps();

		initClientInfo();

		continueToOriginalDestination();
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

	protected abstract N createBrowserInfoForm(String formMarkupId);

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

	@SuppressWarnings("unchecked")
	private void initClientInfo()
	{
		WebSession session = WebSession.get();
		M clientInfo = (M) session.getClientInfo();

		if (clientInfo == null)
		{
			clientInfo = WebSession.get().createClientInfo(RequestCycle.get());
			session.setClientInfo(clientInfo);
		}
		else
		{
			ClientProperties properties = clientInfo.getProperties();
			properties.setJavaEnabled(false);
		}
	}

	/**
	 * Adds components.
	 */
	private void initComps()
	{
		WebComponent meta = new WebComponent("meta");

		final IModel<String> urlModel = new LoadableDetachableModel<String>()
		{
			@Override
			protected String load()
			{
				CharSequence url = urlFor(BrowserInfoPage.class, null);
				return url.toString();
			}
		};

		meta.add(AttributeModifier.replace("content", new AbstractReadOnlyModel<String>()
		{
			@Override
			public String getObject()
			{
				return "0; url=" + urlModel.getObject();
			}

		}));
		add(meta);
		WebMarkupContainer link = new WebMarkupContainer("link");
		link.add(AttributeModifier.replace("href", urlModel));
		add(link);

		browserInfoForm = createBrowserInfoForm("postback");
		add(browserInfoForm);
	}
}
