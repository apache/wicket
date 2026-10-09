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
 * {@link Form#writeHiddenFields()} writes its markup as a string, so it has to escape what it puts
 * into the attributes itself.
 */
public class FormHiddenFieldsTest extends WicketTestCase
{
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

	/** a form submitted with GET, which is when the hidden fields are written */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		/** */
		public TestPage()
		{
			add(new Form<Void>("form"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><form wicket:id=\"form\" method=\"get\"></form></body></html>");
		}
	}
}
