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
package org.apache.wicket.core.request.mapper;

import org.apache.wicket.WicketInternalException;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.component.IRequestablePage;

/**
 * Exception invoked when when stale link has been clicked. The page should then be rerendered with
 * an explanatory error message.
 *
 * @author Matej Knopp
 */
public class StalePageException extends WicketRuntimeException implements WicketInternalException
{
	private static final long serialVersionUID = 1L;

	private final transient IRequestablePage page;

	/**
	 *
	 * Construct.
	 *
	 * @param page
	 */
	public StalePageException(IRequestablePage page)
	{
		this.page = page;
	}

	/**
	 *
	 * @return page instance
	 */
	public IRequestablePage getPage()
	{
		return page;
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
