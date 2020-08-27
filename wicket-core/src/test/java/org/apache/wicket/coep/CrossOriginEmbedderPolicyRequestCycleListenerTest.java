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
package org.apache.wicket.coep;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

import org.apache.wicket.coep.CrossOriginEmbedderPolicyConfiguration.CoepMode;

import static org.apache.wicket.coep.CrossOriginEmbedderPolicyRequestCycleListener.REQUIRE_CORP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CrossOriginEmbedderPolicyRequestCycleListenerTest extends WicketTestCase
{
	private CoepMode mode;
	private String exemptions;

	@Test
	void testEnforcingCoepHeadersSetCorrectly()
	{
		mode = CoepMode.ENFORCING;
		buildApp();
		checkHeaders(CoepMode.ENFORCING);
	}

	@Test
	void testReportingCoepHeadersSetCorrectly()
	{
		mode = CoepMode.REPORTING;
		buildApp();
		checkHeaders(CoepMode.REPORTING);
	}

	@Test
	void testCoepDisabled()
	{
		mode = CoepMode.DISABLED;
		buildApp();
		tester.executeUrl("exempt");
		String coepHeaderValue = tester.getLastResponse().getHeader(CoepMode.REPORTING.header);
		assertNull(coepHeaderValue, "COOP header should be null on DISABLED");
	}

	@Test
	void testCoepHeadersNotSetExemptedPath()
	{
		mode = CoepMode.DISABLED;
		exemptions = "exempt";
		buildApp();
		tester.executeUrl("exempt");
		String coepHeaderValue = tester.getLastResponse().getHeader(CoepMode.REPORTING.header);

		assertNull(coepHeaderValue, "COOP header should be null on exempted path");
	}

	private void checkHeaders(CoepMode mode)
	{
		tester.executeUrl("/");
		String coepHeaderValue = tester.getLastResponse().getHeader(mode.header);

		assertNotNull(coepHeaderValue, "COEP " + mode + " header should not be null");

		assertEquals(REQUIRE_CORP, coepHeaderValue, "Unexpected COEP header: " + coepHeaderValue);
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
				getSecuritySettings().setCrossOriginEmbedderPolicyConfiguration(mode, exemptions);
			}
		};
	}

	// overriding the commonBefore because we want to modify init behavior
	// contents of commonBefore moved to buildApp, called after the coepMode / exemption set in every test
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
