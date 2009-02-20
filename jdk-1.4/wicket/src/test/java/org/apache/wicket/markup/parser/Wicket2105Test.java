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
package org.apache.wicket.markup.parser;

import junit.framework.TestCase;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;

/**
 * Simple test using the WicketTester
 */
public class Wicket2105Test extends TestCase
{
	private WicketTester tester;

	public void setUp()
	{
		tester = new WicketTester(new WicketApplication());
	}

	public void testRenderMyPage()
	{
		// start and render the test page
		tester.startPage(Wicket2105Page.class);

		// assert rendered page class
		tester.assertRenderedPage(Wicket2105Page.class);

		// assert rendered label component
		tester.assertLabel("version", "1.0");
	}

	private class WicketApplication extends WebApplication
	{
		protected void init()
		{
			super.init();

			getMarkupSettings().setStripComments(true);
		}

		public Class getHomePage()
		{
			return Wicket2105Page.class;
		}
	}
}
