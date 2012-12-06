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
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.util.lang.Args;

/**
 * A event broadcasting payload for the case when external source triggers a web socket push.
 *
 * @since 6.4
 */
public class WebSocketPushPayload extends WebSocketPayload<IWebSocketPushMessage>
{
	private final IWebSocketPushMessage data;

	public WebSocketPushPayload(IWebSocketPushMessage data, WebSocketRequestHandler handler)
	{
		super(handler);
		this.data = Args.notNull(data, "data");
	}

	@Override
	public final IWebSocketPushMessage getMessage()
	{
		return data;
	}

}
