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
package org.apache.wicket;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * @author oli
 */
class FeedbackMessagesTest extends WicketTestCase
{

	/**
	 * Test method for
	 * {@link org.apache.wicket.feedback.FeedbackMessages#hasMessageFor(org.apache.wicket.Component, int)}
	 * .
	 */
	@Test
	void hasMessageForComponentInt()
	{
		final Page page = new TestPage_1();
		tester.startPage(page);
		page.debug("debug message");
		page.info("info message");
		page.error("error message");
		assertTrue(tester.getLastRenderedPage().hasErrorMessage());
	}
}
