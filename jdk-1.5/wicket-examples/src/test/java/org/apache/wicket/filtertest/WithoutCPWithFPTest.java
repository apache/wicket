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

import org.apache.wicket.examples.JettyTestCaseDecorator;
import org.apache.wicket.examples.WicketWebTestCase;

import junit.framework.Test;

/**
 * jWebUnit test for Hello World.
 */
public class WithoutCPWithFPTest extends WicketWebTestCase
{

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	public void setUp() throws Exception
	{
		getTestContext().setBaseUrl("http://localhost:8098/");
	}
	/**
	 * 
	 * @return Test
	 */
	public static Test suite()
	{
		JettyTestCaseDecorator deco = (JettyTestCaseDecorator) suite(WithoutCPWithFPTest.class);
		deco.setContextPath("");
		String basedir = System.getProperty("basedir");
		String path = "";
		if (basedir != null)
			path = basedir + "/";
		path += "src/main/testwebapp1";
		deco.setWebappLocation(path);
		return deco;
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 *            name of test
	 */
	public WithoutCPWithFPTest(String name)
	{
		super(name);
	}

	/**
	 * Test page.
	 * 
	 * @throws Exception
	 */
	public void testHelloWorld() throws Exception
	{
		beginAt("/filtertest/hello/message/Test");
		dumpHtml();
		assertTitleEquals("Wicket Examples - helloworld");
		assertTextInElement("message", "Message is: 'Test'");
	}
	public void testWithSlash() throws Exception
	{
		beginAt("/filtertest/hello/message/Test%2FWith%20a%20Slash");
		dumpHtml();
		assertTitleEquals("Wicket Examples - helloworld");
		assertTextInElement("message", "Message is: 'Test/With a Slash'");
	}
}
