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
package org.apache.wicket.atmosphere;

import static org.atmosphere.cpr.HeaderConfig.X_ATMOSPHERE_TRACKMESSAGESIZE;

import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.PerRequestBroadcastFilter;

/**
 * A broadcast filter, based on {@link org.atmosphere.client.TrackMessageSizeFilter}, but with a
 * different delimiter {@code &lt;|msg|&gt;} to prevent conflicts with pipes in the messages.
 */
public class TrackMessageSizeFilter implements PerRequestBroadcastFilter
{

	@Override
	public BroadcastAction filter(AtmosphereResource r, Object message, Object originalMessage)
	{

		AtmosphereRequest request = r.getRequest();
		if ("true".equalsIgnoreCase(request.getHeader(X_ATMOSPHERE_TRACKMESSAGESIZE)) &&
			message != null && String.class.isAssignableFrom(message.getClass()))
		{

			String msg = message.toString();
			msg = msg.length() + "<|msg|>" + msg;
			return new BroadcastAction(BroadcastAction.ACTION.CONTINUE, msg);

		}
		return new BroadcastAction(BroadcastAction.ACTION.CONTINUE, message);
	}

	@Override
	public BroadcastAction filter(Object originalMessage, Object message)
	{
		return new BroadcastAction(message);
	}
}
