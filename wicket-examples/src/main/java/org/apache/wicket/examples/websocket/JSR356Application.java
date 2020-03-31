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

import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.examples.websocket.charts.ChartWebSocketResource;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.https.HttpsConfig;
import org.apache.wicket.protocol.https.HttpsMapper;
import org.apache.wicket.protocol.ws.WebSocketSettings;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the com.wicketinaction.StartNativeWebSocketExample class.
 */
public class JSR356Application extends WicketExampleApplication
{
    private ScheduledExecutorService scheduledExecutorService;

	@Override
	public Class<HomePage> getHomePage()
	{
		return HomePage.class;
	}

	@Override
	public void init()
	{
		super.init();

        scheduledExecutorService = Executors.newScheduledThreadPool(5);

		setRootRequestMapper(new HttpsMapper(getRootRequestMapper(), new HttpsConfig(8080, 8443)));

		mountPage("/behavior", WebSocketBehaviorDemoPage.class);
		mountPage("/resource", WebSocketResourceDemoPage.class);
		mountPage("/resource-multi-tab", WebSocketMultiTabResourceDemoPage.class);

		getSharedResources().add(ChartWebSocketResource.NAME, new ChartWebSocketResource());

		if (System.getenv("OPENSHIFT_APP_NAME") != null)
		{
			// OpenShift uses special proxy for WebSocket connections
			// https://blog.openshift.com/paas-websockets/
			final WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(this);
			webSocketSettings.setPort(8000);
			webSocketSettings.setSecurePort(8443);
		}

		// The websocket example loads JS from ajax.googleapis.com, which is not allowed by the CSP.
		// This now serves as an example on how to disable CSP
		getCspSettings().blocking().disabled();
	}

    @Override
    protected void onDestroy() {
        scheduledExecutorService.shutdownNow();

        super.onDestroy();
    }

    public ScheduledExecutorService getScheduledExecutorService()
    {
        return scheduledExecutorService;
    }

    public static JSR356Application get()
    {
        return (JSR356Application) WebApplication.get();
    }
}
