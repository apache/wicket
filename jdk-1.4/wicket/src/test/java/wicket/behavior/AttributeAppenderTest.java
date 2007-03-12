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
package wicket.behavior;

import junit.framework.TestCase;

/**
 * Tests the attribute appender. Performs only the tests on the actual
 * added functionality to the attribute modifier.
 *
 * @author Martijn Dashorst
 */
public class AttributeAppenderTest extends TestCase
{
	/**
	 * Test
	 */
	public void testNewValue1() {
		AttributeAppender appender = new AttributeAppender("", null, " ");
		assertEquals("oldvalue newvalue", appender.newValue("oldvalue", "newvalue"));
		assertEquals("newvalue", appender.newValue("", "newvalue"));
		assertEquals("oldvalue", appender.newValue("oldvalue", ""));
	}

	/**
	 * Test
	 */
	public void testNewValue2() {
		AttributeAppender appender = new AttributeAppender("", null, ";");
		assertEquals("oldvalue;newvalue", appender.newValue("oldvalue", "newvalue"));
		assertEquals("newvalue", appender.newValue("", "newvalue"));
		assertEquals("oldvalue", appender.newValue("oldvalue", ""));
	}
}
