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

import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.coop.CoopConfiguration.CoopMode;
import org.junit.jupiter.api.Test;

import static org.apache.wicket.coop.CoopConfiguration.COOP_HEADER;

public class CoopRequestCycleListenerTest extends WicketTestCase
{

	@Test
	public void testCoopHeaderSameOrigin()
	{
		tester.getApplication().enableCoop(new CoopConfiguration.Builder()
			.withMode(CoopMode.SAME_ORIGIN).withExemptions("exempt").build());
		checkHeaders(CoopMode.SAME_ORIGIN);
	}

	@Test
	public void testCoopHeaderSameOriginAllowPopups()
	{
		tester.getApplication().enableCoop(new CoopConfiguration.Builder()
				.withMode(CoopMode.SAME_ORIGIN_ALLOW_POPUPS).withExemptions("exempt").build());
		checkHeaders(CoopMode.SAME_ORIGIN_ALLOW_POPUPS);
	}

	@Test
	public void testCoopHeaderUnsafeNone()
	{
		tester.getApplication().enableCoop(new CoopConfiguration.Builder()
				.withMode(CoopMode.UNSAFE_NONE).withExemptions("exempt").build());
		checkHeaders(CoopMode.UNSAFE_NONE);
	}

	@Test
	public void testCoopHeadersNotSetExemptedPath()
	{
		tester.getApplication().enableCoop(new CoopConfiguration.Builder()
			.withMode(CoopMode.SAME_ORIGIN).withExemptions("exempt").build());
		tester.executeUrl("exempt");
		String coopHeaderValue = tester.getLastResponse().getHeader(COOP_HEADER);

		if (coopHeaderValue != null)
		{
			throw new AssertionError("COOP header should be null on exempted path");
		}
	}

	private void checkHeaders( CoopMode mode)
	{
		tester.executeUrl("/");
		String coopHeaderValue = tester.getLastResponse().getHeader(COOP_HEADER);

		if (coopHeaderValue == null)
		{
			throw new AssertionError("COOP header should not be null");
		}

		if (!mode.keyword.equals(coopHeaderValue))
		{
			throw new AssertionError("Unexpected COOP header: " + coopHeaderValue);
		}
	}
}
