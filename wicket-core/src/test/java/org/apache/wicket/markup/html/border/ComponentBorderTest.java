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
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author jcompagner
 */
public class ComponentBorderTest extends WicketTestCase
{
	/**
	 * Tests component use check does not fail when border starts out hidden
	 * 
	 * @throws Exception
	 */
	@Test
	public void componentUseCheck() throws Exception
	{
		/*
		 * Suppose:
		 * 
		 * <div wicket:id="border"><div wicket:id="label"></div> suppose border->label and border's
		 * body is hidden.
		 * 
		 * The label is added to border not to its hidden body so as far as wicket is concerned
		 * label is visible in hierarchy, but when rendering label wont be rendered because in the
		 * markup it is inside the border's hidden body. Thus component use check will fail even
		 * though it shouldnt - make sure it doesnt.
		 */
		tester.getApplication().getDebugSettings().setComponentUseCheck(true);

		HideableBorderPage page = new HideableBorderPage();
		// start with border body hidden
		page.getBorder().setHidden(true);

		tester.startPage(page);
		tester.assertRenderedPage(HideableBorderPage.class);
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void hideableBorder() throws Exception
	{
		executeTest(HideableBorderPage.class, "HideableBorderPage_ExpectedResult.html");

		Page page = tester.getLastRenderedPage();
		Border border = (Border)page.get("hideable");
		assertNotNull(border);
		AjaxLink<?> link = (AjaxLink<?>)border.get("hideLink");
		assertNotNull(link);
		WebMarkupContainer wrapper = (WebMarkupContainer)border.get("wrapper");
		assertNotNull(wrapper);
		tester.clickLink("hideable:hideLink");
		String ajaxResponse = tester.getLastResponseAsString();
		tester.assertComponentOnAjaxResponse(wrapper);
		tester.clickLink("hideable:hideLink");
		ajaxResponse = tester.getLastResponseAsString();
		tester.assertComponentOnAjaxResponse(wrapper);
	}
}
