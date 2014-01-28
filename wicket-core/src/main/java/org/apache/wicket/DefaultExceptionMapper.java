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
package org.apache.wicket;

import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.ListenerInvocationNotAllowedException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.mapper.StalePageException;
import org.apache.wicket.markup.html.pages.ExceptionErrorPage;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.servlet.ResponseIOException;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.EmptyRequestHandler;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.handler.ErrorCodeRequestHandler;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.settings.IExceptionSettings.UnexpectedExceptionDisplay;
import org.apache.wicket.request.resource.PackageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If an exception is thrown when a page is being rendered this mapper will decide which error page
 * to show depending on the exception type and {@link Application#getExceptionSettings() application
 * configuration}
 */
public class DefaultExceptionMapper implements IExceptionMapper
{
	private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionMapper.class);

	@Override
	public IRequestHandler map(Exception e)
	{
		try
		{
			Response response = RequestCycle.get().getResponse();
			if (response instanceof WebResponse)
			{
				// we don't want to cache an exceptional reply in the browser
				((WebResponse)response).disableCaching();
			}
			return internalMap(e);
		}
		catch (RuntimeException e2)
		{
			if (logger.isDebugEnabled())
			{
				logger.error(
					"An error occurred while handling a previous error: " + e2.getMessage(), e2);
			}

			// hmmm, we were already handling an exception! give up
			logger.error("unexpected exception when handling another exception: " + e.getMessage(),
				e);
			return new ErrorCodeRequestHandler(500);
		}
	}

	private IRequestHandler internalMap(Exception e)
	{
		final Application application = Application.get();

		// check if we are processing an Ajax request and if we want to invoke the failure handler
		if (isProcessingAjaxRequest())
		{
			switch (application.getExceptionSettings().getAjaxErrorHandlingStrategy())
			{
				case INVOKE_FAILURE_HANDLER :
					return new ErrorCodeRequestHandler(500);
			}
		}

		if (e instanceof StalePageException)
		{
			// If the page was stale, just re-render it
			// (the url should always be updated by an redirect in that case)
			return new RenderPageRequestHandler(new PageProvider(((StalePageException)e).getPage()));
		}
		else if (e instanceof PageExpiredException)
		{
			return createPageRequestHandler(new PageProvider(Application.get()
				.getApplicationSettings()
				.getPageExpiredErrorPage()));
		}
		else if (e instanceof AuthorizationException ||
			e instanceof ListenerInvocationNotAllowedException)
		{
			return createPageRequestHandler(new PageProvider(Application.get()
				.getApplicationSettings()
				.getAccessDeniedPage()));
		}
		else if (e instanceof ResponseIOException)
		{
			logger.error("Connection lost, give up responding.", e);
			return new EmptyRequestHandler();
		}
		else if (e instanceof PackageResource.PackageResourceBlockedException && application.usesDeploymentConfig())
		{
			logger.debug(e.getMessage(), e);
			return new ErrorCodeRequestHandler(404);
		}
		else
		{

			final UnexpectedExceptionDisplay unexpectedExceptionDisplay = application.getExceptionSettings()
				.getUnexpectedExceptionDisplay();

			logger.error("Unexpected error occurred", e);

			if (IExceptionSettings.SHOW_EXCEPTION_PAGE.equals(unexpectedExceptionDisplay))
			{
				Page currentPage = extractCurrentPage();
				return createPageRequestHandler(new PageProvider(new ExceptionErrorPage(e,
					currentPage)));
			}
			else if (IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE.equals(unexpectedExceptionDisplay))
			{
				return createPageRequestHandler(new PageProvider(
					application.getApplicationSettings().getInternalErrorPage()));
			}
			else
			{
				// IExceptionSettings.SHOW_NO_EXCEPTION_PAGE
				return new ErrorCodeRequestHandler(500);
			}
		}
	}

	private RenderPageRequestHandler createPageRequestHandler(PageProvider pageProvider)
	{
		RequestCycle requestCycle = RequestCycle.get();

		if (requestCycle == null)
		{
			throw new IllegalStateException(
				"there is no current request cycle attached to this thread");
		}

		/*
		 * Use NEVER_REDIRECT policy to preserve the original page's URL for non-Ajax requests and
		 * always redirect for ajax requests
		 */
		RenderPageRequestHandler.RedirectPolicy redirect = RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT;

		if (isProcessingAjaxRequest())
		{
			redirect = RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT;
		}

		return new RenderPageRequestHandler(pageProvider, redirect);
	}

	private boolean isProcessingAjaxRequest()
	{
		RequestCycle rc = RequestCycle.get();
		Request request = rc.getRequest();
		if (request instanceof WebRequest)
		{
			return ((WebRequest)request).isAjax();
		}
		return false;
	}

	/**
	 * @return the page being rendered when the exception was thrown, or {@code null} if it cannot
	 *         be extracted
	 */
	private Page extractCurrentPage()
	{
		final RequestCycle requestCycle = RequestCycle.get();

		IRequestHandler handler = requestCycle.getActiveRequestHandler();

		if (handler == null)
		{
			handler = requestCycle.getRequestHandlerScheduledAfterCurrent();
		}

		if (handler instanceof IPageRequestHandler)
		{
			IPageRequestHandler pageRequestHandler = (IPageRequestHandler)handler;
			return (Page)pageRequestHandler.getPage();
		}
		return null;
	}
}
