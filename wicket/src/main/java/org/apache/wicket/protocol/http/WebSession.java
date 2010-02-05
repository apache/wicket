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

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.settings.IRequestCycleSettings;
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

	/**
	 * Filter that returns all component scoped messages ({@link FeedbackMessage#getReporter()} !=
	 * null).
	 */
	private static final IFeedbackMessageFilter MESSAGES_FOR_COMPONENTS = new IFeedbackMessageFilter()
	{
		private static final long serialVersionUID = 1L;

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

		public boolean accept(FeedbackMessage message)
		{
			return message.getReporter() == null && message.isRendered();
		}
	};

	/** True when the user is signed in */
	private volatile boolean signedIn;

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
	 * @see org.apache.wicket.Session#cleanupFeedbackMessages()
	 */
	@Override
	public void cleanupFeedbackMessages()
	{
		// remove all component feedback messages if we are either using one
		// pass or render to buffer render strategy (in which case we can remove
		// without further delay) or in case the redirect to render strategy is
		// used, when we're doing the render request (isRedirect should return
		// false in that case)

		// TODO NG - does this huge if really make sense?

		if (Application.get().getRequestCycleSettings().getRenderStrategy() != IRequestCycleSettings.RenderStrategy.REDIRECT_TO_RENDER ||
			((WebRequest)RequestCycle.get().getRequest()).isAjax() ||
			(!((WebResponse)RequestCycle.get().getResponse()).isRedirect()))
		{
			// If session scoped, rendered messages got indeed cleaned up, mark
			// the session as dirty
			if (getFeedbackMessages().clear(RENDERED_SESSION_SCOPED_MESSAGES) > 0)
			{
				dirty();
			}

			// see if any component related feedback messages were left unrendered and warn if in
			// dev mode
			if (Application.DEVELOPMENT.equals(getApplication().getConfigurationType()))
			{
				List<FeedbackMessage> messages = getFeedbackMessages().messages(
					WebSession.MESSAGES_FOR_COMPONENTS);
				for (FeedbackMessage message : messages)
				{
					if (!message.isRendered())
					{
						logger.warn(
							"Component-targetted feedback message was left unrendered. This could be because you are missing a FeedbackPanel on the page.  Message: {}",
							message);
					}
				}
			}

			cleanupComponentFeedbackMessages();
		}
	}

	/**
	 * Clear all feedback messages
	 */
	protected void cleanupComponentFeedbackMessages()
	{
		// clean up all component related feedback messages
		getFeedbackMessages().clear(WebSession.MESSAGES_FOR_COMPONENTS);
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
		signOut();

		getApplication().getSecuritySettings().getAuthenticationStrategy().remove();

		super.invalidate();
	}

	/**
	 * Try to logon the user. It'll call {@link #authenticate(String, String)} to do the real work
	 * and that is what you need to subclass to provide your own authentication mechanism.
	 * 
	 * @param username
	 * @param password
	 * @return true, if logon was successful
	 */
	public final boolean signIn(final String username, final String password)
	{
		return signedIn = authenticate(username, password);
	}

	/**
	 * @return true, if user is signed in
	 */
	public final boolean isSignedIn()
	{
		return signedIn;
	}

	/**
	 * Sign the user out.
	 */
	public void signOut()
	{
		signedIn = false;
	}

	/**
	 * Cookie based logins (remember me) may not rely on putting username and password into the
	 * cookie but something else that safely identifies the user. This method is meant to support
	 * these use cases.
	 * 
	 * It is protected (and not public) to enforce that cookie based authentication gets implemented
	 * in a subclass (like you need to subclass authenticate() for 'normal' authentication).
	 * 
	 * @see #authenticate(String, String)
	 * 
	 * @param value
	 */
	protected final void signIn(boolean value)
	{
		signedIn = value;
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
}