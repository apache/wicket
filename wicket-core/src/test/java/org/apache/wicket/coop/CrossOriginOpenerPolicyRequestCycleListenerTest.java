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
package org.apache.wicket.coop;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.coop.CrossOriginOpenerPolicyConfiguration.CoopMode;
import org.junit.jupiter.api.Test;


import static org.apache.wicket.coop.CrossOriginOpenerPolicyRequestCycleListener.COOP_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CrossOriginOpenerPolicyRequestCycleListenerTest extends WicketTestCase
{
	private CoopMode mode;
	private String exemptions;

	@Test
	void testCoopHeaderSameOrigin()
	{
		mode = CoopMode.SAME_ORIGIN;
		buildApp();
		checkHeaders(CoopMode.SAME_ORIGIN);
	}

	@Test
	void testCoopHeaderSameOriginAllowPopups()
	{
		mode = CoopMode.SAME_ORIGIN_ALLOW_POPUPS;
		buildApp();
		checkHeaders(CoopMode.SAME_ORIGIN_ALLOW_POPUPS);
	}

	@Test
	void testCoopHeaderUnsafeNone()
	{
		mode = CoopMode.UNSAFE_NONE;
		buildApp();
		checkHeaders(CoopMode.UNSAFE_NONE);
	}

	@Test
	void testCoopDisabled()
	{
		mode = CoopMode.DISABLED;
		buildApp();
		tester.executeUrl("/");
		String coopHeaderValue = tester.getLastResponse().getHeader(COOP_HEADER);

		assertNull(coopHeaderValue, "COOP header should be null on DISABLED");
	}

	@Test
	void testCoopHeadersNotSetExemptedPath()
	{
		mode = CoopMode.DISABLED;
		exemptions = "exempt";
		buildApp();
		tester.executeUrl("exempt");
		String coopHeaderValue = tester.getLastResponse().getHeader(COOP_HEADER);

		assertNull(coopHeaderValue, "COOP header should be null on exempted path");
	}

	private void checkHeaders(CoopMode mode)
	{
		tester.executeUrl("/");
		String coopHeaderValue = tester.getLastResponse().getHeader(COOP_HEADER);

		assertNotNull(coopHeaderValue, "COOP header should not be null");

		assertEquals(mode.keyword, coopHeaderValue, "Unexpected COOP header: " + coopHeaderValue);
	}
  
	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				getSecuritySettings().setCrossOriginOpenerPolicyConfiguration(mode, exemptions);
			}
		};
	}

	// overriding the commonBefore because we want to modify init behavior
	// contents of commonBefore moved to buildApp, called after the coopMode/exemption set in every test
	@Override
	public void commonBefore()
	{
	}

	private void buildApp()
	{
		ThreadContext.detach();

		WebApplication application = newApplication();
		tester = newWicketTester(application);
	}
}
