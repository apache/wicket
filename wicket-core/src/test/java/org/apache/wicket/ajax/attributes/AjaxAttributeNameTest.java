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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

/**
 * provide some refactoring safety
 * 
 * @author mosmann
 */
public class AjaxAttributeNameTest
{
	/**
	 * do not let json parameter names collide
	 */
	@Test
	public void jsonNamesDoNotCollide()
	{
		Set<String> jsonNames = new HashSet<String>();
		for (AjaxAttributeName name : AjaxAttributeName.values())
		{
			Assert.assertTrue("Attribute: " + name, jsonNames.add(name.jsonName()));
		}
	}

	/**
	 * lets play safe - this will break if anyone changes order or/and content of ajax attribute
	 * names
	 */
	@Test
	public void nobodyDidChangeAnyAjaxAttributeName()
	{
		StringBuilder sb = new StringBuilder();
		for (AjaxAttributeName name : AjaxAttributeName.values())
		{
			sb.append(name.jsonName());
			sb.append("|");
		}

		Assert.assertEquals("all known json parameter names",
			"tr|p|d|id|dt|wr|rt|ad|ch|e|async|dep|ep|pre|coh|fh|sh|ah|bsh|bh|i|sc|mp|f|c|m|u|",
			sb.toString());
	}
}
