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

import junit.framework.TestCase;

import org.apache.velocity.app.Velocity;

import org.apache.wicket.util.tester.WicketTester;

/**
 * Tests for <code>VelocityPanel</code>
 * 
 * @see org.apache.wicket.velocity.markup.html.VelocityPanel
 */
public class VelocityPanelTest extends TestCase
{
	static
	{
		try
		{
			Velocity.init();
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not initialize the Velocity engine", e);
		}
	}

	/**
	 * Basic test
	 */
	public void testVelocityPanel()
	{
		WicketTester tester = new WicketTester();
		tester.startPage(VelocityPage.class);
		tester.assertContains(VelocityPage.TEST_STRING);
		tester.dumpPage();
	}

	/**
	 * Test with Wicket markup parsing
	 */
	public void testVelocityPanelWithMarkupParsing()
	{
		WicketTester tester = new WicketTester();
		tester.startPage(VelocityWithMarkupParsingPage.class);
		tester.assertLabel("velocityPanel:message", VelocityPage.TEST_STRING);
		tester.dumpPage();
	}
}
