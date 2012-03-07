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
package org.apache.wicket.filtertest;

import com.meterware.httpunit.WebResponse;
import org.apache.wicket.examples.WicketWebTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * jWebUnit test for Hello World.
 */
public class WithoutCPWithFPTest extends WicketWebTestCase
{
	/**
	 * @throws Exception
	 */
	@Override
	@Before
	public void before() throws Exception
	{
		if (getContextPath() == null)
		{
			setContextPath("");
		}
		if (getWebappLocation() == null)
		{
			String basedir = System.getProperty("basedir");
			String path = "";
			if (basedir != null)
				path = basedir + "/";
			path += "src/main/testwebapp1";
			setWebappLocation(path);
		}
		super.before();
	}

	/**
	 * Test page.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testHelloWorld() throws Exception
	{
		WebResponse response = beginAt("/filtertest/hello?message=Test");
		assertEquals("Wicket Examples - helloworld", response.getTitle());
		assertEquals("Message is: 'Test'", response.getElementWithID("message").getText());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testWithSlash() throws Exception
	{
		WebResponse response = beginAt("/filtertest/hello?message=Test%2FWith%20a%20Slash");
		assertEquals("Wicket Examples - helloworld", response.getTitle());
		assertEquals("Message is: 'Test/With a Slash'", response.getElementWithID("message")
			.getText());
	}
}
