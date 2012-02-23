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

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.string.StringValue;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for PageParametersEncoder
 */
public class PageParametersEncoderTest extends Assert
{
	/**
	 * Tests that PageParametersEncoder decodes GET parameters, not POST
	 * 
	 * @throws Exception
	 */
	@Test
	public void decodeParameters() throws Exception
	{
		PageParametersEncoder encoder = new PageParametersEncoder();
		Request request = new Request()
		{
			@Override
			public Url getUrl()
			{
				return Url.parse("idx1/idx2?named1=value1&named2=value2");
			}

			@Override
			public Url getClientUrl()
			{
				return null;
			}

			@Override
			public Locale getLocale()
			{
				return null;
			}

			@Override
			public Charset getCharset()
			{
				return null;
			}

			@Override
			public Object getContainerRequest()
			{
				return null;
			}

			@Override
			public IRequestParameters getPostParameters()
			{
				return new PostParameters();
			}
		};

		PageParameters pageParameters = encoder.decodePageParameters(request);
		assertEquals("idx1", pageParameters.get(0).toOptionalString());
		assertEquals("idx2", pageParameters.get(1).toOptionalString());
		assertEquals("value1", pageParameters.get("named1").toOptionalString());
		assertEquals("value2", pageParameters.get("named2").toOptionalString());
		assertEquals(null, pageParameters.get("postOne").toOptionalString());
		assertTrue(pageParameters.getValues("postTwo").isEmpty());
		assertTrue(pageParameters.getValues("postTwo").isEmpty());
	}

	/**
	 * Mock IRequestParameters that provides static POST parameters
	 */
	private static class PostParameters implements IRequestParameters
	{
		private final Map<String, List<StringValue>> params = new HashMap<String, List<StringValue>>();
		{
			params.put("postOne", Arrays.asList(StringValue.valueOf("1")));
			params.put("postTwo",
				Arrays.asList(StringValue.valueOf("2"), StringValue.valueOf("2.1")));
		}

		public Set<String> getParameterNames()
		{
			return params.keySet();
		}

		public StringValue getParameterValue(String name)
		{
			List<StringValue> values = params.get(name);
			return (values != null && !values.isEmpty()) ? values.get(0)
				: StringValue.valueOf((String)null);
		}

		public List<StringValue> getParameterValues(String name)
		{
			List<StringValue> values = params.get(name);
			return values != null ? Collections.unmodifiableList(values) : null;
		}
	}
}
