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
package org.apache.wicket.extensions.markup.html.form.select;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test for {@link Select}.
 */
public class SelectTest extends WicketTestCase
{

	/**
	 * WICKET-4276
	 * 
	 * @throws Exception
	 */
	@Test
	public void rawInputKeepsSelectionOnError() throws Exception
	{
		SelectTestPage page = new SelectTestPage();

		tester.startPage(page);

		tester.getRequest().setParameter("select", page.option1.getValue());

		tester.submitForm(page.form);

		// form has error ...
		assertTrue(page.form.hasError());

		// ... but option1 is selected anyway through rawInput
		assertTrue(page.select.isSelected(page.option1));

		tester.startPage(page);

		// ... even after re-render
		assertTrue(page.select.isSelected(page.option1));

		tester.getRequest().setParameter("select", page.option1.getValue());
		tester.getRequest().setParameter("text", "text is required");
		tester.submitForm(page.form);

		// ... until successful submit without rawInput
		assertFalse(page.select.hasRawInput());
		assertTrue(page.select.isSelected(page.option1));
	}

	/**
	 * WICKET-5011 custom equality
	 */
	@Test
	public void selectionWithouEquals()
	{

		SelectTestPage2 page = new SelectTestPage2();

		assertTrue(page.select.isSelected(page.option0));
	}
}
