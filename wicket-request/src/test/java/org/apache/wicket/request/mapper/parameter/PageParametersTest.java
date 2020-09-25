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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.wicket.util.string.StringValue;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PageParameters}
 */
class PageParametersTest
{

	/**
	 * Tests that adding a key with String[] value is properly parsed and there a several
	 * StringValue's for that key
	 */
	@Test
	void addStringArrayValue()
	{
		PageParameters parameters = new PageParameters();

		String[] input = new String[] { "v1", "v2" };
		parameters.add("key", input, INamedParameters.Type.MANUAL);

		List<StringValue> stringValue = parameters.getValues("key");

		for (String in : input)
		{

			boolean found = false;
			for (StringValue value : stringValue)
			{
				if (value.toString().equals(in))
				{
					found = true;
					break;
				}
			}

			if (found == false)
			{
				throw new IllegalStateException("Expected to find a StringValue with value: " + in);
			}
		}
	}

	@Test
	void addEmptyStringArrayValue()
	{
		PageParameters parameters = new PageParameters();

		String[] input = new String[] {  };
		parameters.add("key", input, INamedParameters.Type.MANUAL);

		assertTrue(parameters.isEmpty());
	}

	@Test
	void addEmptyStringValue()
	{
		PageParameters parameters = new PageParameters();

		parameters.add("key", "", INamedParameters.Type.MANUAL);

		assertFalse(parameters.isEmpty());
		assertTrue(parameters.get("key").isEmpty());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3906
	 */
	@Test
	void getPosition()
	{
		PageParameters parameters = new PageParameters();
		parameters.set("named1", "value1", 3, INamedParameters.Type.MANUAL);
		assertEquals(0, parameters.getPosition("named1"),
			"Adding a parameter at position out of the size of the list will just append it");

		parameters.set("named2", "value2", 0, INamedParameters.Type.MANUAL);
		assertEquals(0, parameters.getPosition("named2"));
		assertEquals(1, parameters.getPosition("named1"), "'named1' should be moved back");


		parameters.set("named3", "value3", -100, INamedParameters.Type.MANUAL);
		assertEquals(0, parameters.getPosition("named2"));
		assertEquals(1, parameters.getPosition("named1"));
		assertEquals(2, parameters.getPosition("named3"),
			"Adding a parameter with negative position will just append it.");
	}

	/**
	 * Tests that overwriting (via #set()) a named parameter will preserve its position
	 */
	@Test
	void set()
	{
		PageParameters parameters = new PageParameters();
		parameters.add("named1", "value1", INamedParameters.Type.MANUAL).add("named2", "value2",
			INamedParameters.Type.MANUAL);

		assertEquals(0, parameters.getPosition("named1"));
		assertEquals(1, parameters.getPosition("named2"));

		// overwrite it
		parameters.set("named1", "newValue", INamedParameters.Type.MANUAL);
		parameters.set("named3", "value3", INamedParameters.Type.MANUAL);
		assertEquals(0, parameters.getPosition("named1"));
		assertEquals("newValue", parameters.get("named1").toString());
		assertEquals(1, parameters.getPosition("named2"));
		assertEquals(2, parameters.getPosition("named3"));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3938
	 * 
	 * Remove the parameter by its name
	 */
	@Test
	void removeParameters()
	{
		PageParameters parameters = new PageParameters()
			.add("named1", "value1", INamedParameters.Type.MANUAL)
			.add("named2", "value2", INamedParameters.Type.MANUAL);

		assertEquals("value1", parameters.get("named1").toString());
		assertEquals("value2", parameters.get("named2").toString());

		parameters.remove("named1");
		parameters.remove("named2");
		assertTrue(parameters.isEmpty());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3938
	 * 
	 * Remove the parameter by its name only if its value is equal to the criteria
	 */
	@Test
	void removeParametersByValue()
	{
		PageParameters parameters = new PageParameters()
			.add("named1", "value1", INamedParameters.Type.MANUAL)
			.add("named1", "value2", INamedParameters.Type.MANUAL);

		assertEquals(2, parameters.getAllNamed().size());

		parameters.remove("named1", "value1");
		assertEquals("value2", parameters.get("named1").toString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4775
	 * 
	 * Merge PageParameters, also when other has multiple values for the same name
	 */
	@Test
	void mergeParameters()
	{
		PageParameters left = new PageParameters().add("left", "left", INamedParameters.Type.MANUAL)
			.add("both", "both1", INamedParameters.Type.MANUAL)
			.add("both", "both2", INamedParameters.Type.MANUAL)
			.set(0, "val0")
			.set(1, "val1");
		PageParameters right = new PageParameters()
			.add("right", "right", INamedParameters.Type.MANUAL)
			.add("both", "both1-r", INamedParameters.Type.MANUAL)
			.add("both", "both2-r", INamedParameters.Type.MANUAL)
			.set(1, "val1-r")
			.set(2, "val2");
		left.mergeWith(right);

		assertEquals("val0", left.get(0).toString());
		assertEquals("val1-r", left.get(1).toString());
		assertEquals("val2", left.get(2).toString());
		assertEquals("left", left.get("left").toString());
		assertEquals("right", left.get("right").toString());
		assertEquals(2, left.getValues("both").size());
		assertEquals("both1-r", left.getValues("both").get(0).toString());
		assertEquals("both2-r", left.getValues("both").get(1).toString());
	}

	@Test
	void mergeEmptyParameters() 
	{
		final PageParameters left = new PageParameters();
		final PageParameters right = new PageParameters();
		left.mergeWith(right);
		
		assertTrue(left.isEmpty());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5669
	 */
	@Test
	void parameterTypes()
	{
		PageParameters p = new PageParameters()
			.add("pathName1", "pathValue1", INamedParameters.Type.PATH)
			.add("pathName1", "pathValue1.1", INamedParameters.Type.PATH)
			.add("pathName2", "pathValue2", INamedParameters.Type.PATH)
			.add("queryName1", "queryValue1", INamedParameters.Type.QUERY_STRING)
			.add("queryName1", "queryValue1.1", INamedParameters.Type.QUERY_STRING)
			.add("queryName2", "queryValue2", INamedParameters.Type.QUERY_STRING)
			.add("manualName1", "manualValue1", INamedParameters.Type.MANUAL)
			.add("manualName1", "manualValue1.1", INamedParameters.Type.MANUAL)
			.add("manualName2", "manualValue2", INamedParameters.Type.MANUAL);

		{
			// PATH
			assertEquals(3, p.getAllNamedByType(INamedParameters.Type.PATH).size());

			List<StringValue> pathName1Values = p.getValues("pathName1");
			assertEquals(2, pathName1Values.size());
			assertEquals("pathValue1", pathName1Values.get(0).toString());
			assertEquals("pathValue1.1", pathName1Values.get(1).toString());

			List<StringValue> pathName2Values = p.getValues("pathName2");
			assertEquals(1, pathName2Values.size());
			assertEquals("pathValue2", pathName2Values.get(0).toString());
		}

		{
			// QUERY STRING
			assertEquals(3, p.getAllNamedByType(INamedParameters.Type.QUERY_STRING).size());

			List<StringValue> queryName1Values = p.getValues("queryName1");
			assertEquals(2, queryName1Values.size());
			assertEquals("queryValue1", queryName1Values.get(0).toString());
			assertEquals("queryValue1.1", queryName1Values.get(1).toString());

			List<StringValue> queryName2Values = p.getValues("queryName2");
			assertEquals(1, queryName2Values.size());
			assertEquals("queryValue2", queryName2Values.get(0).toString());
		}

		{
			// MANUAL
			assertEquals(3, p.getAllNamedByType(INamedParameters.Type.MANUAL).size());

			List<StringValue> manualName1Values = p.getValues("manualName1");
			assertEquals(2, manualName1Values.size());
			assertEquals("manualValue1", manualName1Values.get(0).toString());
			assertEquals("manualValue1.1", manualName1Values.get(1).toString());

			List<StringValue> manualName2Values = p.getValues("manualName2");
			assertEquals(1, manualName2Values.size());
			assertEquals("manualValue2", manualName2Values.get(0).toString());
		}
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5669
	 */
	@Test
	void addWithoutTypeIsManual()
	{
		PageParameters p = new PageParameters();
		p.add("name", "value");
		assertEquals(INamedParameters.Type.MANUAL, p.getAllNamed().get(0).getType());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5669
	 */
	@Test
	void setWithoutTypeIsManual()
	{
		PageParameters p = new PageParameters();
		p.set("name", "value");
		assertEquals(INamedParameters.Type.MANUAL, p.getAllNamed().get(0).getType());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5669
	 */
	@Test
	void setWithIndexWithoutTypeIsManual()
	{
		PageParameters p = new PageParameters();
		p.set("name", "value", 3);
		assertEquals(INamedParameters.Type.MANUAL, p.getAllNamed().get(0).getType());
	}

	/**
	 * NamedPairs equality should not depend on the type
	 *
	 * https://issues.apache.org/jira/browse/WICKET-5669
	 */
	@Test
	void equality()
	{
		PageParameters p1 = new PageParameters().add("a", "b", INamedParameters.Type.MANUAL)
			.set("c", "d", INamedParameters.Type.MANUAL);

		PageParameters p2 = new PageParameters().set("a", "b", INamedParameters.Type.QUERY_STRING)
			.add("c", "d", INamedParameters.Type.PATH);

		assertEquals(p2, p1);
	}

	/**
	 * NamedPairs equality should not depend on the order
	 *
	 * https://issues.apache.org/jira/browse/WICKET-6283
	 */
	@Test
	void equalityOfDifferentNamedParametersOrder()
	{
		PageParameters p1 = new PageParameters().add("a", "b").add("c", "d");
		PageParameters p2 = new PageParameters().add("c", "d").add("a", "b");

		assertEquals(p2, p1);
	}

	/**
	 * namedParameters equality should handle null namedParameters instance.
	 *
	 * https://issues.apache.org/jira/browse/WICKET-6332
	 */
	@Test
	void equalityWithEmptyNamedParameters()
	{
		PageParameters p1 = new PageParameters().add("a", "b");

		assertNotEquals(new PageParameters(), p1);
	}

	/**
	 * indexedParameters equality should handle null namedParameters instance.
	 *
	 * https://issues.apache.org/jira/browse/WICKET-6332
	 */
	@Test
	void equalityWithEmptyIndexedParameters()
	{
		PageParameters p1 = new PageParameters().set(0, "b");

		assertNotEquals(new PageParameters(), p1);
	}

	/**
	 * NamedPairs hashCode should not depend on the type
	 *
	 * https://issues.apache.org/jira/browse/WICKET-5669
	 */
	@Test
	void hashcode()
	{
		PageParameters p1 = new PageParameters().add("a", "b", INamedParameters.Type.MANUAL)
			.set("c", "d", INamedParameters.Type.MANUAL);

		PageParameters p2 = new PageParameters().set("a", "b", INamedParameters.Type.QUERY_STRING)
			.add("c", "d", INamedParameters.Type.PATH);

		assertEquals(p2.hashCode(), p1.hashCode());
	}
}
