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

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * Represents additional error attributes present in a {@link ServletRequest} when the servlet
 * container is handling an error or a forward to an error page mapped by {@code error-page} element
 * in {@code web.xml}.
 * 
 * See documentation for the following request attributes for the values stored in this object:
 * <ul>
 * <li>{@link RequestDispatcher#ERROR_STATUS_CODE}</li>
 * <li>{@link RequestDispatcher#ERROR_MESSAGE}</li>
 * <li>{@link RequestDispatcher#ERROR_REQUEST_URI}</li>
 * <li>{@link RequestDispatcher#ERROR_SERVLET_NAME}</li>
 * <li>{@link RequestDispatcher#ERROR_EXCEPTION_TYPE}</li>
 * <li>{@link RequestDispatcher#ERROR_EXCEPTION}</li>
 * </ul>
 * 
 * @author igor
 */
public class ErrorAttributes
{
	// javax.servlet.error.status_code
	private final Integer statusCode;
	// javax.servlet.error.message
	private final String message;
	// javax.servlet.error.request_uri
	private final String requestUri;
	// javax.servlet.error.servlet_name
	private final String servletName;
	// javax.servlet.error.exception_type
	private final Class<? extends Throwable> exceptionType;
	// javax.servlet.error.exception
	private final Throwable exception;

	/**
	 * Constructor
	 * 
	 * @param statusCode
	 * @param message
	 * @param requestUri
	 * @param servletName
	 * @param exceptionType
	 * @param exception
	 */
	private ErrorAttributes(Integer statusCode, String message, String requestUri,
		String servletName, Class<? extends Throwable> exceptionType, Throwable exception)
	{
		this.statusCode = statusCode;
		this.message = message;
		this.requestUri = requestUri;
		this.servletName = servletName;
		this.exceptionType = exceptionType;
		this.exception = exception;
	}

	/**
	 * Gets statusCode.
	 * 
	 * @return statusCode
	 */
	public Integer getStatusCode()
	{
		return statusCode;
	}

	/**
	 * Gets message.
	 * 
	 * @return message
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * Gets requestUri.
	 * 
	 * @return requestUri
	 */
	public String getRequestUri()
	{
		return requestUri;
	}

	/**
	 * Gets servletName.
	 * 
	 * @return servletName
	 */
	public String getServletName()
	{
		return servletName;
	}

	/**
	 * Gets exceptionType.
	 * 
	 * @return exceptionType
	 */
	public Class<? extends Throwable> getExceptionType()
	{
		return exceptionType;
	}

	/**
	 * Gets exception.
	 * 
	 * @return exception
	 */
	public Throwable getException()
	{
		return exception;
	}

	/**
	 * Factory for creating instances of this class.
	 * 
	 * @param request
	 * @return instance of request contains error attributes or {@code null} if it does not.
	 */
	public static ErrorAttributes of(HttpServletRequest request, String filterPrefix)
	{
		Args.notNull(request, "request");
		Integer code = (Integer)request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		String message = (String)request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
		String uri = DispatchedRequestUtils.getRequestUri(request, RequestDispatcher.ERROR_REQUEST_URI, filterPrefix);
		String servlet = (String)request.getAttribute(RequestDispatcher.ERROR_SERVLET_NAME);
		@SuppressWarnings("unchecked")
		Class<? extends Throwable> type = (Class<? extends Throwable>)request.getAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE);
		Throwable ex = (Throwable)request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

		if (!Strings.isEmpty(uri) || code != null || ex != null)
		{
			return new ErrorAttributes(code, message, uri, servlet, type, ex);
		}
		return null;
	}

	@Override
	public String toString()
	{
		return "ErrorAttributes{" +
				"statusCode=" + statusCode +
				", message='" + message + '\'' +
				", requestUri='" + requestUri + '\'' +
				", servletName='" + servletName + '\'' +
				", exceptionType=" + exceptionType +
				", exception=" + exception +
				'}';
	}
}
