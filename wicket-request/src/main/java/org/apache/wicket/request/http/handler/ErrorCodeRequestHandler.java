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
package org.apache.wicket.request.http.handler;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.http.WebResponse;


/**
 * Response target that is to be used in a servlet environment to send an error code and optionally
 * a message. NOTE: this target can only be used in a servlet environment with
 * {@link IRequestCycle}s.
 * 
 * @author Eelco Hillenius
 */
public final class ErrorCodeRequestHandler implements IRequestHandler
{
	/** the servlet error code. */
	private final int errorCode;

	/** the optional message to send to the client. */
	private final String message;

	/**
	 * Construct.
	 * 
	 * @param errorCode
	 *            the servlet error code; use one of the
	 *            {@link javax.servlet.http.HttpServletResponse} constants
	 * @see javax.servlet.http.HttpServletResponse
	 */
	public ErrorCodeRequestHandler(final int errorCode)
	{
		this(errorCode, null);
	}

	/**
	 * Construct.
	 * 
	 * @param errorCode
	 *            the servlet error code; use one of the
	 *            {@link javax.servlet.http.HttpServletResponse} constants
	 * @param message
	 *            the optional message to send to the client
	 * @see javax.servlet.http.HttpServletResponse
	 */
	public ErrorCodeRequestHandler(final int errorCode, final String message)
	{
		this.errorCode = errorCode;
		this.message = message;
	}

	/**
	 * Respond by sending the set errorCode and optionally the message to the browser.
	 * 
	 * @see org.apache.wicket.request.IRequestHandler#respond(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public void respond(final IRequestCycle requestCycle)
	{
		WebResponse webResponse = (WebResponse)requestCycle.getResponse();
		webResponse.sendError(errorCode, message);
	}

	/**
	 * Gets the servlet error code.
	 * 
	 * @return the servlet error code
	 */
	public final int getErrorCode()
	{
		return errorCode;
	}

	/**
	 * Gets the optional message to send to the client.
	 * 
	 * @return the optional message to send to the client
	 */
	public final String getMessage()
	{
		return message;
	}

	/** {@inheritDoc} */
	@Override
	public void detach(final IRequestCycle requestCycle)
	{
	}
}
