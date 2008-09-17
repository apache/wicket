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
package org.apache._wicket.request.encoder;

import java.util.List;

import org.apache._wicket.PageParameters;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.Url.QueryParameter;
import org.apache.wicket.util.string.StringValue;

/**
 * Simple encoder with direct indexed/named parameters mapping.
 * 
 * @author Matej Knopp
 */
public class SimplePageParametersEncoder implements PageParametersEncoder
{
	/**
	 * Construct.
	 */
	public SimplePageParametersEncoder()
	{
	}

	public PageParameters decodePageParameters(Url url)
	{
		PageParameters parameters = new PageParameters();
		
		int i = 0;
		for (String s : url.getSegments())
		{
			parameters.setIndexedParameter(i, s);
			++i;
		}
		
		for (QueryParameter p : url.getQueryParameters())
		{
			parameters.addNamedParameter(p.getName(), p.getValue());
		}
		
		return parameters;
	}

	public Url encodePageParameters(PageParameters pageParameters)
	{
		Url url = new Url();
		
		for (int i = 0; i < pageParameters.getIndexedParamsCount(); ++i)
		{
			url.getSegments().add(pageParameters.getIndexedParameter(i).toString());
		}
		
		for (String key : pageParameters.getNamedParameterKeys())
		{
			List<StringValue> values = pageParameters.getNamedParameters(key);
			if (values != null)
			{
				for (StringValue value : values)
				{
					QueryParameter param = new QueryParameter(key, value.toString());
					url.getQueryParameters().add(param);
				}
			}
		}
		
		return url;
	}

}
