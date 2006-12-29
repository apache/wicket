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
package wicket.util.value;

import junit.framework.TestCase;

/**
 * @author jcompagner
 */
public class ValueMapTest extends TestCase
{
	/**
	 * @throws Exception
	 */
	public void testStringParseConstructorSimple() throws Exception
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
	public void testStringParseConstructorSpecialChars() throws Exception
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
	public void testStringParseConstructorDelimitersAndEqualsSign() throws Exception
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
}
