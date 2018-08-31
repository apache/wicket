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
package org.apache.wicket.ajax;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * @author jcompagner
 */
class AjaxHeaderContributionTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
    void ajaxHeaderContribution() throws Exception
	{
		tester.startPage(AjaxHeaderContributionPage.class);
		tester.assertResultPage(AjaxHeaderContributionPage.class,
			"AjaxHeaderContributionPage_expected.html");

		tester.executeAjaxEvent("link", "click");
		tester.assertResultPage(AjaxHeaderContributionPage.class,
			"AjaxHeaderContributionPage_ajax_expected.html");

	}

	/**
	 * @throws Exception
	 */
	@Test
    void doubleAjaxHeaderContribution() throws Exception
	{
		tester.startPage(AjaxHeaderContributionPage2.class);
		tester.assertResultPage(AjaxHeaderContributionPage2.class,
			"AjaxHeaderContributionPage2_expected.html");

		tester.executeAjaxEvent("link", "click");
		tester.assertResultPage(AjaxHeaderContributionPage2.class,
			"AjaxHeaderContributionPage2_ajax_expected.html");

	}
}
