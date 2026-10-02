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
package org.apache.wicket;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Wicket writes a markup id into the JavaScript it generates without escaping it, and relies on the
 * check in {@link Component} that an id contains no character able to change the meaning of that
 * JavaScript. {@link ComponentIdCharactersTest} covers the check; this covers the other end, and
 * pins what those literals look like for an id using every character the check allows.
 */
class MarkupIdInGeneratedJavaScriptTest extends WicketTestCase
{
	/** every character a markup id may contain, beyond letters and digits */
	private static final String MARKUP_ID = "a-b_c.d1";

	@Test
	void theMarkupIdIsWrittenIntoItsLiteralsIntact()
	{
		tester.startPage(new TestPage());

		String response = tester.getLastResponseAsString();

		// the handler of the default submit button, bound to the container of its hidden field
		assertTrue(response.contains("Wicket.Event.add('" + MARKUP_ID + "_hf_1', 'click'"), response);

		// the form the script submits, from Form#getJsForListenerUrl
		assertTrue(response.contains("document.getElementById('" + MARKUP_ID + "')"), response);

		// the container of the hidden fields, whose id is derived from the markup id
		assertTrue(response.contains("document.getElementById('" + MARKUP_ID + "_hf_0')"), response);
	}

	/** a form whose markup gives it an id, with a default button and an updating behavior */
	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TestPage()
		{
			Form<Void> form = new Form<>("form")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String getMethod()
				{
					return METHOD_GET;
				}
			};
			add(form);

			Button button = new Button("button");
			form.add(button);
			form.setDefaultButton(button);

			TextField<String> field = new TextField<>("field", Model.of(""));
			field.add(new FormComponentUpdatingBehavior());
			form.add(field);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><head></head><body>" +
				"<form wicket:id=\"form\" id=\"" + MARKUP_ID + "\">" +
				"<input wicket:id=\"field\" type=\"text\"/>" +
				"<input wicket:id=\"button\" type=\"submit\"/>" +
				"</form></body></html>");
		}
	}
}
