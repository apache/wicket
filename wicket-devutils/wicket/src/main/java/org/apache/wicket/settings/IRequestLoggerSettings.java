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
 * @author jcompagner
 */
public interface IRequestLoggerSettings
{
	/**
	 * Enable/Disable the request logger.
	 * 
	 * @param enable
	 *            boolean.
	 */
	void setRequestLoggerEnabled(boolean enable);


	/**
	 * @return true if the request Logger is enabled. (default false)
	 */
	boolean isRequestLoggerEnabled();

	/**
	 * Enable/Disable the recording of the session size for every request.
	 * 
	 * @param record
	 */
	void setRecordSessionSize(boolean record);

	/**
	 * @return true if the session size is recorded. (default true)
	 */
	boolean getRecordSessionSize();

	/**
	 * Set the window of all the requests that is kept in memory for viewing. Default is 2000, You
	 * can set this to 0 then only Sessions data is recorded (number of request, total time, latest
	 * size)
	 * 
	 * @param size
	 */
	void setRequestsWindowSize(int size);

	/**
	 * @return The window size of the recorded requests. (default 2000)
	 */
	int getRequestsWindowSize();
}
