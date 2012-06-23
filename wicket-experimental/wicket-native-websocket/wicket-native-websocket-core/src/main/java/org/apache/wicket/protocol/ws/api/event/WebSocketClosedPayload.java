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
package org.apache.wicket.protocol.ws.api.event;

import org.apache.wicket.ajax.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.ClosedMessage;

/**
 * Payload for event broadcasting when the client has closed the WebSocket connection
 *
 * @since 6.0
 */
public class WebSocketClosedPayload extends WebSocketPayload<ClosedMessage>
{
	private final ClosedMessage message;

	public WebSocketClosedPayload(ClosedMessage message, WebSocketRequestHandler handler)
	{
		super(handler);

		this.message = message;
	}

	@Override
	public final ClosedMessage getMessage()
	{
		return message;
	}
}
