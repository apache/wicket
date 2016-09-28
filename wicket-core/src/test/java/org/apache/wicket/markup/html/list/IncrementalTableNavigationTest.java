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
package org.apache.wicket.markup.html.list;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;


/**
 * Test for simple table behavior.
 */
public class IncrementalTableNavigationTest extends WicketTestCase
{
	/**
	 * Test simple table behavior.
	 * 
	 * @throws Exception
	 */
	@Test
	public void pagedTable() throws Exception
	{
		executeTest(IncrementalTableNavigationPage.class,
			"IncrementalTableNavigationPage_ExpectedResult_1.html");

		Page page = tester.getLastRenderedPage();
		Link<?> link = (Link<?>)page.get("nextNext");
		executeListener(link, "IncrementalTableNavigationPage_ExpectedResult_1-1.html");

		link = (Link<?>)page.get("prev");
		executeListener(link, "IncrementalTableNavigationPage_ExpectedResult_1-2.html");
	}
}
