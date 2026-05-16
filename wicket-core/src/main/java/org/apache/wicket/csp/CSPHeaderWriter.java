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
package org.apache.wicket.csp;

import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;

import static org.apache.wicket.request.IRequestHandlerDelegate.unwrap;

/**
 * Adds {@code Content-Security-Policy} and/or {@code Content-Security-Policy-Report-Only} headers
 * based on the supplied configuration.
 *
 * @author Sven Haster
 * @author Emond Papegaaij
 */
public class CSPHeaderWriter
{
	private final ContentSecurityPolicySettings settings;

	public CSPHeaderWriter(ContentSecurityPolicySettings settings)
	{
		this.settings = settings;
	}

	public void write(WebResponse webResponse, IRequestHandler handler)
	{
		var cycle = RequestCycle.get();
		if (!settings.mustProtectRequest(handler))
		{
			return;
		}

		if (!webResponse.isHeaderSupported())
		{
			return;
		}

		settings.getConfiguration().entrySet().stream().filter(entry -> entry.getValue().isSet())
			.forEach(entry -> {
				CSPHeaderMode mode = entry.getKey();
				CSPHeaderConfiguration config = entry.getValue();
				String headerValue = config.renderHeaderValue(settings, cycle);
				webResponse.setHeader(mode.getHeader(), headerValue);
				if (config.isAddLegacyHeaders())
				{
					webResponse.setHeader(mode.getLegacyHeader(), headerValue);
				}
			});
	}

}