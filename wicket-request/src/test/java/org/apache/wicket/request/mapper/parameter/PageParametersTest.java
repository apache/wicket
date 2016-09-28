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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.util.List;

import org.apache.wicket.util.string.StringValue;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link PageParameters}
 */
public class PageParametersTest extends Assert
{

	/**
	 * Tests that adding a key with String[] value is properly parsed and there a several
	 * StringValue's for that key
	 */
	@Test
	public void addStringArrayValue()
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

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3906
	 */
	@Test
	public void getPosition()
	{
		PageParameters parameters = new PageParameters();
		parameters.set("named1", "value1", 3, INamedParameters.Type.MANUAL);
		assertEquals(
			"Adding a parameter at position out of the size of the list will just append it", 0,
			parameters.getPosition("named1"));

		parameters.set("named2", "value2", 0, INamedParameters.Type.MANUAL);
		assertEquals(0, parameters.getPosition("named2"));
		assertEquals("'named1' should be moved back", 1, parameters.getPosition("named1"));


		parameters.set("named3", "value3", -100, INamedParameters.Type.MANUAL);
		assertEquals(0, parameters.getPosition("named2"));
		assertEquals(1, parameters.getPosition("named1"));
		assertEquals("Adding a parameter with negative position will just append it.", 2,
			parameters.getPosition("named3"));
	}

	/**
	 * Tests that overwriting (via #set()) a named parameter will preserve its position
	 */
	@Test
	public void set()
	{
		PageParameters parameters = new PageParameters();
		parameters
				.add("named1", "value1", INamedParameters.Type.MANUAL)
				.add("named2", "value2", INamedParameters.Type.MANUAL);

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
	public void removeParameters()
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
	public void removeParametersByValue()
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
	public void mergeParameters()
	{
		PageParameters left = new PageParameters()
			.add("left", "left", INamedParameters.Type.MANUAL)
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

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5669
	 */
	@Test
	public void parameterTypes()
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
			assertThat(p.getAllNamedByType(INamedParameters.Type.PATH).size(), is(3));

			List<StringValue> pathName1Values = p.getValues("pathName1");
			assertThat(pathName1Values.size(), is(2));
			assertThat(pathName1Values.get(0).toString(), is("pathValue1"));
			assertThat(pathName1Values.get(1).toString(), is("pathValue1.1"));

			List<StringValue> pathName2Values = p.getValues("pathName2");
			assertThat(pathName2Values.size(), is(1));
			assertThat(pathName2Values.get(0).toString(), is("pathValue2"));
		}

		{
			// QUERY STRING
			assertThat(p.getAllNamedByType(INamedParameters.Type.QUERY_STRING).size(), is(3));

			List<StringValue> queryName1Values = p.getValues("queryName1");
			assertThat(queryName1Values.size(), is(2));
			assertThat(queryName1Values.get(0).toString(), is("queryValue1"));
			assertThat(queryName1Values.get(1).toString(), is("queryValue1.1"));

			List<StringValue> queryName2Values = p.getValues("queryName2");
			assertThat(queryName2Values.size(), is(1));
			assertThat(queryName2Values.get(0).toString(), is("queryValue2"));
		}

		{
			// MANUAL
			assertThat(p.getAllNamedByType(INamedParameters.Type.MANUAL).size(), is(3));

			List<StringValue> manualName1Values = p.getValues("manualName1");
			assertThat(manualName1Values.size(), is(2));
			assertThat(manualName1Values.get(0).toString(), is("manualValue1"));
			assertThat(manualName1Values.get(1).toString(), is("manualValue1.1"));

			List<StringValue> manualName2Values = p.getValues("manualName2");
			assertThat(manualName2Values.size(), is(1));
			assertThat(manualName2Values.get(0).toString(), is("manualValue2"));
		}
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5669
	 */
	@Test
	public void addWithoutTypeIsManual()
	{
		PageParameters p = new PageParameters();
		p.add("name", "value");
		assertThat(p.getAllNamed().get(0).getType(), is(INamedParameters.Type.MANUAL));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5669
	 */
	@Test
	public void setWithoutTypeIsManual()
	{
		PageParameters p = new PageParameters();
		p.set("name", "value");
		assertThat(p.getAllNamed().get(0).getType(), is(INamedParameters.Type.MANUAL));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5669
	 */
	@Test
	public void setWithIndexWithoutTypeIsManual()
	{
		PageParameters p = new PageParameters();
		p.set("name", "value", 3);
		assertThat(p.getAllNamed().get(0).getType(), is(INamedParameters.Type.MANUAL));
	}

	/**
	 * NamedPairs equality should not depend on the type
	 *
	 * https://issues.apache.org/jira/browse/WICKET-5669
	 */
	@Test
	public void equality()
	{
		PageParameters p1 = new PageParameters()
				.add("a", "b", INamedParameters.Type.MANUAL)
				.set("c", "d", INamedParameters.Type.MANUAL);

		PageParameters p2 = new PageParameters()
				.set("a", "b", INamedParameters.Type.QUERY_STRING)
				.add("c", "d", INamedParameters.Type.PATH);

		assertThat(p1, is(equalTo(p2)));
	}

	/**
	 * NamedPairs hashCode should not depend on the type
	 *
	 * https://issues.apache.org/jira/browse/WICKET-5669
	 */
	@Test
	public void hashcode()
	{
		PageParameters p1 = new PageParameters()
				.add("a", "b", INamedParameters.Type.MANUAL)
				.set("c", "d", INamedParameters.Type.MANUAL);

		PageParameters p2 = new PageParameters()
				.set("a", "b", INamedParameters.Type.QUERY_STRING)
				.add("c", "d", INamedParameters.Type.PATH);

		assertThat(p1.hashCode(), is(equalTo(p2.hashCode())));
	}
}
