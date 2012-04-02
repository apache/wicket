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
package org.apache.wicket.ajax.form;

import java.util.Locale;

import org.apache.wicket.WicketTestCase;
import org.junit.Test;

/**
 * @author Janne Hietam&auml;ki (janne)
 */
public class OnChangeAjaxBehaviorTest extends WicketTestCase
{
	/**
	 * @throws Exception
	 */
	@Test
	public void rendering() throws Exception
	{
		tester.getSession().setLocale(Locale.ENGLISH);

		executeTest(OnChangeAjaxBehaviorTestPage.class,
			"OnChangeAjaxBehaviorTestPage_expected.html");
	}

	/**
	 * 
	 */
	@Test
	public void ajaxSubmitWhileAnotherButtonIsNotVisible()
	{
		// start and render the test page
		tester.startPage(HomePage.class);
		// assert rendered page class
		tester.assertRenderedPage(HomePage.class);
		// assert rendered label component
		tester.assertLabel("message",
			"If you see this message wicket is properly configured and running");

		tester.executeAjaxEvent("form:select", "inputchange change");

		// assert rendered page class
		tester.assertRenderedPage(ThirdPage.class);
		tester.assertLabel("label", "Hello world.");
		tester.assertContains("And just plain text");
	}
}
