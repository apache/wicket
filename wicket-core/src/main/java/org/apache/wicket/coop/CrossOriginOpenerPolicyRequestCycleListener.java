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
package org.apache.wicket.coop;

import org.apache.wicket.coep.CrossOriginEmbedderPolicyRequestCycleListener;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Sets <a href="https://github.com/whatwg/html/pull/5334/files">Cross-Origin Opener Policy</a>
 * headers on the responses based on the policy specified by {@link CrossOriginOpenerPolicyConfiguration}. The header
 * is not set for the paths that are exempted from COOP.
 *
 * COOP is a mitigation against cross-origin information leaks and is used to make websites
 * cross-origin isolated. Setting the COOP header allows you to ensure that a top-level window is
 * isolated from other documents by putting them in a different browsing context group, so they
 * cannot directly interact with the top-level window. Using COEP and COOP together allows
 * developers to safely use * powerful features such as <code>SharedArrayBuffer</code>,
 * <code>performance.measureMemory()</code>, * and the JS Self-Profiling API.See
 * {@link CrossOriginEmbedderPolicyRequestCycleListener} for instructions * on how to enable COOP.
 * Read more about cross-origin isolation on
 * <a href="https://web.dev/why-coop-coep/">https://web.dev/why-coop-coep/</a>
 *
 *
 * @author Santiago Diaz - saldiaz@google.com
 * @author Ecenaz Jen Ozmen - ecenazo@google.com
 *
 * @see CrossOriginOpenerPolicyConfiguration
 * @see org.apache.wicket.settings.SecuritySettings
 */
public class CrossOriginOpenerPolicyRequestCycleListener implements IRequestCycleListener
{
	private static final Logger log = LoggerFactory.getLogger(CrossOriginOpenerPolicyRequestCycleListener.class);

	static final String COOP_HEADER = "Cross-Origin-Opener-Policy";

	private CrossOriginOpenerPolicyConfiguration coopConfig;

	public CrossOriginOpenerPolicyRequestCycleListener(CrossOriginOpenerPolicyConfiguration cooopConfig)
	{
		this.coopConfig = cooopConfig;
	}

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
	{
		HttpServletRequest request = (HttpServletRequest)cycle.getRequest().getContainerRequest();
		String path = request.getContextPath();

		if (coopConfig.getExemptions().contains(path))
		{
			log.debug("Request path {} is exempted from COOP, no COOP header added", path);
			return;
		}

		if (cycle.getResponse() instanceof WebResponse)
		{
			WebResponse webResponse = (WebResponse)cycle.getResponse();
			if (webResponse.isHeaderSupported())
			{
				webResponse.setHeader(COOP_HEADER, coopConfig.getHeaderValue());
			}
		}
	}
}
