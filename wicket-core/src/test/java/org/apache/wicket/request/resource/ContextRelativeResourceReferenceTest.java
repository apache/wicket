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

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class ContextRelativeResourceReferenceTest
{
	private static WicketTester tester;
	
	static final String RESOURCE_NAME = "/foo/baar/myLibrary";
	static final String ALREADY_MINIFIED = RESOURCE_NAME + ".min.js";
	static final String TO_BE_MINIFIED = RESOURCE_NAME + ".js";
	static final String CUSTOM_SUFFIX = "compress";
	
	@BeforeClass
	static public void setUp()
	{
		MockApplication application = new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				getResourceSettings().setUseMinifiedResources(true);
			}
		};		
		
		tester = new WicketTester(application);
	}
	
	
	@Test
	public void testMinifyResource() throws Exception
	{
		ContextRelativeResourceReference resourceReference = new ContextRelativeResourceReference(TO_BE_MINIFIED);
		Assert.assertTrue(testResourceKey(resourceReference, ALREADY_MINIFIED));
	}

	@Test
	public void testDontMinifyResource() throws Exception
	{
		ContextRelativeResourceReference resourceReference = new ContextRelativeResourceReference(ALREADY_MINIFIED, false);
		Assert.assertTrue(testResourceKey(resourceReference, ALREADY_MINIFIED));
		
		resourceReference = new ContextRelativeResourceReference(TO_BE_MINIFIED, false);
		Assert.assertTrue(testResourceKey(resourceReference, TO_BE_MINIFIED));
		
	}
	
	@Test
	public void testCustomSuffix() throws Exception
	{
		ContextRelativeResourceReference resourceReference = new ContextRelativeResourceReference(TO_BE_MINIFIED, CUSTOM_SUFFIX);
		Assert.assertTrue(testResourceKey(resourceReference, RESOURCE_NAME + "." + CUSTOM_SUFFIX + ".js"));
	}

	private boolean testResourceKey(ContextRelativeResourceReference resourceReference, String expectedName)
	{
		ContextRelativeResource resource = resourceReference.getResource();
		String resourceKey = resource.getCacheKey().toString();
		
		return resourceKey.endsWith(expectedName);
	}
	
}
