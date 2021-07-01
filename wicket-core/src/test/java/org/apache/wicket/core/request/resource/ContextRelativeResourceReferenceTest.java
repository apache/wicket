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
package org.apache.wicket.core.request.resource;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.ContextRelativeResource;
import org.apache.wicket.request.resource.ContextRelativeResourceReference;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

class ContextRelativeResourceReferenceTest extends WicketTestCase
{
	private static final String RESOURCE_NAME = "/foo/baar/myLibrary";
	private static final String ALREADY_MINIFIED = RESOURCE_NAME + ".min.js";
	private static final String TO_BE_MINIFIED = RESOURCE_NAME + ".js";
	private static final String CUSTOM_SUFFIX = "compress";
	
	@Test
	void testMinifyResource() throws Exception
	{
		ContextRelativeResourceReference resourceReference = new ContextRelativeResourceReference(TO_BE_MINIFIED);
		assertTrue(testResourceKey(resourceReference, ALREADY_MINIFIED));
	}

	@Test
	void testDontMinifyResource() throws Exception
	{
		ContextRelativeResourceReference resourceReference = new ContextRelativeResourceReference(ALREADY_MINIFIED, false);
		assertTrue(testResourceKey(resourceReference, ALREADY_MINIFIED));
		
		resourceReference = new ContextRelativeResourceReference(TO_BE_MINIFIED, false);
		assertTrue(testResourceKey(resourceReference, TO_BE_MINIFIED));
		
	}
	
	@Test
	void testCustomSuffix() throws Exception
	{
		ContextRelativeResourceReference resourceReference = new ContextRelativeResourceReference(TO_BE_MINIFIED, CUSTOM_SUFFIX);
		assertTrue(testResourceKey(resourceReference, RESOURCE_NAME + "." + CUSTOM_SUFFIX + ".js"));
	}
	
	@Override
	protected WicketTester newWicketTester(WebApplication app)
	{
		WicketTester tester = super.newWicketTester(app);
		app.getResourceSettings().setUseMinifiedResources(true);
	
		return tester;
	}
	
	private boolean testResourceKey(ContextRelativeResourceReference resourceReference, String expectedName)
	{
		ContextRelativeResource resource = resourceReference.getResource();
		String resourceKey = resource.getCacheKey().toString();
		
		return resourceKey.endsWith(expectedName);
	}
	
}
