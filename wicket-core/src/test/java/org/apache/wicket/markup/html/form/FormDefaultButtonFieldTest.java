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

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.DefaultMarkupIdGenerator;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.core.util.string.CssUtils;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * {@link Form#appendDefaultButtonField()} writes its markup as a string, so it has to escape what it
 * puts into the attributes itself.
 */
public class FormDefaultButtonFieldTest extends WicketTestCase
{
	/**
	 * The browser decodes the onclick attribute before it runs the handler, so a character reference
	 * in the markup id would end the string literal the id is written into.
	 */
	@Test
	public void markupIdIsEscapedInTheOnclickAttribute()
	{
		tester.getApplication().getMarkupSettings().setMarkupIdGenerator(
			new DefaultMarkupIdGenerator()
			{
				@Override
				public String generateMarkupId(Component component, boolean createIfDoesNotExist)
				{
					// a custom generator that ignores the characters its contract allows
					return "button".equals(component.getId()) ? "b&#39x"
						: super.generateMarkupId(component, createIfDoesNotExist);
				}
			});

		tester.startPage(new TestPage());

		tester.assertContains("getElementById\\('b&amp;#39x'\\)");
	}

	/**
	 * The css class of the hidden div comes from the resource bundles.
	 */
	@Test
	public void cssClassIsEscaped()
	{
		final String key = CssUtils.key(Form.class, "hidden-fields");
		tester.getApplication().getResourceSettings().getStringResourceLoaders().add(0,
			new IStringResourceLoader()
			{
				@Override
				public String loadStringResource(Class<?> clazz, String k, Locale locale,
					String style, String variation)
				{
					return key.equals(k) ? "a\"b" : null;
				}

				@Override
				public String loadStringResource(Component component, String k, Locale locale,
					String style, String variation)
				{
					return loadStringResource((Class<?>)null, k, locale, style, variation);
				}
			});

		tester.startPage(new TestPage());

		tester.assertContains("class=\"a&quot;b\"");
	}

	/** a form with a default button */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/** */
		public TestPage()
		{
			Form<Void> form = new Form<Void>("form");
			add(form);

			Button button = new Button("button");
			form.add(button);
			form.setDefaultButton(button);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><form wicket:id=\"form\">" +
				"<input wicket:id=\"button\" type=\"submit\"/></form></body></html>");
		}
	}
}
