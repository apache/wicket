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
package org.apache.wicket.model;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;

/**
 * Simple test for CompoundPropertyModel and id's like "A.B"
 */
public class TestHomePage extends WicketTestCase
{
	@Test
	public void testRenderMyPage()
	{
		// start and render the test page
		tester.startPage(HomePage.class);

		// assert rendered page class
		tester.assertRenderedPage(HomePage.class);

		FormTester formTester = tester.newFormTester("bookingForm");
		formTester.submit();

		tester.assertErrorMessages("Name is required");
	}
}
