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
package org.apache.wicket.ajax.markup.html.ajaxLink;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.util.tester.TagTester;
import org.junit.Test;


/**
 * 
 */
public class AjaxLinkTest extends WicketTestCase
{
	/**
	 * If the AjaxLink is attached to an "a" tag the href value should be replaced with "#" because
	 * we use the onclick to execute the javascript.
	 */
	@Test
	public void anchorGetsHrefReplaced()
	{
		tester.startPage(AjaxLinkPage.class);

		TagTester ajaxLink = tester.getTagByWicketId("ajaxLink");

		// It was a link to google in the markup, but should be replaced to "#"
		assertTrue(ajaxLink.getAttributeIs("href", "javascript:;"));
	}

	/**
	 * Tests setting the request target to a normal page request from an ajax request.
	 */
	@Test
	public void fromAjaxRequestToNormalPage()
	{
		tester.startPage(AjaxLinkPageToNormalPage.class);
		tester.assertRenderedPage(AjaxLinkPageToNormalPage.class);
		Page page = tester.getLastRenderedPage();
		Component ajaxLink = page.get("ajaxLink");
		AbstractAjaxBehavior behavior = (AbstractAjaxBehavior)ajaxLink.getBehaviors().get(0);
		tester.executeBehavior(behavior);
		tester.assertRenderedPage(NormalPage.class);
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void page_2() throws Exception
	{
		executeTest(AjaxPage2.class, "AjaxPage2_ExpectedResult.html");

		Page page = tester.getLastRenderedPage();
		Component ajaxLink = page.get("pageLayout:pageLayout_body:ajaxLink");
		AbstractAjaxBehavior behavior = (AbstractAjaxBehavior)ajaxLink.getBehaviors().get(0);

		executeBehavior(behavior, "AjaxPage2-1_ExpectedResult.html");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_1() throws Exception
	{
		executeTest(AjaxLinkPage.class, "AjaxLinkPageExpectedResult.html");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void renderHomePage_2() throws Exception
	{
		executeTest(AjaxLinkWithBorderPage.class, "AjaxLinkWithBorderPageExpectedResult.html");

		Page page = tester.getLastRenderedPage();
		Component ajaxLink = page.get("border:border_body:ajaxLink");
		AbstractAjaxBehavior behavior = (AbstractAjaxBehavior)ajaxLink.getBehaviors().get(0);

		executeBehavior(behavior, "AjaxLinkWithBorderPage-1ExpectedResult.html");
	}
}
