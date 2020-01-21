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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.core.request.handler.BufferedResponseRequestHandler;
import org.apache.wicket.core.request.handler.IPageClassRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.settings.SecuritySettings;
import org.apache.wicket.util.lang.Args;

/**
 * An {@link IRequestCycleListener} that adds {@code Content-Security-Policy} and/or
 * {@code Content-Security-Policy-Report-Only} headers based on the supplied configuration.
 *
 * Build the CSP configuration like this:
 * 
 * <pre>
 * {@code
 *  myApplication.getCSP().blocking().clear()
 *      .add(CSPDirective.DEFAULT_SRC, CSPDirectiveSrcValue.NONE)
 *      .add(CSPDirective.SCRIPT_SRC, CSPDirectiveSrcValue.SELF)
 *      .add(CSPDirective.IMG_SRC, CSPDirectiveSrcValue.SELF)
 *      .add(CSPDirective.FONT_SRC, CSPDirectiveSrcValue.SELF));
 *
 *  myApplication.getCSP().reporting().strict();
 * 	}
 * </pre>
 * 
 * See {@link CSPHeaderConfiguration} for more details on specifying the configuration.
 *
 * @see <a href="https://www.w3.org/TR/CSP2/">https://www.w3.org/TR/CSP2</a>
 * @see <a href=
 *      "https://developer.mozilla.org/en-US/docs/Web/Security/CSP">https://developer.mozilla.org/en-US/docs/Web/Security/CSP</a>
 *
 * @author Sven Haster
 * @author Emond Papegaaij
 */
public class ContentSecurityPolicyEnforcer implements IRequestCycleListener
{
	// The number of bytes to use for a nonce, 12 will result in a 16 char nonce.
	private static final int NONCE_LENGTH = 12;

	public static MetaDataKey<String> NONCE_KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final Application application;
	
	private Map<CSPHeaderMode, CSPHeaderConfiguration> configs = new HashMap<>();
	
	private Predicate<IPageClassRequestHandler> protectedPageFilter = handler -> true;

	public ContentSecurityPolicyEnforcer(Application application)
	{
		this.application = Args.notNull(application, "application");
	}

	public CSPHeaderConfiguration blocking()
	{
		return configs.computeIfAbsent(CSPHeaderMode.BLOCKING, x -> new CSPHeaderConfiguration());
	}

	public CSPHeaderConfiguration reporting()
	{
		return configs.computeIfAbsent(CSPHeaderMode.REPORT_ONLY, x -> new CSPHeaderConfiguration());
	}
	
	/**
	 * Sets the predicate that determines which requests must be protected by the CSP. When the
	 * predicate evaluates to false, the request for the page will not be protected.
	 * 
	 * @param protectedPageFilter
	 *            The new filter, must not be null.
	 * @return {@code this} for chaining.
	 */
	public ContentSecurityPolicyEnforcer
			setProtectedPageFilter(Predicate<IPageClassRequestHandler> protectedPageFilter)
	{
		Args.notNull(protectedPageFilter, "protectedPageFilter");
		this.protectedPageFilter = protectedPageFilter;
		return this;
	}

	protected boolean mustProtect(IRequestHandler handler)
	{
		if (handler instanceof IRequestHandlerDelegate)
		{
			return mustProtect(((IRequestHandlerDelegate) handler).getDelegateHandler());
		}
		if (handler instanceof IPageClassRequestHandler)
		{
			return mustProtectPageRequest((IPageClassRequestHandler) handler);
		}
		return !(handler instanceof BufferedResponseRequestHandler);
	}

	protected boolean mustProtectPageRequest(IPageClassRequestHandler handler)
	{
		return protectedPageFilter.test(handler);
	}

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
	{
		if (!mustProtect(handler) || !(cycle.getResponse() instanceof WebResponse))
		{
			return;
		}

		WebResponse webResponse = (WebResponse) cycle.getResponse();
		if (!webResponse.isHeaderSupported())
		{
			return;
		}

		configs.entrySet().stream().filter(entry -> entry.getValue().isSet()).forEach(entry -> {
			CSPHeaderMode mode = entry.getKey();
			CSPHeaderConfiguration config = entry.getValue();
			String headerValue = config.renderHeaderValue(this, cycle);
			webResponse.setHeader(mode.getHeader(), headerValue);
			if (config.isAddLegacyHeaders())
			{
				webResponse.setHeader(mode.getLegacyHeader(), headerValue);
			}
		});
	}
	
	/**
	 * Returns true if any of the headers includes a directive with a nonce.
	 * 
	 * @return If a nonce is used in the CSP.
	 */
	public boolean isNonceEnabled()
	{
		return configs.values().stream().anyMatch(CSPHeaderConfiguration::isNonceEnabled);
	}

	public String getNonce(RequestCycle cycle)
	{
		String nonce = cycle.getMetaData(NONCE_KEY);
		if (nonce == null)
		{
			nonce = getSecuritySettings().getRandomSupplier().getRandomBase64(NONCE_LENGTH);
			cycle.setMetaData(NONCE_KEY, nonce);
		}
		return nonce;
	}

	private SecuritySettings getSecuritySettings()
	{
		return application.getSecuritySettings();
	}
}
