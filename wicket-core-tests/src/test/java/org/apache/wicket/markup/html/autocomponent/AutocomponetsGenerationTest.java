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
package org.apache.wicket.markup.html.autocomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.wicket.Page;
import org.apache.wicket.core.util.string.ComponentRenderer;
import org.apache.wicket.markup.IMarkupCache;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class AutocomponetsGenerationTest extends WicketTestCase
{

	/*
	 * Test for https://issues.apache.org/jira/browse/WICKET-5904
	 * and for https://issues.apache.org/jira/browse/WICKET-5908
	 */
	@Test
	void autocomponetsNumberDoesntChange()
	{
		AutoComponentsPage autoComponentsPage = new AutoComponentsPage();
		tester.startPage(autoComponentsPage);

		int childrenNumber = tester.getLastRenderedPage().size();
		
		//clean markup cache and render the same page instance again
		IMarkupCache markupCache = tester.getApplication().getMarkupSettings().getMarkupFactory().getMarkupCache();
		
		markupCache.clear();
		tester.startPage(autoComponentsPage);
		
		//the number of child components must not have been changed
		assertEquals(childrenNumber, tester.getLastRenderedPage().size());
	}
	
	/*
	 * Test for https://issues.apache.org/jira/browse/WICKET-6116
	 */
	@Test
	void borderResolvesAutocomponents() throws Exception
	{
		AutoComponentsBorder border = new AutoComponentsBorder("id");
		
		ComponentRenderer.renderComponent(border);
		
		//we expect to have a body container and an autocomponent for <img> tag
		assertEquals(2, border.size());
		
		//let's render the same border again
		ComponentRenderer.renderComponent(border);
		
		//the number of child components must not have been changed
		assertEquals(2, border.size());
	}
	
	/*
	 * Test for https://issues.apache.org/jira/browse/WICKET-6256
	 */
	@Test
	void autoComponentsIdsGeneration() throws Exception
	{
		Page page = new UniqueIdTest();
		
		tester.startPage(page);
		
		//render page again. Autocomponents must have the same id
		tester.startPage(page);		
	}
}
