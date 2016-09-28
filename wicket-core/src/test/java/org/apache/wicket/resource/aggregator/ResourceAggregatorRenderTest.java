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
package org.apache.wicket.resource.aggregator;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for the {@link org.apache.wicket.markup.head.ResourceAggregator} class.
 * 
 * @author Hielke Hoeve
 */
public class ResourceAggregatorRenderTest extends WicketTestCase
{
	/**
	 * tests a simple script, including jquery and wicket event js as deps
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPage1() throws Exception
	{
		tester.startPage(ResourceAggregatorTest1Page.class);
		tester.assertResultPage(ResourceAggregatorTest1Page.class,
			"ResourceAggregatorTest1Page_results.html");
	}

	/**
	 * continues on testPage1 to include a jquery plugin
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPage2() throws Exception
	{
		tester.startPage(ResourceAggregatorTest2Page.class);
		tester.assertResultPage(ResourceAggregatorTest2Page.class,
			"ResourceAggregatorTest2Page_results.html");
	}
}
