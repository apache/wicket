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
package org.apache.wicket.feedback;

import org.apache.wicket.Component;
import org.apache.wicket.WicketTestCase;
import org.junit.Test;

/**
 * Test calling of {@link Component#beforeRender()} for {@link IFeedback} components.
 */
public class FeedbackRenderTest extends WicketTestCase
{

	/**
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception
	{
		final FeedbacksPage page = new FeedbacksPage();

		tester.startPage(page);

		// non-IFeedback first, then IFeedback from nested to top
		assertEquals("|id4|id3|id2|id1", page.onBeforeRenderOrder.toString());
	}
}
