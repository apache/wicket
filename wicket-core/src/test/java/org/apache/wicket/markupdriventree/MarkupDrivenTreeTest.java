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
package org.apache.wicket.markupdriventree;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.internal.Enclosure;
import org.apache.wicket.markupdriventree.components.ComponentA;
import org.apache.wicket.markupdriventree.components.ComponentB;
import org.apache.wicket.markupdriventree.components.ComponentC;
import org.apache.wicket.markupdriventree.components.PanelA;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 *
 */
public class MarkupDrivenTreeTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		WebApplication application = new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				getPageSettings().setMarkupDrivenComponentTreeEnabled(true);
			}
		};
		return application;
	}

	@Test
	public void page1()
	{
		tester.startPage(Page1.class);

		tester.assertComponent("a", ComponentA.class);
		tester.assertComponent("a:b", ComponentB.class);
		tester.assertComponent("a:b:c", ComponentC.class);
	}

	@Test
	public void page2()
	{
		tester.startPage(Page2.class);

		tester.assertComponent("c", ComponentC.class);
		tester.assertComponent("c:b", ComponentB.class);
		tester.assertComponent("c:b:a", ComponentA.class);
	}

	@Test
	public void page3()
	{
		tester.startPage(Page3.class);

		tester.assertComponent("c", ComponentC.class);
		tester.assertComponent("b", ComponentB.class);
		tester.assertComponent("b:a", ComponentA.class);
	}

	@Test
	public void pageWithAutoPanel()
	{
		tester.startPage(PageWithAutoPanel.class);

		tester.assertComponent("c", ComponentC.class);
		tester.assertComponent("b", ComponentB.class);
		tester.assertComponent("b:a", ComponentA.class);

		tester.assertComponent("panelA", PanelA.class);
		tester.assertComponent("panelA:a", ComponentA.class);
	}

	@Test
	public void pageWithManuallyAddedPanel()
	{
		tester.startPage(PageWithManuallyAddedPanel.class);

		tester.assertComponent("c", ComponentC.class);
		tester.assertComponent("b", ComponentB.class);
		tester.assertComponent("b:a", ComponentA.class);

		tester.assertComponent("panelA", PanelA.class);
		tester.assertComponent("panelA:a", ComponentA.class);
	}

	@Test
	public void pageWithManuallyAddedPanelPlusEnclosure()
	{
		PageWithAutoPanelWithinEnclosure page = tester.startPage(PageWithAutoPanelWithinEnclosure.class);

		tester.assertComponent("c", ComponentC.class);
		tester.assertComponent("b", ComponentB.class);
		tester.assertComponent("b:a", ComponentA.class);

		Component panelA = page.panelA;
		MarkupContainer parent = panelA.getParent();
		assertThat("PanelA's parent must be the Enclosure", parent, Matchers.instanceOf(Enclosure.class));
	}
}
