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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Args;

/**
 * A CSP value that renders the same value as rendered for the specified directive.
 * 
 * @author papegaaij
 */
public class ClonedCSPValue implements CSPRenderable
{
	private final CSPHeaderConfiguration headerConfiguration;

	private final CSPDirective sourceDirective;

	/**
	 * Creates a new {@code ClonedCSPValue} for the given directive.
	 * 
	 * @param headerConfiguration
	 *            the header to clone from;
	 * @param sourceDirective
	 *            the directive to clone;
	 */
	public ClonedCSPValue(CSPHeaderConfiguration headerConfiguration, CSPDirective sourceDirective)
	{
		this.headerConfiguration = Args.notNull(headerConfiguration, "headerConfiguration");
		this.sourceDirective = Args.notNull(sourceDirective, "sourceDirective");
	}

	@Override
	public String render(ContentSecurityPolicySettings settings, RequestCycle cycle)
	{
		List<CSPRenderable> values = headerConfiguration.getDirectives().get(sourceDirective);
		if (values == null)
		{
			throw new IllegalStateException(
				"CSP directive " + sourceDirective + " not set, cannot clone its value.");
		}
		return values.stream().map(r -> r.render(settings, cycle)).collect(Collectors.joining(" "));
	}

	@Override
	public String toString()
	{
		return "clone(" + sourceDirective.getValue() + ")";
	}
}
