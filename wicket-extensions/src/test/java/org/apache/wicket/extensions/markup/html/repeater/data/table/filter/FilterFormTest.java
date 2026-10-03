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
package org.apache.wicket.extensions.markup.html.repeater.data.table.filter;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.core.util.string.CssUtils;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

public class FilterFormTest extends WicketTestCase
{

	@Test
	public void test() throws Exception {
		executeTest(FilterFormTestPage.class, "FilterFormTestPage_expected.html");
	}

	/**
	 * The css class from the resource bundle is written into the markup of the hidden fields.
	 */
	@Test
	public void cssClassIsEscapedAsMarkup()
	{
		tester.getApplication().getResourceSettings().getStringResourceLoaders().add(0,
			new IStringResourceLoader()
			{
				@Override
				public String loadStringResource(Class<?> clazz, String key, Locale locale,
					String style, String variation)
				{
					return CssUtils.key(Form.class, "hidden-fields").equals(key) ? "a'b" : null;
				}

				@Override
				public String loadStringResource(Component component, String key, Locale locale,
					String style, String variation)
				{
					return loadStringResource((Class<?>)null, key, locale, style, variation);
				}
			});

		tester.startPage(FilterFormTestPage.class);

		String response = tester.getLastResponseAsString();
		assertTrue(response, response.contains("class='a&#039;b'"));
	}
}
