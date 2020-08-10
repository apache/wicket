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

import static org.apache.wicket.csp.CSPDirective.BASE_URI;
import static org.apache.wicket.csp.CSPDirective.CHILD_SRC;
import static org.apache.wicket.csp.CSPDirective.CONNECT_SRC;
import static org.apache.wicket.csp.CSPDirective.DEFAULT_SRC;
import static org.apache.wicket.csp.CSPDirective.FONT_SRC;
import static org.apache.wicket.csp.CSPDirective.IMG_SRC;
import static org.apache.wicket.csp.CSPDirective.MANIFEST_SRC;
import static org.apache.wicket.csp.CSPDirective.REPORT_URI;
import static org.apache.wicket.csp.CSPDirective.SCRIPT_SRC;
import static org.apache.wicket.csp.CSPDirective.STYLE_SRC;
import static org.apache.wicket.csp.CSPDirectiveSrcValue.NONCE;
import static org.apache.wicket.csp.CSPDirectiveSrcValue.NONE;
import static org.apache.wicket.csp.CSPDirectiveSrcValue.SELF;
import static org.apache.wicket.csp.CSPDirectiveSrcValue.STRICT_DYNAMIC;
import static org.apache.wicket.csp.CSPDirectiveSrcValue.UNSAFE_EVAL;
import static org.apache.wicket.csp.CSPDirectiveSrcValue.UNSAFE_INLINE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.request.cycle.RequestCycle;

/**
 * {@code CSPHeaderConfiguration} contains the configuration for a Content-Security-Policy header.
 * This configuration is constructed using the available {@link CSPDirective}s. An number of default
 * profiles is provided. These profiles can be used as a basis for a specific CSP. Extra directives
 * can be added or existing directives modified.
 *
 * @author papegaaij
 * @see <a href="https://www.w3.org/TR/CSP2/">https://www.w3.org/TR/CSP2</a>
 * @see <a href=
 *      "https://developer.mozilla.org/en-US/docs/Web/Security/CSP">https://developer.mozilla.org/en-US/docs/Web/Security/CSP</a>
 */
public class CSPHeaderConfiguration
{
	public static final String CSP_VIOLATION_REPORTING_URI = "cspviolation";

	private final Map<CSPDirective, List<CSPRenderable>> directives = new EnumMap<>(CSPDirective.class);

	private boolean addLegacyHeaders = false;

	private boolean nonceEnabled = false;

	private String reportUriMountPath = null;

	/**
	 * Removes all directives from the CSP, returning an empty configuration.
	 *
	 * @return {@code this} for chaining.
	 */
	public CSPHeaderConfiguration disabled()
	{
		return clear();
	}

	/**
	 * Builds a CSP configuration with the following directives: {@code default-src 'none';}
	 * {@code script-src 'self' 'unsafe-inline' 'unsafe-eval';}
	 * {@code style-src 'self' 'unsafe-inline';} {@code img-src 'self';} {@code connect-src 'self';}
	 * {@code font-src 'self';} {@code manifest-src 'self';} {@code child-src 'self';}
	 * {@code frame-src 'self'} {@code base-uri 'self'}. This will allow resources to be loaded
	 * from {@code 'self'} (the current host). In addition, unsafe inline Javascript,
	 * {@code eval()} and inline CSS is allowed.
	 *
	 * It is recommended to not allow {@code unsafe-inline} or {@code unsafe-eval}, because those
	 * can be used to trigger XSS attacks in your application (often in combination with another
	 * bug). Because older application often rely on inline scripting and styling, this CSP can be
	 * used as a stepping stone for older Wicket applications, before switching to {@link #strict}.
	 * Using a CSP with unsafe directives is still more secure than using no CSP at all.
	 *
	 * @return {@code this} for chaining.
	 */
	public CSPHeaderConfiguration unsafeInline()
	{
		return clear().add(DEFAULT_SRC, NONE)
			.add(SCRIPT_SRC, SELF, UNSAFE_INLINE, UNSAFE_EVAL)
			.add(STYLE_SRC, SELF, UNSAFE_INLINE)
			.add(IMG_SRC, SELF)
			.add(CONNECT_SRC, SELF)
			.add(FONT_SRC, SELF)
			.add(MANIFEST_SRC, SELF)
			.add(CHILD_SRC, SELF)
			.add(BASE_URI, SELF);
	}

	/**
	 * Builds a strict, very secure CSP configuration with the following directives:
	 * {@code default-src 'none';} {@code script-src 'strict-dynamic' 'nonce-XYZ';}
	 * {@code style-src 'nonce-XYZ';} {@code img-src 'self';} {@code connect-src 'self';}
	 * {@code font-src 'self';} {@code manifest-src 'self';} {@code child-src 'self';}
	 * {@code frame-src 'self'} {@code base-uri 'self'}. This will allow most resources to be loaded
	 * from {@code 'self'} (the current host). Scripts and styles are only allowed when rendered with
	 * the correct nonce.
	 * Wicket will automatically add the nonces to the {@code script} and {@code link} (CSS)
	 * elements and to the headers.
	 *
	 * @return {@code this} for chaining.
	 */
	public CSPHeaderConfiguration strict()
	{
		return clear().add(DEFAULT_SRC, NONE)
			.add(SCRIPT_SRC, STRICT_DYNAMIC, NONCE)
			.add(STYLE_SRC, NONCE)
			.add(IMG_SRC, SELF)
			.add(CONNECT_SRC, SELF)
			.add(FONT_SRC, SELF)
			.add(MANIFEST_SRC, SELF)
			.add(CHILD_SRC, SELF)
			.add(BASE_URI, SELF);
	}

	/**
	 * Configures the CSP to report violations back at the application.
	 *
	 * WARNING: CSP reporting can generate a lot of traffic. A single page load can trigger multiple
	 * violations and flood your logs or even DDoS your server. In addition, it is an open endpoint
	 * for your application and can be used by an attacker to flood your application logs. Do not
	 * enable this feature on a production application unless you take the needed precautions to
	 * prevent this.
	 *
	 * @return {@code this} for chaining
	 * @see <a href=
	 *      "https://scotthelme.co.uk/just-how-much-traffic-can-you-generate-using-csp">https://scotthelme.co.uk/just-how-much-traffic-can-you-generate-using-csp</a>
	 */
	public CSPHeaderConfiguration reportBack()
	{
		return reportBackAt(CSP_VIOLATION_REPORTING_URI);
	}

	/**
	 * Configures the CSP to report violations at the specified relative URI.
	 *
	 * WARNING: CSP reporting can generate a lot of traffic. A single page load can trigger multiple
	 * violations and flood your logs or even DDoS your server. In addition, it is an open endpoint
	 * for your application and can be used by an attacker to flood your application logs. Do not
	 * enable this feature on a production application unless you take the needed precautions to
	 * prevent this.
	 *
	 * @param mountPath
	 *            The path to report the violations at.
	 * @return {@code this} for chaining
	 * @see <a href=
	 *      "https://scotthelme.co.uk/just-how-much-traffic-can-you-generate-using-csp">https://scotthelme.co.uk/just-how-much-traffic-can-you-generate-using-csp</a>
	 */
	public CSPHeaderConfiguration reportBackAt(String mountPath)
	{
		return add(REPORT_URI, new RelativeURICSPValue(mountPath));
	}

	/**
	 * Returns the report URI mount path.
	 *
	 * @return the report URI mount path.
	 */
	String getReportUriMountPath()
	{
		return reportUriMountPath;
	}

	/**
	 * True when the {@link CSPDirectiveSrcValue#NONCE} is used in one of the directives.
	 *
	 * @return When any of the directives contains a nonce.
	 */
	public boolean isNonceEnabled()
	{
		return nonceEnabled;
	}

	/**
	 * True when legacy headers should be added.
	 *
	 * @return True when legacy headers should be added.
	 */
	public boolean isAddLegacyHeaders()
	{
		return addLegacyHeaders;
	}

	/**
	 * Enable legacy {@code X-Content-Security-Policy} headers for older browsers, such as IE.
	 *
	 * @param addLegacyHeaders
	 *            True when the legacy headers should be added.
	 * @return {@code this} for chaining
	 */
	public CSPHeaderConfiguration setAddLegacyHeaders(boolean addLegacyHeaders)
	{
		this.addLegacyHeaders = addLegacyHeaders;
		return this;
	}

	/**
	 * Removes the given directive from the configuration.
	 *
	 * @param directive
	 *            The directive to remove.
	 * @return {@code this} for chaining
	 */
	public CSPHeaderConfiguration remove(CSPDirective directive)
	{
		directives.remove(directive);
		return recalculateState();
	}

	/**
	 * Adds the given values to the CSP directive on this configuraiton.
	 *
	 * @param directive
	 *            The directive to add the values to.
	 * @param values
	 *            The values to add.
	 * @return {@code this} for chaining
	 */
	public CSPHeaderConfiguration add(CSPDirective directive, CSPRenderable... values)
	{
		for (CSPRenderable value : values)
		{
			doAddDirective(directive, value);
		}
		return recalculateState();
	}

	/**
	 * Adds a free-form value to a directive for the CSP header. This is primarily meant to used for
	 * URIs.
	 *
	 * @param directive
	 *            The directive to add the values to.
	 * @param values
	 *            The values to add.
	 * @return {@code this} for chaining
	 */
	public CSPHeaderConfiguration add(CSPDirective directive, String... values)
	{
		for (String value : values)
		{
			doAddDirective(directive, new FixedCSPValue(value));
		}
		return recalculateState();
	}

	/**
	 * Returns an unmodifiable map of the directives set for this header.
	 * 
	 * @return The directives set for this header.
	 */
	public Map<CSPDirective, List<CSPRenderable>> getDirectives()
	{
		return Collections.unmodifiableMap(directives);
	}

	/**
	 * @return true if this {@code CSPHeaderConfiguration} has any directives configured.
	 */
	public boolean isSet()
	{
		return !directives.isEmpty();
	}

	/**
	 * Removes all CSP directives from the configuration.
	 *
	 * @return {@code this} for chaining.
	 */
	public CSPHeaderConfiguration clear()
	{
		directives.clear();
		return recalculateState();
	}

	private CSPHeaderConfiguration recalculateState()
	{
		nonceEnabled = directives.values()
			.stream()
			.flatMap(List::stream)
			.anyMatch(value -> value == CSPDirectiveSrcValue.NONCE);

		reportUriMountPath = null;
		List<CSPRenderable> reportValues = directives.get(CSPDirective.REPORT_URI);
		if (reportValues != null && !reportValues.isEmpty())
		{
			CSPRenderable reportUri = reportValues.get(0);
			if (reportUri instanceof RelativeURICSPValue)
			{
				reportUriMountPath = reportUri.toString();
			}
		}
		return this;
	}

	private void doAddDirective(CSPDirective directive, CSPRenderable value)
	{
		// Add backwards compatible frame-src
		// see http://caniuse.com/#feat=contentsecuritypolicy2
		if (CSPDirective.CHILD_SRC.equals(directive)
			&& !directives.containsKey(CSPDirective.FRAME_SRC))
		{
			doAddDirective(CSPDirective.FRAME_SRC,
				new ClonedCSPValue(this, CSPDirective.CHILD_SRC));
		}
		List<CSPRenderable> values = directives.computeIfAbsent(directive, x -> new ArrayList<>());
		directive.checkValueForDirective(value, values);
		values.add(value);
	}

	/**
	 * Renders this {@code CSPHeaderConfiguration} into an HTTP header. The returned String will be
	 * in the form {@code "key1 value1a value1b; key2 value2a; key3 value3a value3b value3c"}.
	 *
	 * @param settings
	 *            The {@link ContentSecurityPolicySettings} that renders the header.
	 * @param cycle
	 *            The current {@link RequestCycle}.
	 * @return the rendered header.
	 */
	public String renderHeaderValue(ContentSecurityPolicySettings settings, RequestCycle cycle)
	{
		return directives.entrySet()
			.stream()
			.map(e -> e.getKey().getValue() + " "
				+ e.getValue()
					.stream()
					.map(r -> r.render(settings, cycle))
					.collect(Collectors.joining(" ")))
			.collect(Collectors.joining("; "));
	}
}
