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

import junit.framework.TestCase;

/**
 * Tests {@link PropertyVariableInterpolator}
 * 
 * @author Gerolf Seitz
 */
public class PropertyVariableInterpolatorTest extends TestCase
{
	/**
	 * 
	 */
	public void testWithValue()
	{
		TestClass object = new TestClass("value");
		String result = PropertyVariableInterpolator.interpolate("${key}", object);
		assertEquals("value", result.toString());
	}

	/**
	 * 
	 */
	public void testWithoutValue()
	{
		String result = PropertyVariableInterpolator.interpolate("${key}", null);
		assertEquals("${key}", result.toString());
	}

	/**
	 * 
	 */
	public void testWithValueWithSingleQuotes()
	{
		TestClass object = new TestClass("'value '' with multiple' quotes'");
		String result = PropertyVariableInterpolator.interpolate("${key}", object);
		assertEquals("''value '''' with multiple'' quotes''", result);
	}

	private static class TestClass
	{
		private final String key;

		public TestClass(String key)
		{
			this.key = key;
		}
	}
}
