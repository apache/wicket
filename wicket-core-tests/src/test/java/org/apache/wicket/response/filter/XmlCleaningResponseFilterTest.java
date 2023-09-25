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
package org.apache.wicket.response.filter;

import static org.apache.wicket.page.XmlPartialPageUpdate.END_ROOT_ELEMENT;
import static org.apache.wicket.page.XmlPartialPageUpdate.START_ROOT_ELEMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.util.string.AppendingStringBuffer;
import org.junit.jupiter.api.Test;



/**
 * Tests for XmlCleaningResponseFilter
 */
class XmlCleaningResponseFilterTest {

	/**
	 * Tests that invalid XML characters are removed
	 * @throws Exception
	 */
	@Test
	void filterInvalid() throws Exception
	{
		XmlCleaningResponseFilter filter = new XmlCleaningResponseFilter();
		int[] invalidChars = new int[] {0x0008, 0x0010, 0xD800, 0xDDDD, 0xFFFE};

		for (int invalidChar : invalidChars)
		{
			CharSequence text = createText(invalidChar);

			AppendingStringBuffer filtered = filter.filter(new AppendingStringBuffer(text));
			assertEquals(START_ROOT_ELEMENT+END_ROOT_ELEMENT, filtered.toString(), String.format("checking Unicode codepoint 0x%X:", invalidChar));
		}
	}

	/**
	 * Tests that invalid XML characters are removed
	 * @throws Exception
	 */
	@Test
	void filterMultipleInvalid() throws Exception
	{
		XmlCleaningResponseFilter filter = new XmlCleaningResponseFilter();
		CharSequence text = new StringBuilder()
			.append(START_ROOT_ELEMENT)
			.append(new String(new int[]{0x0008}, 0, 1))
			.append("a")
			.append(new String(new int[]{0x0010}, 0, 1))
			.append("b")
			.append(new String(new int[]{0xD800}, 0, 1))
			.append(END_ROOT_ELEMENT);

		AppendingStringBuffer filtered = filter.filter(new AppendingStringBuffer(text));
		assertEquals(START_ROOT_ELEMENT+"ab"+END_ROOT_ELEMENT, filtered.toString());
	}

	/**
	 * Tests that valid XML characters are preserved
	 * @throws Exception
	 */
	@Test
	void filterValid() throws Exception
	{
		XmlCleaningResponseFilter filter = new XmlCleaningResponseFilter();
		int[] validChars = new int[] {0x9, 0xA, 'a', 0xE000, 0xFFFC, 0x10400};

		for (int validChar : validChars)
		{
			CharSequence text = createText(validChar);

			AppendingStringBuffer filtered = filter.filter(new AppendingStringBuffer(text));
			assertEquals(text.toString(), filtered.toString(), String.format("checking Unicode codepoint 0x%X:", validChar));
		}
	}

	// using a int because a Java char cannot represent all Unicode characters; some require two chars.
	private CharSequence createText(int ch)
	{
		String character = new String(new int[] {ch}, 0, 1);
		return new StringBuilder()
				.append(START_ROOT_ELEMENT)
				.append(character)
				.append(END_ROOT_ELEMENT);
	}

	/**
	 * Asserts that XmlCleaningResponseFilter#shouldFilter() returns true when
	 * there is <ajax-response> in the text to filter
	 * @throws Exception
	 */
	@Test
	void shouldFilter() throws Exception
	{
		XmlFilter filter = new XmlFilter();

		assertFalse(filter.shouldFilter(new AppendingStringBuffer("anything")));

		assertTrue(filter.shouldFilter(
				new AppendingStringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"" +
						" standalone=\"yes\"><ajax-response></ajax-response>")));
	}

	/**
	 * Makes #shouldFilter() method public
	 */
	private static class XmlFilter extends XmlCleaningResponseFilter
	{
		@Override
		public boolean shouldFilter(AppendingStringBuffer responseBuffer) {
			return super.shouldFilter(responseBuffer);
		}
	}
}
