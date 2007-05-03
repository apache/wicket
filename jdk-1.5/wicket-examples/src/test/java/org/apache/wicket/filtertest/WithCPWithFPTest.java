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

import junit.framework.Test;

import org.apache.wicket.examples.JettyTestCaseDecorator;


/**
 * jWebUnit test for Hello World.
 */
public class WithCPWithFPTest extends WithoutCPWithFPTest
{

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	public void setUp() throws Exception
	{
		getTestContext().setBaseUrl("http://localhost:8098/somecontext");
	}

	/**
	 * @param name
	 */
	public WithCPWithFPTest(String name)
	{
		super(name);
	}
	/**
	 * 
	 * @return Test
	 */
	public static Test suite()
	{
		JettyTestCaseDecorator deco = (JettyTestCaseDecorator) suite(WithCPWithFPTest.class);
		deco.setContextPath("/somecontext");
		String basedir = System.getProperty("basedir");
		String path = "";
		if (basedir != null)
			path = basedir + "/";
		path += "src/main/testwebapp1";
		deco.setWebappLocation(path);
		return deco;
	}
}
