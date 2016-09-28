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

import org.apache.wicket.Page;
import org.apache.wicket.atmosphere.AtmosphereBehavior;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.util.tester.WicketTester;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.handler.AtmosphereHandlerAdapter;

/**
 * A specialization that doesn't use Meteor to create AtmosphereResource
 * but creates it manually by using the WicketTester's http request and response
 */
class TesterAtmosphereBehavior extends AtmosphereBehavior
{
	private final EventBus eventBus;
	private final WicketTester wicketTester;

	TesterAtmosphereBehavior(WicketTester wicketTester, EventBus eventBus)
	{
		this.wicketTester = wicketTester;
		this.eventBus = eventBus;
	}

	@Override
	public void onRequest()
	{
		TesterBroadcaster broadcaster = (TesterBroadcaster) eventBus.getBroadcaster();

		AtmosphereResource atmosphereResource = new AtmosphereResourceImpl();
		AtmosphereRequest atmosphereRequest = AtmosphereRequest.wrap(wicketTester.getRequest());
		AtmosphereResponse atmosphereResponse = AtmosphereResponse.wrap(wicketTester.getResponse());
		TesterAsyncSupport asyncSupport = new TesterAsyncSupport();
		atmosphereResource.initialize(broadcaster.getApplicationConfig(), broadcaster, atmosphereRequest, atmosphereResponse,
				asyncSupport, new AtmosphereHandlerAdapter());

		atmosphereResource.setBroadcaster(broadcaster);
		broadcaster.addAtmosphereResource(atmosphereResource);

		String uuid = atmosphereResource.uuid();
		Page page = getComponent().getPage();

		page.setMetaData(ATMOSPHERE_UUID, uuid);
		eventBus.registerPage(uuid, page);
	}
}
