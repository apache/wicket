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
package org.apache.wicket.examples.websocket;

import java.util.concurrent.ScheduledExecutorService;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.examples.websocket.charts.ChartUpdater;
import org.apache.wicket.examples.websocket.charts.WebSocketChart;
import org.apache.wicket.protocol.https.RequireHttps;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;

@RequireHttps
public class WebSocketBehaviorDemoPage extends WicketExamplePage
{
	public WebSocketBehaviorDemoPage()
	{
		WebSocketChart chartPanel = new WebSocketChart("chartPanel");
		chartPanel.add(new WebSocketBehavior()
		{
			@Override
			protected void onConnect(ConnectedMessage message)
			{
				super.onConnect(message);

				ScheduledExecutorService service = JSR356Application.get().getScheduledExecutorService();
				ChartUpdater.start(message, service);
			}
		});
		add(chartPanel);

		add(new WebSocketBehavior()
		{
			@Override
			protected void onMessage(WebSocketRequestHandler handler, TextMessage message)
			{
				super.onMessage(handler, message);
			}
		});
	}
}
