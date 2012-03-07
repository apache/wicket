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

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import org.junit.After;
import org.junit.Before;

/**
 * WebTestCase for tests that need to run inside Jetty to test for example the wicket filter using
 * HttpUnit as the testing mechanism.
 * 
 * @author Juergen Donnerstag
 * @author Martijn Dashorst
 */
public abstract class WicketWebTestCase extends JettyTestCaseDecorator
{
	/** The base url used to connect the conversation to */
	private String baseUrl = "http://localhost:8098/";

	/** The web conversation that keeps track of our requests. */
	private WebConversation conversation;

	/**
	 * @param base
	 */
	public void setBaseUrl(String base)
	{
		baseUrl = base;
	}

	/**
	 * @throws Exception
	 */
	@Override
	@Before
	public void before() throws Exception
	{
		super.before();
		conversation = new WebConversation();
	}

	@Override
	@After
	public void after() throws Exception
	{
		baseUrl = null;
		super.after();
	}

	protected WebResponse beginAt(String part) throws Exception
	{
		return conversation.getResponse(baseUrl + part);
	}
}
