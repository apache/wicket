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
package org.apache.wicket.request.handler;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;

/**
 * Request handler that performs no work
 * 
 * @author igor.vaynberg
 * @author Eelco Hillenius
 */
public final class EmptyRequestHandler implements IRequestHandler
{
	/**
	 * Construct.
	 */
	public EmptyRequestHandler()
	{
	}

	/**
	 * Does nothing at all.
	 * 
	 * @see org.apache.wicket.request.IRequestHandler#respond(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public void respond(final IRequestCycle requestCycle)
	{
	}

	/** {@inheritDoc} */
	@Override
	public void detach(final IRequestCycle requestCycle)
	{
	}

}
