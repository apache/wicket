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
package org.apache.wicket.contrib.markup.html.velocity;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for <code>VelocityPanel</code>
 * 
 * @see org.apache.wicket.velocity.markup.html.VelocityPanel
 */
public class VelocityPanelTest
{
	WicketTester tester;

	/**
	 * 
	 */
	@BeforeEach
	public void before()
	{
		tester = new WicketTester();
	}

	/**
	 * 
	 */
	@AfterEach
	public void after()
	{
		tester.destroy();
	}

	/**
	 * Basic test
	 */
	@Test
	public void testVelocityPanel()
	{
		tester.startPage(VelocityPage.class);
		tester.assertContains(VelocityPage.TEST_STRING);
	}

	/**
	 * Test with Wicket markup parsing
	 */
	@Test
	public void testVelocityPanelWithMarkupParsing()
	{
		tester.startPage(VelocityWithMarkupParsingPage.class);
		tester.assertLabel("velocityPanel:message", VelocityPage.TEST_STRING);
	}
}
