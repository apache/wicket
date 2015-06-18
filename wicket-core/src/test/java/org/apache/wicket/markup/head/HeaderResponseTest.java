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
package org.apache.wicket.markup.head;

import org.apache.wicket.markup.html.header.response.ConcretePage;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * This test verifies the ordering of header items.
 * 
 * @author papegaaij
 */
public class HeaderResponseTest extends WicketTestCase
{
	/**
	 * Renders items in child-first order and priority items and parent-first order. The expected
	 * order is:
	 * <ul>
	 * <li>children in depth first order, first rendering the wicket:head, then the header
	 * contributions</li>
	 * <li>next the head of the base page is rendered</li>
	 * <li>followed by the wicket:head of the concrete page</li>
	 * </ul>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAllMarkup() throws Exception
	{
		executeTest(ConcretePage.class, "ExpectedResult.html");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAllMarkupPageFirst() throws Exception
	{
		tester.getApplication()
			.getResourceSettings()
			.setHeaderItemComparator(new PriorityFirstComparator(true));
		executeTest(ConcretePage.class, "ExpectedResultPageFirst.html");
	}
}
