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
package org.apache.wicket.examples;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/**
 * WebTestCase for tests that need to run inside Jetty to test for example the wicket filter using
 * HttpUnit as the testing mechanism.
 * 
 * @author Juergen Donnerstag
 * @author Martijn Dashorst
 */
public abstract class WicketWebTestCase extends TestCase
{
	private static final Log logger = LogFactory.getLog(WicketWebTestCase.class);

	/** The base url used to connect the conversation to */
	private String baseUrl = "http://localhost:8098/";

	/** The web conversation that keeps track of our requests. */
	private WebConversation conversation;

	/**
	 * Suite method.
	 * 
	 * @param clazz
	 * @return Test suite
	 */
	public static Test suite(Class clazz)
	{
		// The javascript 'history' variable is not supported by
		// httpunit and we don't want httpunit to throw an
		// exception just because they can not handle it.
		HttpUnitOptions.setExceptionsThrownOnScriptError(false);

		System.setProperty("wicket.configuration", "deployment");

		TestSuite suite = new TestSuite();
		suite.addTestSuite(clazz);
		JettyTestCaseDecorator deco = new JettyTestCaseDecorator(suite);

		return deco;
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public WicketWebTestCase(String name)
	{
		super(name);
	}

	/**
	 * Constructor
	 */
	public WicketWebTestCase()
	{
	}

	/**
	 * @param base
	 */
	public void setBaseUrl(String base)
	{
		baseUrl = base;
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception
	{
		conversation = new WebConversation();
	}

	protected WebResponse beginAt(String part) throws Exception
	{
		return conversation.getResponse(baseUrl + part);
	}
}
