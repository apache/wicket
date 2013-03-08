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
package org.apache.wicket.markup.html.internal;

import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.basic.Label;


/**
 * Simple test using the WicketTester
 */
public class TogglePageTest extends WicketTestCase
{


	/**
	 * 
	 */
	public void testNoAjaxPage()
	{
		{
			// On
			FullReloadPage noAjaxPage = tester.startPage(FullReloadPage.class);
			assertVisible(noAjaxPage.getToggleable());
			tester.clickLink(noAjaxPage.getLink().getPageRelativePath());
		}
		{
			// Off
			FullReloadPage noAjaxPage = (FullReloadPage)tester.getLastRenderedPage();
			assertInvisible(noAjaxPage.getToggleable());
			tester.clickLink(noAjaxPage.getLink().getPageRelativePath());
		}
		{
			// On
			FullReloadPage noAjaxPage = (FullReloadPage)tester.getLastRenderedPage();
			assertVisible(noAjaxPage.getToggleable());
			tester.clickLink(noAjaxPage.getLink().getPageRelativePath());
		}
		{
			// Off
			FullReloadPage noAjaxPage = (FullReloadPage)tester.getLastRenderedPage();
			assertInvisible(noAjaxPage.getToggleable());
		}
	}

	/**
	 * 
	 */
	public void testTraditionalAjaxEnclosurePage()
	{
		{
			// On
			TraditionalEnclosureAjaxPage ajaxPage = tester.startPage(TraditionalEnclosureAjaxPage.class);
			assertVisible(ajaxPage.getToggleable());
			tester.clickLink(ajaxPage.getLink().getPageRelativePath(), true);
		}
		{
			// Off
			TraditionalEnclosureAjaxPage ajaxPage = (TraditionalEnclosureAjaxPage)tester.getLastRenderedPage();
			tester.assertComponentOnAjaxResponse(ajaxPage.getTraditionalAjaxVisibilityToggleRequiresPlaceholder());
			assertInvisible(ajaxPage.getToggleable());
			tester.clickLink(ajaxPage.getLink().getPageRelativePath(), true);
		}
		{
			// On
			TraditionalEnclosureAjaxPage ajaxPage = (TraditionalEnclosureAjaxPage)tester.getLastRenderedPage();
			tester.assertComponentOnAjaxResponse(ajaxPage.getTraditionalAjaxVisibilityToggleRequiresPlaceholder());
			assertVisible(ajaxPage.getToggleable());
			tester.clickLink(ajaxPage.getLink().getPageRelativePath(), true);
		}
		{
			// Off
			TraditionalEnclosureAjaxPage ajaxPage = (TraditionalEnclosureAjaxPage)tester.getLastRenderedPage();
			tester.assertComponentOnAjaxResponse(ajaxPage.getTraditionalAjaxVisibilityToggleRequiresPlaceholder());
			assertInvisible(ajaxPage.getToggleable());
		}
	}

	/**
	 * 
	 */
	public void testInlineEnclosureWithAdditionalAjaxTarget()
	{
		{
			// On
			InlineEnclosureWithAdditionalAjaxTargetPage ajaxPage = tester.startPage(InlineEnclosureWithAdditionalAjaxTargetPage.class);
			assertVisible(ajaxPage.getLabel1());
			assertVisible(ajaxPage.getLabel2());
			tester.clickLink(ajaxPage.getLink().getPageRelativePath(), true);
		}
		{
			// Off
			InlineEnclosureWithAdditionalAjaxTargetPage ajaxPage = (InlineEnclosureWithAdditionalAjaxTargetPage)tester.getLastRenderedPage();
			tester.assertComponentOnAjaxResponse(ajaxPage.getLabel2());
			assertInVisible(ajaxPage.getLabel1());
			assertInVisible(ajaxPage.getLabel2());
			tester.clickLink(ajaxPage.getLink().getPageRelativePath(), true);
		}
		{
			// On
			InlineEnclosureWithAdditionalAjaxTargetPage ajaxPage = (InlineEnclosureWithAdditionalAjaxTargetPage)tester.getLastRenderedPage();
			tester.assertComponentOnAjaxResponse(ajaxPage.getLabel1());
			tester.assertComponentOnAjaxResponse(ajaxPage.getLabel2());
			assertVisible(ajaxPage.getLabel1());
			assertVisible(ajaxPage.getLabel2());
			tester.clickLink(ajaxPage.getLink().getPageRelativePath(), true);
		}
		{
			// Off
			InlineEnclosureWithAdditionalAjaxTargetPage ajaxPage = (InlineEnclosureWithAdditionalAjaxTargetPage)tester.getLastRenderedPage();
			tester.assertComponentOnAjaxResponse(ajaxPage.getLabel2());
			assertInVisible(ajaxPage.getLabel1());
			assertInVisible(ajaxPage.getLabel2());
		}
	}

	/**
	 * 
	 */
	public void testInlineEnclosureAjaxPage()
	{
		String inlineEnclosureIdPrefix = "InlineEnclosure-";

		String inlineEnclosureHiddenPattern = "<tr id=\"" + inlineEnclosureIdPrefix +
			"0\" style=\"display:none\"></tr>";

		String inlineEnclosureVisiblePattern = "<tr bgcolor=\"red\" id=\"" +
			inlineEnclosureIdPrefix + "0\">";

		{
			// On
			InlineEnclosureAjaxPage ajaxPage = tester.startPage(InlineEnclosureAjaxPage.class);
			assertVisible(ajaxPage.getToggleable());
			tester.clickLink(ajaxPage.getLink().getPageRelativePath(), true);
		}
		{
			// Off
			InlineEnclosureAjaxPage ajaxPage = (InlineEnclosureAjaxPage)tester.getLastRenderedPage();
			tester.assertContains(inlineEnclosureHiddenPattern);
			assertInvisible(ajaxPage.getToggleable());
			tester.clickLink(ajaxPage.getLink().getPageRelativePath(), true);
		}
		{
			// On
			InlineEnclosureAjaxPage ajaxPage = (InlineEnclosureAjaxPage)tester.getLastRenderedPage();
			tester.assertContains(inlineEnclosureVisiblePattern);
			assertVisible(ajaxPage.getToggleable());
			tester.clickLink(ajaxPage.getLink().getPageRelativePath(), true);
		}
		{
			// Off
			InlineEnclosureAjaxPage ajaxPage = (InlineEnclosureAjaxPage)tester.getLastRenderedPage();
			tester.assertContains(inlineEnclosureHiddenPattern);
			assertInvisible(ajaxPage.getToggleable());
		}
	}

	private final String toggledText = "This button (and red border) should appear and disappear by pressing toggle";


	/**
	 * @param toggleable
	 */
	protected void assertInvisible(Component toggleable)
	{
		tester.assertInvisible(toggleable.getPageRelativePath());
		assertDoesNotContain(toggledText);
		assertDoesNotContain("Also this");
	}

	/**
	 * @param toggleable
	 */
	protected void assertVisible(Component toggleable)
	{
		tester.assertVisible(toggleable.getPageRelativePath());
		tester.assertContains(Pattern.quote(toggledText));
		tester.assertContains(Pattern.quote("Also this"));
	}


	/**
	 * @param label
	 */
	protected void assertVisible(Label label)
	{
		tester.assertVisible(label.getPageRelativePath());
		tester.assertContains(Pattern.quote(label.getInnermostModel().getObject().toString()));
	}

	/**
	 * @param label
	 */
	protected void assertInVisible(Label label)
	{
		tester.assertInvisible(label.getPageRelativePath());
	}

	/**
	 * @param string
	 */
	private void assertDoesNotContain(String string)
	{
		assertFalse("Should not contain: " + string,
			tester.getLastResponseAsString().contains(string));
	}


}