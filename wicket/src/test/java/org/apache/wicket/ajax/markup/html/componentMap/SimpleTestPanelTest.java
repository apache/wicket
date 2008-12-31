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
package org.apache.wicket.ajax.markup.html.componentMap;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.util.diff.DiffUtil;


/**
 * Test for ajax handler.
 * 
 * @author Juergen Donnerstag
 */
public class SimpleTestPanelTest extends WicketTestCase
{
	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public SimpleTestPanelTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_2() throws Exception
	{
		tester.setupRequestAndResponse(true);
		executeTest(SimpleTestPage.class, "SimpleTestPageExpectedResult.html");

		Page page = tester.getLastRenderedPage();
		tester.executeBehavior(((SimpleTestPanel)page.get("testPanel")).getTimeBehavior());

		// Validate the document
		String document = tester.getServletResponse().getDocument();
		DiffUtil.validatePage(document, SimpleTestPage.class,
			"SimpleTestPageExpectedResult-1.html", true);
	}
}
