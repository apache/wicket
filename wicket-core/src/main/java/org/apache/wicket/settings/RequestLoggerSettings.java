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
package org.apache.wicket.settings;

/**
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class RequestLoggerSettings
{
	private boolean recordSessionSize = true;

	private int requestsWindowSize = 0;

	private boolean requestLoggerEnabled;

	/**
	 * @return true if the session size is recorded. (default true)
	 */
	public boolean getRecordSessionSize()
	{
		return recordSessionSize;
	}

	/**
	 * @return The window size of the recorded requests. (default 2000)
	 */
	public int getRequestsWindowSize()
	{
		return requestsWindowSize;
	}

	/**
	 * @return true if the request Logger is enabled. (default false)
	 */
	public boolean isRequestLoggerEnabled()
	{
		return requestLoggerEnabled;
	}

	/**
	 * Enable/Disable the recording of the session size for every request.
	 *
	 * @param record
	 * @return {@code this} object for chaining
	 */
	public RequestLoggerSettings setRecordSessionSize(boolean record)
	{
		recordSessionSize = record;
		return this;
	}

	/**
	 * Enable/Disable the request logger.
	 *
	 * @param enable
	 *            boolean.
	 * @return {@code this} object for chaining
	 */
	public RequestLoggerSettings setRequestLoggerEnabled(boolean enable)
	{
		requestLoggerEnabled = enable;
		return this;
	}

	/**
	 * Set the window of all the requests that is kept in memory for viewing. Default is 2000, You
	 * can set this to 0 then only Sessions data is recorded (number of request, total time, latest
	 * size)
	 *
	 * @param size
	 * @return {@code this} object for chaining
	 */
	public RequestLoggerSettings setRequestsWindowSize(int size)
	{
		requestsWindowSize = size;
		return this;
	}
}
