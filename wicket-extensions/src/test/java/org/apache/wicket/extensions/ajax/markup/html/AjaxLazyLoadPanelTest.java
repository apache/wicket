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
package org.apache.wicket.extensions.ajax.markup.html;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test for {@link AjaxLazyLoadPanel}.
 * 
 * @author svenmeier
 */
public class AjaxLazyLoadPanelTest extends WicketTestCase
{

	@Test
	public void immediatelyContentReady() {
		
		AjaxLazyLoadPanelPage page = new AjaxLazyLoadPanelPage();
		page.contentReady = true;
		
		tester.startPage(page);
		
		// render the loading component at least once
		tester.assertContainsNot("LOADED");

		AjaxLazyLoadPanelTester.executeAjaxLazyLoadPanel(tester);
		
		tester.assertContains("LOADED");
	}
	
	@Test
	public void lateContentReady() {
		
		AjaxLazyLoadPanelPage page = new AjaxLazyLoadPanelPage();
		page.contentReady = false;
		
		tester.startPage(page);
		
		tester.assertContainsNot("LOADED");
		
		AjaxLazyLoadPanelTester.executeAjaxLazyLoadPanel(tester);

		tester.assertContainsNot("LOADED");

		page.contentReady = true;
		AjaxLazyLoadPanelTester.executeAjaxLazyLoadPanel(tester);
		
		tester.assertContains("LOADED");
	}
	
	@Test
	public void lateVisible() {
		
		AjaxLazyLoadPanelPage page = new AjaxLazyLoadPanelPage();
		page.contentReady = true;
		page.panel.setVisible(false);
		
		tester.startPage(page);
		
		// neither content nor timer since not visible 
		tester.assertContainsNot("LOADED");
		tester.assertContainsNot("Wicket.Timer.set\\(");

		tester.clickLink(page.link);
		
		// no content yet, but timer  
		tester.assertContainsNot("LOADED");
		tester.assertContains("Wicket.Timer.set\\(");

		AjaxLazyLoadPanelTester.executeAjaxLazyLoadPanel(tester);
		
		// content still, but no timer necessary anymore 
		tester.assertContains("LOADED");
		tester.assertContainsNot("Wicket.Timer.set\\(");
	}
}
