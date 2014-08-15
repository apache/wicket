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

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.atmosphere.AtmosphereBehavior;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.util.tester.WicketTester;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.HeaderConfig;

/**
 * A helper for testing Atmosphere enabled pages
 */
public class AtmosphereTester
{
	/**
	 * The EventBus that will be used to post/push messages
	 * to the suspended http response
	 */
	private final EventBus eventBus;

	private final WicketTester wicketTester;
	
	/**
	 * The response which will be suspended by Atmosphere and
	 * all pushes/post will go to
	 */
	private MockHttpServletResponse suspendedResponse;

	private MockHttpServletResponse lastResponse;
	
	/**
	 * Constructor.
	 *
	 * @param wicketTester
	 *          The testing helper
	 * @param page
	 *          The page to test
	 */
	public AtmosphereTester(final WicketTester wicketTester, Page page)
	{
		this.wicketTester = wicketTester;

		WebApplication application = wicketTester.getApplication();

		TesterBroadcaster broadcaster = createBroadcaster();

		if (EventBus.isInstalled(application))
		{
			this.eventBus = EventBus.get(application);
			this.eventBus.setBroadcaster(broadcaster);
		}
		else
		{
			this.eventBus = new EventBus(application, broadcaster);
		}

		initialize(wicketTester, page);
	}

	private void initialize(final WicketTester wicketTester, Page page)
	{
		// remove any already installed AtmosphereBehaviors on the page
		List<AtmosphereBehavior> behaviors = page.getBehaviors(AtmosphereBehavior.class);
		page.remove(behaviors.toArray(new AtmosphereBehavior[behaviors.size()]));

		// install AtmosphereBehavior that doesn't use Meteor
		AtmosphereBehavior atmosphereBehavior = new TesterAtmosphereBehavior(wicketTester, eventBus);
		page.add(atmosphereBehavior);

		// start the page to collect all @Subscribe methods in the component hierarchy
		wicketTester.startPage(page);

		// pretend it is a websocket connection
		wicketTester.getRequest().setHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT, AtmosphereResource.TRANSPORT.WEBSOCKET.name());

		// start the "upgrade" connection
		suspendedResponse = wicketTester.getResponse();
		wicketTester.executeBehavior(atmosphereBehavior);
	}

	private TesterBroadcaster createBroadcaster()
	{
		TesterBroadcaster broadcaster = new TesterBroadcaster();

		AtmosphereFramework framework = new AtmosphereFramework();
		AtmosphereConfig config = new AtmosphereConfig(framework);

		TesterBroadcasterFactory broadcasterFactory = new TesterBroadcasterFactory(config, broadcaster);
		framework.setBroadcasterFactory(broadcasterFactory);

		broadcaster.initialize("wicket-atmosphere-tester", config);

		return broadcaster;
	}

	/**
	 * @return The collected so far pushed data in the suspended response
	 */
	public String getPushedResponse()
	{
		return suspendedResponse != null ? suspendedResponse.getDocument() : null;
	}

	/**
	 * Resets the suspended response to be empty
	 * @return this instance, for chaining
	 */
	public AtmosphereTester resetResponse()
	{
		if (suspendedResponse != null)
		{
			suspendedResponse.reset();
		}
		return this;
	}

	/**
	 * Posts a message to all suspended responses
	 *
	 * @param message
	 *          The message to push
	 * @return this instance, for chaining
	 */
	public AtmosphereTester post(Object message)
	{
		eventBus.post(message);
		return this;
	}

	/**
	 * Posts a message to the suspended response with the given uuid
	 *
	 * @param message
	 *          The message to push
	 * @param resourceUuid
	 *          The identifier of the suspended http response
	 * @return this instance, for chaining
	 */
	public AtmosphereTester post(Object message, String resourceUuid)
	{
		eventBus.post(message, resourceUuid);
		return this;
	}

	/**
	 * Switches the current <em>lastResponse</em> with the <em>suspendedResponse</em>
	 * so the application test can use WicketTester's assert methods.
	 *
	 * Note: Make sure to call {@linkplain #switchOffTestMode()} later to be able to
	 * assert on non-Atmosphere related responses
	 *
	 * @return this instance, for chaining
	 */
	public AtmosphereTester switchOnTestMode()
	{
		lastResponse = wicketTester.getLastResponse();
		wicketTester.setLastResponse(suspendedResponse);
		return this;
	}

	/**
	 * Sets back the <em>lastResponse</em> with the saved one by {@link #switchOnTestMode()}.
	 *
	 * @return this instance, for chaining
	 */
	public AtmosphereTester switchOffTestMode()
	{
		if (lastResponse != null)
		{
			wicketTester.setLastResponse(lastResponse);
			lastResponse = null;
		}
		return this;
	}
}
