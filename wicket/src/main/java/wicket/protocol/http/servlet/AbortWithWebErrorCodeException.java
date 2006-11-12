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
package wicket.protocol.http.servlet;

import wicket.AbortException;
import wicket.RequestCycle;
import wicket.protocol.http.request.WebErrorCodeResponseTarget;

/**
 * Causes Wicket to abort processing and set the specified HTTP error code, with
 * the provided message if provided.
 * 
 * @author Eelco Hillenius
 * 
 * @see AbortException
 * @see WebErrorCodeResponseTarget
 */
public final class AbortWithWebErrorCodeException extends AbortException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param errorCode
	 *            the servlet error code; use one of the
	 *            {@link javax.servlet.http.HttpServletResponse} constants
	 * @see javax.servlet.http.HttpServletResponse
	 */
	public AbortWithWebErrorCodeException(int errorCode)
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
	public AbortWithWebErrorCodeException(int errorCode, String message)
	{
		RequestCycle.get().setRequestTarget(new WebErrorCodeResponseTarget(errorCode, message));
	}
}
