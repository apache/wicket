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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * @author svenmeier
 */
class ButtonTest extends WicketTestCase
{

	/**
	 * WICKET-4734 Asserting that the value attribute on tag input is escaped once by default
	 */
	@Test
	void whenInputElement_thenModelObjectIsUsedForValueAttribute()
	{
		tester.getApplication().getMarkupSettings().setStripWicketTags(false);
		String text = "some text & another text";
		TestPage testPage = new TestPage(Model.of(text));
		tester.startPage(testPage);

		TagTester buttonTagTester = tester.getTagByWicketId("button");
		assertNotNull(buttonTagTester);
		assertEquals(text, buttonTagTester.getAttribute("value"));
		assertNull(buttonTagTester.getValue());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-6225
	 */
	@Test
	void whenButtonElement_thenModelObjectIsUsedAsTextContent()
	{
		tester.getApplication().getMarkupSettings().setStripWicketTags(false);
		String text = "some text & another text";
		TestPage testPage = new TestPage(Model.of(text)) {
			@Override
			public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
			{
				return new StringResourceStream("<html><body>"
						+ "<form wicket:id=\"form\"><button wicket:id=\"button\"></button></form></body></html>");
			}
		};
		tester.startPage(testPage);

		TagTester buttonTagTester = tester.getTagByWicketId("button");
		assertNotNull(buttonTagTester);
		assertNull(buttonTagTester.getAttribute("value"));
		assertEquals(text, buttonTagTester.getValue());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-6225
	 */
	@Test
	void whenButtonElementWithoutModelObject_thenUseTextContentFromHtml()
	{
		tester.getApplication().getMarkupSettings().setStripWicketTags(false);
		String text = "some text & another text";
		final String textInHtml = "Button label in HTML";
		TestPage testPage = new TestPage(Model.of("")) {
			@Override
			public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
			{
				return new StringResourceStream("<html><body>"
				                                + "<form wicket:id=\"form\"><button wicket:id=\"button\">"
				                                + textInHtml
				                                + "</button></form></body></html>");
			}
		};
		tester.startPage(testPage);

		TagTester buttonTagTester = tester.getTagByWicketId("button");
		assertNotNull(buttonTagTester);
		assertNull(buttonTagTester.getAttribute("value"));
		assertEquals(textInHtml, buttonTagTester.getValue());
	}

	/**
	 * WICKET-5235 button does not use an inherited model
	 */
	@Test
	void buttonDoesNotInheritModel()
	{
		TestPage testPage = new TestPage(null);
		tester.startPage(testPage);
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;
		Form<Object> form;
		Button button;

		/** */
		TestPage(IModel<String> buttonModel)
		{
			add(form = new Form<Object>("form", new CompoundPropertyModel<>(new Object())));
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
