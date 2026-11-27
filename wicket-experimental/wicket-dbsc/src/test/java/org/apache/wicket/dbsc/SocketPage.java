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
package org.apache.wicket.dbsc;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.TextMessage;

/**
 * Receives WebSocket messages and records them.
 */
public class SocketPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	static final List<String> RECEIVED = new CopyOnWriteArrayList<>();

	/**
	 * Constructor.
	 */
	public SocketPage()
	{
		add(new WebSocketBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onMessage(WebSocketRequestHandler handler, TextMessage message)
			{
				RECEIVED.add(message.getText());
			}
		});
	}
}
