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
package org.apache.wicket.util.string;

import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for StringValue
 */
public class StringValueTest extends Assert
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-4309
	 */
	@Test
	public void toOptionalXyzWithEmptyString()
	{
		StringValue sv = new StringValue("");
		assertNull(sv.toOptionalBoolean());
		assertNull(sv.toOptionalCharacter());
		assertNull(sv.toOptionalDouble());
		assertNull(sv.toOptionalDuration());
		assertNull(sv.toOptionalInteger());
		assertNull(sv.toOptionalLong());
		assertEquals("", sv.toOptionalString());
		assertNull(sv.toOptionalTime());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4309
	 */
	@Test
	public void toOptionalXyzWithNull()
	{
		StringValue sv = new StringValue(null);
		assertNull(sv.toOptionalBoolean());
		assertNull(sv.toOptionalCharacter());
		assertNull(sv.toOptionalDouble());
		assertNull(sv.toOptionalDuration());
		assertNull(sv.toOptionalInteger());
		assertNull(sv.toOptionalLong());
		assertNull(sv.toOptionalString());
		assertNull(sv.toOptionalTime());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4356
     * https://issues.apache.org/jira/browse/WICKET-4580
	 */
	@Test
	public void defaultValues()
	{
		StringValue sv = new StringValue("unknown");

        // https://issues.apache.org/jira/browse/WICKET-4356

		assertTrue(sv.toBoolean(true));
		assertFalse(sv.toBoolean(false));
		
		assertEquals(4, sv.toInt(4));
		assertEquals(4.0, sv.toDouble(4.0), 0.005);
		assertEquals('c', sv.toChar('c'));
		assertEquals(Duration.seconds(3), sv.toDuration(Duration.seconds(3)));
		assertEquals(Time.millis(5), sv.toTime(Time.millis(5)));
		assertEquals(40L, sv.toLong(40));

		assertEquals("unknown", sv.toString("def"));

        // https://issues.apache.org/jira/browse/WICKET-4580

        // test correct values don't use defaultValues
        assertEquals(Boolean.TRUE, new StringValue("true").toBooleanObject(Boolean.FALSE));
        assertEquals(Character.valueOf('c'), new StringValue("c").toCharacter('d'));
        assertEquals(Double.valueOf(4.0), new StringValue("4.0").toDoubleObject(5.0));
        assertEquals(Integer.valueOf(4), new StringValue("4").toInteger(5));
        assertEquals(Long.valueOf(4L), new StringValue("4").toLongObject(5L));
        // test inconvertible so use defaultValues
        assertTrue(sv.toBooleanObject(Boolean.TRUE));
        assertFalse(sv.toBooleanObject(Boolean.FALSE));
        assertEquals(Character.valueOf('c'), sv.toCharacter('c'));
        assertEquals(4.0, sv.toDoubleObject(4.0), 0.005);
        assertEquals(Integer.valueOf(4), sv.toInteger(4));
        assertEquals(Long.valueOf(4L), sv.toLongObject(4L));
        assertNull(sv.toBooleanObject(null));
        assertNull(sv.toCharacter(null));
        assertNull(sv.toDoubleObject(null));
        assertNull(sv.toInteger(null));
        assertNull(sv.toLongObject(null));
	}

	static enum TestEnum {
		FOO, BAR, BAZ
	}

	@Test
	public void enums() throws Exception
	{
		assertEquals(TestEnum.FOO, new StringValue("FOO").toEnum(TestEnum.class));
		assertEquals(TestEnum.FOO, new StringValue("FOO").toEnum(TestEnum.BAR));
		assertEquals(TestEnum.FOO, new StringValue("FOO").toEnum(TestEnum.class, TestEnum.BAR));

		assertEquals(TestEnum.BAR, new StringValue(null).toEnum(TestEnum.BAR));
		assertEquals(TestEnum.BAZ, new StringValue("killer rabbit").toEnum(TestEnum.BAZ));
		assertEquals(TestEnum.BAZ,
			new StringValue("killer rabbit").toEnum(TestEnum.class, TestEnum.BAZ));
		assertNull(new StringValue(null).toOptionalEnum(TestEnum.class));
	}

	@Test(expected = StringValueConversionException.class)
	public void failingEnum() throws Exception
	{
		new StringValue("camelot").toEnum(TestEnum.class);
	}

	@Test(expected = StringValueConversionException.class)
	public void failingEnum2() throws Exception
	{
		new StringValue("camelot").toOptionalEnum(TestEnum.class);
	}
}
