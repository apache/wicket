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
package org.apache.wicket.page;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebPage;
import org.junit.Test;

/**
 * Tests for Page#isPageStateless
 */
public class PageStatelessnessTest extends WicketTestCase
{
	/**
	 * Tests that a page is not stateless if it adds stateful components in #onInitialize()
	 * https://issues.apache.org/jira/browse/WICKET-5083
	 */
	@Test
	public void isPageStatelessWithOnInitialize()
	{
		Page page = new TestPage();
		assertFalse(page.isPageStateless());
	}

	public static class TestPage extends WebPage
	{

		public TestPage()
		{
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();

			setStatelessHint(false);
		}
	}
}
