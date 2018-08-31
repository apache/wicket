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
package org.apache.wicket.request.cycle;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.request.cycle.RerenderPage.Supplier;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Test cases for re-rendering pages.
 */
class RerenderPageTest extends WicketTestCase
{
	/**
	 * A testcase for WICKET-5960.
	 * 
	 * Due to the changes in WICKET-5309, a page is re-rendered when any of the URL segments is
	 * modified during the request. The re-render causes the {@code <head>} section to be empty
	 * because it was already rendered in the first try.
	 */
	@Test
	void wicket5960()
	{
		// mount the page so we have URL segments
		tester.getApplication().mount(new MountedMapper("/rerender/${value}", RerenderPage.class));

		// start the page with a value of 1
		PageParameters pars = new PageParameters();
		pars.add("value", 1);

		// render the page
		RerenderPage page = tester.startPage(RerenderPage.class, pars);
		tester.assertRenderedPage(RerenderPage.class);
		tester.assertContains("<!-- I should be present 1 -->");

		// add a supplier to modify the URL during render
		page.setNewValueHandler(new Supplier<Integer>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Integer get()
			{
				return 2;
			}
		});

		// rerender the page
		tester.startPage(page);
		tester.assertRenderedPage(RerenderPage.class);

		// due to the mentioned issue, no headers are rendered at all.
		tester.assertContains("<!-- I should be present 2 -->");
	}

	/**
	 * Another test case for WICKET-5960.
	 * 
	 * When an AJAX update was performed, the next normal request would still find the page left
	 * with the PartialHtmlHeaderContainer causing an empty {@code 
	 * <head>} section to be rendered. This test case walks Wicket through this scenario.
	 */
	@Test
	void nonAjaxRequestAfterAjaxUpdatedComponentShouldHaveHtmlHeadSection()
	{
		// perform a normal render of the page
		tester.startPage(RerenderAjaxPage.class);
		tester.assertRenderedPage(RerenderAjaxPage.class);

		MockHttpServletResponse firstResponseBeforeAjaxUpdate = tester.getLastResponse();

		// call an ajax event that updates a component
		tester.executeAjaxEvent("form:username", "blur");
		tester.assertComponentOnAjaxResponse("feedback");

		// perform a normal render of the page (in this case submitting the form which triggers a
		// feedback error
		tester.submitForm("form");

		// record the response for later reference
		MockHttpServletResponse normalResponseAfterAjaxUpdate = tester.getLastResponse();

		// submit the form again to ascertain if the HTML head section was restored upon the second
		// render
		tester.submitForm("form");

		// record the response for later reference
		MockHttpServletResponse secondNormalResponse = tester.getLastResponse();

		// assert that the first response indeed got the correct <head> section
		assertThat(firstResponseBeforeAjaxUpdate.getDocument(),
			containsString(RerenderAjaxPage.HEAD_TEXT));

		// assert that the second normal response after the AJAX update indeed got the correct
		// <head> section (this worked while the bug was still present)
		assertThat(secondNormalResponse.getDocument(), containsString(RerenderAjaxPage.HEAD_TEXT));

		// assert that the first normal response after the AJAX update indeed got the correct
		// <head> section (this failed while the bug was still present)
		assertThat(normalResponseAfterAjaxUpdate.getDocument(),
			containsString(RerenderAjaxPage.HEAD_TEXT));
	}
}
