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

import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.http.WebResponse;

/**
 * Handler that renders a {@link BufferedWebResponse}.
 *
 * @author Matej Knopp
 */
public class BufferedResponseRequestHandler implements IRequestHandler
{
	private final BufferedWebResponse bufferedWebResponse;

	/**
	 * Construct.
	 *
	 * @param bufferedWebResponse
	 */
	public BufferedResponseRequestHandler(BufferedWebResponse bufferedWebResponse)
	{
		this.bufferedWebResponse = bufferedWebResponse;
	}

	/** {@inheritDoc} */
	@Override
	public void detach(IRequestCycle requestCycle)
	{
	}

	/** {@inheritDoc} */
	@Override
	public void respond(IRequestCycle requestCycle)
	{
		bufferedWebResponse.writeTo((WebResponse)requestCycle.getResponse());
	}
}
