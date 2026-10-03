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
package org.apache.wicket.extensions.ajax.markup.html.form.upload;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests for {@link UploadProgressBar}.
 */
public class UploadProgressBarTest extends WicketTestCase
{
	/**
	 * The starting text from the resource bundle is written into a JavaScript string literal.
	 */
	@Test
	public void startingTextIsEscapedForJavaScript()
	{
		tester.getApplication().getResourceSettings().getStringResourceLoaders().add(0,
			new IStringResourceLoader()
			{
				@Override
				public String loadStringResource(Class<?> clazz, String key, Locale locale,
					String style, String variation)
				{
					return UploadProgressBar.RESOURCE_STARTING.equals(key) ? "l'\\envoi" : null;
				}

				@Override
				public String loadStringResource(Component component, String key, Locale locale,
					String style, String variation)
				{
					return loadStringResource((Class<?>)null, key, locale, style, variation);
				}
			});

		tester.startPage(new TestPage());

		String response = tester.getLastResponseAsString();
		assertTrue(response, response.contains("'l\\'\\\\envoi'"));
	}

	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TestPage()
		{
			Form<Void> form = new Form<Void>("form");
			add(form);
			form.add(new UploadProgressBar("bar", form));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><head></head><body>" +
				"<form wicket:id=\"form\"><div wicket:id=\"bar\"></div></form></body></html>");
		}
	}
}
