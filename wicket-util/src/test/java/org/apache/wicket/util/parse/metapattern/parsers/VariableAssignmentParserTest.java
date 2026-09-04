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
package org.apache.wicket.util.parse.metapattern.parsers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since 1.5.8
 */
class VariableAssignmentParserTest
{
	/**
	 * Tests parsing of attribute names with and without namespaces
	 *
	 * https://issues.apache.org/jira/browse/WICKET-4679
	 */
	@Test
	void parseAttributeName()
	{
		String tagName = "tagName";

		VariableAssignmentParser parser = new VariableAssignmentParser(tagName + " name='value'");

		assertTrue(parser.matcher().find(tagName.length()));
		assertEquals("name", parser.getKey());
		assertEquals("'value'", parser.getValue());

		parser = new VariableAssignmentParser(tagName + " namespace:name='value'");

		assertTrue(parser.matcher().find(tagName.length()));
		assertEquals("namespace:name", parser.getKey());
		assertEquals("'value'", parser.getValue());

		parser = new VariableAssignmentParser(tagName + " namespace:name:subname='value'");

		assertTrue(parser.matcher().find(tagName.length()));
		assertEquals("namespace:name:subname", parser.getKey());
		assertEquals("'value'", parser.getValue());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-6815
	 */
	@Test
	void testTagAttributeParsing()
	{
		String tag = "label wicket:id=\"myid\" bind:test=\"test\" v-bind:test2=\"test2\" :test3=\"test3\" @test4=\"test4\"";
		VariableAssignmentParser attributeParser = new VariableAssignmentParser(tag);
		int pos = 5;
		assertTrue(attributeParser.matcher().find(pos));
		String key = attributeParser.getKey();
		String value = attributeParser.getValue();
		pos = attributeParser.matcher().end(0);
		assertEquals("wicket:id", key);
		assertEquals("\"myid\"", value);

		assertTrue(attributeParser.matcher().find(pos));
		key = attributeParser.getKey();
		value = attributeParser.getValue();
		pos = attributeParser.matcher().end(0);
		assertEquals("bind:test", key);
		assertEquals("\"test\"", value);

		assertTrue(attributeParser.matcher().find(pos));
		key = attributeParser.getKey();
		value = attributeParser.getValue();
		pos = attributeParser.matcher().end(0);
		assertEquals("v-bind:test2", key);
		assertEquals("\"test2\"", value);

		assertTrue(attributeParser.matcher().find(pos));
		key = attributeParser.getKey();
		value = attributeParser.getValue();
		pos = attributeParser.matcher().end(0);
		assertEquals(":test3", key);
		assertEquals("\"test3\"", value);

		assertTrue(attributeParser.matcher().find(pos));
		key = attributeParser.getKey();
		value = attributeParser.getValue();
		assertEquals("@test4", key);
		assertEquals("\"test4\"", value);
	}
}
