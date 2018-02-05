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
package org.apache.wicket.stateless;

import org.apache.wicket.Page;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class StatelessPageWithFeedbackTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			public Class<? extends Page> getHomePage()
			{
				return StatelessPageWithFeedback.class;
			}
		};
	}

	/**
	 * After submit, both feedbackmessages (from onInitialize and onSubmit) must be visible
	 */
	@Test
	@Ignore("WICKET-6529 is not fixed")
	public void wicket6529()
	{
		tester.startPage(StatelessPageWithFeedback.class);
		tester.assertFeedback("feedback", "error in onInitialize");
		tester.clickLink("form:submit");
		String response = tester.getLastResponseAsString();
		Assert.assertTrue("onInitialize", response.contains("error in onInitialize"));
		Assert.assertTrue("onSubmit", response.contains("error in onSubmit"));
	}
}
