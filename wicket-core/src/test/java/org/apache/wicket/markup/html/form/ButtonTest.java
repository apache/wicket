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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;
import org.junit.Test;

/**
 * @author svenmeier
 */
public class ButtonTest extends WicketTestCase
{

	/**
	 * WICKET-4734 Asserting that the value attribute on tag input is escaped once by default
	 */
	@Test
	public void valueAttribute()
	{
		TestPage testPage = new TestPage();
		String text = "some text & another text";
		testPage.buttonModel.setObject(text);
		tester.startPage(testPage);
		assertTrue(tester.getLastResponseAsString().contains(Strings.escapeMarkup(text)));
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;
		Form<Void> form;
		Button button;
		IModel<String> buttonModel = Model.of((String)null);

		/** */
		public TestPage()
		{
			add(form = new Form<Void>("form"));
			form.add(button = new Button("button", buttonModel));
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>"
				+ "<form wicket:id=\"form\"><input wicket:id=\"button\" /></form></body></html>");
		}
	}
}