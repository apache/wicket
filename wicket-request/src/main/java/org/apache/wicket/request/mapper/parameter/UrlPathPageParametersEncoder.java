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

import java.util.Iterator;

import org.apache.wicket.request.Url;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;


/**
 * <p>
 * Encodes page parameters into Url path fragments instead of the query string like the default
 * {@link PageParametersEncoder}. The parameters are encoded in the following format:
 * {@code /param1Name/param1Value/param2Name/param2Value}.
 * </p>
 * <strong>Note</strong>: Because of the nature of the encoder it doesn't support POST request
 * parameters.
 * <p>
 * This used to be the default way of encoding page parameters in 1.4.x applications. Newer 1.5.x+
 * applications use the query string, by default. This class facilitates backwards compatibility and
 * migrations of 1.4.x application to 1.5.x+ codebase.
 * <p>
 * Example usage:
 * {@code mount(new MountedMapper("/myPage", MyPage.class, new UrlPathPageParametersEncoder()); }
 * 
 * @author Chris Colman
 * @author Luniv (on Stack Overflow)
 * @author ivaynberg
 */
public class UrlPathPageParametersEncoder implements IPageParametersEncoder
{
	@Override
	public Url encodePageParameters(PageParameters params)
	{
		Args.notNull(params, "params");
		Args.isTrue(params.getIndexedCount() == 0,
			"This encoder does not support indexed page parameters. Specified parameters: %s",
			params);

		Url url = new Url();

		for (PageParameters.NamedPair pair : params.getAllNamed())
		{
			url.getSegments().add(pair.getKey());
			url.getSegments().add(pair.getValue());
		}

		return url;
	}

	@Override
	public PageParameters decodePageParameters(Url url)
	{
		PageParameters params = new PageParameters();

		for (Iterator<String> segment = url.getSegments().iterator(); segment.hasNext();)
		{
			String key = segment.next();
			if (Strings.isEmpty(key))
			{
				// keys cannot be empty
				continue;
			}
			// A trailing slash can be seen as an extra segment with a "" value so check
			// if there is a matching value for this parameter name and ignore it if not
			if (segment.hasNext())
			{
				String value = segment.next();

				params.add(key, value);
			}
		}

		return params.isEmpty() ? null : params;
	}
}
