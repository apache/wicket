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

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.ClientInfo;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * This page uses a form post right after the page has loaded in the browser, using JavaScript or
 * alternative means to detect and pass on settings to the embedded form. The form submit method
 * updates this session's {@link org.apache.wicket.request.ClientInfo} object and then redirects to
 * the original location as was passed in as a URL argument in the constructor.
 * </p>
 * <p>
 * This page is being used by the default implementation of {@link WebRequestCycle#newClientInfo},
 * which in turn uses {@link IRequestCycleSettings#getGatherExtendedBrowserInfo() a setting} to
 * determine whether this page should be redirected to (it does when it is true).
 * </p>
 *
 * @author Eelco Hillenius
 */
public class BrowserInfoPage extends WebPage
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(BrowserInfoPage.class);

	private static final long serialVersionUID = 1L;

	/** the url to continue to after this page. */
	private String continueTo;

	/**
	 * Bookmarkable constructor. This is not for normal framework client use. It will be called
	 * whenever Javascript is not supported, and the browser info page's meta refresh fires to this
	 * page. Prior to this, the other constructor should already have been called.
	 *
	 * @param parameters
	 *            page parameters with the original url in it
	 */
	public BrowserInfoPage(PageParameters parameters)
	{
		String to = Strings.toString(parameters.get("cto"));
		if (to == null)
		{
			throw new IllegalArgumentException("parameter cto must be provided!");
		}
		setContinueTo(to);
		initComps();
		WebRequestCycle requestCycle = (WebRequestCycle)getRequestCycle();
		WebSession session = (WebSession)getSession();
		ClientInfo clientInfo = session.getClientInfo();
		if (clientInfo == null)
		{
			clientInfo = new WebClientInfo(requestCycle);
			getSession().setClientInfo(clientInfo);
		}
		else if (clientInfo instanceof WebClientInfo)
		{
			WebClientInfo info = (WebClientInfo)clientInfo;
			ClientProperties properties = info.getProperties();
			properties.setJavaEnabled(false);
		}
		else
		{
			warnNotUsingWebClientInfo(clientInfo);
		}
		continueToPrevious();
	}

	/**
	 * Constructor. The page will redirect to the given url after waiting for the given number of
	 * seconds.
	 *
	 * @param continueTo
	 *            the url to redirect to when the browser info is handled
	 */
	public BrowserInfoPage(final String continueTo)
	{
		if (continueTo == null)
		{
			throw new IllegalArgumentException("Argument continueTo must be not null");
		}
		setContinueTo(continueTo);
		initComps();
	}

	/**
	 * @see org.apache.wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return false;
	}

	/**
	 * Adds components.
	 */
	private final void initComps()
	{
		WebComponent meta = new WebComponent("meta");
		PageParameters parameters = new PageParameters();
		parameters.put("cto", continueTo);
		CharSequence url = urlFor(new BookmarkablePageRequestTarget(BrowserInfoPage.class,
				parameters));
		meta.add(new AttributeModifier("content", true, new Model("0; url=" + url)));
		add(meta);
		WebMarkupContainer link = new WebMarkupContainer("link");
		link.add(new AttributeModifier("href", true, new Model((Serializable)url)));
		add(link);
		add(new BrowserInfoForm("postback")
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.markup.html.pages.BrowserInfoForm#afterSubmit()
			 */
			protected void afterSubmit()
			{
				continueToPrevious();
			}
		});
	}

	/**
	 * Continue to the location previous to this interception.
	 */
	protected final void continueToPrevious()
	{
		// continue to original destination
		RequestCycle.get().setRequestTarget(new RedirectRequestTarget(continueTo));
	}

	/**
	 * Log a warning that for in order to use this page, you should really be using
	 * {@link WebClientInfo}.
	 *
	 * @param clientInfo
	 *            the actual client info object
	 */
	void warnNotUsingWebClientInfo(ClientInfo clientInfo)
	{
		log.warn("using " + getClass().getName() + " makes no sense if you are not using " +
				WebClientInfo.class.getName() + " (you are using " +
				clientInfo.getClass().getName() + " instead)");
	}

	/**
	 * Set the url to continue to after this page.
	 *
	 * @param continueTo
	 *            the url
	 */
	protected final void setContinueTo(String continueTo)
	{
		this.continueTo = continueTo;
	}
}
