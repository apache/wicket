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

import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;

/**
 * A registry that keeps all currently opened web socket connections in
 * maps in Application's meta data.
 *
 * TODO remove the synchronizations below and use ConcurrentMap#putIfAbsent()
 *
 * @since 6.0
 */
public class SimpleWebSocketConnectionRegistry implements IWebSocketConnectionRegistry
{
	private static final MetaDataKey<ConcurrentMap<String, ConcurrentMap<Integer, WebSocketConnection>>> KEY =
			new MetaDataKey<ConcurrentMap<String, ConcurrentMap<Integer, WebSocketConnection>>>()
	{
	};

	@Override
	public WebSocketConnection getConnection(Application application, String sessionId, Integer pageId)
	{
		Args.notNull(application, "application");
		Args.notNull(sessionId, "sessionId");
		Args.notNull(pageId, "pageId");

		WebSocketConnection connection = null;
		ConcurrentMap<String, ConcurrentMap<Integer, WebSocketConnection>> connectionsBySession = application.getMetaData(KEY);
		if (connectionsBySession != null)
		{
			ConcurrentMap<Integer, WebSocketConnection> connectionsByPage = connectionsBySession.get(sessionId);
			if (connectionsByPage != null)
			{
				connection = connectionsByPage.get(pageId);
			}
		}
		return connection;
	}

	@Override
	public void setConnection(Application application, String sessionId, Integer pageId, WebSocketConnection connection)
	{
		Args.notNull(application, "application");
		Args.notNull(sessionId, "sessionId");
		Args.notNull(pageId, "pageId");

		ConcurrentMap<String, ConcurrentMap<Integer, WebSocketConnection>> connectionsBySession = application.getMetaData(KEY);
		if (connectionsBySession == null)
		{
			synchronized (KEY)
			{
				connectionsBySession = application.getMetaData(KEY);
				if (connectionsBySession == null)
				{
					connectionsBySession = Generics.newConcurrentHashMap();
					application.setMetaData(KEY, connectionsBySession);
				}
			}
		}

		ConcurrentMap<Integer, WebSocketConnection> connectionsByPage = connectionsBySession.get(sessionId);
		if (connectionsByPage == null && connection != null)
		{
			synchronized (connectionsBySession)
			{
				connectionsByPage = connectionsBySession.get(sessionId);
				if (connectionsByPage == null)
				{
					connectionsByPage = Generics.newConcurrentHashMap();
					connectionsBySession.put(sessionId, connectionsByPage);
				}
			}
		}

		if (connection != null)
		{
			connectionsByPage.put(pageId, connection);
		}
		else if (connectionsByPage != null)
		{
			connectionsByPage.remove(pageId);
			if (connectionsByPage.isEmpty())
			{
				connectionsBySession.remove(sessionId);
			}
		}
	}

	@Override
	public void removeConnection(Application application, String sessionId, Integer pageId)
	{
		setConnection(application, sessionId, pageId, null);
	}
}
