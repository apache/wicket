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

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.examples.websocket.charts.ChartWebSocketResource;
import org.apache.wicket.examples.websocket.charts.WebSocketChart;
import org.apache.wicket.protocol.https.RequireHttps;
import org.apache.wicket.protocol.ws.api.BaseWebSocketBehavior;

import java.util.UUID;

@RequireHttps
public class WebSocketMultiTabResourceDemoPage extends WicketExamplePage
{
	public WebSocketMultiTabResourceDemoPage()
	{
		WebSocketChart chartPanel = new WebSocketChart("chartPanel");
		chartPanel.add(new BaseWebSocketBehavior(ChartWebSocketResource.NAME, UUID.randomUUID().toString()));
		add(chartPanel);
	}
}
