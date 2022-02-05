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
package org.apache.wicket.protocol.ws.api.message;

import java.nio.ByteBuffer;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.util.lang.Args;

/**
 * A {@link IWebSocketMessage message} with Pong message data
 *
 * @since 6.0
 */
public class PongMessageMessage extends AbstractClientMessage
{
	private final ByteBuffer byteBuffer;

	/**
	 *
	 * @param application
	 *      the Wicket application
	 * @param sessionId
	 *      the id of the http session
	 * @param key
	 *      the page id or resource name
	 * @param byteBuffer
	 *      the message sent from the client
	 */
	public PongMessageMessage(Application application, String sessionId, IKey key, ByteBuffer byteBuffer)
	{
		super(application, sessionId, key);
		this.byteBuffer = Args.notNull(byteBuffer, "byteBuffer");
	}

	public ByteBuffer getByteBuffer()
	{
		return byteBuffer;
	}
}
