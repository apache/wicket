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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;

public class TextFieldTest extends WicketTestCase
{
	public void testEmptyInputConvertedToNull()
	{
		TestPage testPage = new TestPage();
		testPage.textField.setType(String.class);
		testPage.textField.setConvertEmptyInputStringToNull(true);
		tester.startPage(testPage);
		FormTester formTester = tester.newFormTester(testPage.form.getId());
		formTester.setValue(testPage.textField.getId(), "");
		formTester.submit();
		assertEquals(null, testPage.textField.getDefaultModelObject());
	}

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		Form<Void> form;
		TextField<String> textField;

		public TestPage()
		{
			add(form = new Form<Void>("form"));
			form.add(textField = new TextField<String>("text", Model.of((String)null)));
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>"
				+ "<form wicket:id=\"form\"><input wicket:id=\"text\" /></form></body></html>");
		}
	}
}