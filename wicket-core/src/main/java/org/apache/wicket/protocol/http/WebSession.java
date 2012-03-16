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
package org.apache.wicket.protocol.http;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.pages.BrowserInfoPage;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A session subclass for the HTTP protocol.
 * 
 * @author Jonathan Locke
 */
public class WebSession extends Session
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(WebSession.class);

	public static WebSession get()
	{
		return (WebSession)Session.get();
	}

	/**
	 * Filter that returns all component scoped messages ({@link FeedbackMessage#getReporter()} !=
	 * null).
	 */
	private static final IFeedbackMessageFilter MESSAGES_FOR_COMPONENTS = new IFeedbackMessageFilter()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean accept(FeedbackMessage message)
		{
			return message.getReporter() != null;
		}
	};

	/**
	 * Filter that returns all session scoped messages ({@link FeedbackMessage#getReporter()} ==
	 * null).
	 */
	private static final IFeedbackMessageFilter RENDERED_SESSION_SCOPED_MESSAGES = new IFeedbackMessageFilter()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean accept(FeedbackMessage message)
		{
			return message.getReporter() == null && message.isRendered();
		}
	};

	private static final MetaDataKey<Boolean> BROWSER_WAS_POLLED_KEY = new MetaDataKey<Boolean>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Constructor. Note that {@link RequestCycle} is not available until this constructor returns.
	 * 
	 * @param request
	 *            The current request
	 */
	public WebSession(Request request)
	{
		super(request);
	}

	/**
	 * Call signOut() and remove the logon data from whereever they have been persisted (e.g.
	 * Cookies)
	 * 
	 * @see org.apache.wicket.Session#invalidate()
	 */
	@Override
	public void invalidate()
	{
		if (isSessionInvalidated() == false)
		{
			getApplication().getSecuritySettings().getAuthenticationStrategy().remove();

			super.invalidate();
		}
	}

	/**
	 * Note: You must subclass WebSession and implement your own. We didn't want to make it abstract
	 * to force every application to implement it. Instead we throw an exception.
	 * 
	 * @param username
	 *            The username
	 * @param password
	 *            The password
	 * @return True if the user was authenticated successfully
	 */
	public boolean authenticate(final String username, final String password)
	{
		throw new WicketRuntimeException(
			"You must subclass WebSession and implement your own authentication method for all Wicket applications using authentication.");
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * To gather the client information this implementation redirects temporarily to a special page
	 * ({@link BrowserInfoPage}).
	 * <p>
	 * Note: Do <strong>not</strong> call this method from your custom {@link Session} constructor
	 * because the temporary page needs a constructed {@link Session} to be able to work.
	 * <p>
	 * If you need to set a default client info property then better use
	 * {@link #setClientInfo(org.apache.wicket.core.request.ClientInfo)} directly.
	 */
	@Override
	public WebClientInfo getClientInfo()
	{
		if (clientInfo == null)
		{
			RequestCycle requestCycle = RequestCycle.get();

			if (getApplication().getRequestCycleSettings().getGatherExtendedBrowserInfo())
			{
				if (getMetaData(BROWSER_WAS_POLLED_KEY) == null)
				{
					// we haven't done the redirect yet; record that we will be
					// doing that now and redirect
					setMetaData(BROWSER_WAS_POLLED_KEY, Boolean.TRUE);

					WebPage browserInfoPage = newBrowserInfoPage();
					getPageManager().touchPage(browserInfoPage);
					throw new RestartResponseAtInterceptPageException(browserInfoPage);
				}
				// if we get here, the redirect already has been done; clear
				// the meta data entry; we don't need it any longer is the client
				// info object will be cached too
				setMetaData(BROWSER_WAS_POLLED_KEY, null);
			}
			clientInfo = new WebClientInfo(requestCycle);
		}
		return (WebClientInfo)clientInfo;
	}

	/**
	 * Override this method if you want to use a custom page for gathering the client's browser
	 * information.<br/>
	 * The easiest way is just to extend {@link BrowserInfoPage} and provide your own markup file
	 * 
	 * @return the {@link WebPage} which should be used while gathering browser info
	 */
	protected WebPage newBrowserInfoPage()
	{
		return new BrowserInfoPage();
	}
}