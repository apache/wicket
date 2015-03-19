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

import java.io.IOException;

import org.apache.wicket.IWicketInternalException;
import org.apache.wicket.WicketRuntimeException;

/**
 * Distinct IO exceptions from the those triggered while responding a request. Wicket needs to be
 * aware of this specific IO exception in order to give up from responding, since there are no more
 * connection to client.
 * 
 * @author Pedro Santos
 */
public class ResponseIOException extends WicketRuntimeException implements IWicketInternalException
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * @param cause
	 */
	public ResponseIOException(IOException cause)
	{
		super(cause);
	}

}
