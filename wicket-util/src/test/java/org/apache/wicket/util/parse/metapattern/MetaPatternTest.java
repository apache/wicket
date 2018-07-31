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
package org.apache.wicket.util.parse.metapattern;

import org.apache.wicket.util.parse.metapattern.parsers.CommaSeparatedVariableParser;
import org.apache.wicket.util.parse.metapattern.parsers.IntegerVariableAssignmentParser;
import org.apache.wicket.util.parse.metapattern.parsers.TagNameParser;
import org.apache.wicket.util.parse.metapattern.parsers.VariableAssignmentParser;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for this object
 * 
 * @author Jonathan Locke
 */
public final class MetaPatternTest
{
	/**
	 * 
	 */
	@Test
	public void simple()
	{
		// Parse "variable = <number>"
		final Group variable = new Group(MetaPattern.VARIABLE_NAME);
		final IntegerGroup value = new IntegerGroup(MetaPattern.INTEGER);
		final MetaPattern variableAssignment = new MetaPattern(variable,
			MetaPattern.OPTIONAL_WHITESPACE, MetaPattern.EQUALS, MetaPattern.OPTIONAL_WHITESPACE,
			value);

		final Matcher matcher = variableAssignment.matcher("foo = 9");
		assertTrue(matcher.matches());
		assertEquals("foo", matcher.group(1));
		assertEquals("9", matcher.group(2));
	}

	/**
	 * Test assignment of variables.
	 */
	@Test
	public void variableAssignmentParser()
	{
		VariableAssignmentParser parser = new VariableAssignmentParser("foo = 9");
		assertTrue(parser.matches());
		assertEquals("foo", parser.getKey());
		assertEquals("9", parser.getValue());

		parser = new VariableAssignmentParser("foo=9");
		assertTrue(parser.matches());
		assertEquals("foo", parser.getKey());
		assertEquals("9", parser.getValue());
	}

	/**
	 * Test assignment of integers.
	 */
	@Test
	public void integerVariableAssignmentParser()
	{
		IntegerVariableAssignmentParser parser = new IntegerVariableAssignmentParser("foo = 9");
		assertTrue(parser.matches());
		assertEquals("foo", parser.getVariable());
		assertEquals(9, parser.getIntValue());
		assertEquals(9, parser.getLongValue());

		parser = new IntegerVariableAssignmentParser("foo=9");
		assertTrue(parser.matches());
		assertEquals("foo", parser.getVariable());
		assertEquals(9, parser.getIntValue());
		assertEquals(9, parser.getLongValue());

		parser = new IntegerVariableAssignmentParser("foo=a");
		assertFalse(parser.matches());
	}

	/**
	 * Test parsing of comma separated variables.
	 */
	@Test
	public void commaSeparatedVariableParser()
	{
		CommaSeparatedVariableParser parser = new CommaSeparatedVariableParser("a,b,c");
		assertTrue(parser.matches());
		assertEquals(3, parser.getValues().size());
		assertEquals("a", parser.getValues().get(0));
		assertEquals("b", parser.getValues().get(1));
		assertEquals("c", parser.getValues().get(2));

		// no whitespaces will be removed
		parser = new CommaSeparatedVariableParser(" a ,b, c , d ");
		assertTrue(parser.matches());
		assertEquals(4, parser.getValues().size());
		assertEquals(" a ", parser.getValues().get(0));
		assertEquals("b", parser.getValues().get(1));
		assertEquals(" c ", parser.getValues().get(2));
		assertEquals(" d ", parser.getValues().get(3));

		// It'll care for "" and '' but it'll not remove them
		parser = new CommaSeparatedVariableParser("'a ',\" b\",'c,d'");
		assertTrue(parser.matches());
		assertEquals(3, parser.getValues().size());
		assertEquals("'a '", parser.getValues().get(0));
		assertEquals("\" b\"", parser.getValues().get(1));
		assertEquals("'c,d'", parser.getValues().get(2));

		// But no escapes. Because no separator is following the 2nd "'",
		// it'll stop parsing the string.
		parser = new CommaSeparatedVariableParser("'a\'b, c");
		assertTrue(parser.matches());
		assertEquals(1, parser.getValues().size());
		assertEquals("'a'", parser.getValues().get(0));

		parser = new CommaSeparatedVariableParser("a");
		assertTrue(parser.matches());
		assertEquals(1, parser.getValues().size());
		assertEquals("a", parser.getValues().get(0));

		// Empty elements are not supported
		parser = new CommaSeparatedVariableParser("a,,");
		assertTrue(parser.matches());
		assertEquals(1, parser.getValues().size());
		assertEquals("a", parser.getValues().get(0));
	}

	/**
	 * Test the tag parser.
	 */
	@Test
	public void tagParser()
	{
		String tag = "name";
		TagNameParser parser = new TagNameParser(tag);
		assertEquals(true, parser.matcher().matches());
		assertEquals("name", parser.getName());
		assertNull(parser.getNamespace());

		tag = "namespace:name";
		parser = new TagNameParser(tag);
		assertEquals(true, parser.matcher().matches());
		assertEquals("name", parser.getName());
		assertEquals("namespace", parser.getNamespace());

		tag = "namespace:";
		parser = new TagNameParser(tag);
		assertEquals(false, parser.matcher().matches());

		tag = ":names";
		parser = new TagNameParser(tag);
		assertEquals(false, parser.matcher().matches());
	}
}