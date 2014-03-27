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
package org.apache.wicket.core.request.handler;

import org.apache.wicket.Application;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.http.WebResponse;

/**
 * The empty AJAX request target does output an empty AJAX response.
 * <br/>
 * May be used as a light, "do nothing" Ajax response.
 *
 * @author Matej Knopp
 */
public final class EmptyAjaxRequestHandler implements IRequestHandler
{
	/** immutable hashcode. */
	private static final int HASH = 17 * 1542323;

	/** singleton instance. */
	private static final EmptyAjaxRequestHandler instance = new EmptyAjaxRequestHandler();

	/**
	 * Construct.
	 */
	private EmptyAjaxRequestHandler()
	{
	}

	/**
	 * Gets the singleton instance.
	 *
	 * @return the singleton instance
	 */
	public static EmptyAjaxRequestHandler getInstance()
	{
		return instance;
	}

	/** {@inheritDoc} */
	@Override
	public void respond(IRequestCycle requestCycle)
	{
		WebResponse response = (WebResponse)requestCycle.getResponse();
		final String encoding = Application.get()
			.getRequestCycleSettings()
			.getResponseRequestEncoding();

		// Set content type based on markup type for page
		response.setContentType("text/xml; charset=" + encoding);

		// Make sure it is not cached by a client
		response.disableCaching();

		response.write("<?xml version=\"1.0\" encoding=\"");
		response.write(encoding);
		response.write("\"?><ajax-response></ajax-response>");
	}

	/** {@inheritDoc} */
	@Override
	public void detach(IRequestCycle requestCycle)
	{
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof EmptyAjaxRequestHandler;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return HASH;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "EmptyAjaxRequestTarget";
	}
}
