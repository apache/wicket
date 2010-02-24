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
package org.apache.wicket.protocol.http.servlet;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.AbortException;
import org.apache.wicket.AbstractRestartResponseException;
import org.apache.wicket.protocol.http.request.WebErrorCodeResponseHandler;

/**
 * Causes Wicket to abort processing and set the specified HTTP error code, with the provided
 * message if specified.
 * 
 * @author igor.vaynberg
 * 
 * @see AbortException
 */
public final class AbortWithWebErrorCodeException extends AbstractRestartResponseException
{
	private static final long serialVersionUID = 1L;

	private final int errorCode;
	private final String message;

	/**
	 * Construct.
	 * 
	 * @param errorCode
	 *            the servlet error code; use one of the
	 *            {@link javax.servlet.http.HttpServletResponse} constants
	 * @see javax.servlet.http.HttpServletResponse
	 */
	public AbortWithWebErrorCodeException(int errorCode, String message)
	{
		super(new WebErrorCodeResponseHandler(errorCode, message));
		this.errorCode = errorCode;
		this.message = message;
	}

	/**
	 * Gets the error code.
	 * 
	 * @return errorCode
	 * @see HttpServletResponse
	 */
	public int getErrorCode()
	{
		return errorCode;
	}

	/**
	 * Gets the error message
	 * 
	 * @return error message
	 */

	@Override
	public String getMessage()
	{
		return message;
	}


}
