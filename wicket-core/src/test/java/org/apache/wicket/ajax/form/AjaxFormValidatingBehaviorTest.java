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

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author shuraa
 */
public class AjaxFormValidatingBehaviorTest extends WicketTestCase
{

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5999
	 */
	@Test
	public void ajaxUpdateOrdinaryFeedbackComponents() throws Exception
	{
		tester.startPage(AjaxFormValidatingBehaviorTestPage.class);
		tester.assertRenderedPage(AjaxFormValidatingBehaviorTestPage.class);
		tester.assertVisible("form1:feedback");
		tester.executeAjaxEvent("form1:input", "blur");
		tester.assertVisible("form1:feedback");
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5999
	 */
	@Test
	public void ajaxUpdateInitiallyInvisibleFeedbackComponents() throws Exception
	{
		tester.startPage(AjaxFormValidatingBehaviorTestPage.class);
		tester.assertRenderedPage(AjaxFormValidatingBehaviorTestPage.class);
		tester.assertInvisible("form2:feedback");
		tester.executeAjaxEvent("form2:input", "blur");
		tester.assertVisible("form2:feedback");
	}

}
