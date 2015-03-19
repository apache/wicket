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

import org.apache.wicket.core.util.string.interpolator.PropertyVariableInterpolator;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link PropertyVariableInterpolator}
 * 
 * @author Gerolf Seitz
 */
public class PropertyVariableInterpolatorTest extends Assert
{
	/**
	 * 
	 */
	@Test
	public void withValue()
	{
		TestClass object = new TestClass("value");
		String result = new PropertyVariableInterpolator("${key}", object).toString();
		assertEquals("value", result.toString());
	}

	/**
	 * A test that shows a usage of escape character. The first two '$' characters are squashed to
	 * '$' and '${key}' is interpolated to the respective value
	 */
	@Test
	public void withValueAndEscape()
	{
		TestClass object = new TestClass("3.24");
		String result = new PropertyVariableInterpolator("$$${key}", object).toString();
		assertEquals("$3.24", result.toString());
	}

	/**
	 * 
	 */
	@Test
	public void withoutValue()
	{
		String result = new PropertyVariableInterpolator("${key}", null).toString();
		assertEquals("${key}", result.toString());
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
