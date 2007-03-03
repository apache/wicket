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
package wicket.protocol.http.portlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.Application;
import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.protocol.http.IRequestLogger;

/**
 * A RequestCycleProcessor for portlet render requests. The events are not
 * processed in the render phase.
 * 
 * @see PortletRequestCycle
 * 
 * @author Janne Hietam&auml;ki
 * 
 */
public class PortletRenderRequestCycleProcessor extends AbstractPortletRequestCycleProcessor
{

	/** log. */
	private static final Logger log = LoggerFactory
			.getLogger(PortletRenderRequestCycleProcessor.class);

	/**
	 * Construct.
	 */
	public PortletRenderRequestCycleProcessor()
	{
	}

	/**
	 * Process only PortletMode and WindowState changes in the RenderRequests
	 */
	@Override
	public void processEvents(final RequestCycle requestCycle)
	{
		PortletPage<?> page = (PortletPage<?>)requestCycle.getRequest().getPage();
		if (page != null)
		{
			PortletRequestCycle cycle = (PortletRequestCycle)requestCycle;
			page.setPortletMode(cycle.getPortletRequest().getPortletRequest().getPortletMode());
			page.setWindowState(cycle.getPortletRequest().getPortletRequest().getWindowState());
		}
	}

	/**
	 * @see wicket.request.AbstractRequestCycleProcessor#respond(wicket.RequestCycle)
	 */
	@Override
	public void respond(RequestCycle requestCycle)
	{
		IRequestTarget requestTarget = requestCycle.getRequestTarget();
		if (requestTarget != null)
		{
			IRequestLogger logger = Application.get().getRequestLogger();
			if (logger != null)
			{
				logger.logResponseTarget(requestTarget);
			}

			respondHeaderContribution(requestCycle, requestTarget);
			requestTarget.respond(requestCycle);
		}
	}

	/**
	 * Handle header contribution.
	 * 
	 * @param requestCycle
	 *            The request cycle
	 * @param requestTarget
	 *            The request target
	 */
	private void respondHeaderContribution(final RequestCycle requestCycle,
			final IRequestTarget requestTarget)
	{
		// TODO no idea how this should work now; seems this needs a forward
		// port from 1.3. Though header contributions aren't officially
		// supported by the portlet spec to begin with, so I wonder how well
		// that code works in the first place (Eelco)
	}
}