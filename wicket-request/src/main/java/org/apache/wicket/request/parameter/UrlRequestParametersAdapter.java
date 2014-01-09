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
package org.apache.wicket.request.parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.Url.QueryParameter;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.StringValue;

/**
 * Utility class that expresses query parameters from {@link Url} as {@link IRequestParameters}.
 * 
 * @author Matej Knopp
 */
public class UrlRequestParametersAdapter implements IRequestParameters
{
	private final Url url;

	/**
	 * Construct.
	 * 
	 * @param url
	 */
	public UrlRequestParametersAdapter(final Url url)
	{
		Args.notNull(url, "url");

		this.url = url;
	}

	/**
	 * @see org.apache.wicket.request.IRequestParameters#getParameterNames()
	 */
	@Override
	public Set<String> getParameterNames()
	{
		Set<String> result = new LinkedHashSet<>();
		for (QueryParameter parameter : url.getQueryParameters())
		{
			result.add(parameter.getName());
		}
		return Collections.unmodifiableSet(result);
	}

	/**
	 * @see org.apache.wicket.request.IRequestParameters#getParameterValue(java.lang.String)
	 */
	@Override
	public StringValue getParameterValue(final String name)
	{
		return url.getQueryParameterValue(name);
	}

	/**
	 * @see org.apache.wicket.request.IRequestParameters#getParameterValues(java.lang.String)
	 */
	@Override
	public List<StringValue> getParameterValues(final String name)
	{
		List<StringValue> values = null;
		for (QueryParameter parameter : url.getQueryParameters())
		{
			if (Objects.equal(name, parameter.getName()))
			{
				if (values == null)
				{
					values = new ArrayList<>();
				}
				values.add(StringValue.valueOf(parameter.getValue()));
			}
		}
		return values != null ? Collections.unmodifiableList(values) : null;
	}
}
