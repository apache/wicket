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
package org.apache.wicket.coep;

import org.apache.wicket.coop.CrossOriginOpenerPolicyRequestCycleListener;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Sets <a href="https://wicg.github.io/cross-origin-embedder-policy/">Cross-Origin Embedder
 * Policy</a> (COEP) headers on the responses based on the mode specified by
 * {@link CrossOriginEmbedderPolicyConfiguration}. COEP can be enabled in <code>REPORTING</code>
 * mode which will set the headers as <code>Cross-Origin-Embedder-Policy-Report-Only</code> or
 * <code>ENFORCING</code> mode which will set the header as
 * <code>Cross-Origin-Embedder-Policy</code>. The header is not set for the paths that are exempted
 * from COEP. The only valid value of COEP is <code>require-corp</code>, so if the listener is
 * enabled the policy value will be specified as so.
 *
 * COEP prevents a document from loading any non-same-origin resources which don't explicitly grant
 * the document permission to be loaded. Using COEP and COOP together allows developers to safely
 * use powerful features such as <code>SharedArrayBuffer</code>,
 * <code>performance.measureMemory()</code>, and the JS Self-Profiling API.See
 * {@link CrossOriginOpenerPolicyRequestCycleListener} for instructions on how to enable COOP.
 * Read more about cross-origin isolation on
 * <a href="https://web.dev/why-coop-coep/">https://web.dev/why-coop-coep/</a>
 *
 * 
 * @author Santiago Diaz - saldiaz@google.com
 * @author Ecenaz Jen Ozmen - ecenazo@google.com
 *
 * @see CrossOriginEmbedderPolicyConfiguration
 * @see org.apache.wicket.settings.SecuritySettings
 */
public class CrossOriginEmbedderPolicyRequestCycleListener implements IRequestCycleListener
{
	private static final Logger log = LoggerFactory.getLogger(CrossOriginEmbedderPolicyRequestCycleListener.class);

	static final String REQUIRE_CORP = "require-corp";

	private CrossOriginEmbedderPolicyConfiguration coepConfig;

	public CrossOriginEmbedderPolicyRequestCycleListener(CrossOriginEmbedderPolicyConfiguration coepConfig)
	{
		this.coepConfig = Args.notNull(coepConfig, "coepConfig");
	}

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
	{
		final Object containerRequest = cycle.getRequest().getContainerRequest();
		if (containerRequest instanceof HttpServletRequest)
		{
			HttpServletRequest request = (HttpServletRequest) containerRequest;
			String path = request.getContextPath();
			final String coepHeaderName = coepConfig.getCoepHeader();

			if (coepConfig.getExemptions().contains(path))
			{
				log.debug("Request path {} is exempted from COEP, no '{}' header added", path, coepHeaderName);
				return;
			}

			if (cycle.getResponse() instanceof WebResponse)
			{
				WebResponse webResponse = (WebResponse) cycle.getResponse();
				if (webResponse.isHeaderSupported())
				{
					webResponse.setHeader(coepHeaderName, REQUIRE_CORP);
				}
			}
		}
	}
}
