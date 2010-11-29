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

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanelTester;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.DummyPanelPage;
import org.apache.wicket.util.tester.TestPanelSource;

/**
 * 
 * Tests {@link AjaxLazyLoadPanelTester}
 * 
 * @author Antony Stubbs
 */

public class AjaxLazyLoadPanelTesterTest extends WicketTestCase
{
	/**
	 * Test
	 */
	public void test()
	{
		final Page dummyPanelPage = new DummyPanelPage(new TestPanelSource()
		{
			private static final long serialVersionUID = 1L;

			public Panel getTestPanel(String panelId)
			{
				return new AjaxLazyLoadPanel(panelId)
				{
					private static final long serialVersionUID = 1L;

					@Override
					public Component getLazyLoadComponent(String markupId)
					{
						return new Label(markupId, "lazy panel test").setRenderBodyOnly(true);
					}
				};
			}

		});
		tester.startPage(dummyPanelPage);
		tester.assertLabel(
			"panel:content",
			"<img alt=\"Loading...\" src=\"resource/org.apache.wicket.ajax.AbstractDefaultAjaxBehavior/indicator.gif\"/>");
		AjaxLazyLoadPanelTester.executeAjaxLazyLoadPanel(tester, dummyPanelPage);
		tester.debugComponentTrees();
		tester.assertLabel("panel:content", "lazy panel test");
		String doc = tester.getLastResponseAsString();
		assertNotNull(doc);
	}
}
