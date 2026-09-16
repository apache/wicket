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
package org.apache.wicket.examples.ajax.builtin;

import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests the number editor of {@link EditableLabelPage}.
 */
class EditableLabelPageTest extends WicketTestCase
{
	@Test
	void rejectsANumberThatIsNotAnInteger()
	{
		saveNumber("abc");

		tester.assertErrorMessages("The value of 'Number' is not a valid Integer.");
		tester.assertVisible("form:number:editor");
		tester.assertComponentOnAjaxResponse("form:feedback");
	}

	@Test
	void rejectsAnEmptyNumber()
	{
		saveNumber("");

		tester.assertErrorMessages("'Number' is required.");
		tester.assertVisible("form:number:editor");
	}

	@Test
	void acceptsAnInteger()
	{
		saveNumber("7");

		tester.assertNoErrorMessage();
		tester.assertInvisible("form:number:editor");
		tester.assertLabel("form:number:label", "0000000007");
	}

	private void saveNumber(String value)
	{
		tester.startPage(EditableLabelPage.class);
		tester.executeAjaxEvent("form:number:label", "click");

		FormComponent<?> editor = (FormComponent<?>)tester
			.getComponentFromLastRenderedPage("form:number:editor");
		tester.getRequest().setParameter(editor.getInputName(), value);
		tester.getRequest().setParameter("save", "true");
		tester.getRequest().setMethod(Form.METHOD_GET);
		tester.executeBehavior((AbstractAjaxBehavior)editor.getBehaviorById(0));
	}
}
