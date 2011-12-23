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
package org.apache.wicket.request.resource;

import java.util.Arrays;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.resource.bundles.ConcatBundleResource;
import org.junit.Test;

/**
 * Testcases for resource bundles
 * 
 * @author papegaaij
 */
public class ResouceBundleTest extends WicketTestCase
{
	/**
	 * Tests the concatenation of 2 javascript files
	 */
	@Test
	public void concatBundle()
	{
		ConcatBundleResource bundle = new ConcatBundleResource(Arrays.asList(
			JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
					ResouceBundleTest.class, "a.js")),
			JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				ResouceBundleTest.class, "b.js"))));

		tester.startResource(bundle);
		assertEquals("//a\n//b\n", tester.getLastResponseAsString());
	}
}
