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
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.StringValue;

/**
 * {@link IRequestParameters} implementation that combines other {@link IRequestParameters}s.
 * 
 * @author Matej Knopp
 */
public class CombinedRequestParametersAdapter implements IRequestParameters
{
	private final IRequestParameters parameters[];

	/**
	 * Construct.
	 * 
	 * @param parameters
	 */
	public CombinedRequestParametersAdapter(final IRequestParameters... parameters)
	{
		this.parameters = Args.notNull(parameters, "parameters");
	}

	/**
	 * @see org.apache.wicket.request.IRequestParameters#getParameterNames()
	 */
	@Override
	public Set<String> getParameterNames()
	{
		Set<String> result = new LinkedHashSet<>();
		for (IRequestParameters p : parameters)
		{
			result.addAll(p.getParameterNames());
		}
		return Collections.unmodifiableSet(result);
	}

	/**
	 * @see org.apache.wicket.request.IRequestParameters#getParameterValue(java.lang.String)
	 */
	@Override
	public StringValue getParameterValue(final String name)
	{
		for (IRequestParameters p : parameters)
		{
			StringValue value = p.getParameterValue(name);
			if (!value.isNull())
			{
				return value;
			}
		}
		return StringValue.valueOf((String)null);
	}

	/**
	 * @see org.apache.wicket.request.IRequestParameters#getParameterValues(java.lang.String)
	 */
	@Override
	public List<StringValue> getParameterValues(final String name)
	{
		List<StringValue> result = new ArrayList<>();
		for (IRequestParameters p : parameters)
		{
			List<StringValue> values = p.getParameterValues(name);
			if (values != null)
			{
				for (StringValue v : values)
				{
					result.add(v);
				}
			}
		}

		if (result.isEmpty())
		{
			return null;
		}
		else
		{
			return Collections.unmodifiableList(result);
		}
	}
}
