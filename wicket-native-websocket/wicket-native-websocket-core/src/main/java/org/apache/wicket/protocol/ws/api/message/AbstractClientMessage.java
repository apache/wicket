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

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.util.lang.Args;

/**
 * A base message for all messages with information about the client
 */
public abstract class AbstractClientMessage implements IWebSocketMessage
{
	private final Application application;
	private final String sessionId;
	private final IKey key;

	public AbstractClientMessage(Application application, String sessionId, IKey key)
	{
		this.application = Args.notNull(application, "application");
		this.sessionId = Args.notNull(sessionId, "sessionId");
		this.key = Args.notNull(key, "key");
	}

	public Application getApplication()
	{
		return application;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public IKey getKey()
	{
		return key;
	}

}
