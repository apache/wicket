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
package org.apache.wicket.markup.html;

import org.junit.Assert;

import org.apache.wicket.Component;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.junit.Test;

/**
 * Unit test to ensure that calling {@link Component#getMarkupId()} still works as advertised in
 * Wicket 1.4.
 */
public class ComponentMarkupIdTest extends WicketTestCase
{
	/** */
	@Test
	public void idGeneratedWhenComponentNotAddedToPage()
	{
		// In wicket 1.4 the following sequence would not cause an exception
		Label label = new Label("bla", "Hello, World!");

		// however in 1.5 the following statement generated a MarkupNotFoundException
		String markupId = label.getMarkupId();

		// instead 1.4 would just generate the missing markup identifier
		Assert.assertEquals("bla1", markupId);
	}

	/** */
	@Test
	public void idGeneratedWhenPanelNotAddedToPage()
	{
		// In wicket 1.4 the following sequence would not cause an exception
		Panel panel = new EmptyPanel("bla");

		// however in 1.5 the following statement generated a WicketRuntimeException
		// that the markup type could not be determined
		String markupId = panel.getMarkupId();

		// instead 1.4 would just generate the missing markup identifier
		Assert.assertEquals("bla1", markupId);
	}

	/**
	 * This tests the expected behavior where the DOM id for the component is retrieved from the
	 * markup file, when the component has been added to the page.
	 */
	@Test
	public void idFromMarkupRetrievedWhenPanelAddedToPage()
	{
		ComponentMarkupIdTestPage page = new ComponentMarkupIdTestPage();
		tester.startPage(page);
		tester.assertRenderedPage(ComponentMarkupIdTestPage.class);

		Assert.assertEquals("markupPanel", page.markupPanel.getMarkupId());
	}

	/**
	 * This tests the expected behavior where the DOM id for the component is retrieved from the
	 * markup file, when the component has been added to the page.
	 */
	@Test
	public void idFromMarkupRetrievedWhenLabelAddedToPage()
	{
		ComponentMarkupIdTestPage page = new ComponentMarkupIdTestPage();
		tester.startPage(page);
		tester.assertRenderedPage(ComponentMarkupIdTestPage.class);

		Assert.assertEquals("markupLabel", page.markupLabel.getMarkupId());
	}

	/**
	 * Tests that a generated ID is kept, even if an identifier in the markup was set.
	 */
	@Test
	public void generatedIdOverridesIdFromMarkupWhenLabelAddedToPage()
	{
		ComponentMarkupIdTestPage page = new ComponentMarkupIdTestPage();
		tester.startPage(page);
		tester.assertRenderedPage(ComponentMarkupIdTestPage.class);

		Assert.assertEquals("generatedLabel1", page.generatedLabelMarkupId);
		Assert.assertEquals("generatedLabel1", page.generatedLabel.getMarkupId());
	}

	/**
	 * Tests that a generated ID is kept, even if an identifier in the markup was set.
	 */
	@Test
	public void generatedIdOverridesIdFromMarkup()
	{
		ComponentMarkupIdTestPage page = new ComponentMarkupIdTestPage();
		tester.startPage(page);
		tester.assertRenderedPage(ComponentMarkupIdTestPage.class);

		Assert.assertEquals("generatedPanel2", page.generatedPanelMarkupId);
		Assert.assertEquals("generatedPanel2", page.generatedPanel.getMarkupId());
	}

	/**
	 * Tests that a ID set from Java code using {@link Component#setOutputMarkupId(boolean)} is
	 * kept, even if an identifier in the markup was set.
	 */
	@Test
	public void fixedIdFromJavaForLabelOverridesIdFromMarkup()
	{
		ComponentMarkupIdTestPage page = new ComponentMarkupIdTestPage();
		tester.startPage(page);
		tester.assertRenderedPage(ComponentMarkupIdTestPage.class);

		Assert.assertEquals("javaLabel", page.fixedLabel.getMarkupId());
	}

	/**
	 * Tests that a ID set from Java code using {@link Component#setOutputMarkupId(boolean)} is
	 * kept, even if an identifier in the markup was set.
	 */
	@Test
	public void fixedIdFromJavaForPanelOverridesIdFromMarkup()
	{
		ComponentMarkupIdTestPage page = new ComponentMarkupIdTestPage();
		tester.startPage(page);
		tester.assertRenderedPage(ComponentMarkupIdTestPage.class);

		Assert.assertEquals("javaPanel", page.fixedPanel.getMarkupId());
	}
}
