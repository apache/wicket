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
package org.apache.wicket.examples.ajax.prototype;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * jWebUnit test for Hello World.
 */
public class AjaxTest extends WicketTestCase
{
	/**
	 * Test page.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_1() throws Exception
	{
		tester.getApplication().getMarkupSettings().setStripWicketTags(false);
		Index page = new Index();

		tester.startPage(page);
		tester.assertContains("Wicket Examples - Prototype.js / component render");
		assertEquals(0, page.get("counter").getDefaultModelObject());

		tester.startPage(page);
		tester.clickLink(tester.getComponentFromLastRenderedPage("link"));
		assertEquals(1, page.get("counter").getDefaultModelObject());
		tester.assertResultPage("<span wicket:id=\"counter\">1</span>");

		tester.startPage(page);
		tester.clickLink(tester.getComponentFromLastRenderedPage("link"));
		assertEquals(2, page.get("counter").getDefaultModelObject());
		tester.assertResultPage("<span wicket:id=\"counter\">2</span>");
	}
}
