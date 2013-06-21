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
package org.apache.wicket.util.string.interpolator;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link MapVariableInterpolator}
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class VariableInterpolatorTest extends Assert
{
	/**
	 * 
	 */
	@Test
	public void withValue()
	{
		Map<String, String> params = new HashMap<>();
		params.put("key", "value");
		MapVariableInterpolator in = new MapVariableInterpolator("${key}", params);
		assertEquals("value", in.toString());
	}

	/**
	 * 
	 */
	@Test
	public void withoutValue()
	{
		Map<String, String> params = new HashMap<>();
		MapVariableInterpolator in = new MapVariableInterpolator("${key}", params);
		assertEquals("${key}", in.toString());
	}

	/**
	 * 
	 */
	@Test
	public void withoutValueAndException()
	{
		Map<String, String> params = new HashMap<>();
		MapVariableInterpolator in = new MapVariableInterpolator("${key}", params, true);
		try
		{
			in.toString();
			fail("Should throw an exception");
		}
		catch (Exception e)
		{

		}
	}

	/**
	 * 
	 */
	@Test
	public void literal()
	{
		Map<String, String> params = new HashMap<>();
		params.put("key", "value");
		MapVariableInterpolator in = new MapVariableInterpolator("aaa $${key} bbb", params);
		assertEquals("aaa ${key} bbb", in.toString());
	}
}