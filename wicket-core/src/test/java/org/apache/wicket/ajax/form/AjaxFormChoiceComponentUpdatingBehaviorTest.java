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
package org.apache.wicket.ajax.form;

import org.apache.wicket.WicketTestCase;
import org.junit.Test;

public class AjaxFormChoiceComponentUpdatingBehaviorTest extends WicketTestCase
{

	/**
	 * WICKET-5230 nested FormComponent with error message makes group invalid
	 */
	@Test
	public void nestedInvalidFormComponent()
	{
		ChoiceComponentPage page = tester.startPage(ChoiceComponentPage.class);

		tester.submitForm(page.form);

		assertTrue(page.text.hasErrorMessage());

		tester.getRequest().setParameter("form.group", page.radioFalse.getValue());
		tester.executeAjaxEvent(page.group, "click");

		// group is invalid because nested text has error message
		assertFalse(page.group.isValid());
		// .. so model object stays unchanged
		assertEquals(Boolean.TRUE, page.group.getModelObject());
	}
}
