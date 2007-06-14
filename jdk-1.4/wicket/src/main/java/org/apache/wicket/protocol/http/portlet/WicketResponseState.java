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

/**
 * @author Ate Douma
 */
public class WicketResponseState
{
	private int errorCode;
	private String errorMessage;
	private int statusCode;
	private String redirectLocation;

	/**
	 * Gets errorCode.
	 * @return errorCode
	 */
	public int getErrorCode()
	{
		return errorCode;
	}

	/**
	 * Sets errorCode.
	 * @param errorCode errorCode
	 */
	public void setErrorCode(int errorCode)
	{
		this.errorCode = errorCode;
	}
	
	/**
	 * Gets errorMessage.
	 * @return errorMessage
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}
	
	/**
	 * Sets errorMessage.
	 * @param errorMessage errorMessage
	 */
	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}
	
	/**
	 * Gets redirectLocation.
	 * @return redirectLocation
	 */
	public String getRedirectLocation()
	{
		return redirectLocation;
	}
	
	/**
	 * Sets redirectLocation.
	 * @param redirectLocation redirectLocation
	 */
	public void setRedirectLocation(String redirectLocation)
	{
		this.redirectLocation = redirectLocation;
	}
	
	/**
	 * Gets statusCode.
	 * @return statusCode
	 */
	public int getStatusCode()
	{
		return statusCode;
	}
	
	/**
	 * Sets statusCode.
	 * @param statusCode statusCode
	 */
	public void setStatusCode(int statusCode)
	{
		this.statusCode = statusCode;
	}
	
	public void reset()
	{
		errorCode = 0;
		errorMessage = null;
		statusCode = 0;
		redirectLocation = null;
	}
}
