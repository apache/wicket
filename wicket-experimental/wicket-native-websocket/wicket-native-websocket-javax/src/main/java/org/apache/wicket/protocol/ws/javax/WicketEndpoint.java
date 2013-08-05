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
package org.apache.wicket.protocol.ws.javax;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WicketEndpoint extends Endpoint
{
	private static final Logger LOG = LoggerFactory.getLogger(WicketEndpoint.class);

	private JavaxWebSocketProcessor javaxWebSocketProcessor;

	@Override
	public void onOpen(Session session, EndpointConfig endpointConfig)
	{
		WebApplication app = (WebApplication) session.getUserProperties().get(JavaxWebSocketFilter.APPLICATION_KEY);

		try
		{
			ThreadContext.setApplication(app);
			javaxWebSocketProcessor = new JavaxWebSocketProcessor(session, app);
		}
		finally
		{
			ThreadContext.detach();
		}

	}

	@Override
	public void onClose(Session session, CloseReason closeReason)
	{
		super.onClose(session, closeReason);

		javaxWebSocketProcessor.onClose(closeReason.getCloseCode().getCode(), closeReason.getReasonPhrase());
	}

	@Override
	public void onError(Session session, Throwable t)
	{
		super.onError(session, t);

		LOG.error("An error occurred in web socket connection with id : " + session.getId(), t);
	}
}
