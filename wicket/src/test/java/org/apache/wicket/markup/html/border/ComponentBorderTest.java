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
package org.apache.wicket.markup.html.border;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * @author jcompagner
 */
public class ComponentBorderTest extends WicketTestCase
{
	/**
	 * Create the test.
	 * 
	 * @param name
	 * 		The test name
	 */
	public ComponentBorderTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testMarkupComponentBorder() throws Exception
	{
		executeTest(MarkupComponentBorderTestPage.class,
			"MarkupComponentBorderTestPage_ExpectedResult.html");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testHideableBorder() throws Exception
	{
		executeTest(HideableBorderPage.class, "HideableBorderPage_ExpectedResult.html");

		Page<?> page = tester.getLastRenderedPage();
		Border<?> border = (Border<?>)page.get("hideable");
		assertNotNull(border);
		AjaxLink<?> link = (AjaxLink<?>)border.get("hideLink");
		WebMarkupContainer<?> wrapper = (WebMarkupContainer<?>)border.get("wrapper");
		assertNotNull(link);
		tester.clickLink("hideable:hideLink");
		tester.assertComponentOnAjaxResponse(wrapper);
		tester.clickLink("hideable:hideLink");
		tester.assertComponentOnAjaxResponse(wrapper);
	}
}
