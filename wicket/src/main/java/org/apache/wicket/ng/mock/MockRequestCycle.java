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
package org.apache.wicket.ng.mock;

import org.apache.wicket.ng.request.IRequestHandler;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.cycle.RequestCycleContext;

/**
 * Request cycle that allows to override {@link IRequestHandler} resolving to force custom
 * {@link IRequestHandler}.
 * 
 * @author Matej Knopp
 */
public class MockRequestCycle extends RequestCycle
{
	/**
	 * Construct.
	 * 
	 * @param context
	 */
	public MockRequestCycle(RequestCycleContext context)
	{
		super(context);
	}

	private IRequestHandler forcedRequestHandler;

	/**
	 * Forces the specified request handler to be resolved.
	 * 
	 * @param requestHandler
	 */
	public void forceRequestHandler(IRequestHandler requestHandler)
	{
		forcedRequestHandler = requestHandler;
	}

	@Override
	protected IRequestHandler resolveRequestHandler()
	{
		if (forcedRequestHandler != null)
		{
			return forcedRequestHandler;
		}
		else
		{
			return super.resolveRequestHandler();
		}
	}
}
