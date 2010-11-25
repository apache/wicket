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
package org.apache.wicket.protocol.http.request;

import org.apache.wicket.WicketRuntimeException;

/**
 * Represents any kind of failure related to processing a url
 * 
 * @author igor.vaynberg
 */
public class InvalidUrlException extends WicketRuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public InvalidUrlException()
	{
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param message
	 * @param cause
	 */
	public InvalidUrlException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public InvalidUrlException(String message)
	{
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param cause
	 */
	public InvalidUrlException(Throwable cause)
	{
		super(cause);
	}
}
