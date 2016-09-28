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
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.HTML5Attributes;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 *
 */
public class TextAreaTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-5289
	 */
	@Test
	public void requiredAttribute()
	{
		TestPage testPage = new TestPage();
		testPage.textArea.setOutputMarkupId(true);
		testPage.textArea.setType(String.class);
		testPage.textArea.setRequired(true);
		testPage.textArea.add(new HTML5Attributes());
		tester.startPage(testPage);

		TagTester tagTester = tester.getTagById(testPage.textArea.getMarkupId());
		String required = tagTester.getAttribute("required");
		assertEquals("required", required);
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;
		Form<Void> form;
		TextArea<String> textArea;
		IModel<String> textModel = Model.of((String)null);

		/** */
		public TestPage()
		{
			add(form = new Form<>("form"));
			form.add(textArea = new TextArea<>("textarea", textModel));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body>"
					+ "<form wicket:id=\"form\"><textarea wicket:id=\"textarea\"></textarea></form></body></html>");
		}
	}
}
