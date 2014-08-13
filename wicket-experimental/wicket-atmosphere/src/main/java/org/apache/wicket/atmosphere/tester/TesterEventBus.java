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

import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.protocol.http.WebApplication;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.BroadcasterLifeCyclePolicy;
import org.atmosphere.cpr.DefaultBroadcasterFactory;
import org.atmosphere.util.SimpleBroadcaster;

/**
 *
 */
public class TesterEventBus extends EventBus
{
	AtmosphereFramework framework = new AtmosphereFramework();
	AtmosphereConfig config = new AtmosphereConfig(framework);

	public TesterEventBus(WebApplication application)
	{
		super(application, new SimpleBroadcaster());

		framework.setBroadcasterFactory(new TesterBroadcasterFactory(config));

		getBroadcaster().initialize("wicket-atmosphere-tester", config);
	}

	@Override
	public SimpleBroadcaster getBroadcaster()
	{
		return (SimpleBroadcaster) super.getBroadcaster();
	}

	private static class TesterBroadcasterFactory extends DefaultBroadcasterFactory
	{
		protected TesterBroadcasterFactory(AtmosphereConfig c)
		{
			super(SimpleBroadcaster.class, BroadcasterLifeCyclePolicy.ATMOSPHERE_RESOURCE_POLICY.NEVER.name(), c);

			// expose myself as BroadcasterFactory.getDefault();
			factory = this;
		}
	}
}
