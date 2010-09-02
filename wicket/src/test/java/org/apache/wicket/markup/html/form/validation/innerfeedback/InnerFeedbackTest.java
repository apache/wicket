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
package org.apache.wicket.markup.html.form.validation.innerfeedback;

import junit.framework.TestCase;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;

/**
 * Test for Wicket-2974
 */
public class InnerFeedbackTest extends TestCase
{

	private WicketTester tester;

	@Override
	protected void setUp() throws Exception
	{
		tester = new WicketTester(new WebApplication()
		{
			@Override
			protected void init()
			{
				super.init();

				// we don't care about FormComponentFeedbackBorder's 'errorindicator'
				getDebugSettings().setComponentUseCheck(false);
			}

			/**
			 * @see org.apache.wicket.Application#getHomePage()
			 */
			@Override
			public Class<HomePage> getHomePage()
			{
				return HomePage.class;
			}
		});
	}

	public void testInnerFeedback() throws Exception
	{
		tester.startPage(HomePage.class);

		// page's feedback
		tester.assertInfoMessages(new String[] { "info on field", "page onbeforerender" });

		FormTester formTester = tester.newFormTester("form");
		formTester.submit();

		// feedback message for LocalizedFeedbackBorder
		// without the fix the same error message was reported for the page's feedback panel too
		tester.assertErrorMessages(new String[] { "Field 'field' is required." });

		// page's feedback
		tester.assertInfoMessages(new String[] { "page onbeforerender" });

		formTester = tester.newFormTester("form");
		formTester.setValue("fieldborder:field", "some text");
		formTester.submit();

		tester.assertErrorMessages(new String[0]);

		// page's feedback
		tester.assertInfoMessages(new String[] { "form submitted", "page onbeforerender" });
	}
}
