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
package org.apache.wicket;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.RequestHandlerStack.ReplaceHandlerException;
import org.apache.wicket.request.handler.basic.EmptyRequestHandler;


/**
 * Immediately aborts any further processing.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class AbortException extends ReplaceHandlerException
{
	private static final long serialVersionUID = 1L;


	protected AbortException(IRequestHandler replacementRequestHandler)
	{
		super(replacementRequestHandler, true);
	}

	/**
	 * Constructor
	 */
	public AbortException()
	{
		super(EmptyRequestHandler.getInstance(), true);
	}

	/**
	 * @see java.lang.Throwable#fillInStackTrace()
	 */
	@Override
	public synchronized Throwable fillInStackTrace()
	{
		// we do not need a stack trace, so to speed things up just return null
		return null;
	}

}
