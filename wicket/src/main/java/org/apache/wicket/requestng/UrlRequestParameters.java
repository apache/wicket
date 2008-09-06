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
package org.apache.wicket.requestng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.requestng.Url.QueryParameter;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.StringValue;

/**
 * Utility class that expresses query parameters from {@link Url} as {@link RequestParameters}.
 * 
 * @author Matej Knopp
 */
public class UrlRequestParameters implements RequestParameters
{
	private final Url url;

	/**
	 * Construct.
	 * @param url
	 */
	public UrlRequestParameters(Url url)
	{
		if (url == null)
		{
			throw new IllegalArgumentException("Argument 'url' may not be null.");
		}
		this.url = url;
	}
	
	public Set<String> getParameterNames()
	{
		Set<String> result = new HashSet<String>();
		for (QueryParameter parameter : url.getQueryParameters())
		{
			result.add(parameter.getName());
		}
		return Collections.unmodifiableSet(result);
	}

	public StringValue getParameterValue(String name)
	{
		return url.getQueryParameterValue(name);
	}

	public List<StringValue> getParameterValues(String name)
	{
		List<StringValue> values = null;
		for (QueryParameter parameter : url.getQueryParameters())
		{
			if (Objects.equal(name, parameter.getName()))
			{
				if (values == null)
				{
					values = new ArrayList<StringValue>();
				}
				values.add(StringValue.valueOf(parameter.getValue()));
			}
		}
		return values != null ? Collections.unmodifiableList(values) : null;
	}
}
