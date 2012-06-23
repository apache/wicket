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
import org.apache.wicket.protocol.ws.api.message.BinaryMessage;
import org.apache.wicket.util.lang.Args;

/**
 * Payload for event broadcasting when a binary message comes in the WebSocket connection
 * 
 * @since 6.0
 */
public class WebSocketBinaryPayload extends WebSocketPayload<BinaryMessage>
{
	private final BinaryMessage binaryMessage;

	public WebSocketBinaryPayload(BinaryMessage binaryMessage, WebSocketRequestHandler handler)
	{
		super(handler);

		this.binaryMessage = Args.notNull(binaryMessage, "binaryMessage");
	}

	public final BinaryMessage getMessage()
	{
		return binaryMessage;
	}
}
