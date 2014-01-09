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
package org.apache.wicket.protocol.ws.api;

import org.apache.wicket.protocol.ws.api.event.WebSocketBinaryPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketClosedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketConnectedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketTextPayload;
import org.apache.wicket.protocol.ws.api.message.BinaryMessage;
import org.apache.wicket.protocol.ws.api.message.ClosedMessage;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.request.resource.IResource;

/**
 * An IResource that can be used as WebSocket endpoint
 */
public abstract class WebSocketResource implements IResource
{
	void onPayload(WebSocketPayload<?> payload)
	{
		WebSocketRequestHandler webSocketHandler = payload.getHandler();

		if (payload instanceof WebSocketTextPayload)
		{
			WebSocketTextPayload textPayload = (WebSocketTextPayload) payload;
			TextMessage data = textPayload.getMessage();
			onMessage(webSocketHandler, data);
		}
		else if (payload instanceof WebSocketBinaryPayload)
		{
			WebSocketBinaryPayload binaryPayload = (WebSocketBinaryPayload) payload;
			BinaryMessage binaryData = binaryPayload.getMessage();
			onMessage(webSocketHandler, binaryData);
		}
		else if (payload instanceof WebSocketConnectedPayload)
		{
			WebSocketConnectedPayload connectedPayload = (WebSocketConnectedPayload) payload;
			ConnectedMessage message = connectedPayload.getMessage();
			onConnect(message);
		}
		else if (payload instanceof WebSocketClosedPayload)
		{
			WebSocketClosedPayload connectedPayload = (WebSocketClosedPayload) payload;
			ClosedMessage message = connectedPayload.getMessage();
			onClose(message);
		}
	}

	protected void onConnect(ConnectedMessage message)
	{
	}

	protected void onClose(ClosedMessage message)
	{
	}

	protected void onMessage(WebSocketRequestHandler handler, TextMessage message)
	{
	}

	protected void onMessage(WebSocketRequestHandler handler, BinaryMessage binaryMessage)
	{
	}

	@Override
	public final void respond(Attributes attributes)
	{
	}
}
