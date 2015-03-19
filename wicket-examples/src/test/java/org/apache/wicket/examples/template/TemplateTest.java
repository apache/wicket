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
package org.apache.wicket.examples.template;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for Template application
 */
public class TemplateTest extends Assert
{
	/**
	 * Test page.
	 */
	@Test
	public void test_1()
	{
		WicketTester tester = new WicketTester(new TemplateApplication());
		tester.startPage(tester.getApplication().getHomePage());
		String doc = tester.getLastResponse().getDocument();
		tester.assertContains("Wicket Examples - template");
		tester.assertContains("This example shows two different ways of building your page up from shared parts.");

		tester.startPage(org.apache.wicket.examples.template.pageinheritance.Page1.class);
		doc = tester.getLastResponse().getDocument();
		tester.assertContains("Template example, page 1 - page inheritance");
		tester.assertContains("This is some concrete content from a panel.");

		tester.startPage(org.apache.wicket.examples.template.border.Page1.class);
		tester.assertRenderedPage(org.apache.wicket.examples.template.border.Page1.class);
		doc = tester.getLastResponse().getDocument();
		tester.assertContains("Template example, page 1 - border");
		tester.assertContains("contents here");
		tester.destroy();
	}
}
