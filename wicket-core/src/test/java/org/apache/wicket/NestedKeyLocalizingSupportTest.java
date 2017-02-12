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
package org.apache.wicket;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.DummyApplication;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for nested keys
 * 
 * @author Tobias Soloschenko
 *
 */
public class NestedKeyLocalizingSupportTest extends WicketTestCase
{

	private Localizer localizer;

	@Override
	protected WebApplication newApplication()
	{
		DummyApplication dummyApplication = new DummyApplication(){
			@Override
			protected void init()
			{
				super.init();
				localizer = this.getResourceSettings().getLocalizer();
				localizer.addLocalizationSupport(new NestedKeyLocalizationSupport());
			}
		};
		return dummyApplication;
	}

	/**
	 * Clears up the context
	 * 
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		tester.destroy();
	}

	/**
	 * Tests a nested key
	 */
	@Test
	public void testGetStringWithNestedKey()
	{
		Assert.assertEquals("Expected string should be returned",
			"This is a test with a nested string",
			localizer.getString("test.nested.string", null, null, "DEFAULT"));
	}

	/**
	 * Tests a recursive nested key
	 */
	@Test
	public void testGetStringWithRecursiveNestedKey()
	{
		Assert.assertEquals("Expected string should be returned",
			"Testing multi level nesting: This is a test with a nested string",
			localizer.getString("test.nested.nested.string", null, null, "DEFAULT"));
	}
}
