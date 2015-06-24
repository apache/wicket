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
package org.apache.wicket.extensions.ajax.markup.html.autocomplete;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test for {@link AutoCompleteBehavior}.
 */
public class AutoCompleteTest extends WicketTestCase
{

	/**
	 * WICKET-4998 + WICKET-4990
	 */
	@Test
	public void autoCompleteAjaxBeforeChangeAjax()
	{
		tester.startPage(new AutoCompletePage());

		String lastResponseAsString = tester.getLastResponseAsString();

		int autoCompleteEventHandler = lastResponseAsString.indexOf("new Wicket.AutoComplete");
		int eventHandler = lastResponseAsString.indexOf("Wicket.Ajax.ajax");

		assertTrue(autoCompleteEventHandler != -1);
		assertTrue(eventHandler != -1);

		// autocomplete setup comes before event handler
		assertTrue(autoCompleteEventHandler < eventHandler);

		tester.clickLink("link");

		int autoCompleteEventHandler2 = lastResponseAsString.indexOf("new Wicket.AutoComplete");
		int eventHandler2 = lastResponseAsString.indexOf("Wicket.Ajax.ajax");

		assertTrue(autoCompleteEventHandler2 != -1);
		assertTrue(eventHandler2 != -1);

		// autocomplete setup comes before event handler
		assertTrue(autoCompleteEventHandler2 < eventHandler2);
	}
}
