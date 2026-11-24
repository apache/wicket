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
package org.apache.wicket.dbsc;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;

/**
 * Checks the bound cookie once per request, before the first request handler runs. The refresh
 * endpoint is left out: the browser calls it precisely because the cookie has expired.
 */
final class BoundCredentialCheck implements IRequestCycleListener
{
	private static final MetaDataKey<Boolean> CHECKED = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final DeviceBoundSessions deviceBoundSessions;

	BoundCredentialCheck(DeviceBoundSessions deviceBoundSessions)
	{
		this.deviceBoundSessions = deviceBoundSessions;
	}

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
	{
		if (cycle.getMetaData(CHECKED) != null)
		{
			return;
		}
		cycle.setMetaData(CHECKED, Boolean.TRUE);
		if (handler instanceof ResourceReferenceRequestHandler resource &&
			resource.getResourceReference() instanceof EndpointReference)
		{
			return;
		}
		deviceBoundSessions.checkCredential(cycle);
	}
}
