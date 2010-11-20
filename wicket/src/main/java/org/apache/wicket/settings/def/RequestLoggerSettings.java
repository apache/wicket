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
package org.apache.wicket.settings.def;

import org.apache.wicket.settings.IRequestLoggerSettings;

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
public class RequestLoggerSettings implements IRequestLoggerSettings
{
	private boolean recordSessionSize = true;

	private int requestsWindowSize = 0;

	private boolean requestLoggerEnabled;

	/**
	 * @see org.apache.wicket.settings.IRequestLoggerSettings#getRecordSessionSize()
	 */
	public boolean getRecordSessionSize()
	{
		return recordSessionSize;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestLoggerSettings#getRequestsWindowSize()
	 */
	public int getRequestsWindowSize()
	{
		return requestsWindowSize;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestLoggerSettings#isRequestLoggerEnabled()
	 */
	public boolean isRequestLoggerEnabled()
	{
		return requestLoggerEnabled;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestLoggerSettings#setRecordSessionSize(boolean)
	 */
	public void setRecordSessionSize(boolean record)
	{
		recordSessionSize = record;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestLoggerSettings#setRequestLoggerEnabled(boolean)
	 */
	public void setRequestLoggerEnabled(boolean enable)
	{
		requestLoggerEnabled = enable;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestLoggerSettings#setRequestsWindowSize(int)
	 */
	public void setRequestsWindowSize(int size)
	{
		requestsWindowSize = size;
	}
}
