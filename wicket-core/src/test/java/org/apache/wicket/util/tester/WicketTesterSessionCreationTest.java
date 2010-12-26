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
package org.apache.wicket.util.tester;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

/**
 * WICKET-1215
 */
public class WicketTesterSessionCreationTest
{

	private static Locale EXPECTED = Locale.FRENCH;

	/**
	 * WicketTester recreates session after setting attributes on it
	 */
	@Test
	public void wicket1215()
	{
		WicketTester tester = new WicketTester();
		tester.getSession().setLocale(EXPECTED);
		tester.startPage(LocalePage.class);
		assertEquals(EXPECTED, tester.getSession().getLocale());
	}

	/***/
	public static class LocalePage extends DummyHomePage
	{
		/***/
		public LocalePage()
		{
			assertEquals(EXPECTED, getSession().getLocale());
		}
	}
}
