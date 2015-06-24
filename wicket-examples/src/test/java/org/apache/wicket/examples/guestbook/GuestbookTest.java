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
package org.apache.wicket.examples.guestbook;

import java.util.ArrayList;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Before;
import org.junit.Test;


/**
 * jWebUnit test for Hello World.
 */
public class GuestbookTest extends WicketTestCase
{
	/**
	 * Sets up the test.
	 */
	@Before
	public void setUp()
	{
		GuestBook.clear();
	}

	/**
	 * Test page.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_1() throws Exception
	{
		tester.startPage(GuestBook.class);
		tester.assertContains("Wicket Examples - guestbook");

		// check if the list of comments is empty
		tester.assertListView("comments", new ArrayList<>());
		tester.assertComponent("commentForm", Form.class);
		FormTester formTester = tester.newFormTester("commentForm");
		formTester.setValue("text", "test-1");
		formTester.submit();

		tester.assertModelValue("comments:0:text", "test-1");

		formTester = tester.newFormTester("commentForm");
		formTester.setValue("text", "test-2");
		formTester.submit();
		tester.assertModelValue("comments:0:text", "test-2");
		tester.assertModelValue("comments:1:text", "test-1");

		formTester = tester.newFormTester("commentForm");
		formTester.setValue("text", "test-3");
		formTester.setValue("comment", "test-3");
		formTester.submit();
		tester.assertModelValue("comments:0:text", "test-2");
		tester.assertModelValue("comments:1:text", "test-1");
		tester.assertErrorMessages("Caught a spammer!!!");
	}
}
