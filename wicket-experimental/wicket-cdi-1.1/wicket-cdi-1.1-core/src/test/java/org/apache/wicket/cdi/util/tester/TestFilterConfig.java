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
package org.apache.wicket.cdi.util.tester;

import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * @author jsarman
 */
public class TestFilterConfig implements FilterConfig
{

	private String filterName = "CdiApp" + UUID.randomUUID();
	private Map<String, String> params;

	public TestFilterConfig()
	{
		this(null);
	}

	public TestFilterConfig(Map<String, String> initialParams)
	{
		this.params = new TreeMap<String, String>();
		this.params.put("applicationName", "mockApp");
		if (initialParams != null)
		{
			this.params.putAll(initialParams);
		}
	}

	public void put(String paramName, String value)
	{
		params.put(paramName, value);
	}

	public void remove(String paramName)
	{
		params.remove(paramName);
	}

	public void putAll(Map<String, String> additionalParams)
	{
		params.putAll(additionalParams);
	}

	@Override
	public String getFilterName()
	{
		return filterName;
	}

	@Override
	public ServletContext getServletContext()
	{
		throw new UnsupportedOperationException("This is not supported.");
	}

	@Override
	public String getInitParameter(String name)
	{
		return params.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames()
	{
		throw new UnsupportedOperationException("This is not Supported.");
	}
}
