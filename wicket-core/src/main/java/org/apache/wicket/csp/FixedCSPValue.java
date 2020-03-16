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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.Strings;

/**
 * A simple CSP value that renders the string specified.
 * 
 * @author papegaaij
 */
public class FixedCSPValue implements CSPRenderable
{
	private final String value;

	/**
	 * Creates a new {@code FixedCSPValue} for the given value.
	 * 
	 * @param value
	 *            the value to render;
	 */
	public FixedCSPValue(String value)
	{
		if (Strings.isEmpty(value))
		{
			throw new IllegalArgumentException("CSP directive cannot have empty or null values");
		}
		this.value = value;
	}

	@Override
	public String render(ContentSecurityPolicySettings settings, RequestCycle cycle)
	{
		return value;
	}
	
	@Override
	public void checkValidityForSrc()
	{
		String strValue = value;
		if ("data:".equals(strValue) || "https:".equals(strValue))
		{
			return;
		}

		// strip off "*." so "*.example.com" becomes "example.com" and we can check if
		// it is a valid uri
		if (strValue.startsWith("*."))
		{
			strValue = strValue.substring(2);
		}

		try
		{
			new URI(strValue);
		}
		catch (URISyntaxException urise)
		{
			throw new IllegalArgumentException("Illegal URI for -src directive", urise);
		}
	}
	
	@Override
	public String toString()
	{
		return value;
	}
}
