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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.junit.Test;


/**
 * Simple test using the WicketTester
 * 
 * @author Joonas Hamalainen
 */
public class AjaxEnclosureTest extends WicketTestCase
{
	private final String inlineEnclosureIdPrefix = "wicket__InlineEnclosure_";
	private final String inlineEnclosureHiddenPattern = "<div id=\"" + inlineEnclosureIdPrefix +
		"\\w+\" style=\"display:none\"></div>";
	private final String inlineEnclosureVisiblePattern = "<div id=\"" + inlineEnclosureIdPrefix +
		"\\w+\">";

	@Override
	protected WebApplication newApplication()
	{
		WebApplication webApplication = new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				getMarkupSettings().setStripWicketTags(true);
			}
		};
		return webApplication;
	}

	/**
	 * Test toggling the controlling child inside the inline enclosure
	 */
	@Test
	public void ajaxTogglingControllingChildShouldToggleInlineEnclosure()
	{
		{
			// enclosure On
			AjaxEnclosurePage_1 ajaxPage = tester.startPage(AjaxEnclosurePage_1.class);
			assertVisible(ajaxPage.getLabel1(), true);
			assertVisible(ajaxPage.getLabel2(), true);
			String doc = tester.getLastResponseAsString();
			tester.clickLink(ajaxPage.getToggleLabel1Link().getPageRelativePath());
		}
		{
			// enclosure Off
			String doc = tester.getLastResponseAsString();
			AjaxEnclosurePage_1 ajaxPage = (AjaxEnclosurePage_1)tester.getLastRenderedPage();
			tester.assertContains(inlineEnclosureHiddenPattern);
			assertInvisible(ajaxPage.getLabel1());
			assertInvisible(ajaxPage.getLabel2());
			tester.clickLink(ajaxPage.getToggleLabel1Link().getPageRelativePath());
		}
		{
			// enclosure On
			String doc = tester.getLastResponseAsString();
			AjaxEnclosurePage_1 ajaxPage = (AjaxEnclosurePage_1)tester.getLastRenderedPage();
			tester.assertContains(inlineEnclosureVisiblePattern);
			assertVisible(ajaxPage.getLabel1(), true);
			assertVisible(ajaxPage.getLabel2(), true);
			tester.clickLink(ajaxPage.getToggleLabel1Link().getPageRelativePath());
		}
		{
			// enclosure Off
			String doc = tester.getLastResponseAsString();
			AjaxEnclosurePage_1 ajaxPage = (AjaxEnclosurePage_1)tester.getLastRenderedPage();
			tester.assertContains(inlineEnclosureHiddenPattern);
			assertInvisible(ajaxPage.getLabel1());
			assertInvisible(ajaxPage.getLabel2());
			tester.clickLink(ajaxPage.getToggleLabel1Link().getPageRelativePath());
		}
	}

	/**
	 * Test toggling a non-controlling child inside the inline enclosure
	 */
	@Test
	public void ajaxTogglingNonControllingChildShouldNotToggleEnclosure()
	{
		{
			// label 2 On
			AjaxEnclosurePage_1 ajaxPage = tester.startPage(AjaxEnclosurePage_1.class);
			ensureEnclosureIsVisible(ajaxPage, new AtomicInteger(1));
			assertVisible(ajaxPage.getLabel1(), true);
			assertVisible(ajaxPage.getLabel2(), true);
			tester.clickLink(ajaxPage.getToggleLabel2Link().getPageRelativePath());
		}
		{
			// label 2 Off
			AjaxEnclosurePage_1 ajaxPage = (AjaxEnclosurePage_1)tester.getLastRenderedPage();
			ensureEnclosureIsVisible(ajaxPage, new AtomicInteger(1));
			assertVisible(ajaxPage.getLabel1(), false);
			assertInvisible(ajaxPage.getLabel2());
			tester.clickLink(ajaxPage.getToggleLabel2Link().getPageRelativePath());
		}
		{
			// label 2 On
			AjaxEnclosurePage_1 ajaxPage = (AjaxEnclosurePage_1)tester.getLastRenderedPage();
			ensureEnclosureIsVisible(ajaxPage, new AtomicInteger(1));
			assertVisible(ajaxPage.getLabel1(), false);
			assertVisible(ajaxPage.getLabel2(), false);
			tester.clickLink(ajaxPage.getToggleLabel2Link().getPageRelativePath());
		}
		{
			// label 2 Off
			AjaxEnclosurePage_1 ajaxPage = (AjaxEnclosurePage_1)tester.getLastRenderedPage();
			ensureEnclosureIsVisible(ajaxPage, new AtomicInteger(1));
			assertVisible(ajaxPage.getLabel1(), false);
			assertInvisible(ajaxPage.getLabel2());
			tester.clickLink(ajaxPage.getToggleLabel2Link().getPageRelativePath());
		}
	}

	/**
	 * 
	 */
	@Test
	public void nestedInlineEnclosuresShouldToggleNormally()
	{
		{
			// 1. test that enclosure1, enclosure2, label1, label2 are visible, click link1,
			// hiding label1 and the whole enclosure
			AjaxEnclosurePage_2 ajaxPage = tester.startPage(AjaxEnclosurePage_2.class);
			ensureEnclosureIsVisible(ajaxPage, new AtomicInteger(1));
			assertVisible(ajaxPage.getEnclosure2Marker(), true);
			assertVisible(ajaxPage.getLabel1(), true);
			assertVisible(ajaxPage.getLabel2(), true);
			tester.clickLink(ajaxPage.getToggleLabel1Link().getPageRelativePath());
		}
		{
			// 2. test that enclosure1, enclosure2, label1, label2 are INvisible, click link 1,
			// bringing all back
			AjaxEnclosurePage_2 ajaxPage = (AjaxEnclosurePage_2)tester.getLastRenderedPage();
			ensureEnclosureIsInvisible(ajaxPage, new AtomicInteger(2));
			assertInvisible(ajaxPage.getEnclosure2Marker());
			assertInvisible(ajaxPage.getLabel1());
			assertInvisible(ajaxPage.getLabel2());
			tester.clickLink(ajaxPage.getToggleLabel1Link().getPageRelativePath());
		}
		{
			// 3. test that enclosure1, enclosure2, label1, label2 are visble, click link 2,
			// hiding label 2 and enclosure 2
			AjaxEnclosurePage_2 ajaxPage = (AjaxEnclosurePage_2)tester.getLastRenderedPage();
			ensureEnclosureIsVisible(ajaxPage, new AtomicInteger(1));
			// ensureEnclosureIsVisible(enclosure2Path, ajaxPage);
			assertVisible(ajaxPage.getEnclosure2Marker(), false);
			assertVisible(ajaxPage.getLabel1(), false);
			assertVisible(ajaxPage.getLabel2(), false);
			tester.clickLink(ajaxPage.getToggleLabel2Link().getPageRelativePath());
		}
		{
			// 4. test that enclosure1, label1 are visible and enclosure2, label2 INvisible.
			// click link 2 again
			AjaxEnclosurePage_2 ajaxPage = (AjaxEnclosurePage_2)tester.getLastRenderedPage();
			ensureEnclosureIsVisible(ajaxPage, new AtomicInteger(2));
			assertVisible(ajaxPage.getLabel1(), false);
			assertInvisible(ajaxPage.getEnclosure2Marker());
			assertInvisible(ajaxPage.getLabel2());
			tester.clickLink(ajaxPage.getToggleLabel2Link().getPageRelativePath());
		}
		{
			// 3. test that enclosure1, enclosure2, label1, label2 are visble, Click link 1,
			// hiding all
			AjaxEnclosurePage_2 ajaxPage = (AjaxEnclosurePage_2)tester.getLastRenderedPage();
			ensureEnclosureIsVisible(ajaxPage, new AtomicInteger(1));
			assertVisible(ajaxPage.getEnclosure2Marker(), false);
			assertVisible(ajaxPage.getLabel1(), false);
			assertVisible(ajaxPage.getLabel2(), false);
			tester.clickLink(ajaxPage.getToggleLabel1Link().getPageRelativePath());
		}
		{
			// 4. test that enclosure1, enclosure2 label1, label2 are invisible. click link 2
			AjaxEnclosurePage_2 ajaxPage = (AjaxEnclosurePage_2)tester.getLastRenderedPage();
			ensureEnclosureIsInvisible(ajaxPage, new AtomicInteger(2));
			assertInvisible(ajaxPage.getEnclosure2Marker());
			assertInvisible(ajaxPage.getLabel1());
			assertInvisible(ajaxPage.getLabel2());
			tester.clickLink(ajaxPage.getToggleLabel2Link().getPageRelativePath());
		}
		{
			// 5. test that enclosure1, enclosure2 label1, label2 are invisible. click link 1
			AjaxEnclosurePage_2 ajaxPage = (AjaxEnclosurePage_2)tester.getLastRenderedPage();
			ensureEnclosureIsInvisible(ajaxPage, new AtomicInteger(1));
			assertInvisible(ajaxPage.getEnclosure2Marker());
			assertInvisible(ajaxPage.getLabel1());
			assertInvisible(ajaxPage.getLabel2());
			tester.clickLink(ajaxPage.getToggleLabel1Link().getPageRelativePath());
		}
		{
			// 6. test that enclosure1, label1 are visible, and enclosure2, label2 invisible
			// (because of step 4)
			AjaxEnclosurePage_2 ajaxPage = (AjaxEnclosurePage_2)tester.getLastRenderedPage();
			ensureEnclosureIsVisible(ajaxPage, new AtomicInteger(2));
			assertInvisible(ajaxPage.getEnclosure2Marker());
			assertVisible(ajaxPage.getLabel1(), false);
			assertInvisible(ajaxPage.getLabel2());
			tester.clickLink(ajaxPage.getToggleLabel1Link().getPageRelativePath());
		}

	}

	/**
	 * 
	 */
	@Test
	public void controllingChildShouldDefaultToTheSingleComponentInsideEnclosure()
	{
		{
			// enclosure On
			AjaxEnclosurePage_3 ajaxPage = tester.startPage(AjaxEnclosurePage_3.class);
			assertVisible(ajaxPage.getLabel1(), true);
			ensureEnclosureIsVisible(ajaxPage, new AtomicInteger(1));
			tester.clickLink(ajaxPage.getToggleLabel1Link().getPageRelativePath());
		}
		{
			// enclosure Off
			AjaxEnclosurePage_3 ajaxPage = (AjaxEnclosurePage_3)tester.getLastRenderedPage();
			tester.assertContains(inlineEnclosureHiddenPattern);
			assertInvisible(ajaxPage.getLabel1());
			ensureEnclosureIsInvisible(ajaxPage, new AtomicInteger(1));
			tester.clickLink(ajaxPage.getToggleLabel1Link().getPageRelativePath());
		}
		{
			// enclosure On
			AjaxEnclosurePage_3 ajaxPage = (AjaxEnclosurePage_3)tester.getLastRenderedPage();
			tester.assertContains(inlineEnclosureVisiblePattern);
			assertVisible(ajaxPage.getLabel1(), true);
			ensureEnclosureIsVisible(ajaxPage, new AtomicInteger(1));
			tester.clickLink(ajaxPage.getToggleLabel1Link().getPageRelativePath());
		}
		{
			// enclosure Off
			AjaxEnclosurePage_3 ajaxPage = (AjaxEnclosurePage_3)tester.getLastRenderedPage();
			tester.assertContains(inlineEnclosureHiddenPattern);
			assertInvisible(ajaxPage.getLabel1());
			ensureEnclosureIsInvisible(ajaxPage, new AtomicInteger(1));
			tester.clickLink(ajaxPage.getToggleLabel1Link().getPageRelativePath());
		}
	}

	private void ensureEnclosureIsVisible(Page ajaxPage, AtomicInteger n)
	{
		InlineEnclosure enclosure = findNthComponent(InlineEnclosure.class, ajaxPage, n);
		assertTrue("Is not visible", enclosure.determineVisibility());
	}

	private void ensureEnclosureIsInvisible(Page ajaxPage, AtomicInteger n)
	{
		InlineEnclosure enclosure = findNthComponent(InlineEnclosure.class, ajaxPage, n);
		if (enclosure != null)
		{
			assertFalse("Is visible", enclosure.determineVisibility());
		}
	}

	private <T> T findNthComponent(final Class<T> type, MarkupContainer container, final AtomicInteger n)
	{
		// finds the Nth InlineEnclosure in the children
		Component instance = container.visitChildren(new IVisitor<Component, Component>()
		{
			@Override
			public void component(Component object, IVisit<Component> visit)
			{
				if (type.isInstance(object) && n.decrementAndGet() == 0)
				{
					visit.stop(object);
				}
			}
		});
		return type.cast(instance);
	}

	protected void assertVisible(Label label, boolean checkAlsoMarkup)
	{
		tester.assertVisible(label.getPageRelativePath());
		if (checkAlsoMarkup)
		{
			tester.assertContains(Pattern.quote(label.getInnermostModel().getObject().toString()));
		}
	}

	protected void assertInvisible(Label label)
	{
		// tester.assertInvisible(label.getPageRelativePath());
		assertDoesNotContain(Pattern.quote(label.getInnermostModel().getObject().toString()));
	}

	protected void assertDoesNotContain(String string)
	{
		assertFalse("Should not contain: " + string,
			tester.getLastResponseAsString().contains(string));
	}
}