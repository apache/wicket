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
package org.apache.wicket;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Test for calling {@link Component#setResponsePage(Class)} in constructor.
 */
class SetResponsePageTest extends WicketTestCase
{
	/** Fix setting response page in constructor. */
	@Test
    void setResponsePage()
	{
		tester.startPage(Page1.class);
		tester.assertRenderedPage(Page3.class);
	}

	/** first page, redirects to page 2. */
	public static class Page1 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/** Construct. */
        public Page1()
		{
			setResponsePage(Page2.class);
		}
	}

	/** second page, redirects to page 3. */
	public static class Page2 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/** Construct. */
		public Page2()
		{
			setResponsePage(new Page3());
		}
	}

	/** final target page. */
	public static class Page3 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/** Construct. */
        public Page3()
		{
		}
	}
}
