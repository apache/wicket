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

import static org.hamcrest.CoreMatchers.is;

import org.apache.wicket.request.Url;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for PageParametersEncoder
 */
public class PageParametersEncoderTest extends Assert
{
	/**
	 * Tests that PageParametersEncoder decodes parameters
	 * 
	 * @throws Exception
	 */
	@Test
	public void decodeParameters() throws Exception
	{
		PageParametersEncoder encoder = new PageParametersEncoder();

		Url url = Url.parse("idx1/idx2?named1=value1&=&named2=value2");

		PageParameters pageParameters = encoder.decodePageParameters(url);
		assertThat(pageParameters.getIndexedCount(), is(2));
		assertThat(pageParameters.getNamedKeys().size(), is(2));
		assertEquals("idx1", pageParameters.get(0).toOptionalString());
		assertEquals("idx2", pageParameters.get(1).toOptionalString());
		assertEquals("value1", pageParameters.get("named1").toOptionalString());
		assertEquals("value2", pageParameters.get("named2").toOptionalString());
	}
}
