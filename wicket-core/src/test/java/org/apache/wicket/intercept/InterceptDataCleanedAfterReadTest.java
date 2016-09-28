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
package org.apache.wicket.intercept;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-4066
 */
public class InterceptDataCleanedAfterReadTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new Wicket4066Application();
	}

	/**
	 * Tests that InterceptData is cleared after the first successful read.
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-4066
	 */
	@Test
	public void wicket4066()
	{
		// go to a secured page (a redirect to LoginPage with interception will be done)
		tester.startPage(SecurePage.class);
		tester.assertRenderedPage(LoginPage.class);

		FormTester formTester = tester.newFormTester("form");
		formTester.submit();
		// continueToOriginalDestination() should return us to SecurePage
		tester.assertRenderedPage(SecurePage.class);

		// go directly to LoginPage (without interception)
		tester.startPage(LoginPage.class);
		FormTester formTester2 = tester.newFormTester("form");
		formTester2.submit();
		// no original destination then go to SuccessPage
		tester.assertRenderedPage(SuccessPage.class);
	}

}
