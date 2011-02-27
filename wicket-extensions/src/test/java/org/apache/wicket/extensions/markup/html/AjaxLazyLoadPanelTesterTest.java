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
package org.apache.wicket.extensions.markup.html;

import junit.framework.TestCase;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanelTester;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.DummyPanelPage;
import org.apache.wicket.util.tester.ITestPanelSource;
import org.apache.wicket.util.tester.WicketTester;

/**
 * 
 * Tests {@link AjaxLazyLoadPanelTester}
 * 
 * @author Antony Stubbs
 */

public class AjaxLazyLoadPanelTesterTest extends TestCase
{
	private MarkupContainer ajaxLazyLoadPanel;

	/**
	 * Test
	 */
	public void test()
	{
		WicketTester wt = new WicketTester();
		final Page dummyPanelPage = new DummyPanelPage(new ITestPanelSource()
		{
			private static final long serialVersionUID = 1L;

			public Panel getTestPanel(String panelId)
			{
				AjaxLazyLoadPanel ajaxLazyLoadPanel1 = new AjaxLazyLoadPanel(panelId)
				{
					private static final long serialVersionUID = 1L;

					@Override
					public Component getLazyLoadComponent(String markupId)
					{
						return new Label(markupId, "lazy panel test").setRenderBodyOnly(true);
					}
				};
				ajaxLazyLoadPanel = ajaxLazyLoadPanel1;
				return (Panel)ajaxLazyLoadPanel;
			}

		});
		wt.startPage(dummyPanelPage);
		wt.assertLabel(
			"panel:content",
			"<img alt=\"Loading...\" src=\"resources/org.apache.wicket.ajax.AbstractDefaultAjaxBehavior/indicator.gif\"/>");
		AjaxLazyLoadPanelTester.executeAjaxLazyLoadPanel(wt, dummyPanelPage);
		wt.debugComponentTrees();
		wt.assertLabel("panel:content", "lazy panel test");
		String doc = wt.getServletResponse().getDocument();
		assertNotNull(doc);
	}
}
