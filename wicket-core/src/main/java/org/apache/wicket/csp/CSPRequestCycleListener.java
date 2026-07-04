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
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;

import static org.apache.wicket.request.IRequestHandlerDelegate.unwrap;

/**
 * An {@link IRequestCycleListener} that adds {@code Content-Security-Policy} and/or
 * {@code Content-Security-Policy-Report-Only} headers based on the supplied configuration.
 *
 * @author Sven Haster
 * @author Emond Papegaaij
 * @see CSPHeaderWriter
 * @deprecated
 */
public class CSPRequestCycleListener implements IRequestCycleListener
{
	private final ContentSecurityPolicySettings settings;

	public CSPRequestCycleListener(ContentSecurityPolicySettings settings)
	{
		this.settings = settings;
	}

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
	{
		// WICKET-7028- this is needed for redirect to buffer use case.
		protect(cycle, handler);
	}

	@Override
	public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler)
	{
		protect(cycle, handler);
	}

	protected void protect(RequestCycle cycle, IRequestHandler handler)
	{
		/*
		 * page request handlers are protected during page rendering,
		 * not at this point.
		 * it's important to avoid hooking the protection here
		 * because to call response.reset() during rendering is a
		 * valid use case that would end up undoing the protection
		 * made inside this request listener
		 */
		if (unwrap(handler) instanceof RenderPageRequestHandler)
		{
			return;
		}

		if (!(cycle.getResponse() instanceof WebResponse))
		{
			return;
		}

		settings.getHeaderWriter().write((WebResponse)cycle.getResponse(), handler);
	}

	/**
	 * Must the given handler be protected.
	 *
	 * @param handler
	 *            handler
	 * @return <code>true</code> if must be protected
	 * @see ContentSecurityPolicySettings#mustProtectRequest(IRequestHandler)
	 */
	protected boolean mustProtect(IRequestHandler handler)
	{
		if (handler instanceof IRequestHandlerDelegate)
		{
			return mustProtect(((IRequestHandlerDelegate)handler).getDelegateHandler());
		}

		return settings.mustProtectRequest(handler);
	}

}
