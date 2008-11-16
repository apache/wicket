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
import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.util.string.Strings;
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

	/**
	 * Constructor. Note that {@link RequestCycle} is not available until this constructor returns.
	 * 
	 * @deprecated Use #WebSession(Request) instead
	 * 
	 * @param application
	 *            The application
	 * @param request
	 *            The current request
	 */
	@Deprecated
	public WebSession(final Application application, Request request)
	{
		super(application, request);
	}

	/**
	 * Constructor. Note that {@link RequestCycle} is not available until this constructor returns.
	 * 
	 * @deprecated Use #WebSession(Request)
	 * 
	 * @param application
	 *            The application
	 * @param request
	 *            The current request
	 */
	@Deprecated
	public WebSession(final WebApplication application, Request request)
	{
		super(application, request);
	}

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
	 * @see org.apache.wicket.Session#isCurrentRequestValid(org.apache.wicket.RequestCycle)
	 */
	@Override
	protected boolean isCurrentRequestValid(RequestCycle lockedRequestCycle)
	{
		WebRequest lockedRequest = (WebRequest)lockedRequestCycle.getRequest();

		// if the request that's holding the lock is ajax, we allow this request
		if (lockedRequest.isAjax() == true)
		{
			return true;
		}

		RequestCycle currentRequestCycle = RequestCycle.get();
		WebRequest currentRequest = (WebRequest)currentRequestCycle.getRequest();

		if (currentRequest.isAjax() == false)
		{
			// if this request is not ajax, we allow it
			return true;
		}

		String lockedPageId = Strings.firstPathComponent(lockedRequest.getRequestParameters()
			.getComponentPath(), Component.PATH_SEPARATOR);
		String currentPageId = Strings.firstPathComponent(currentRequestCycle.getRequest()
			.getRequestParameters()
			.getComponentPath(), Component.PATH_SEPARATOR);

		int lockedVersion = lockedRequest.getRequestParameters().getVersionNumber();
		int currentVersion = currentRequest.getRequestParameters().getVersionNumber();

		if (currentPageId.equals(lockedPageId) && currentVersion == lockedVersion)
		{
			// we don't allow this request
			return false;
		}

		return true;
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
		if (Application.get().getRequestCycleSettings().getRenderStrategy() != IRequestCycleSettings.REDIRECT_TO_RENDER ||
			((WebRequest)RequestCycle.get().getRequest()).isAjax() ||
			(!RequestCycle.get().isRedirect()))
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

			// clean up all component related feedback messages
			getFeedbackMessages().clear(WebSession.MESSAGES_FOR_COMPONENTS);
		}
	}
}
