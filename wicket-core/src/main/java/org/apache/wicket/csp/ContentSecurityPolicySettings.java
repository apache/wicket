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

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Args;

/**
 * Build the CSP configuration like this:
 * 
 * <pre>
 * {@code
 *  myApplication.getCspSettings().blocking().clear()
 *      .add(CSPDirective.DEFAULT_SRC, CSPDirectiveSrcValue.NONE)
 *      .add(CSPDirective.SCRIPT_SRC, CSPDirectiveSrcValue.SELF)
 *      .add(CSPDirective.IMG_SRC, CSPDirectiveSrcValue.SELF)
 *      .add(CSPDirective.FONT_SRC, CSPDirectiveSrcValue.SELF));
 *
 *  myApplication.getCspSettings().reporting().strict();
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
public class ContentSecurityPolicySettings
{
	// The number of bytes to use for a nonce, 18 will result in a 24 char nonce.
	private static final int NONCE_LENGTH = 18;

	public static final MetaDataKey<String> NONCE_KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final Map<CSPHeaderMode, CSPHeaderConfiguration> configs = new EnumMap<>(
		CSPHeaderMode.class);

	private Predicate<IRequestHandler> protectedFilter = RenderPageRequestHandler.class::isInstance;

	private Supplier<String> nonceCreator;
	
	public ContentSecurityPolicySettings(Application application)
	{
		Args.notNull(application, "application");
		
		nonceCreator = () -> {
				return application.getSecuritySettings().getRandomSupplier().getRandomBase64(NONCE_LENGTH);
			};
	}

	public CSPHeaderConfiguration blocking()
	{
		return configs.computeIfAbsent(CSPHeaderMode.BLOCKING, x -> new CSPHeaderConfiguration());
	}

	public CSPHeaderConfiguration reporting()
	{
		return configs.computeIfAbsent(CSPHeaderMode.REPORT_ONLY,
			x -> new CSPHeaderConfiguration());
	}

	/**
	 * Sets the creator of nonces.
	 * 
	 * @param nonceCreator
	 *            The new creator, must not be null.
	 * @return {@code this} for chaining.
	 */
	public ContentSecurityPolicySettings setNonceCreator(Supplier<String> nonceCreator)
	{
		Args.notNull(nonceCreator, "nonceCreator");
		this.nonceCreator = nonceCreator;
		return this;
	}
	
	/**
	 * Sets the predicate that determines which requests must be protected by the CSP. When the
	 * predicate evaluates to false, the request will not be protected.
	 * 
	 * @param protectedFilter
	 *            The new filter, must not be null.
	 * @return {@code this} for chaining.
	 */
	public ContentSecurityPolicySettings setProtectedFilter(
		Predicate<IRequestHandler> protectedFilter)
	{
		Args.notNull(protectedFilter, "protectedFilter");
		this.protectedFilter = protectedFilter;
		return this;
	}

	/**
	 * Should any request be protected by CSP.
	 *
	 * @param handler
	 * @return <code>true</code> by default for all {@link RenderPageRequestHandler}s
	 * 
	 * @see #setProtectedFilter(Predicate)
	 */
	protected boolean mustProtectRequest(IRequestHandler handler)
	{
		return protectedFilter.test(handler);
	}

	/**
	 * Returns true if any of the headers includes a directive with a nonce.
	 * 
	 * @return If a nonce is used in the CSP.
	 */
	public final boolean isNonceEnabled()
	{
		return configs.values().stream().anyMatch(CSPHeaderConfiguration::isNonceEnabled);
	}

	public String getNonce(RequestCycle cycle)
	{
		IRequestHandler handler = cycle.getActiveRequestHandler();
		
		Page currentPage = IPageRequestHandler.getPage(handler);

		String nonce = cycle.getMetaData(NONCE_KEY);
		if (nonce == null)
		{
			if (currentPage != null)
			{
				nonce = currentPage.getMetaData(NONCE_KEY);
			}
			if (nonce == null)
			{
				nonce = createNonce();
			}
			cycle.setMetaData(NONCE_KEY, nonce);
		}

		if (currentPage != null)
		{
			currentPage.setMetaData(NONCE_KEY, nonce);
		}

		return nonce;
	}

	/**
	 * Create a new nonce.
	 *
	 * @return nonce
	 * 
	 * @see #setNonceCreator(Supplier)
	 */
	protected String createNonce()
	{
		return nonceCreator.get();
	}

	/**
	 * Returns the CSP configuration per {@link CSPHeaderMode}.
	 * 
	 * @return the CSP configuration per {@link CSPHeaderMode}.
	 */
	public Map<CSPHeaderMode, CSPHeaderConfiguration> getConfiguration()
	{
		return Collections.unmodifiableMap(configs);
	}

	/**
	 * Enforce CSP settings on an application.
	 * 
	 * @param application
	 *            application
	 */
	public void enforce(WebApplication application)
	{
		application.getRequestCycleListeners().add(new CSPRequestCycleListener(this));
		application.getHeaderResponseDecorators()
			.add(response -> new CSPNonceHeaderResponseDecorator(response, this));
		application.mount(new ReportCSPViolationMapper(this));
	}

	/**
	 * Is CSP enabled.
	 */
	public boolean isEnabled()
	{
		return configs.values().stream().anyMatch(CSPHeaderConfiguration::isSet);
	}
}
