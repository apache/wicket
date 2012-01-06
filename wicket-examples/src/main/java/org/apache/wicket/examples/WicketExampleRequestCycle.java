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

import org.apache.wicket.examples.source.SourcesPage;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.util.lang.Exceptions;

/**
 * Handles the PageExpiredException so that the SourcesPage can recover from a session expired.
 * 
 * TODO NG
 * 
 * @author rgravener
 */
public class WicketExampleRequestCycle extends RequestCycle
{

	/**
	 * Construct.
	 * 
	 * @param context
	 */
	public WicketExampleRequestCycle(RequestCycleContext context)
	{
		super(context);
	}

	/**
	 * @see RequestCycle#handleException(Exception)
	 */
	@Override
	public IRequestHandler handleException(final Exception e)
	{
		PageExpiredException pageExpiredException = Exceptions.findCause(e,
			PageExpiredException.class);
		if (pageExpiredException != null)
		{
			handlePageExpiredException(pageExpiredException);
		}
		return super.handleException(e);
	}

	/**
	 * Checks to see if the request was ajax based. If so we send a 404 so that the
	 * {@link org.apache.wicket.ajax.attributes.IAjaxCallListener#getFailureHandler(org.apache.wicket.Component)} failure script} is executed.
	 * 
	 * @param e
	 */
	private void handlePageExpiredException(final PageExpiredException e)
	{
		Request request = getRequest();
		if (request instanceof WebRequest)
		{
			WebRequest webRequest = (WebRequest)request;

			if (webRequest.isAjax() &&
				!request.getRequestParameters().getParameterValue(SourcesPage.PAGE_CLASS).isNull())
			{
				// If there is a better way to figure out if SourcesPage was the request, we should
				// do that.
				throw new AbortWithHttpErrorCodeException(404, "");
			}
		}
	}
}
