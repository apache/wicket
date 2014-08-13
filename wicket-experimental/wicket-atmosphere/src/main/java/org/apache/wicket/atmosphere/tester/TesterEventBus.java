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
package org.apache.wicket.atmosphere.tester;

import java.util.concurrent.Executors;

import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.protocol.http.WebApplication;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.BroadcasterConfig;
import org.atmosphere.util.VoidExecutorService;

/**
 *
 */
class TesterEventBus extends EventBus
{
	public TesterEventBus(WebApplication application)
	{
		super(application, createBroadcaster());
	}

	private static TesterBroadcaster createBroadcaster()
	{
		TesterBroadcaster broadcaster = new TesterBroadcaster();

		AtmosphereFramework framework = new AtmosphereFramework();
		AtmosphereConfig config = new AtmosphereConfig(framework);

		TesterBroadcasterFactory broadcasterFactory = new TesterBroadcasterFactory(config, broadcaster);
		framework.setBroadcasterFactory(broadcasterFactory);

		broadcaster.initialize("wicket-atmosphere-tester", config);

		VoidExecutorService sameThreadExecutorService = new VoidExecutorService();
		BroadcasterConfig broadcasterConfig = new BroadcasterConfig(
				sameThreadExecutorService,
				sameThreadExecutorService,
				Executors.newSingleThreadScheduledExecutor(),
				config, "tester-broadcaster-config");
		broadcaster.setBroadcasterConfig(broadcasterConfig);
		broadcasterConfig.setAsyncWriteService(sameThreadExecutorService);
		broadcasterConfig.setExecutorService(sameThreadExecutorService);

		return broadcaster;
	}

	@Override
	public TesterBroadcaster getBroadcaster()
	{
		return (TesterBroadcaster) super.getBroadcaster();
	}
}
