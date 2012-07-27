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

import org.junit.Assert;
import org.junit.Test;

/**
 * @since 1.5.8
 */
public class VariableAssignmentParserTest extends Assert
{
	/**
	 * Tests parsing of attribute names with and without namespaces
	 *
	 * https://issues.apache.org/jira/browse/WICKET-4679
	 */
	@Test
	public void parseAttributeName()
	{
		String tagName = "tagName";

		VariableAssignmentParser parser = new VariableAssignmentParser(tagName + " name='value'");

		parser.matcher().find(tagName.length());
		assertEquals("name", parser.getKey());
		assertEquals("'value'", parser.getValue());

		parser = new VariableAssignmentParser(tagName + " namespace:name='value'");

		parser.matcher().find(tagName.length());
		assertEquals("namespace:name", parser.getKey());
		assertEquals("'value'", parser.getValue());

		parser = new VariableAssignmentParser(tagName + " namespace:name:subname='value'");

		parser.matcher().find(tagName.length());
		assertEquals("namespace:name:subname", parser.getKey());
		assertEquals("'value'", parser.getValue());
	}
}
