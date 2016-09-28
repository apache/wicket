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
package org.apache.wicket.markup.html.pages;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.WebSession;

/**
 * This page uses a form post right after the page has loaded in the browser, using JavaScript or
 * alternative means to detect and pass on settings to the embedded form. The form submit method
 * updates this session's {@link org.apache.wicket.core.request.ClientInfo} object and then redirects to
 * the original location as was passed in as a URL argument in the constructor.
 * <p>
 * If JavaScript is not enabled in the browser, a "refresh" meta-header will initiate a get on this page to
 * continue with the original destination. As a fallback the user can click a link to do the same. 
 * <p>
 * This page is being used by the default implementation of {@link org.apache.wicket.Session#getClientInfo()},
 * which in turn uses
 * {@link org.apache.wicket.settings.RequestCycleSettings#getGatherExtendedBrowserInfo() a setting} to
 * determine whether this page should be redirected to (it does when it is true).
 * 
 * @author Eelco Hillenius
 */
public class BrowserInfoPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private BrowserInfoForm browserInfoForm;

	/**
	 * Bookmarkable constructor.
	 */
	public BrowserInfoPage()
	{
		initComps();
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(OnLoadHeaderItem.forScript(
				String.format("Wicket.BrowserInfo.submitForm('%s')", browserInfoForm.getFormMarkupId())));
	}

	@Override
	public boolean isVersioned()
	{
		return false;
	}

	/**
	 * Adds components.
	 */
	private void initComps()
	{
		IModel<ClientProperties> properties = new IModel<ClientProperties>()
		{
			@Override
			public ClientProperties getObject()
			{
				return WebSession.get().getClientInfo().getProperties();
			}
		};

		add(new ContinueLink("link", properties));

		browserInfoForm = new BrowserInfoForm("postback", properties)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void afterSubmit()
			{
				getModelObject().setJavaScriptEnabled(true);

				continueToOriginalDestination();

				// switch to home page if no original destination was intercepted
				setResponsePage(getApplication().getHomePage());
			}
		};
		add(browserInfoForm);
	}
	
	private static class ContinueLink extends Link<ClientProperties> {

		public ContinueLink(String id, IModel<ClientProperties> properties)
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
