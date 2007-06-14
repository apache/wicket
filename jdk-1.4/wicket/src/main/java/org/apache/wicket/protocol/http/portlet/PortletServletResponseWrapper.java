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
package org.apache.wicket.protocol.http.portlet;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * @author Ate Douma
 */
public class PortletServletResponseWrapper extends HttpServletResponseWrapper
{
	private WicketResponseState responseState;
	
	public PortletServletResponseWrapper(HttpServletResponse response, WicketResponseState responseState)
	{
		super(response);
		this.responseState = responseState;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponseWrapper#sendError(int, java.lang.String)
	 */
	public void sendError(int errorCode, String errorMessage) throws IOException
	{
		responseState.setErrorCode(errorCode);
		responseState.setErrorMessage(errorMessage);
	}
	
	/**
	 * @see javax.servlet.http.HttpServletResponseWrapper#sendError(int)
	 */
	public void sendError(int errorCode) throws IOException
	{
		responseState.setErrorCode(errorCode);
		responseState.setErrorMessage(null);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponseWrapper#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String redirectLocation) throws IOException
	{
		responseState.setRedirectLocation(redirectLocation);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int)
	 */
	public void setStatus(int statusCode)
	{
		responseState.setStatusCode(statusCode);
	}
}
