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
package org.apache.wicket.ajax.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * provide some refactoring safety
 * 
 * @author mosmann
 */
class AjaxAttributeNameTest
{
	/**
	 * do not let json parameter names collide
	 */
	@Test
	void jsonNamesDoNotCollide()
	{
		Set<String> jsonNames = new HashSet<>();
		for (AjaxAttributeName name : AjaxAttributeName.values())
		{
			assertTrue(jsonNames.add(name.jsonName()), "Attribute: " + name);
		}
	}

	/**
	 * lets play safe - this will break if anyone changes order or/and content of ajax attribute
	 * names
	 */
	@Test
	void nobodyDidChangeAnyAjaxAttributeName()
	{
		StringBuilder sb = new StringBuilder();
		for (AjaxAttributeName name : AjaxAttributeName.values())
		{
			sb.append(name.jsonName());
			sb.append('|');
		}

		assertEquals(
			"tr|p|d|id|dt|wr|rt|pd|sp|ch|e|async|dep|ep|pre|coh|fh|sh|ah|bsh|bh|ih|dh|i|sc|mp|f|c|m|u|sel|sr|",
			sb.toString(), "all known json parameter names");
	}
}
