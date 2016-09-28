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
package org.apache.wicket.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gerolf Seitz
 */
public class AjaxComponentVisibilityTest extends WicketTestCase
{

	private Component test1;
	private Component test2;
	private Component test3;

	/**
	 * 
	 */
	@Before
	public void before()
	{
		tester.startPage(new AjaxHeaderContributionPage());
		test1 = tester.getLastRenderedPage().get("test1");
		test2 = tester.getLastRenderedPage().get("test2");
		test3 = tester.getLastRenderedPage().get("test3");
	}

	/**
	 * 
	 */
	@Test
	public void componentsAddedToAjax()
	{
		test2.setVisible(false);
		test3.setVisible(false).setOutputMarkupPlaceholderTag(true);

		tester.clickLink("link");

		// test1 should be added without any problems
		assertFalse(tester.isComponentOnAjaxResponse(test1).wasFailed());
		// test2 is not in the ajax response because it's invisible
		assertTrue(tester.isComponentOnAjaxResponse(test2).wasFailed());
		// test3 is in the ajax response because it renders a placeholder tag
		assertFalse(tester.isComponentOnAjaxResponse(test3).wasFailed());
	}
}
