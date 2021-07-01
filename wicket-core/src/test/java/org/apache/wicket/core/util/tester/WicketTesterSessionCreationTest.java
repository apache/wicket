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
package org.apache.wicket.core.util.tester;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * WICKET-1215
 */
class WicketTesterSessionCreationTest extends WicketTestCase
{

	private static Locale EXPECTED = Locale.FRENCH;

	/**
	 * WicketTester recreates session after setting attributes on it
	 */
	@Test
	void wicket1215()
	{
		tester.getSession().setLocale(EXPECTED);
		tester.startPage(LocalePage.class);
		assertEquals(EXPECTED, tester.getSession().getLocale());
	}

	/***/
	public static class LocalePage extends DummyHomePage
	{
		private static final long serialVersionUID = 1L;

		/***/
        public LocalePage()
		{
			assertEquals(EXPECTED, getSession().getLocale());
		}
	}
}
