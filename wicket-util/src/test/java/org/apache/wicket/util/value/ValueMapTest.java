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
package org.apache.wicket.util.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * @author jcompagner
 * @author Doug Donohoe
 */
public class ValueMapTest
{
	/**
	 * @throws Exception
	 */
	@Test
	public void stringParseConstructorSimple() throws Exception
	{
		ValueMap vm = new ValueMap("param=value");
		assertEquals(1, vm.size());
		assertEquals("value", vm.get("param"));

		vm = new ValueMap("param1=value1,param2=value2");
		assertEquals(2, vm.size());
		assertEquals("value1", vm.get("param1"));
		assertEquals("value2", vm.get("param2"));

		vm = new ValueMap("param1=value1;param2=value2", ";");
		assertEquals(2, vm.size());
		assertEquals("value1", vm.get("param1"));
		assertEquals("value2", vm.get("param2"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void stringParseConstructorSpecialChars() throws Exception
	{
		ValueMap vm = new ValueMap("param1=val>ue1;param2=value2", ";");
		assertEquals(2, vm.size());
		assertEquals("val>ue1", vm.get("param1"));
		assertEquals("value2", vm.get("param2"));

		vm = new ValueMap("param1=val:ue1;param2=value2", ";");
		assertEquals(2, vm.size());
		assertEquals("val:ue1", vm.get("param1"));
		assertEquals("value2", vm.get("param2"));

		vm = new ValueMap("param1=val?ue1;param2=val<>ue2", ";");
		assertEquals(2, vm.size());
		assertEquals("val?ue1", vm.get("param1"));
		assertEquals("val<>ue2", vm.get("param2"));

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void stringParseConstructorDelimitersAndEqualsSign() throws Exception
	{
		ValueMap vm = new ValueMap("param1=val=ue1;param2=value2", ";");
		assertEquals(2, vm.size());
		assertEquals("val=ue1", vm.get("param1"));
		assertEquals("value2", vm.get("param2"));

		vm = new ValueMap("param1=value1;param2=val=ue2", ";");
		assertEquals(2, vm.size());
		assertEquals("value1", vm.get("param1"));
		assertEquals("val=ue2", vm.get("param2"));

		vm = new ValueMap("param1=val;ue1;param2=value2", ";");
		assertEquals(2, vm.size());
		assertEquals("val;ue1", vm.get("param1"));
		assertEquals("value2", vm.get("param2"));

		vm = new ValueMap("param1=value1;param2=val;ue2", ";");
		assertEquals(2, vm.size());
		assertEquals("value1", vm.get("param1"));
		assertEquals("val;ue2", vm.get("param2"));

		vm = new ValueMap("param1=va=l;ue1;param2=val;ue2;param3=val=ue3", ";");
		assertEquals(3, vm.size());
		assertEquals("va=l;ue1", vm.get("param1"));
		assertEquals("val;ue2", vm.get("param2"));
		assertEquals("val=ue3", vm.get("param3"));

	}

	/**
	 * Enumeration for testing
	 */
	public enum TestEnum {
		/**	 */
		one,
		/**	 */
		two,
		/**	 */
		three
	}

	/**
	 * Test getting enums from value map
	 */
	@Test
	public void testEnum()
	{
		String name = "name";

		TestEnum fetch = TestEnum.valueOf("one");
		assertEquals(TestEnum.one, fetch);

		ValueMap vm = new ValueMap();
		vm.put(name, "one");

		// test get
		TestEnum test = vm.getAsEnum(name, TestEnum.class, TestEnum.three);
		assertEquals(TestEnum.one, test);

		// test get alternate
		test = vm.getAsEnum(name, TestEnum.three);
		assertEquals(TestEnum.one, test);

		// test get alternate null
		try
		{
			vm.getAsEnum(name, (TestEnum)null);
			fail("Should have thrown an exception");
		}
		catch (IllegalArgumentException ignored)
		{

		}

		// test get if nothing there
		test = vm.getAsEnum("missing", TestEnum.class, TestEnum.two);
		assertEquals(TestEnum.two, test);

		test = vm.getAsEnum("missing", TestEnum.class, null);
		assertNull(test);

		test = vm.getAsEnum("missing", TestEnum.class);
		assertNull(test);

		// test get if value doesn't match enum
		vm.put(name, "bogus");
		test = vm.getAsEnum(name, TestEnum.class, TestEnum.one);
		assertEquals(TestEnum.one, test);
	}

	/**
	 * test other getAs methods
	 */
	@Test
	public void getAs()
	{
		ValueMap vm = new ValueMap();

		Boolean booleanValue = true;
		Integer integerValue = 42;
		Long longValue = integerValue * 1L;
		Double doubleValue = integerValue * 1.0D;
		Instant timeValue = Instant.now();
		Duration durationValue = Duration.ofHours(1);

		boolean defBoolean = !booleanValue;
		int defInteger = 10101;
		long defLong = defInteger * 1L;
		double defDouble = defInteger * 1.0D;
		Instant defTime = Instant.now();
		Duration defDuration = Duration.ofHours(42);

		vm.put("num", integerValue.toString());
		vm.put("num.bad", "xxx");
		vm.put("instant", timeValue.toString());
		vm.put("instant.bad", "xxx");
		vm.put("duration", durationValue.toString());
		vm.put("duration.bad", "xxx");
		vm.put("boolean", booleanValue.toString());
		vm.put("boolean.bad", "xxx");

		// boolean
		assertEquals(booleanValue, vm.getAsBoolean("boolean"));
		assertNull(vm.getAsBoolean("boolean.bad"));
		assertEquals(defBoolean, vm.getAsBoolean("boolean.bad", defBoolean));
		assertNull(vm.getAsBoolean("boolean.missing"));
		assertEquals(defBoolean, vm.getAsBoolean("boolean.missing", defBoolean));
		assertEquals(!defBoolean, vm.getAsBoolean("boolean.missing", !defBoolean));

		// integer
		assertEquals(integerValue, vm.getAsInteger("num"));
		assertNull(vm.getAsInteger("num.bad"));
		assertEquals(defInteger, vm.getAsInteger("num.bad", defInteger));
		assertNull(vm.getAsInteger("num.missing"));
		assertEquals(defInteger, vm.getAsInteger("num.missing", defInteger));

		// long
		assertEquals(longValue, vm.getAsLong("num"));
		assertNull(vm.getAsLong("num.bad"));
		assertEquals(defLong, vm.getAsLong("num.bad", defLong));
		assertNull(vm.getAsLong("num.missing"));
		assertEquals(defLong, vm.getAsLong("num.missing", defLong));

		// double
		assertEquals(doubleValue, vm.getAsDouble("num"));
		assertNull(vm.getAsDouble("num.bad"));
		assertEquals(defDouble, vm.getAsDouble("num.bad", defDouble), 0.001);
		assertNull(vm.getAsDouble("num.missing"));
		assertEquals(defDouble, vm.getAsDouble("num.missing", defDouble), 0.001);

		// instant
		assertEquals(timeValue, vm.getAsInstant("instant"));

		assertNull(vm.getAsInstant("instant.bad"));
		assertEquals(defTime, vm.getAsTime("instant.bad", defTime));
		assertNull(vm.getAsInstant("instant.missing"));
		assertEquals(defTime, vm.getAsTime("instant.missing", defTime));

		// duration
		assertEquals(durationValue, vm.getAsDuration("duration"));
		assertNull(vm.getAsDuration("duration.bad"));
		assertEquals(defDuration, vm.getAsDuration("duration.bad", defDuration));
		assertNull(vm.getAsDuration("duration.missing"));
		assertEquals(defDuration, vm.getAsDuration("duration.missing", defDuration));
		
		vm.remove("instant");
		assertEquals("num = \"42\" num.bad = \"xxx\" instant.bad = \"xxx\" duration = \"PT1H\" duration.bad = \"xxx\" boolean = \"true\" boolean.bad = \"xxx\"", vm.toString());
	}

	/**
	 * 
	 */
	@Test
	public void array2()
	{
		ValueMap parameters = new ValueMap("a=1,a=2,a=3");
		String[] a = parameters.getStringArray("a");
		assertEquals(3, a.length);
		assertEquals("1", a[0]);
		assertEquals("2", a[1]);
		assertEquals("3", a[2]);
	}
}
