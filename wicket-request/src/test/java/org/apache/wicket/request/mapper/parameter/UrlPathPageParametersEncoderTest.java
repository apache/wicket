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
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link UrlPathPageParametersEncoder}
 */
public class UrlPathPageParametersEncoderTest extends Assert
{

	/**
	 * Encode named parameters in the segments (so they look like indexed parameters)
	 */
	@Test
	public void encodeNamedParameters()
	{
		PageParameters params = new PageParameters();
		params.add("name1", "value1", INamedParameters.Type.MANUAL);
		params.add("name2", "value2", INamedParameters.Type.MANUAL);

		UrlPathPageParametersEncoder encoder = new UrlPathPageParametersEncoder();
		Url url = encoder.encodePageParameters(params);

		assertEquals("name1/value1/name2/value2", url.toString());
	}

	/**
	 * Encode named parameters in the segments (so they look like indexed parameters) and the name
	 * and/or value have non-ASCII characters
	 */
	@Test
	public void encodeNamedParametersWithSpecialChars()
	{
		// the non-ASCII characters are randomly chosen
		PageParameters params = new PageParameters();
		params.add("name1", "valueএ", INamedParameters.Type.MANUAL);
		params.add("nameㄘ", "value2", INamedParameters.Type.MANUAL);

		UrlPathPageParametersEncoder encoder = new UrlPathPageParametersEncoder();
		Url url = encoder.encodePageParameters(params);

		assertEquals("name1/value%E0%A6%8F/name%E3%84%98/value2", url.toString());
	}

	/**
	 * This encoder doesn't support indexed parameters
	 */
	@Test(expected = IllegalArgumentException.class)
	public void encodeIndexedParameters()
	{
		PageParameters params = new PageParameters();
		params.set(0, "value1");
		params.set(1, "value2");

		UrlPathPageParametersEncoder encoder = new UrlPathPageParametersEncoder();
		encoder.encodePageParameters(params);
	}

	/**
	 * Decode properly encoded parameters
	 */
	@Test
	public void decodeUrl()
	{
		Url url = Url.parse("name1/value1/name2/value2");

		UrlPathPageParametersEncoder decoder = new UrlPathPageParametersEncoder();
		PageParameters parameters = decoder.decodePageParameters(url);

		assertEquals(2, parameters.getAllNamed().size());
		assertEquals("value1", parameters.get("name1").toString());
		assertEquals("value2", parameters.get("name2").toString());
	}

	/**
	 * Decode encoded parameters with trailing slash. The parameter with the empty name should be
	 * ignored
	 */
	@Test
	public void decodeUrlWithTrailingSlash()
	{
		Url url = Url.parse("name1/value1/name2/value2/");

		UrlPathPageParametersEncoder decoder = new UrlPathPageParametersEncoder();
		PageParameters parameters = decoder.decodePageParameters(url);

		assertEquals(2, parameters.getAllNamed().size());
		assertEquals("value1", parameters.get("name1").toString());
		assertEquals("value2", parameters.get("name2").toString());
	}

	/**
	 * Decode encoded parameters with trailing slash. The parameter with the empty value should be
	 * ignored
	 */
	@Test
	public void decodeUrlWithTrailingSlashAfterName()
	{
		Url url = Url.parse("name1/value1/name2/value2/name3");

		UrlPathPageParametersEncoder decoder = new UrlPathPageParametersEncoder();
		PageParameters parameters = decoder.decodePageParameters(url);

		assertEquals(2, parameters.getAllNamed().size());
		assertEquals("value1", parameters.get("name1").toString());
		assertEquals("value2", parameters.get("name2").toString());
	}

	/**
	 * Decode encoded parameters with a leading slash. The empty name segment should be ignored.
	 */
	@Test
	public void decodeUrlWithLeadingSlash()
	{
		Url url = Url.parse("/name1/value1/name2/value2");

		UrlPathPageParametersEncoder decoder = new UrlPathPageParametersEncoder();
		PageParameters parameters = decoder.decodePageParameters(url);

		assertEquals(2, parameters.getAllNamed().size());
		assertEquals("value1", parameters.get("name1").toString());
		assertEquals("value2", parameters.get("name2").toString());
	}

	/**
	 * Decode encoded parameters with a slashes in the middle. The empty name segments should be
	 * ignored.
	 */
	@Test
	public void decodeUrlWithSlashesInTheMiddle()
	{
		Url url = Url.parse("name1/value1////name2/value2");

		UrlPathPageParametersEncoder decoder = new UrlPathPageParametersEncoder();
		PageParameters parameters = decoder.decodePageParameters(url);

		assertEquals(2, parameters.getAllNamed().size());
		assertEquals("value1", parameters.get("name1").toString());
		assertEquals("value2", parameters.get("name2").toString());
	}

	/**
	 * Decode encoded parameters with a slashes in the middle. The empty name segments should be
	 * ignored.
	 */
	@Test
	public void decodeUrlWithSlashesInTheMiddleAndEmptyValue()
	{
		Url url = Url.parse("name1/value1////name2//");

		UrlPathPageParametersEncoder decoder = new UrlPathPageParametersEncoder();
		PageParameters parameters = decoder.decodePageParameters(url);

		assertEquals(2, parameters.getAllNamed().size());
		assertEquals("value1", parameters.get("name1").toString());
		assertEquals("", parameters.get("name2").toString());
	}
}
