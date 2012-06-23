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

import java.io.NotSerializableException;

import javax.servlet.http.HttpSession;

import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.request.handler.IPageProvider;
import org.apache.wicket.settings.IStoreSettings;

/**
 * Thrown when a {@link Page} instance cannot be found by its id in the page stores. The page may be
 * missing because of reasons like:
 * <ul>
 * <li>the page have never been stored there, e.g. an error occurred during the storing process</li>
 * <li>the http session has expired and thus all pages related to this session are erased too</li>
 * <li>the page instance has been erased because the store size exceeded</li>
 * </ul>
 *
 * <p>This exception is used to tell Wicket to respond with the configured PageExpiredPage, so its
 * stacktrace it is not really needed.</p>
 *
 * @see HttpSession#setMaxInactiveInterval(int)
 * @see IStoreSettings#setMaxSizePerSession(org.apache.wicket.util.lang.Bytes)
 * @see NotSerializableException
 * @see IPageProvider#getPageInstance()
 */
public class PageExpiredException extends WicketRuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see WicketRuntimeException#WicketRuntimeException(java.lang.String)
	 */
	public PageExpiredException(final String message)
	{
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param message
	 * @param cause
	 */
	public PageExpiredException(final String message, Exception cause)
	{
		super(message, cause);
	}

	/**
	 * Suppress loading of the stacktrace because it is not needed.
	 *
	 * @see java.lang.Throwable#fillInStackTrace()
	 */
	@Override
	public Throwable fillInStackTrace()
	{
		// don't do anything here
		return null;
	}
}
