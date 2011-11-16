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
package org.apache.wicket.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.IWritableRequestParameters;
import org.apache.wicket.util.string.StringValue;

/**
 * Mutable mock implementation of {@link IRequestParameters}.
 * 
 * @author Matej Knopp
 */
public class MockRequestParameters implements IWritableRequestParameters
{
	private final Map<String, List<StringValue>> parameters = new HashMap<String, List<StringValue>>();

	@Override
	public Set<String> getParameterNames()
	{
		return Collections.unmodifiableSet(parameters.keySet());
	}

	@Override
	public StringValue getParameterValue(String name)
	{
		List<StringValue> values = parameters.get(name);
		return (values != null && !values.isEmpty()) ? values.get(0)
			: StringValue.valueOf((String)null);
	}

	@Override
	public List<StringValue> getParameterValues(String name)
	{
		List<StringValue> values = parameters.get(name);
		return values != null ? Collections.unmodifiableList(values) : null;
	}

	@Override
	public void setParameterValues(String name, List<StringValue> values)
	{
		parameters.put(name, values);
	}


	/**
	 * Sets value for given key.
	 * 
	 * @param name
	 * @param value
	 */
	public void setParameterValue(String name, String value)
	{
		List<StringValue> list = new ArrayList<StringValue>(1);
		list.add(StringValue.valueOf(value));
		parameters.put(name, list);
	}

	/**
	 * Adds value for given key.
	 * 
	 * @param name
	 * @param value
	 */
	public void addParameterValue(String name, String value)
	{
		List<StringValue> list = parameters.get(name);
		if (list == null)
		{
			list = new ArrayList<StringValue>(1);
			parameters.put(name, list);
		}
		list.add(StringValue.valueOf(value));
	}

	@Override
	public void reset()
	{
		parameters.clear();
	}

}
