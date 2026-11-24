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
package org.apache.wicket.examples.devicebound;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeviceBoundApplicationTest
{
	private WicketTester tester;

	@BeforeEach
	void setUp()
	{
		tester = new WicketTester(new DeviceBoundApplication());
	}

	@AfterEach
	void tearDown()
	{
		tester.destroy();
	}

	@Test
	void signInAsksTheBrowserToBindTheSession()
	{
		tester.startPage(DeviceBoundPage.class);
		tester.assertRenderedPage(DeviceBoundSignInPage.class);

		FormTester form = tester.newFormTester("signInPanel:signInForm");
		form.setValue("username", "wicket");
		form.setValue("password", "wicket");
		tester.setFollowRedirects(false);
		form.submit();

		String registration = tester.getLastResponse().getHeader("Secure-Session-Registration");
		assertTrue(registration.startsWith("(ES256 RS256);path="), registration);

		tester.setFollowRedirects(true);
		tester.startPage(DeviceBoundPage.class);
		tester.assertRenderedPage(DeviceBoundPage.class);
		tester.assertLabel("status:bound", "no");
		tester.clickLink("request");
		tester.assertLabel("status:requests", "1");
	}
}
