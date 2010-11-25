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
package org.apache.wicket.examples;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.examples.source.SourcesPage;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;

/**
 * Handles the PageExpiredException so that the SourcesPage can recover from a session expired.
 * 
 * @author rgravener
 */
public class WicketExampleRequestCycle extends WebRequestCycle
{
	/**
	 * Construct.
	 * 
	 * @param application
	 * @param request
	 * @param response
	 */
	public WicketExampleRequestCycle(WebApplication application, WebRequest request,
		Response response)
	{
		super(application, request, response);
	}

	/**
	 * @see org.apache.wicket.RequestCycle#onRuntimeException(org.apache.wicket.Page,
	 *      java.lang.RuntimeException)
	 */
	@Override
	public Page onRuntimeException(final Page page, final RuntimeException e)
	{
		final Throwable cause;
		if (e.getCause() != null)
		{
			cause = e.getCause();
		}
		else
		{
			cause = e;
		}

		if (cause instanceof PageExpiredException)
		{
			handlePageExpiredException((PageExpiredException)cause);
		}
		return super.onRuntimeException(page, e);
	}

	/**
	 * Checks to see if the request was ajax based. If so we send a 404 so that the
	 * org.apache.wicket.ajax.IAjaxCallDecorator failure script is executed.
	 * 
	 * @param e
	 */
	private void handlePageExpiredException(final PageExpiredException e)
	{
		Response response = getOriginalResponse();
		if (response instanceof BufferedWebResponse)
		{
			BufferedWebResponse bufferedWebResponse = (BufferedWebResponse)response;
			Request request = getRequest();
			if (bufferedWebResponse.isAjax() &&
				request.getParameter(SourcesPage.PAGE_CLASS) != null)
			{
				// If there is a better way to figure out if SourcesPage was the request, we should
				// do that.
				throw new AbortWithWebErrorCodeException(404);
			}
		}
	}
}
