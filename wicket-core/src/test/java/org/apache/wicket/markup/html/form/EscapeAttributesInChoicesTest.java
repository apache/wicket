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

import static org.apache.wicket.util.tester.TagTester.createTagByAttribute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Test XSS vulnerability in RadioChoice and CheckBoxMultipleChoice
 */
class EscapeAttributesInChoicesTest extends WicketTestCase
{
	@Test
	void escapeAttributes()
	{
		tester.startPage(EscapeAttributesInChoicesPage.class);
		String lastResponseAsString = tester.getLastResponseAsString();

		TagTester radioTagTester = tester.getTagById("radiofield1-apple\" onmouseover=\"alert('hi');\" \"");
		assertNotNull(radioTagTester);
		assertNull(radioTagTester.getAttribute("onmouseover"));

		TagTester dropDownChoiceOptionTagTester = createTagByAttribute(lastResponseAsString, "value", "apple\" onmouseover=\"alert('hi');\" \"");
		assertNotNull(dropDownChoiceOptionTagTester);
		assertNull(dropDownChoiceOptionTagTester.getAttribute("onmouseover"));

		TagTester checkBoxMultipleChoiceTagTester = createTagByAttribute(lastResponseAsString, "name", "checkboxfield");
		assertNotNull(checkBoxMultipleChoiceTagTester);
		assertEquals("apple\" onmouseover=\"alert('hi');\" \"", checkBoxMultipleChoiceTagTester.getAttribute("value"));
		assertNull(checkBoxMultipleChoiceTagTester.getAttribute("onmouseover"));

		TagTester labelForCheckBoxMultipleChoiceTagTester = createTagByAttribute(lastResponseAsString, "for", "checkboxfield2-checkboxfield_apple\" onmouseover=\"alert('hi');\" \"");
		assertNotNull(labelForCheckBoxMultipleChoiceTagTester);
		assertNull(labelForCheckBoxMultipleChoiceTagTester.getAttribute("onmouseover"));
	}
}
