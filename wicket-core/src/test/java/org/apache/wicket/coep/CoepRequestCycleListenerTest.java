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

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

import org.apache.wicket.coep.CoepConfiguration.CoepMode;

import static org.apache.wicket.coep.CoepConfiguration.REQUIRE_CORP;

public class CoepRequestCycleListenerTest extends WicketTestCase
{
	@Test
	public void testEnforcingCoepHeadersSetCorrectly()
	{
		tester.getApplication().enableCoep(new CoepConfiguration.Builder()
			.withMode(CoepConfiguration.CoepMode.ENFORCING).withExemptions("exempt").build());
		checkHeaders(CoepConfiguration.CoepMode.ENFORCING);
	}

	@Test
	public void testReportingCoepHeadersSetCorrectly()
	{
		tester.getApplication().enableCoep(new CoepConfiguration.Builder()
			.withMode(CoepConfiguration.CoepMode.REPORTING).withExemptions("exempt").build());
		checkHeaders(CoepConfiguration.CoepMode.REPORTING);
	}

	@Test
	public void testCoepHeadersNotSetExemptedPath()
	{
		tester.getApplication().enableCoep(new CoepConfiguration.Builder()
			.withMode(CoepConfiguration.CoepMode.ENFORCING).withExemptions("exempt").build());
		tester.executeUrl("exempt");
		String coopHeaderValue = tester.getLastResponse()
			.getHeader(CoepConfiguration.CoepMode.ENFORCING.header);

		if (coopHeaderValue != null)
		{
			throw new AssertionError("COOP header should be null on exempted path");
		}
	}

	private void checkHeaders(CoepMode mode)
	{
		tester.executeUrl("/");
		String coepHeaderValue = tester.getLastResponse().getHeader(mode.header);

		if (coepHeaderValue == null)
		{
			throw new AssertionError("COEP" + mode + "header should not be null");
		}

		if (!REQUIRE_CORP.equals(coepHeaderValue))
		{
			throw new AssertionError("Unexpected COEP header: " + coepHeaderValue);
		}
	}
}
