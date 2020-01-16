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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.request.cycle.RequestCycle;

/**
 * 
 * @author papegaaij
 */
public class CSPHeaderConfiguration
{
	private Map<CSPDirective, List<CSPRenderable>> directives = new EnumMap<>(CSPDirective.class);

	private boolean addLegacyHeaders = false;

	public CSPHeaderConfiguration()
	{
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
	 * Adds the given values to the CSP directive on this configuraiton.
	 * 
	 * @param directive
	 *            The directive to add the values to.
	 * @param values
	 *            The values to add.
	 */
	public CSPHeaderConfiguration addDirective(CSPDirective directive, CSPRenderable... values)
	{
		for (CSPRenderable value : values)
		{
			doAddDirective(directive, value);
		}
		return this;
	}

	/**
	 * Adds a free-form value to a directive for the CSP header. This is primarily meant to used for
	 * URIs.
	 * 
	 * @param directive
	 *            The directive to add the values to.
	 * @param values
	 *            The values to add.
	 */
	public CSPHeaderConfiguration addDirective(CSPDirective directive, String... values)
	{
		for (String value : values)
		{
			doAddDirective(directive, new FixedCSPDirective(value));
		}
		return this;
	}

	/**
	 * @return true if this {@code CSPHeaderConfiguration} has any directives configured.
	 */
	public boolean isSet()
	{
		return !directives.isEmpty();
	}

	@SuppressWarnings("deprecation")
	private CSPHeaderConfiguration doAddDirective(CSPDirective directive, CSPRenderable value)
	{
		// Add backwards compatible frame-src
		// see http://caniuse.com/#feat=contentsecuritypolicy2
		if (CSPDirective.CHILD_SRC.equals(directive))
		{
			doAddDirective(CSPDirective.FRAME_SRC, value);
		}
		List<CSPRenderable> values = directives.computeIfAbsent(directive, x -> new ArrayList<>());
		directive.checkValueForDirective(value, values);
		values.add(value);
		return this;
	}

	/**
	 * Renders this {@code CSPHeaderConfiguration} into an HTTP header. The returned String will be
	 * in the form {@code "key1 value1a value1b; key2 value2a; key3 value3a value3b value3c"}.
	 * 
	 * @param listener
	 *            The {@link CSPSettingRequestCycleListener} that renders the header.
	 * @param cycle
	 *            The current {@link RequestCycle}.
	 * @return the rendered header.
	 */
	public String renderHeaderValue(CSPSettingRequestCycleListener listener, RequestCycle cycle)
	{
		return directives.entrySet()
			.stream()
			.map(e -> e.getKey().getValue() + " "
				+ e.getValue()
					.stream()
					.map(r -> r.render(listener, cycle))
					.collect(Collectors.joining(" ")))
			.collect(Collectors.joining("; "));
	}
}
