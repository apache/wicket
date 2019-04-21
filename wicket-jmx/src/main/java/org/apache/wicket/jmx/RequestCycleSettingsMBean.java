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
package org.apache.wicket.jmx;

import java.io.UnsupportedEncodingException;

import org.apache.wicket.markup.html.pages.BrowserInfoPage;

/**
 * Request cycle settings.
 * 
 * @author eelcohillenius
 */
public interface RequestCycleSettingsMBean
{
	/**
	 * @return True if this application buffers its responses
	 */
	boolean getBufferResponse();

	/**
	 * Gets whether Wicket should try to get extensive client info by redirecting to
	 * {@link BrowserInfoPage a page that polls for client capabilities}. This method is used by the
	 * default implementation of {@link WebRequestCycle#newClientInfo()}, so if that method is
	 * overriden, there is no guarantee this method will be taken into account.
	 * 
	 * @return Whether to gather extensive client info
	 */
	boolean getGatherExtendedBrowserInfo();

	/**
	 * In order to do proper form parameter decoding it is important that the response and the
	 * following request have the same encoding. see
	 * http://www.crazysquirrel.com/computing/general/form-encoding.jspx for additional information.
	 * 
	 * @return The request and response encoding
	 */
	String getResponseRequestEncoding();

	/**
	 * Gets the time that a request will by default be waiting for the previous request to be
	 * handled before giving up.
	 * 
	 * @return The time out
	 */
	String getTimeout();

	/**
	 * @param bufferResponse
	 *            True if this application should buffer responses.
	 */
	void setBufferResponse(boolean bufferResponse);

	/**
	 * Sets whether Wicket should try to get extensive client info by redirecting to
	 * {@link BrowserInfoPage a page that polls for client capabilities}. This method is used by the
	 * default implementation of {@link WebRequestCycle#newClientInfo()}, so if that method is
	 * overriden, there is no guarantee this method will be taken into account.
	 * 
	 * @param gatherExtendedBrowserInfo
	 *            Whether to gather extensive client info
	 */
	void setGatherExtendedBrowserInfo(boolean gatherExtendedBrowserInfo);

	/**
	 * In order to do proper form parameter decoding it is important that the response and the
	 * following request have the same encoding. see
	 * http://www.crazysquirrel.com/computing/general/form-encoding.jspx for additional information.
	 * 
	 * Default encoding: UTF-8
	 * 
	 * @param responseRequestEncoding
	 *            The request and response encoding to be used.
	 * @throws UnsupportedEncodingException
	 *             is encoding is not supported
	 */
	void setResponseRequestEncoding(final String responseRequestEncoding)
		throws UnsupportedEncodingException;

	/**
	 * Sets the time that a request will by default be waiting for the previous request to be
	 * handled before giving up.
	 * 
	 * @param timeout
	 */
	void setTimeout(String timeout);


	/**
	 * Sets how many attempts Wicket will make to render the exception request handler before
	 *         giving up.
	 * @param retries
	 *      the number of attempts
	 */
	void setExceptionRetryCount(int retries);

	/**
	 * @return How many times will Wicket attempt to render the exception request handler before
	 *         giving up.
	 */
	int getExceptionRetryCount();
}
