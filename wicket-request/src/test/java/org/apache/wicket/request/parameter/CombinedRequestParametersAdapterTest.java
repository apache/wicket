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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.wicket.request.Url;
import org.apache.wicket.util.string.StringValue;
import org.junit.jupiter.api.Test;

/**
 * @since 1.5.5
 */
public class CombinedRequestParametersAdapterTest
{
	/**
	 * Tests that org.apache.wicket.request.parameter.CombinedRequestParametersAdapter#getParameterValues(java.lang.String)
	 * will return all values for a given parameter by name.
	 *
	 * https://issues.apache.org/jira/browse/WICKET-4417
	 *
	 * @throws Exception
	 */
	@Test
	public void getParameterValuesSameNameSameValue() throws Exception
	{
		Url url = Url.parse("?param1=value1&param1=value1");
		UrlRequestParametersAdapter urlAdapter = new UrlRequestParametersAdapter(url);
		CombinedRequestParametersAdapter adapter = new CombinedRequestParametersAdapter(urlAdapter);

		List<StringValue> values = adapter.getParameterValues("param1");
		assertEquals(2, values.size());
	}
}
