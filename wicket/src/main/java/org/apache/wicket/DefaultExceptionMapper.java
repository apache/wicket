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
import org.apache.wicket.markup.html.pages.ExceptionErrorPage;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.EmptyRequestHandler;
import org.apache.wicket.request.handler.IPageRequestHandler;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.mapper.StalePageException;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.settings.IExceptionSettings.UnexpectedExceptionDisplay;
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

	// default policy is to not change the URL in the address bar of the browser:
	// - the url syntax eventually gives the user some indication of the error
	// - the user can hit refresh in the browser to retry loading the page
	private RenderPageRequestHandler.RedirectPolicy redirectPolicy = RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT;

	/**
	 * get the redirect policy in case of error (controls if the URL changes in case of displaying an error)
	 *
	 * @return redirect policy
	 */
	public RenderPageRequestHandler.RedirectPolicy getRedirectPolicy()
	{
		return redirectPolicy;
	}

	/**
	 * set the redirect policy in case of error (you can control if the URL changes in case of displaying an error)
	 *
	 * @param redirectPolicy redirection policy
	 */
	public void setRedirectPolicy(RenderPageRequestHandler.RedirectPolicy redirectPolicy)
	{
		this.redirectPolicy = redirectPolicy;
	}

	public IRequestHandler map(Exception e)
	{
		if (e instanceof StalePageException)
		{
			// If the page was stale, just rerender it
			// (the url should always be updated by an redirect in that case)
			return new RenderPageRequestHandler(new PageProvider(((StalePageException)e).getPage()));
		}
		else if (e instanceof PageExpiredException)
		{
			return createPageRequestHandler(new PageProvider(Application.get()
				.getApplicationSettings()
				.getPageExpiredErrorPage()));
		}
		else if (e instanceof AuthorizationException)
		{
			return createPageRequestHandler(new PageProvider(Application.get()
				.getApplicationSettings()
				.getAccessDeniedPage()));
		}
		else
		{
			final Application application = Application.get();
			final UnexpectedExceptionDisplay unexpectedExceptionDisplay = application.getExceptionSettings()
				.getUnexpectedExceptionDisplay();

			logger.error("Unexpected error occurred", e);

			if (IExceptionSettings.SHOW_EXCEPTION_PAGE.equals(unexpectedExceptionDisplay))
			{
				Page currentPage = extractCurrentPage();
				return createPageRequestHandler(new PageProvider(new ExceptionErrorPage(e,	currentPage)));
			}
			else if (IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE.equals(unexpectedExceptionDisplay))
			{
				return createPageRequestHandler(new PageProvider(
					application.getApplicationSettings().getInternalErrorPage()));
			}
			else
			{
				// IExceptionSettings.SHOW_NO_EXCEPTION_PAGE
				return new EmptyRequestHandler();
			}
		}
	}

	private RenderPageRequestHandler createPageRequestHandler(PageProvider pageProvider)
	{
		return new RenderPageRequestHandler(pageProvider, redirectPolicy);
	}

	/**
	 * @return the page being rendered when the exception was thrown, or {@code null} if it cannot
	 *         be extracted
	 */
	private Page extractCurrentPage()
	{
		final RequestCycle requestCycle = RequestCycle.get();
		final IRequestHandler activeRequestHandler = requestCycle.getActiveRequestHandler();

		Page currentPage = null;

		if (activeRequestHandler instanceof IPageRequestHandler)
		{
			IPageRequestHandler pageRequestHandler = (IPageRequestHandler)activeRequestHandler;
			currentPage = (Page)pageRequestHandler.getPage();
		}

		return currentPage;
	}
}
