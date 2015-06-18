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
package org.apache.wicket.markup.html.form.feedback;

import org.apache.wicket.Page;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author jcompagner
 */
public class FormFeedbackTest extends WicketTestCase
{

	/**
	 * @throws Exception
	 */
	@Test
	public void formComponentFeedbackBorder() throws Exception
	{
		Page page = tester.startPage(FeedbackFormPage.class);
		tester.assertRenderedPage(FeedbackFormPage.class);
		tester.assertResultPage(getClass(), "FeedbackFormPage_result1.html");
		tester.executeListener(page.get("form"));
		tester.assertRenderedPage(FeedbackFormPage.class);
		tester.assertResultPage(getClass(), "FeedbackFormPage_result2.html");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void formComponentFeedbackIndicator() throws Exception
	{
		Page page = tester.startPage(FeedbackIndicatorFormPage.class);
		tester.assertRenderedPage(FeedbackIndicatorFormPage.class);
		tester.assertResultPage(getClass(), "FeedbackIndicatorFormPage_result1.html");
		tester.executeListener(page.get("form"));
		tester.assertRenderedPage(FeedbackIndicatorFormPage.class);
		tester.assertResultPage(getClass(), "FeedbackIndicatorFormPage_result2.html");
	}

}
