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
import org.apache.wicket.util.lang.Args;

/**
 * A base class for all event broadcasting payloads for WebSocket messages.
 *
 * @since 6.0
 */
public abstract class WebSocketPayload<T>
{
	private final WebSocketRequestHandler handler;

	public WebSocketPayload(final WebSocketRequestHandler handler)
	{
		this.handler = Args.notNull(handler, "handler");
	}
	
	public abstract T getMessage();

	public final WebSocketRequestHandler getHandler()
	{
		return handler;
	}
}
