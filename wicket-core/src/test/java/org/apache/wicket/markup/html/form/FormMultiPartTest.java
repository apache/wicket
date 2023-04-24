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
package org.apache.wicket.markup.html.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 */
class FormMultiPartTest extends WicketTestCase
{

	@Test
	void multipartHard()
	{
		MultiPartFormPage page = new MultiPartFormPage();

		page.form.setMultiPart(true);
		tester.startPage(page);

		assertEquals(0, page.asked);

		assertTrue(page.form.isMultiPart());
	}

	@Test
	void multipartHint()
	{
		MultiPartFormPage page = new MultiPartFormPage();

		page.multiPart = false;
		tester.startPage(page);
		assertEquals(1, page.asked);
		assertFalse(page.form.isMultiPart());

		page.multiPart = true;
		tester.newFormTester("form").submit(page.button1);
		assertEquals(2, page.asked);
		assertTrue(page.form.isMultiPart());

		page.multiPart = false;
		tester.newFormTester("form").submit(page.button1);
		assertEquals(3, page.asked);
		assertFalse(page.form.isMultiPart());
	}

	@Test
	void multipartHintAjax()
	{
		MultiPartFormPage page = new MultiPartFormPage();

		page.multiPart = false;
		tester.startPage(page);
		assertEquals(1, page.asked);
		assertFalse(page.form.isMultiPart());

		page.multiPart = true;
		tester.executeAjaxEvent(page.button1, "click");
		assertEquals(3, page.asked);
		assertTrue(page.form.isMultiPart());

		page.multiPart = false;
		tester.executeAjaxEvent(page.button1, "click");
		assertEquals(5, page.asked);
		assertFalse(page.form.isMultiPart());
	}
}
