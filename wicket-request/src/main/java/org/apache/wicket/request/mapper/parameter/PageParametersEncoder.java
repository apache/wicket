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
package org.apache.wicket.request.mapper.parameter;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.Url.QueryParameter;
import org.apache.wicket.util.string.Strings;

/**
 * Simple encoder with direct indexed/named parameters mapping.
 * 
 * @author Matej Knopp
 */
public class PageParametersEncoder implements IPageParametersEncoder
{
	/**
	 * Construct.
	 */
	public PageParametersEncoder()
	{
	}

	@Override
	public PageParameters decodePageParameters(final Url url)
	{
		PageParameters parameters = new PageParameters();

		int i = 0;
		for (String s : url.getSegments())
		{
			parameters.set(i, s);
			++i;
		}

		for (QueryParameter p : url.getQueryParameters())
		{
			String parameterName = p.getName();
			if (Strings.isEmpty(parameterName) == false)
			{
				parameters.add(parameterName, p.getValue(), INamedParameters.Type.QUERY_STRING);
			}
		}

		return parameters.isEmpty() ? null : parameters;
	}

	@Override
	public Url encodePageParameters(final PageParameters pageParameters)
	{
		Url url = new Url();

		if (pageParameters != null)
		{
			for (int i = 0; i < pageParameters.getIndexedCount(); ++i)
			{
				url.getSegments().add(pageParameters.get(i).toString());
			}

			for (PageParameters.NamedPair pair : pageParameters.getAllNamed())
			{
				QueryParameter param = new QueryParameter(pair.getKey(), pair.getValue());
				url.getQueryParameters().add(param);
			}
		}

		return url;
	}
}
